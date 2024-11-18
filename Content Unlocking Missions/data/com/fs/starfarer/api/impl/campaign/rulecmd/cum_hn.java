package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class cum_hn extends BaseCommandPlugin {
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected PersonAPI person;
	protected FactionAPI faction;
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		this.dialog = dialog;
		this.memoryMap = memoryMap;
		
		String command = params.get(0).getString(memoryMap);
		if (command == null) return false;
		
		memory = getEntityMemory(memoryMap);
		
		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();
		
		if (command.equals("selectOres")) {
			selectCores(params.get(1).getInt(memoryMap));
		} else if (command.equals("playerHasOres")) {
			return playerHasOres();
		} else if (command.equals("playerOreTimer")) {
			return playerTimer();
		}
		
		return true;
	}

	protected void selectCores(int mult) {
		CargoAPI copy = Global.getFactory().createCargo(false);
		//copy.addAll(cargo);
		//copy.setOrigSource(playerCargo);
                final float totalOres = 4000f*mult;
                final float totalrareOres = 1000f*mult;
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && (spec.getDemandClass().equals(Commodities.ORE) || spec.getDemandClass().equals(Commodities.RARE_ORE))) {
				copy.addFromStack(stack);
			}
		}
		copy.sort();
                if (copy.getQuantity(CargoAPI.CargoItemType.RESOURCES, Commodities.ORE) >= totalOres) {copy.removeCommodity(Commodities.ORE, copy.getQuantity(CargoAPI.CargoItemType.RESOURCES, Commodities.ORE)-totalOres);}
		if (copy.getQuantity(CargoAPI.CargoItemType.RESOURCES, Commodities.RARE_ORE) >= totalrareOres) {copy.removeCommodity(Commodities.RARE_ORE, copy.getQuantity(CargoAPI.CargoItemType.RESOURCES, Commodities.RARE_ORE)-totalrareOres);}
                
		final float width = 310f;
		dialog.showCargoPickerDialog("Select ores to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
			public void pickedCargo(CargoAPI cargo) {
				cargo.sort();
				for (CargoStackAPI stack : cargo.getStacksCopy()) {
					playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
					if (stack.isCommodityStack()) { // should be always, but just in case
						AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
					}
				}
                                
				float bounty = computeoreValue(cargo);
                                float bounty2 = computerareoreValue(cargo);
				float repChange = computeoreReputationValue(cargo);

				
                                
                                if (bounty2 > 0) {
					playerCargo.addCommodity(Commodities.RARE_METALS, bounty2);
                                        AddRemoveCommodity.addCommodityGainText(Commodities.RARE_METALS, (int)bounty2, text);
				}
                                
                                if (bounty > 0) {
					playerCargo.addCommodity(Commodities.METALS, bounty);
                                        AddRemoveCommodity.addCommodityGainText(Commodities.METALS, (int)bounty, text);
				}
				
				if (repChange >= 1f) {
					CustomRepImpact impact = new CustomRepImpact();
					impact.delta = repChange * 0.01f;
					Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.CUSTOM, impact,
												  null, text, true), 
												  person);
					
					impact.delta *= 0.5f;
					if (impact.delta >= 0.01f) {
						Global.getSector().adjustPlayerReputation(
								new RepActionEnvelope(RepActions.CUSTOM, impact,
													  null, text, true), 
													  faction.getId());
					}
				}
				person.getMemoryWithoutUpdate().set("$cum_hn_tradeagain", true, Math.max(2f, ((bounty*0.35f/totalOres)+(bounty2*0.65f/totalrareOres))*60f));
				FireBest.fire(null, dialog, memoryMap, "cum_oresTurnedIn");
			}
			public void cancelledCargoSelection() {
			}
			public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
			
				float bounty = computeoreValue(cargo);
                                float bounty2 = computerareoreValue(cargo);
				float repChange = computeoreReputationValue(combined);
                                
				float opad = 10f;

				panel.setParaFontOrbitron();
				panel.addPara("Ibrahim Interstellar Transport and Salvage", faction.getBaseUIColor(), 1f);
				panel.setParaFontDefault();
				
				panel.addImage(Global.getSettings().getSpriteName("characters", "ibrahim"), 128f, 128f, 3f);
				
				if (bounty > 0f || bounty2 > 0f) panel.addPara("Turning ores in to " + "will result in:", opad); else {panel.addPara("A convenient transaction for the discreet captains.", opad);}
				/*panel.beginGridFlipped(width, 1, 40f, 10f);
				//panel.beginGrid(150f, 1);
				panel.addToGrid(0, 0, "Metal per Ore", "1");
				panel.addToGrid(0, 1, "Transplutonics per Transplutonic Ore", "1");
				panel.addGrid(pad);*/
                                if (bounty > 0f) panel.addPara("- %s Metal", opad * 1f, Misc.getHighlightColor(), Misc.getWithDGS(bounty)+Strings.X);
                                if (bounty2 > 0f) panel.addPara("- %s Transplutonics", opad * 1f, Misc.getHighlightColor(), Misc.getWithDGS(bounty2)+Strings.X);
                                if (repChange >= 1f) panel.addPara(person.getName().getFullName() +"'s standing with you will improve by %s points.", opad * 1f, Misc.getHighlightColor(), Misc.getRoundedValue(repChange));
				if (repChange >= 2f) panel.addPara("The " + faction.getDisplayNameWithArticleWithoutArticle() + "'s standing with you will improve by %s points.", opad * 1f, Misc.getHighlightColor(), Misc.getRoundedValue(repChange/2f));
                                if (bounty > 0f || bounty2 > 0f) panel.addPara("This transaction cannot be done again for %s days.", opad * 1f, Misc.getHighlightColor(), Misc.getRoundedValueMaxOneAfterDecimal(Math.max(2f, (((bounty*0.35f/totalOres)+(bounty2*0.65f/totalrareOres))*60f))));
			}
		});
	}

	protected float computeoreValue(CargoAPI cargo) {
		float bounty = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.ORE)) {
				bounty += stack.getSize();
			}
		}
		return bounty;
	}
        
	protected float computerareoreValue(CargoAPI cargo) {
		float bounty = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.RARE_ORE)) {
				bounty += stack.getSize();
			}
		}
		return bounty;
	}
	
	protected float computeoreReputationValue(CargoAPI cargo) {
		float rep = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.ORE)) {
				rep += getBaseRepValue(spec.getId()) * stack.getSize();
			}
                        if (spec != null && spec.getDemandClass().equals(Commodities.RARE_ORE)) {
				rep += getBaseRepValue(spec.getId()) * stack.getSize();
			}
		}
		return rep;
	}
	
	public static float getBaseRepValue(String coreType) {
		if (Commodities.ORE.equals(coreType)) {
			return 0.001f;
		}
		if (Commodities.RARE_ORE.equals(coreType)) {
			return 0.005f;
		}
		return 0f;
	}
	
	
	protected boolean playerHasOres() {
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && (spec.getDemandClass().equals(Commodities.ORE) || spec.getDemandClass().equals(Commodities.RARE_ORE))) {
				return true;
			}
		}
		return false;
	}
        
        protected boolean playerTimer() {
		person.getMemoryWithoutUpdate().set("$cum_hn_tradeagain2", Misc.getAtLeastStringForDays((int) person.getMemoryWithoutUpdate().getExpire("$cum_hn_tradeagain")), 0);
		return true;
	}

	
	
}















