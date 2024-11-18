package data.hullmods.ix;

import java.util.Collection;
import java.util.Random;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.ix.DModHandler;

public class NinthHullmod extends BaseHullMod {

	private static float ZERO_FLUX_SPEED_BOOST = 5f;
	private static float SHIELD_UPGRADES = 35f;
	private static float ACCELERATION_BONUS = 15f;
	private static float CR_PENALTY = 25f;
	private static String DUPLICATE = "ae_ninth";
	private static String HAS_RESET_HULLMOD = "ix_smod_handler";
	
	private static String SENTINEL_S = "ix_point_defense_small";
	private static String SENTINEL_SH = "ix_point_defense_small_handler";
	private static String SENTINEL_M = "ix_point_defense_medium";
	private static String SENTINEL_MH = "ix_point_defense_medium_handler";
	private static String SMOD_TAG = "variant_always_retain_smods_on_salvage";

	private static String DAWNSTAR_P = "ix_dawnstar_proton";
	private static String DAWNSTAR_PH = "ix_dawnstar_proton_handler";
	private static String DAWNSTAR_N = "ix_dawnstar_neutron";
	private static String DAWNSTAR_NH = "ix_dawnstar_neutron_handler";
	private static String DAWNSTAR_E = "ix_dawnstar_electron";
	private static String DAWNSTAR_EH = "ix_dawnstar_electron_handler";
	private static String DAWNSTAR_CONTROLLER = "ix_dawnstar_controller";
	
	private static String CPB_L_ID = "dawnstar_lcpb_ix";
	private static String CPB_H_ID = "dawnstar_hcpb_ix";

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_SPEED_BOOST);
		stats.getShieldUnfoldRateMult().modifyMult(id, 1f + SHIELD_UPGRADES * 0.01f);
		stats.getShieldTurnRateMult().modifyMult(id, 1f + SHIELD_UPGRADES * 0.01f);
		stats.getAcceleration().modifyMult(id, 1f + ACCELERATION_BONUS * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f + ACCELERATION_BONUS * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f + ACCELERATION_BONUS * 0.01f);
		stats.getCRLossPerSecondPercent().modifyMult(id, 1f + CR_PENALTY * 0.01f);
		stats.getVariant().getSMods().remove(DUPLICATE);
		stats.getVariant().getPermaMods().remove(DUPLICATE);
		stats.getVariant().getHullMods().remove(DUPLICATE);
		stats.getVariant().addTag(SMOD_TAG);
		DModHandler.clearDModsFromStrikeFleetShip(stats);
		
		//adds pd mode to Flamebreaker (IX) empty hulls sold on the market
		if (stats.getVariant().getHullSpec().getHullId().startsWith("flamebreaker_ix") 
					&& !stats.getVariant().hasHullMod(SENTINEL_S) 
					&& !stats.getVariant().hasHullMod(SENTINEL_M)) {
			stats.getVariant().addMod(SENTINEL_MH);
		}
		
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
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {		
		if (isForModSpec || ship == null) return;
		if (ship.getVariant().hasHullMod(HAS_RESET_HULLMOD)) {
			String s = "This ship can remove its s-mods one time. Activate by applying the %s hullmod.";
			tooltip.addPara(s, 10f, Misc.getHighlightColor(), "System Reset");
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ZERO_FLUX_SPEED_BOOST;
		if (index == 1) return "" + (int) SHIELD_UPGRADES + "%";
		if (index == 2) return "" + (int) ACCELERATION_BONUS + "%";
		if (index == 3) return "" + (int) CR_PENALTY + "%";
		return null;
	}
}