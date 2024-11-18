package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MSS_UnstableOptics extends BaseHullMod {

    public static final float BEAM_RANGE_MINUS = 40f;
    public static final float BEAM_RANGE_PLUS = 40f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBeamPDWeaponRangeBonus().modifyPercent(id, BEAM_RANGE_PLUS);
        stats.getBeamWeaponRangeBonus().modifyPercent(id, - BEAM_RANGE_MINUS);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BEAM_RANGE_MINUS +"%";
        return null;
    }


}
