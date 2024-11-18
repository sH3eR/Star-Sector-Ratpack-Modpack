package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class MSS_ForwardBoostStats extends BaseShipSystemScript {

    private static final float MAX_SPEED_BONUS = 50f;
    private static final float MAX_SPEED_PENALTY = 25f;
    private static final float ACCELERATION_BONUS = 400f;

    private float penalty;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        ShipAPI ship = null;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        //Some code magic from DR
        float forwardDir = ship.getFacing();
        float currDir = VectorUtils.getFacing(ship.getVelocity());
        float reverseScale = Math.abs(MathUtils.getShortestRotation(currDir, forwardDir) / 90f);
        if (ship.getVelocity().length() < 1f) {
            reverseScale = 0f;
        }
        if (reverseScale > 1f) {
            /* Going backwards */
            penalty = (MAX_SPEED_BONUS + ((reverseScale - 1f) * MAX_SPEED_PENALTY))*effectLevel;
        } else {
            /* Going forwards */
            penalty = reverseScale * MAX_SPEED_BONUS * effectLevel;
        }

        stats.getMaxSpeed().modifyFlat(id, (MAX_SPEED_BONUS-penalty) * effectLevel);
        stats.getAcceleration().modifyPercent(id, ACCELERATION_BONUS * effectLevel);
        stats.getDeceleration().modifyPercent(id, ACCELERATION_BONUS * effectLevel);
        stats.getTurnAcceleration().modifyMult(id, 1f + 2f * effectLevel);
        stats.getMaxTurnRate().modifyMult(id, 1f + 1f * effectLevel);

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Speed increased by " + (int)(MAX_SPEED_BONUS-penalty) + "su", false);
        }
        return null;
    }

}
