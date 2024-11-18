package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class MSS_attitudeStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        stats.getMaxSpeed().modifyFlat(id, 25f);
        stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
        stats.getDeceleration().modifyPercent(id, 150f * effectLevel);
        stats.getTurnAcceleration().modifyFlat(id, 30f * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
        stats.getMaxTurnRate().modifyFlat(id, 15f * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
        
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
                return new StatusData("improved turn rate", false);
        } else if (index == 1) {
                return new StatusData("+25 top speed", false);
        }
        return null;
    }
}
