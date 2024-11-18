package data.scripts.vice.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;

import lunalib.lunaSettings.LunaSettings;

//adds adaptive subsystems to Remnant ships encountered by the player that are not part of this mod
public class EnemyEncounterListener extends BaseCampaignEventListener {
	
	private static String HANDLER_HULLMOD = "vice_interdiction_handler";
	private static String PENALTY_HULLMOD = "vice_adaptive_malfunction";
	private static String IXMOD = "ix_ninth";
	private static String TWMOD = "tw_trinity_retrofit";
	
	public EnemyEncounterListener() {
		super(true);
	}
	
	@Override
	public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
		if (dialog.getInteractionTarget() == null) return;
		MarketAPI market = null;
		if (dialog.getInteractionTarget().getMarket() != null) {
			market = dialog.getInteractionTarget().getMarket();
			CampaignFleetAPI stationFleet = Misc.getStationFleet(market);
			if (stationFleet != null) {
				if (isSynthesisFleetChecker(stationFleet)) equipSubsystemsToFleet(stationFleet, true);
				else equipSubsystemsToFleet(stationFleet);
			}
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
				swapSpecialModifications(allFleet); //swaps Special Modifications from Andradanism xo
				if (isSynthesisFleetChecker(otherFleet)) equipSubsystemsToFleet(allFleet, true);
				else equipSubsystemsToFleet(allFleet);
			}
		}
	}
	
	private static boolean isSynthesisFleetChecker(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().is("$xo_synthesis_fleet", true);
	}
	
	private static boolean isAndradaFleetChecker(CampaignFleetAPI fleet) {
		return fleet.getMemoryWithoutUpdate().is("$xo_andrada_fleet", true);
	}
	
	private static String OLD_PLATE = "andrada_mods";
	private static String NEW_PLATE = "vice_special_modifications";
	
	private static void swapSpecialModifications(CampaignFleetAPI fleet) {
		if (fleet == null) return;
		boolean isGivingBetterPlate = isAndradaFleetChecker(fleet);
		List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();		
		for (FleetMemberAPI member : fleetList) {
			if (isGivingBetterPlate && member.getVariant().hasHullMod(OLD_PLATE)) {
				member.getVariant().getHullMods().remove(OLD_PLATE);
				member.getVariant().getHullMods().add(NEW_PLATE);
			}
			else if (!isGivingBetterPlate && member.getVariant().hasHullMod(NEW_PLATE)) {
				member.getVariant().getHullMods().remove(NEW_PLATE);
				member.getVariant().getHullMods().add(OLD_PLATE);
			}
		}
	}	
	
	private static void equipSubsystemsToFleet(CampaignFleetAPI fleet) {
		equipSubsystemsToFleet(fleet, false);
	}
	
	//for synthesis xo default skill, hullmods are "illegal" on hulls and will self delete if salvaged
	private static void equipSubsystemsToFleet(CampaignFleetAPI fleet, boolean isSynthesisFleet) {
		if (fleet == null) return;
		
		//Lunalib settings
		boolean isAbyssalEnabled = true;
		boolean isMessEnabled = true;
		boolean isSynthesisEnabled = true;
		boolean isVriEnabled = true;
		String remnantDifficulty = "Challenging";
		
		if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
			isAbyssalEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_abyssalEnabled");
			isMessEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_messEnabled");
			isSynthesisEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_synthesisEnabled");
			isVriEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_vriEnabled");
			remnantDifficulty = LunaSettings.getString("EmergentThreats_Vice", "vice_remnantDifficulty");
		}
		
		List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();
		for (FleetMemberAPI member : fleetList) {
			//adaptive tactical core check to see if ship has no captain.
			//doing it here since it can't easily be done with ShipVariantAPI
			boolean isWithoutCaptain = (member.getCaptain() == null || member.getCaptain().isDefault());

			ShipVariantAPI var = member.getVariant();
			boolean addNone = false;
			boolean isModule = false; //lets shipwide integration handle adaptive subsystems
			
			//add Interdictor Pulse handler to non-IX Paragons
			addParagonInterdictorHandler(var);
			
			boolean isAbyssalFleet = false;
			
			if (var.getHullSpec().getSuppliesPerMonth() == 0 && var.getHullSpec().getEngineSpec().getMaxSpeed() == 0) isModule = true;
			
			boolean isAIModShip = (var.hasHullMod("ix_converted_hull") || var.hasHullMod("ix_semi_automated"));
			String maker = var.getHullSpec().getManufacturer();
			if (!var.isFighter() && !isModule && (isAIModShip || isSynthesisFleet
										|| maker.equals("Remnant") 
										|| maker.equals("Remnant Mess Object") 
										|| maker.equals("Dustkeeper Proxies") 
										|| maker.equals("Dustkeeper Contingency") 
										|| maker.equals("Abyssal") 
										|| maker.equals("Seraph") 
										|| maker.equals("Volantian Remnant Conversion") 
										|| maker.equals("XIV Remnant"))) {
				String modToDelete = null;
				String vriMod = null;
				String synthesisMod = null;
				if (maker.equals("Abyssal") || maker.equals("Seraph")) isAbyssalFleet = true;
				for (String mod : var.getHullMods()) {
					//if ship has adaptive mod or setting is not Challenging, do not add new mod
					if (mod.startsWith("vice_adaptive") || !remnantDifficulty.equals("Challenging")) addNone = true;
					//if ship has adaptive mod and setting is not Challenging, delete adaptive mod
					if (mod.startsWith("vice_adaptive") && !remnantDifficulty.equals("Challenging")) modToDelete = mod;
					if (mod.startsWith("vice_adaptive")) {
						vriMod = mod;
						synthesisMod = mod;
					}
				}
				if (modToDelete != null) var.removeMod(modToDelete);
				
				//if setting is Easy, add penalty mod, else remove, synthesis and abyssal fleets are exempt
				if (remnantDifficulty.equals("Easy") && !isSynthesisFleet && !isAbyssalFleet) {
					var.addMod(PENALTY_HULLMOD);
					addNone = true;
				}
				else var.removeMod(PENALTY_HULLMOD);
				
				//clear disabled crossmod hullmods
				if (!isAbyssalEnabled) {
					var.removeMod("vice_adaptive_entropy_projector_abyssal");
					addNone = true;
				}
				if (!isMessEnabled) {
					var.removeMod("vice_adaptive_metastatic_growth");
					addNone = true;
				}
				//if fleet has synthesis marker but feature is disabled
				if (!isSynthesisEnabled && isSynthesisFleetChecker(fleet)) {
					if (!isAIModShip && synthesisMod != null) var.removeMod(synthesisMod);
					addNone = true;
				}
				if (maker.equals("Volantian Remnant Conversion") && !isVriEnabled && vriMod != null) {
					var.removeMod(vriMod);
					addNone = true;
				}

				if (addNone) continue;
				var.addMod(modPicker(var, isWithoutCaptain));
			}
		}
	}
	
	private static void addParagonInterdictorHandler(ShipVariantAPI var) {
		String hullId = var.getHullSpec().getHullId();
		boolean isParagon = hullId.contains("paragon");
		boolean isParagonIX = hullId.startsWith("paragon_ix");
		
		//do nothing for non-Paragons or IX Paragons
		if (!isParagon || isParagonIX) return;

		//add handler to Paragon skins
		if (isParagon && !var.hasHullMod(HANDLER_HULLMOD)) var.addPermaMod(HANDLER_HULLMOD);
		
		//add handler to Zeus
		if (hullId.startsWith("swp_boss_paragon") && !var.hasHullMod(HANDLER_HULLMOD)) var.addPermaMod(HANDLER_HULLMOD);
	}
	
	private static String modPicker (ShipVariantAPI var, boolean isWithoutCaptain) {
		ShipHullSpecAPI spec = var.getHullSpec();
		Collection<String> mods = var.getHullMods();
		
		//special faction hullmods
		if (spec.getManufacturer().equals("Abyssal")) return "vice_adaptive_entropy_projector_abyssal";
		if (spec.getManufacturer().equals("Seraph")) return "vice_adaptive_entropy_projector_abyssal";
		if (spec.getManufacturer().equals("Remnant Mess Object")) return "vice_adaptive_metastatic_growth";
		
		//stations always have Adaptive Neural Net
		if (var.hasHullMod("vast_bulk")) return "vice_adaptive_neural_net";
		
		//if ship has no shields and no conflicting phase mods, add adaptive phase coils (otherwise make it potential hullmod later)
		boolean noShields = (spec.getShieldType().equals(ShieldType.PHASE) || spec.getShieldType().equals(ShieldType.NONE));
		if (noShields && (!var.hasHullMod("phase_anchor") && !var.hasHullMod("adaptive_coils"))) return "vice_adaptive_phase_coils";
		
		List<String> weaponSlots = var.getNonBuiltInWeaponSlots();		
		boolean isCTE = false;
		//get number of non-PD beam and pulse weapons
		int beamCount = 0;
		int pulseCount = 0;
		for (String slot : weaponSlots) {
			WeaponSpecAPI weapon = var.getWeaponSpec(slot);
			//make adaptive entropy projector the mod for ships that have Charge Transfer Emitter weapons
			if (weapon.getWeaponId().equals("cte_tw")) isCTE = true;
			else if (weapon.getType().equals(WeaponType.ENERGY)) {
				if (weapon.getAIHints().contains(AIHints.PD)) continue;
				else if (weapon.isBeam()) beamCount++;
				else if (!weapon.isBeam()) pulseCount++;
			}
		}
		if (isCTE) return "vice_adaptive_entropy_projector";
		
		//list of valid mods to add, does not include drone bay, flight command, or gravity drive
		List<String> MODLIST = new ArrayList<String>();
		MODLIST.add("vice_adaptive_entropy_projector");
		MODLIST.add("vice_adaptive_neural_net");
		MODLIST.add("vice_adaptive_phase_coils");
		MODLIST.add("vice_adaptive_reactor_chamber");
		
		//add special hullmods as potential choices for IX affiliated ships
		String maker = var.getHullSpec().getManufacturer();
		if (maker == null) maker = "";
		if (maker.equals("IX Battlegroup") || maker.equals("IX Auxiliary") || maker.equals("Trinity Worlds")) {
			MODLIST.add("vice_adaptive_flux_dissipator");
			MODLIST.add("vice_adaptive_entropy_arrester");
		}
		
		//if 4 beam weapons and no optics mods, add adaptive emitter diodes, else if 1-3 make it potential hullmod
		boolean hasOptics = (mods.contains("advancedoptics") 
							|| mods.contains("high_scatter_amp")
							|| mods.contains("vice_attuned_emitter_diodes")
							|| mods.contains("ix_laser_collimator"));
		if (beamCount > 3 && !hasOptics) return "vice_adaptive_emitter_diodes";
		else if (beamCount >= 1 && beamCount <= 3 && !hasOptics) MODLIST.add("vice_adaptive_emitter_diodes");
		
		//if 4 pulse weapons and no coherer, add adaptive emitter diodes, else if 1-3 no coherer make it potential hullmod
		if ((pulseCount > 3) && !var.hasHullMod("coherer")) return "vice_adaptive_pulse_resonator";
		else if ((pulseCount >= 1 && beamCount <= 3) && !var.hasHullMod("coherer")) MODLIST.add("vice_adaptive_pulse_resonator");
		
		//if ship has no AI core, make adaptive tactical core potential mod
		if (isWithoutCaptain) MODLIST.add("vice_adaptive_tactical_core");
		
		//if ship has shields, make adaptive temporal shell potential mod
		if (!noShields) MODLIST.add("vice_adaptive_temporal_shell");
		
		//if ship has no other engine upgrades, make adaptive thruster control potential mod
		boolean hasEngineUpgrade = (mods.contains("auxiliarythrusters") || mods.contains("unstable_injector"));
		if (!hasEngineUpgrade) MODLIST.add("vice_adaptive_thruster_control");
		
		Random rand = new Random();
		int modIndex = rand.nextInt(MODLIST.size());
		
		return(MODLIST.get(modIndex).toString());
	}
}