package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class UGH_ThrushWarheads extends BaseHullMod {
	public static final float MIS_DUR = 20f;
	public static final float SMODIFIER = 25f;
	public static final float SMODIFIER_AMMO = 20f;
	
        @Override
        public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            boolean sMod = isSMod(stats);
            
            stats.getMissileHealthBonus().modifyMult(id, sMod ? (1f + (SMODIFIER * 0.01f)) : (1f - (MIS_DUR * 0.01f)));
            stats.getMissileAmmoBonus().modifyPercent(id, (sMod ? SMODIFIER_AMMO : 0));
            stats.getMissileMaxSpeedBonus().modifyMult(id, 1.33f);
            stats.getMissileWeaponRangeBonus().modifyPercent(id, 33f);
            stats.getMissileMaxTurnRateBonus().modifyMult(id, 0.5f);
            stats.getMissileTurnAccelerationBonus().modifyMult(id, 0.2f);
            stats.getMissileWeaponFluxCostMod().modifyMult(id, 0.8f);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "1.5";
            if (index == 1) return "33%";
            if (index == 2) return "" + (int) (MIS_DUR) + "%";
            if (index == 3) return "80%";
            if (index == 4) return "20%";
            return null;
        }
        
        @Override
	public String getSModDescriptionParam(int index, HullSize hullSize) {
            if (index == 0) return "" + (int) (SMODIFIER) + "%";
            if (index == 1) return "" + (int) (SMODIFIER_AMMO) + "%";
            return null;
	}
}

			
			