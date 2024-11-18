package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.Color;

public class istl_SlammerJetsStats extends BaseShipSystemScript
{
    // -- Base size for particles -------------------------------------------
    private static final float PARTICLE_BASE_SIZE = 2f;
    // -- Base duration of particles -----------------------------------------
    private static final float PARTICLE_BASE_DURATION = 0.4f;
    // -- Base chance to spawn particles this frame --------------------------
    private static final float PARTICLE_BASE_CHANCE = 0.5f;
    // -- Base brightness of particles -----------------------------------------
    private static final float PARTICLE_BASE_BRIGHTNESS = 8.0f;
    // -- Multiplier for particle velocity -----------------------------------
    private static final float PARTICLE_VELOCITY_MULT = 0.3f;
    // -- Maximum angle from the engine vector for particle velocity ----------
    private static final float CONE_ANGLE = 45f;
    // -- Color of skipjets when fully spooled up ----------------------------
    private static final Color COLOR_FULL = new Color(105,255,205,255);
    
    // an instance of SkipjetParticleFX customized for this system
    private static final SkipjetParticleFX myParticleFX = new SkipjetParticleFX(
            PARTICLE_BASE_SIZE,
            PARTICLE_BASE_DURATION,
            PARTICLE_BASE_BRIGHTNESS,
            PARTICLE_BASE_CHANCE,
            PARTICLE_VELOCITY_MULT,
            CONE_ANGLE,
            COLOR_FULL
    );

    public void apply(MutableShipStatsAPI stats,
            String id,
            State state,
            float effectLevel)
    {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().modifyFlat(id, 0f);
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 150f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 200f);
        }
        else
        {
            stats.getMaxSpeed().modifyFlat(id, 180f * effectLevel);
            stats.getMaxSpeed().modifyPercent(id, 30f * effectLevel);
            stats.getAcceleration().modifyPercent(id, 1000f * effectLevel);
            stats.getDeceleration().modifyPercent(id, 350f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 600f * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 60f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 120f * effectLevel);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id)
    {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel)
    {
        if (index == 0)
        {
            return new StatusData("improved maneuverability", false);
        }
        else if (index == 1)
        {
            return new StatusData("increased top speed", false);
        }
        return null;
    }
}
