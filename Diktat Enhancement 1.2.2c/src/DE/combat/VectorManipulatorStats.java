package DE.combat;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

public class VectorManipulatorStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 2f;
	public static final float DAMAGE_BONUS = 1.5f;
	public static final float FLUX_REDUCTION = 100f;
	public static final float RANGE_PENALTY = 100f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		float mult2 = 1f + DAMAGE_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getEnergyWeaponDamageMult().modifyMult(id, mult2);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, -RANGE_PENALTY);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, -RANGE_PENALTY);
		
//		ShipAPI ship = (ShipAPI)stats.getEntity();
//		ship.blockCommandForOneFrame(ShipCommand.FIRE);
//		ship.setHoldFireOneFrame(true);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		float mult2 = 1f + DAMAGE_BONUS * effectLevel;
		float bonusPercent2 = (int) ((mult2 - 1f) * 100f);
		if (index == 0) {
			return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("energy damage +" + (int) bonusPercent2 + "%", false);
		}
		if (index == 2) {
			return new StatusData("weapons flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		if (index == 3) {
			return new StatusData("weapons range -" + (int) RANGE_PENALTY + "%", true);
		}
		return null;
	}
}
