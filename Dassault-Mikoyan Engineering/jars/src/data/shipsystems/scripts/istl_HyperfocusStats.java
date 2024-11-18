package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class istl_HyperfocusStats extends BaseShipSystemScript
{

	public static final float DAMAGE_BONUS_PERCENT = 30f;
        public static final float RANGE_BONUS_PERCENT = 60f;
        public static final float MANEUVER_MALUS_PERCENT = 30f;

		//public static final float EXTRA_DAMAGE_TAKEN_PERCENT = 100f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
		
                float rangePercent = RANGE_BONUS_PERCENT * effectLevel;
                stats.getBeamWeaponRangeBonus().modifyPercent(id, rangePercent);
                
                float thresholdPercent = RANGE_BONUS_PERCENT * effectLevel;
		stats.getWeaponRangeThreshold().modifyPercent(id, thresholdPercent);
                
		stats.getAcceleration().modifyPercent(id, MANEUVER_MALUS_PERCENT * 3f);
		stats.getDeceleration().modifyPercent(id, MANEUVER_MALUS_PERCENT);
		stats.getTurnAcceleration().modifyPercent(id, MANEUVER_MALUS_PERCENT * 3f);
		stats.getMaxTurnRate().modifyPercent(id, MANEUVER_MALUS_PERCENT);
                
		//float damageTakenPercent = EXTRA_DAMAGE_TAKEN_PERCENT * effectLevel;
//		stats.getArmorDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getHullDamageTakenMult().modifyPercent(id, damageTakenPercent);
//		stats.getShieldDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getWeaponDamageTakenMult().modifyPercent(id, damageTakenPercent);
		//stats.getEngineDamageTakenMult().modifyPercent(id, damageTakenPercent);
		
		//stats.getBeamWeaponFluxCostMult().modifyMult(id, 10f);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getBeamWeaponRangeBonus().unmodify(id);
		stats.getWeaponRangeThreshold().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
//		stats.getArmorDamageTakenMult().unmodify(id);
//		stats.getHullDamageTakenMult().unmodify(id);
//		stats.getShieldDamageTakenMult().unmodify(id);
//		stats.getWeaponDamageTakenMult().unmodify(id);
//		stats.getEngineDamageTakenMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		float rangePercent = RANGE_BONUS_PERCENT * effectLevel;
		float speedPercent = MANEUVER_MALUS_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% energy weapon damage" , false);
		} else if (index == 1) {
			return new StatusData("+" + (int) rangePercent + "% beam weapon range", false);
		} else if (index == 2) {
			return new StatusData("-" + (int) speedPercent + "% turning and acceleration", false);
		} else if (index == 3) {
			return null;
		}
		return null;
	}
}
