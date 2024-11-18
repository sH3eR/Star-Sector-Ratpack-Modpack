package DE.campaign.rulecmd;

import java.util.ArrayList;
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
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveAnyItem;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * NotifyEvent $eventHandle <params> 
 * DE_WeaponTradeTab (command here)
 */
public class DE_WeaponTradeTab extends BaseCommandPlugin {
	
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
	protected List<String> allowedweaponslist = new ArrayList<String>();
	protected float count;
	protected float reqcount;
	protected float overflowcount;
	protected float omegacap;
	protected boolean isomega;
	
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

		isomega = false;
		omegacap = 1.0f;

		if (command.equals("selectHegemonyWeapons")) {
			allowedweaponslist.add("lightneedler");
			allowedweaponslist.add("hveldriver");
			allowedweaponslist.add("heavymauler");
			allowedweaponslist.add("heavyneedler");
			allowedweaponslist.add("gauss");
			allowedweaponslist.add("multineedler");
			reqcount = memory.getInt("$Hegemonyweaponamount");
			selectWeapons();
		} else if (command.equals("selectPLWeapons")) {
			allowedweaponslist.add("gorgon");
			allowedweaponslist.add("gorgonpod");
			allowedweaponslist.add("gazer");
			allowedweaponslist.add("gazerpod");
			allowedweaponslist.add("dragon");
			allowedweaponslist.add("dragonpod");
			allowedweaponslist.add("hydra");
			reqcount = memory.getInt("$PLweaponamount");
			selectWeapons();
		} else if (command.equals("selectTTWeapons")) {
			allowedweaponslist.add("amblaster");
			allowedweaponslist.add("heavyblaster");
			allowedweaponslist.add("heavyburst");
			allowedweaponslist.add("ionpulser");
			allowedweaponslist.add("ionbeam");
			allowedweaponslist.add("plasma");
			allowedweaponslist.add("guardian");
			allowedweaponslist.add("tachyonlance");
			reqcount = memory.getInt("$TTweaponamount");
			selectWeapons();
		} else if (command.equals("selectOmegaWeapons")) {
			allowedweaponslist.add("minipulser");
			allowedweaponslist.add("shockrepeater");
			allowedweaponslist.add("riftlance");
			allowedweaponslist.add("riftbeam");
			allowedweaponslist.add("cryoflux");
			allowedweaponslist.add("cryoblaster");
			allowedweaponslist.add("disintegrator");
			allowedweaponslist.add("riftcascade");
			allowedweaponslist.add("vpdriver");
			allowedweaponslist.add("realitydisruptor");
			allowedweaponslist.add("amsrm");
			allowedweaponslist.add("resonatormrm");
			allowedweaponslist.add("rifttorpedo");
			reqcount = memory.getInt("$omegaweaponamount");
			omegacap = 2.0f;
			isomega = true;
			selectWeapons();
		}
		return true;
	}

	/*protected boolean personCanAcceptCores() {
		if (person == null || !buysAICores) return false;
		
		return Ranks.POST_BASE_COMMANDER.equals(person.getPostId()) ||
			   Ranks.POST_STATION_COMMANDER.equals(person.getPostId()) ||
			   Ranks.POST_ADMINISTRATOR.equals(person.getPostId()) ||
			   Ranks.POST_OUTPOST_COMMANDER.equals(person.getPostId());
	}*/

	protected void selectWeapons() {
		CargoAPI copy = Global.getFactory().createCargo(false);
		//copy.addAll(cargo);
		//copy.setOrigSource(playerCargo);
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
			if (spec != null && allowedweaponslist.contains(spec.getWeaponId())) {//compare from a list and add anything inside
				copy.addFromStack(stack);
			}
		}
		copy.sort();

		final float width = 310f;
		dialog.showCargoPickerDialog("Select weapons to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
			public void pickedCargo(CargoAPI cargo) {
				if (cargo.isEmpty()) {
					cancelledCargoSelection();
					return;
				}
				
				cargo.sort();
				for (CargoStackAPI stack : cargo.getStacksCopy()) {
					float canproceed = computeCountValue(cargo);
					if (!isomega) {
						//if (canproceed >= memory.getInt("$Hegemonyweaponamount")) {
							//playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
							playerCargo.removeWeapons(stack.getWeaponSpecIfWeapon().getWeaponId(), (int) stack.getSize());
							if (stack.isWeaponStack()) { // should be always, but just in case
								int num = (int) stack.getSize();
								AddRemoveCommodity.addWeaponLossText(stack.getWeaponSpecIfWeapon().getWeaponId(), num, text);
								allowedweaponslist.clear();
						/*String key = "$turnedIn_" + stack.getCommodityId();
						int turnedIn = faction.getMemoryWithoutUpdate().getInt(key);
						faction.getMemoryWithoutUpdate().set(key, turnedIn + num);

						// Also, total of all cores! -dgb
						String key2 = "$turnedIn_allCores";
						int turnedIn2 = faction.getMemoryWithoutUpdate().getInt(key2);
						faction.getMemoryWithoutUpdate().set(key2, turnedIn2 + num);*/
							}
						//}
					} else {
						//if (canproceed >= memory.getInt("$Omegaweaponamount")) {
							//playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
							playerCargo.removeWeapons(stack.getWeaponSpecIfWeapon().getWeaponId(), (int) stack.getSize());
							if (stack.isWeaponStack()) { // should be always, but just in case
								int num = (int) stack.getSize();
								AddRemoveCommodity.addWeaponLossText(stack.getWeaponSpecIfWeapon().getWeaponId(), num, text);
								allowedweaponslist.clear();
							}
						//}
					}
				}

				float repChange = computeOverflowReputationValue(cargo);
				//if (repChange >= 1f) {
					CustomRepImpact impact = new CustomRepImpact();
					impact.delta = repChange * 0.01f;
					Global.getSector().adjustPlayerReputation(
							new RepActionEnvelope(RepActions.CUSTOM, impact,
									null, text, true),
							faction.getId());
				//}
				FireBest.fire(null, dialog, memoryMap, "WeaponsTurnedIn");
			}
			public void cancelledCargoSelection() {
			}

			public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

				//float bounty = computeCoreCreditValue(combined);
				float repChange = computeOverflowReputationValue(combined);

				float pad = 3f;
				float small = 5f;
				float opad = 10f;

				panel.setParaFontOrbitron();
				panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), 1f);
				//panel.addTitle(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor());
				//panel.addPara(faction.getDisplayNameLong(), faction.getBaseUIColor(), opad);
				//panel.addPara(faction.getDisplayName() + " (" + entity.getMarket().getName() + ")", faction.getBaseUIColor(), opad);
				panel.setParaFontDefault();

				panel.addImage(faction.getLogo(), width * 1f, 3f);
				panel.addPara("Giving Lucanus weapons will raise reputation with the Sindrian Diktat up to a maximum of 15, or 25 for Omega weapons.", opad);
				panel.beginGridFlipped(width, 1, 40f, 10f);
				//panel.beginGrid(150f, 1);
				panel.addGrid(pad);

				panel.addPara("If you turn in the selected weapons, your standing with " + faction.getDisplayNameWithArticle() + " will improve by %s points.",
						opad * 1f, Misc.getHighlightColor(),
						"" + (int) repChange);

				panel.addPara("Small weapons count as 1 point, mediums at 2 and larges at 3, halved and rounded down. Donating Omega weapons doubles the reputation gain compared to normal weapons.", opad);
			}
		});
	}

	// works!
	protected float computeCountValue(CargoAPI cargo) {
		float count = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
			if (allowedweaponslist.contains(spec.getWeaponId())) {
				if (spec != null && spec.getSize().equals(WeaponAPI.WeaponSize.SMALL)) {
				count += 1 * stack.getSize();
				} else if (spec != null && spec.getSize().equals(WeaponAPI.WeaponSize.MEDIUM)) {
					count += 2 * stack.getSize();
				} else if (spec != null && spec.getSize().equals(WeaponAPI.WeaponSize.LARGE)) {
					count += 3 * stack.getSize();
				}
			}
		}
		return count;
	}

	// does not work...
	protected float computeOverflowReputationValue(CargoAPI cargo) {
		float rep = 0;
		float count = computeCountValue(cargo);
		if (!isomega) {
				//overflowcount = count - memory.getInt("$Hegemonyweaponamount");
				rep = count * omegacap * 0.5f;
			// log base rep amount
			String str5 = String.format("This script is firing, maximum possible rep change is %s", rep);
			Global.getLogger(this.getClass()).info(str5);
				if (rep >= 15) { // cap the amount of rep so you cant farm lucanus for ez rep
					rep = 15 * omegacap;
				}
		} else {
				//overflowcount = count - memory.getInt("$Omegaweaponamount");
				rep = count * omegacap;
				if (rep >= 25) { // cap the amount of rep so you cant farm lucanus for ez rep
					rep = 25 * omegacap * 0.5f;
				}
		}
		if (rep < 0) {
			rep = 0;
		}
		/*for (CargoStackAPI stack : cargo.getStacksCopy()) {
			WeaponSpecAPI spec = stack.getWeaponSpecIfWeapon();
			if (allowedweaponslist.contains(spec.getWeaponId())) {
				if (spec != null && spec.getSize().equals(WeaponAPI.WeaponSize.SMALL)) {
					rep += getBaseRepValue(spec.getId()) * stack.getSize();
				}
			}
		}*/
		//if (rep < 1f) rep = 1f;
		// log rep amount after everything
		String str5 = String.format("This script is firing, current rep change is %s", rep);
		Global.getLogger(this.getClass()).info(str5);
		return rep;
	}
}















