package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static data.scripts.util.MSS_txt.txt;

public class MSS_safetiesStatsCapital extends BaseShipSystemScript {

    public static final float
            ROF_BONUS = 1f,
            BEAM_DAMAGE = 1f,
            RANGE_DROP = 400f;
    public static final float
            FLUX_REDUCTION = 200f,
            FAILURE_RATE = 0.04f;
    private float
            totalPeakTimeLoss = 0f,
            CR_LOSS_MULT = 3f,
            EXTRA_OVERRIDE = 0;

    private static final Map<ShipAPI.HullSize, Float> speed = new HashMap<>(4);
    static {
        speed.put(ShipAPI.HullSize.FRIGATE, 50f);
        speed.put(ShipAPI.HullSize.DESTROYER, 30f);
        speed.put(ShipAPI.HullSize.CRUISER, 20f);
        speed.put(ShipAPI.HullSize.CAPITAL_SHIP, 10f);
    }

    private static final float PEAK_MULT = 0.33f;
    private static final float FLUX_DISSIPATION_MULT = 2f;
    private static final float RANGE_THRESHOLD = 600f;
    private static final float RANGE_MULT = 0.25f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        final ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }
        ShipAPI.HullSize hullSize = ship.getHullSize();

        float amount = Global.getCombatEngine().getElapsedInLastFrame();

        totalPeakTimeLoss += (CR_LOSS_MULT - 1f) * amount;
        stats.getPeakCRDuration().modifyFlat(id, -totalPeakTimeLoss / ship.getMutableStats().getPeakCRDuration().getMult());

        stats.getMaxSpeed().modifyFlat(id, speed.get(hullSize));
        stats.getAcceleration().modifyFlat(id, speed.get(hullSize) * 2f);
        stats.getDeceleration().modifyFlat(id, speed.get(hullSize) * 2f);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f);

        stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);

        stats.getVentRateMult().modifyMult(id, 0f);

        stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT * effectLevel);

        Color color = new Color(255,100,255,255);
        ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
        ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);

        /*
        stats.getBallisticRoFMult().modifyMult(id, 1f + ROF_BONUS * effectLevel);
        stats.getEnergyRoFMult().modifyMult(id, 1f + ROF_BONUS * effectLevel);
        stats.getBeamWeaponDamageMult().modifyMult(id, 1f + BEAM_DAMAGE * effectLevel);

        float threshold = stats.getWeaponRangeThreshold().getModifiedValue();
        if (threshold == 0) {
            stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_DROP);
        } else if (threshold > RANGE_DROP) {
            float drop = stats.getWeaponRangeThreshold().getModifiedValue() - RANGE_DROP;
            stats.getWeaponRangeThreshold().modifyFlat(id, -drop);
        }
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, 1 - effectLevel);

        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);

        if (((ShipAPI) (stats.getEntity())).getVariant().getHullMods().contains("safetyoverrides")) {
            EXTRA_OVERRIDE = 0.08f;
        } else {
            EXTRA_OVERRIDE = 0f;
        }

        stats.getWeaponMalfunctionChance().modifyFlat(id, (FAILURE_RATE + EXTRA_OVERRIDE) * effectLevel);
        */
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getZeroFluxMinimumFluxLevel().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getVentRateMult().unmodify(id);
        stats.getWeaponRangeThreshold().unmodify(id);
        stats.getWeaponRangeMultPastThreshold().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData((int)Math.round(200*effectLevel)+txt("sstm_lion1"), false);
        }
        if (index == 1) {
            return new StatusData(txt("sstm_switch2") + (int) (1000 - (RANGE_DROP * effectLevel)) + txt("su"), false);
        }
        return null;
    }
}
