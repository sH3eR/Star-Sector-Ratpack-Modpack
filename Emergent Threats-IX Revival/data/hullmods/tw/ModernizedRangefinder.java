package data.hullmods.tw;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;

public class ModernizedRangefinder extends BaseHullMod {
	
	private static float RANGE_BONUS_BASE = 50f;
	private static float RANGE_BONUS_GOOD = 60f;
	private static float RANGE_BONUS_BEST = 100f;
	private static float RANGE_BONUS_FLAT = 200f;
	private static String THIS_MOD = "tw_modernized_rangefinder";
	private static String CONFLICT_MOD = "magellan_trajectory_analyzer";
	private static String CONFLICT_MOD_2 = "te_specialized_rangefinder";
	private static String DTC_MOD_ID = "dedicated_targeting_core";
	private static String ITU_MOD_ID = "targetingunit";
	private static String ATC_MOD_ID = "advancedcore";
	private static String ARCHAIC = "archaic_c";

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new CompositeRangeModifier());
	}
	
	public static class CompositeRangeModifier implements WeaponRangeModifier {
		public CompositeRangeModifier() {}
		
		public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getType() != WeaponType.COMPOSITE || !weapon.getSpec().hasTag(ARCHAIC)) return 0f;
			if (ship.getVariant().hasHullMod(DTC_MOD_ID)) {
				if (ship.getVariant().getSMods().contains(DTC_MOD_ID)) return RANGE_BONUS_GOOD * 0.01f;
				else return RANGE_BONUS_BASE * 0.01f;
			}
			else if (ship.getVariant().hasHullMod(ITU_MOD_ID)) return RANGE_BONUS_GOOD * 0.01f;
			else if (ship.getVariant().hasHullMod(ATC_MOD_ID)) return RANGE_BONUS_BEST * 0.01f;
			return 0f;
		}
		public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			float bonus = 0f;
			if (!ship.getVariant().getSMods().contains(THIS_MOD)) return bonus;
			if (weapon.getType() == WeaponType.COMPOSITE || weapon.getType() == WeaponType.MISSILE) {
				 return RANGE_BONUS_FLAT;
			}
			return 0f;
		}
		public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		return !ship.getVariant().hasHullMod(CONFLICT_MOD) && !ship.getVariant().hasHullMod(CONFLICT_MOD_2);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(CONFLICT_MOD) && ship.getVariant().hasHullMod(CONFLICT_MOD_2)) {
			return "Comparable system already present";
		}
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "archaic";
		if (index == 1) return "" + (int) RANGE_BONUS_BASE + "%";
		if (index == 2) return "" + (int) RANGE_BONUS_GOOD + "%";
		if (index == 3) return "" + (int) RANGE_BONUS_BEST + "%";
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) RANGE_BONUS_FLAT;
		return null;
	}
}