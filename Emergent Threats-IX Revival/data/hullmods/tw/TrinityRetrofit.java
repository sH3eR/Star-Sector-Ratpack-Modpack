package data.hullmods.tw;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TrinityRetrofit extends BaseHullMod {

	private static Map speedBoost = new HashMap();
	static {
		speedBoost.put(HullSize.FRIGATE, 20f);
		speedBoost.put(HullSize.DESTROYER, 15f);
		speedBoost.put(HullSize.CRUISER, 12f);
		speedBoost.put(HullSize.CAPITAL_SHIP, 10f);
	}
	
	private static Map<HullSize, String> nimbusWingId = new HashMap<HullSize, String>();
	static {
		nimbusWingId.put(HullSize.DESTROYER, "nimbus_tw_wing_s");
		nimbusWingId.put(HullSize.CRUISER, "nimbus_tw_wing_m");
		nimbusWingId.put(HullSize.CAPITAL_SHIP, "nimbus_tw_wing_l");
	}
	
	private static float RANGE_MULT = 0.90f;
	private static float REPAIR_PENALTY = 20f;
	
	private static String DAWNSTAR_P = "ix_dawnstar_proton";
	private static String DAWNSTAR_PH = "ix_dawnstar_proton_handler";
	private static String DAWNSTAR_N = "ix_dawnstar_neutron";
	private static String DAWNSTAR_NH = "ix_dawnstar_neutron_handler";
	private static String DAWNSTAR_E = "ix_dawnstar_electron";
	private static String DAWNSTAR_EH = "ix_dawnstar_electron_handler";
	private static String DAWNSTAR_CONTROLLER = "ix_dawnstar_controller";
	
	private static String CPB_L_ID = "dawnstar_lcpb_ix";
	private static String CPB_H_ID = "dawnstar_hcpb_ix";
	
	//disallows other extra bay mods, vice_adaptive_drone_bay exclusion handled by that hullmod
	private static String CONFLICT_MOD_1 = "unstable_injector";
	private static String CONFLICT_MOD_2 = "converted_hangar";
	private static String TW_MOD_NAME = "Drone Control Node";
	private static String TW_DRONE_NAME = "Nimbus Combat Drones";
	
	private static String DRONE_NODE_HULLMOD = "tw_drone_control_node";
	private static String PENALTY_HULLMOD = "tw_equipment_error";
	
	//Equalizer (TW) supercruise
	private static float ZERO_FLUX_LEVEL = 10f;
	private static String EQUALIZER_HULLMOD = "tw_energy_weapon_integration";
	
	//Glycon (TW) DP reduction
	private static float DP_REDUCTION = 2f;
	private static String GLYCON_TW_ID = "glycon_tw";
	private static String GLYCON_TW_D_ID = "glycon_tw_default_D";
		
	//Ionos (TW) DP increase
	private static float DP_INCREASE_IONOS = 12.5f;
	private static String IONOS_TW_ID = "ionos_tw";
	private static String IONOS_TW_D_ID = "ionos_tw_default_D";
	
	//Maquech data
	private static String MAQUECH_HULLMOD = "tw_enhanced_control_node";
	private static String MAQUECH_WING = "nimbus_tw_wing_c";
	
	//Nimbus valid wing IDs
	private static String NIMBUS_PREF = "nimbus_tw_";
	private static String NIMBUS_WING = "nimbus_tw_wing";
	private static String NIMBUS_REM = "vice_nimbus_wing";
	
	//Radiant data 
	private static String RADIANT_HULLMOD = "ix_converted_hull";
	private static String RADIANT_WING = "nimbus_tw_wing_r";
	private static String GRAV_HULLMOD = "vice_adaptive_gravity_drive";
	private static String GRAV_HULLMOD_DISPLAY = "Adaptive Gravity Drive";
	private static String GRAV_SYSTEM = "vice_fleetjump";
	private static String FTR_HULLMOD = "vice_adaptive_flight_command";
	private static String FTR_HULLMOD_DISPLAY = "Adaptive Flight Command";
	private static String FTR_SYSTEM = "vice_targetingsweep";
	private static String BASE_SYSTEM = "displacer_degraded";
	
	//Shrike (TW) DP increase
	private static float DP_INCREASE = 2f;
	private static String SHRIKE_TW_ID = "shrike_tw";
	private static String SHRIKE_TW_D_ID = "shrike_tw_default_D";
	
	@Override	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, (Float) speedBoost.get(hullSize));
		stats.getBallisticWeaponRangeBonus().modifyMult(id, RANGE_MULT);
		stats.getEnergyWeaponRangeBonus().modifyMult(id, RANGE_MULT);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f + REPAIR_PENALTY * 0.01f);
		
		ShipVariantAPI variant = stats.getVariant();
		//adds Nimbus bay and squad of appropriate size when drone control node is present
		if (variant.hasHullMod(RADIANT_HULLMOD) && variant.hasHullMod(MAQUECH_HULLMOD)) {
			if (variant.getWingId(0) == null) variant.setWingId(0, RADIANT_WING);
			if (variant.getWingId(1) == null) variant.setWingId(1, RADIANT_WING);
			
			if (variant.hasHullMod(FTR_HULLMOD)) {
				variant.setWingId(2, null);
				variant.setWingId(3, null);
			}
			else {
				if (variant.getWingId(2) == null) variant.setWingId(2, RADIANT_WING);
				if (variant.getWingId(3) == null) variant.setWingId(3, RADIANT_WING);
			}
			
			//system swap for Radiant adaptive flight command or adaptive gravity drive
			if (variant.getNonBuiltInHullmods().contains(FTR_HULLMOD)) {
				variant.getHullSpec().setShipSystemId(FTR_SYSTEM);
			}
			else if (variant.getNonBuiltInHullmods().contains(GRAV_HULLMOD)) {
				variant.getHullSpec().setShipSystemId(GRAV_SYSTEM);
			}
			else variant.getHullSpec().setShipSystemId(BASE_SYSTEM);
		}
		else if (variant.hasHullMod(MAQUECH_HULLMOD)) {
			if (variant.getWingId(0) == null) variant.setWingId(0, MAQUECH_WING);
			if (variant.getWingId(1) == null) variant.setWingId(1, MAQUECH_WING);
		}
		else if (variant.hasHullMod(DRONE_NODE_HULLMOD)) {
			stats.getNumFighterBays().modifyFlat(id, 1f);
			if (variant.getWingId(0) == null && hullSize != HullSize.FRIGATE) {
				String wingId = (String) nimbusWingId.get(hullSize);
				variant.setWingId(0, wingId);
			}
		}

		//applies CR penalty hullmod if ship equips standard strike craft, Maquech (TW) always passes this check
		//also applies penalty if too much equipment is fitted, due to Maquech (TW) drones having fitting cost
		boolean isAllValidDrones = true;
		int fighterBays = stats.getNumFighterBays().getModifiedInt();
		for (int i = 0; i < fighterBays; i++) {
			if (isValidDrone(variant.getWingId(i), i) == false) isAllValidDrones = false;
		}
		if (variant.hasHullMod(MAQUECH_HULLMOD) && getRemainingOP(stats) >= 0) isAllValidDrones = true;
		if (!isAllValidDrones) variant.addMod(PENALTY_HULLMOD);
		else variant.removeMod(PENALTY_HULLMOD);
		
		String shipId = variant.getHullSpec().getHullId();
		boolean isEqualizer = variant.hasHullMod(EQUALIZER_HULLMOD);
		boolean isGlycon = shipId.equals(GLYCON_TW_ID) || shipId.equals(GLYCON_TW_D_ID);
		boolean isShrike = shipId.equals(SHRIKE_TW_ID) || shipId.equals(SHRIKE_TW_D_ID);
		boolean isIonos = shipId.equals(IONOS_TW_ID) || shipId.equals(IONOS_TW_D_ID);
		if (isEqualizer) stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_LEVEL * 0.01f);
		else if (isGlycon) stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, -DP_REDUCTION);
		else if (isShrike) stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, DP_INCREASE);
		else if (isIonos) {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id, 1f + DP_INCREASE_IONOS * 0.01f);
			stats.getSuppliesToRecover().modifyMult(id, 1f + DP_INCREASE_IONOS * 0.01f);
			stats.getSuppliesPerMonth().modifyMult(id, 1f + DP_INCREASE_IONOS * 0.01f);
		}
		variant.getHullMods().remove(CONFLICT_MOD_1);
		variant.getHullMods().remove(CONFLICT_MOD_2);
		
		//adds dawnstar reactor to ships with CPB weapons
		boolean hasDawnstar = false;
		ShipVariantAPI var = stats.getVariant();
		Collection<String> weaponSlotList = var.getFittedWeaponSlots();
		for (String slot : weaponSlotList) {
			String weaponId = var.getWeaponSpec(slot).getWeaponId();
			if (CPB_L_ID.equals(weaponId) || CPB_H_ID.equals(weaponId)) hasDawnstar = true;
		}
		if (hasDawnstar && !var.hasHullMod(DAWNSTAR_CONTROLLER)) {
			Random rand = new Random();
			int mod = rand.nextInt(3);
			if (mod == 0) {
				var.addMod(DAWNSTAR_P);
				var.addMod(DAWNSTAR_PH);
			}
			else if (mod == 1) {
				var.addMod(DAWNSTAR_N);
				var.addMod(DAWNSTAR_NH);
			}
			else {
				var.addMod(DAWNSTAR_E);
				var.addMod(DAWNSTAR_EH);
			}
			var.addMod(DAWNSTAR_CONTROLLER);
		}
	}
	
	//check if drones are valid, can only have first squad installed and it must be Nimbus variant
	private boolean isValidDrone(String id, int i) {
		if (id == null) return true;
		else if (id != null && i == 0) {
			if (id.equals(NIMBUS_REM) || id.startsWith(NIMBUS_PREF)) return true;
			else return false;
		}
		
		else return false;
	}
	
	//check if equipment has gone over fitting availability
	private int getRemainingOP(MutableShipStatsAPI stats) {
		int fullOP = 45;
		if (stats.getVariant().hasHullMod(RADIANT_HULLMOD)) fullOP = 320;
		int unusedOP = 0;
		try {
			unusedOP = fullOP - stats.getVariant().computeOPCost(Global.getSector().getCharacterData().getPerson().getFleetCommanderStats()); 
		}
		catch (Exception e) {
			unusedOP = fullOP - stats.getVariant().computeOPCost(Global.getFactory().createPerson().getFleetCommanderStats());
		}
		return unusedOP;
	}
	
	@Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			for (CargoStackAPI s : cargo.getStacksCopy()) {
				if (s.isFighterWingStack()) {
					String wingId = s.getFighterWingSpecIfWing().getVariantId();
					if (!wingId.equals(NIMBUS_WING) && wingId.startsWith(NIMBUS_PREF)) cargo.removeStack(s);
				}
			}
		}
		catch (Exception e) {}
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {		
		if (isForModSpec || ship == null) return;
		
		if (!ship.isFrigate() && !ship.getVariant().hasHullMod(MAQUECH_HULLMOD)) {
			String d = "This ship cannot be modified to field standard strike craft, but can install a %s that grants access to one or more %s.";
			tooltip.addPara(d, 10f, Misc.getHighlightColor(), TW_MOD_NAME, TW_DRONE_NAME);
		}
		else if (ship.getVariant().hasHullMod(MAQUECH_HULLMOD)) {
			String d = "This ship is equipped with wings of %s by default, although it can operate standard strike craft in their place.";
			tooltip.addPara(d, 10f, Misc.getHighlightColor(), TW_DRONE_NAME);
		}
		else if (ship.isFrigate()) tooltip.addPara("This ship is too small to install a %s.", 10f, Misc.getHighlightColor(), TW_MOD_NAME);
		

		String shipId = ship.getHullSpec().getHullId();
		boolean isEqualizer = ship.getVariant().hasHullMod(EQUALIZER_HULLMOD);
		boolean isGlycon = shipId.equals(GLYCON_TW_ID) || shipId.equals(GLYCON_TW_D_ID);
		boolean isIonos = shipId.equals(IONOS_TW_ID) || shipId.equals(IONOS_TW_D_ID);
		boolean isRadiant = ship.getVariant().hasHullMod(RADIANT_HULLMOD);
		boolean isShrike = shipId.equals(SHRIKE_TW_ID) || shipId.equals(SHRIKE_TW_D_ID);
		String header = "Special Modifier:";
		if (isEqualizer) {
			String s = "%s The engines can engage a supercruise mode that grants the 0-flux speed bonus when the ship is below %s flux.";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), header, "" + (int) ZERO_FLUX_LEVEL + "%");
		}
		else if (isGlycon) {
			String s = "%s Due to a lack of advanced missile nanoforge components onboard, the ship deployment cost is reduced by %s.";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), header, "" + (int) DP_REDUCTION);
		}
		else if (isIonos) {
			String s = "%s The complexity of the ship's upgraded large weapon mounts have increased its deployment and maintenance cost by %s.";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), header, "" + (int) DP_INCREASE_IONOS + "%");
		}
		else if (isRadiant) {
			String s = "%s Compatible with the %s and %s hullmods. Equipping either hullmod will alter the ship's system.";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), header, FTR_HULLMOD_DISPLAY, GRAV_HULLMOD_DISPLAY);
		}
		else if (isShrike) {
			String s = "%s The increased complexity from the built-in hullmod and weapons aboard this ship has increased its deployment cost by %s.";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), header, "" + (int) DP_INCREASE);
		}
		return;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) speedBoost.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) speedBoost.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) speedBoost.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) speedBoost.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return "" + (int) Math.round((1f - RANGE_MULT) * 100f) + "%";
		if (index == 5) return "" + (int) REPAIR_PENALTY + "%";
		return null;
	}
}