package data.scripts.ix.listeners;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.util.NameListUtil;
import lunalib.lunaSettings.LunaSettings;

//gives chance to add Panoptic Interface to encountered IX Battlegroup ships
//adds special loot to HVB mission
//also adds one of three Dawnstar Reactors to ships randomly fit with CPBs
public class IXEncounterListener extends BaseCampaignEventListener {
	
	private static String COMMAND_MOD_ID = "ix_panoptic_command";
	private static String STRATEGIC_MOD_ID = "ix_panoptic_strategic";
	private static String TACTICAL_MOD_ID = "ix_panoptic_tactical";

	private static String IX_FAC_ID = "ix_battlegroup";
	private static String IX_MOD_ID = "ix_ninth";
	private static String IX_ELITE_MOD_ID = "ix_smod_handler";
	private static String IX_BOSS_MOD_ID = "ix_hvb_handler";
	private static String CHECKER_MOD_ID = "ix_panoptic_checker";
	private static String CANDOR_MOD_ID = "ix_semi_automated";
	
	private static float ODDS_REGULAR = 0.1f;
	private static float ODDS_LEADER = 0.3f;
	private static float ODDS_ELITE_BONUS = 0.2f;
	
	private static String DAWNSTAR_P = "ix_dawnstar_proton";
	private static String DAWNSTAR_PH = "ix_dawnstar_proton_handler";
	private static String DAWNSTAR_N = "ix_dawnstar_neutron";
	private static String DAWNSTAR_NH = "ix_dawnstar_neutron_handler";
	private static String DAWNSTAR_E = "ix_dawnstar_electron";
	private static String DAWNSTAR_EH = "ix_dawnstar_electron_handler";
	private static String DAWNSTAR_CONTROLLER = "ix_dawnstar_controller";

	private static String CPB_L_ID = "dawnstar_lcpb_ix";
	private static String CPB_H_ID = "dawnstar_hcpb_ix";
	
	public IXEncounterListener() {
		super(true);
	}
	
	@Override
	public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
		//gives Dawnstar Reactor to nearest station when initiating battle from "consider your military options"
		if (dialog.getInteractionTarget() == null) return;
		MarketAPI market = null;
		if (dialog.getInteractionTarget().getMarket() != null) {
			market = dialog.getInteractionTarget().getMarket();
			CampaignFleetAPI stationFleet = Misc.getStationFleet(market);
			if (stationFleet != null) equipComponentsToFleet(stationFleet);
		}
		
		CampaignFleetAPI otherFleet = null;
		CampaignFleetAPI allFleet = null;
		boolean isValidFleet = true;
		try { 
			otherFleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			if (otherFleet.isPlayerFleet()) isValidFleet = false;
		}
		catch (Exception e) {
			isValidFleet = false;
		} 
		finally {
			if (isValidFleet) {
				if (otherFleet.getBattle() != null) allFleet = otherFleet.getBattle().getNonPlayerCombined();
				else allFleet = otherFleet;
				equipComponentsToFleet(allFleet);
			}
		}
	}	
	
	//type -1 adds random reactor type
	private void equipDawnstarToVariant (ShipVariantAPI variant, int type) {
		boolean hasDawnstar = false;
		Collection<String> weaponSlotList = variant.getFittedWeaponSlots();
		for (String slot : weaponSlotList) {
			String weaponId = variant.getWeaponSpec(slot).getWeaponId();
			if (CPB_L_ID.equals(weaponId) || CPB_H_ID.equals(weaponId)) hasDawnstar = true;
		}
		if (hasDawnstar && !variant.hasHullMod(DAWNSTAR_CONTROLLER)) {
			if (type == -1) {
				Random rand = new Random();
				type = rand.nextInt(3);
			}
			if (type == 0) {
				variant.addMod(DAWNSTAR_P);
				variant.addMod(DAWNSTAR_PH);
			}
			else if (type == 1) {
				variant.addMod(DAWNSTAR_N);
				variant.addMod(DAWNSTAR_NH);
			}
			else {
				variant.addMod(DAWNSTAR_E);
				variant.addMod(DAWNSTAR_EH);
			}
			variant.addMod(DAWNSTAR_CONTROLLER);
		}
	}
	
	private void equipComponentsToFleet (CampaignFleetAPI fleet) {
		if (fleet == null) return;
		
		//Lunalib settings
		boolean isInterfaceEnabled = true;
		if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
			isInterfaceEnabled = LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_interface_enabled");
		}
		boolean isFirstRename = true;

		//add dawnstar reactor
		Random rand = new Random();
		int type = rand.nextInt(3);
		List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();
		for (FleetMemberAPI member : fleetList) {
			ShipVariantAPI var = member.getVariant();
			
			//check station modules, adds the same reactor type to all modules
			if (var.isStation()) {
				for (String slot : var.getModuleSlots()) {
					if (var.getModuleVariant(slot) != null) {
						ShipVariantAPI module = var.getModuleVariant(slot);
						equipDawnstarToVariant(module, type);
					}
				}
				//add Antimatter Stabilizer to IX stations
				if (member.getFleetCommander().getFaction().getId().equals(IX_FAC_ID)) {
					String item = "ix_antimatter_stabilizer";
					CargoAPI cargo = Global.getFactory().createCargo(true);
					SectorEntityToken carrier = (SectorEntityToken) fleet;
					if (BaseSalvageSpecial.getCombinedExtraSalvage(carrier).getCommodityQuantity(item) < 1f) {
						cargo.addCommodity(item, 1f);
						BaseSalvageSpecial.addExtraSalvage(carrier, cargo);
					}
					else if (BaseSalvageSpecial.getCombinedExtraSalvage(carrier).getCommodityQuantity(item) > 1f) {
						BaseSalvageSpecial.clearExtraSalvage(carrier);
						cargo.addCommodity(item, 1f);
						BaseSalvageSpecial.addExtraSalvage(carrier, cargo);
					}
				}
			}

			//add random Dawnstar Reactor to CPB equipped ships without a reactor 
			else equipDawnstarToVariant(var, -1);
			
			//add special loot to HVB mission
			if (var.hasHullMod(IX_BOSS_MOD_ID)) {
				if (HullSize.CAPITAL_SHIP.equals(var.getHullSize()) 
						&& !Global.getSector().getMemoryWithoutUpdate().getBoolean("$ix_biochip_looted")) {
					CargoAPI cargo = Global.getFactory().createCargo(true);
					SectorEntityToken carrier = (SectorEntityToken) fleet;
					if (Global.getSettings().getModManager().isModEnabled("aotd_vok")) {
						cargo.addSpecial(new SpecialItemData("special_ship_bp", "radiant_ix:$ix_aqq_radiant"), 1);
					}
					if (Global.getSettings().getModManager().isModEnabled("EmergentThreats_Vice")) {
						cargo.addHullmods("vice_interdiction_array", 1);
						cargo.addHullmods("vice_adaptive_entropy_arrester", 1);
						cargo.addHullmods("vice_adaptive_flux_dissipator", 1);
					}
					BaseSalvageSpecial.addExtraSalvage(carrier, cargo);
					Global.getSector().getMemoryWithoutUpdate().set("$ix_biochip_looted", true);
				}
				
				if (HullSize.CRUISER.equals(var.getHullSize())) {
					if (member.getShipName().startsWith("TTDS") && (isFirstRename)) {
						member.setShipName(NameListUtil.HGS_Vindicator);
						isFirstRename = false;
					}
					else if ((member.getShipName().startsWith("TTDS") && (!isFirstRename))) {
						member.setShipName(NameListUtil.HGS_Vanquisher);
					}
				}
			}
			
			//add or remove panoptic interface from encountered fleet depending on lunalib setting
			boolean isWithoutCaptain = (member.getCaptain() == null || member.getCaptain().isDefault());
			boolean isOnlyCore = true;
			if (var.hasHullMod(IX_MOD_ID) && !var.hasHullMod(IX_BOSS_MOD_ID)) {
				//clear interface mods if lunalib setting is off
				if (!isInterfaceEnabled) {
					var.getPermaMods().remove(COMMAND_MOD_ID);
					var.getPermaMods().remove(TACTICAL_MOD_ID);
					var.getPermaMods().remove(STRATEGIC_MOD_ID);
					var.getHullMods().remove(COMMAND_MOD_ID);
					var.getHullMods().remove(TACTICAL_MOD_ID);
					var.getHullMods().remove(STRATEGIC_MOD_ID);
				}
				
				//do nothing if ship already has checker
				else if (!var.hasHullMod(CHECKER_MOD_ID)) {
					float odds = member.isFlagship() ? ODDS_LEADER : ODDS_REGULAR;
					if (var.hasHullMod(IX_ELITE_MOD_ID)) odds += ODDS_ELITE_BONUS;
					if (Math.random() < odds) {
						String mod = "";
						//only one command interface max per encounter, never on Candor IX
						if (isWithoutCaptain && isOnlyCore && !var.hasHullMod(CANDOR_MOD_ID)) {
							mod = COMMAND_MOD_ID;
							isOnlyCore = false;
						}
						else mod = Math.random() <= 0.5f ? TACTICAL_MOD_ID : STRATEGIC_MOD_ID;
						var.addPermaMod(mod);
					}
					//always add checker even if interface is not added so ship is not checked again
					var.addPermaMod(CHECKER_MOD_ID);
				}
			}
		}
	}
}