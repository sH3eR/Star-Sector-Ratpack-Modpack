package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.Color;

public class istl_WitchJetsStats extends BaseShipSystemScript
{
    // -- Base size for particles -------------------------------------------
    // This is modified slightly based on a random number and the current
    // effect level of the skipjet system.
    private static final float PARTICLE_BASE_SIZE = 2f;
    // -- Base duration of particles -----------------------------------------
    // This increases slightly as the effect level spools up.
    private static final float PARTICLE_BASE_DURATION = 0.4f;
    // -- Base chance to spawn particles this frame --------------------------
    // This chance is checked per engine every frame. It's modified based on
    // the skipjet system's current effect level.
    private static final float PARTICLE_BASE_CHANCE = 0.5f;
    // -- Base brightness of particles -----------------------------------------
    // This increases slightly as the effect level spools up.
    private static final float PARTICLE_BASE_BRIGHTNESS = 8.0f;
    // -- Multiplier for particle velocity -----------------------------------
    // The particle velocity is based on the ship's current velocity,
    // multiplied by this value.
    private static final float PARTICLE_VELOCITY_MULT = 0.3f;

    // -- Maximum angle from the engine vector for particle velocity ----------
    // Particles will be spawned with velocity vectors up to this many degrees
    // from either side of the engine's vector.
    private static final float CONE_ANGLE = 45f;
    // -- Color of skipjets when fully spooled up ----------------------------
    // This should be the color that the skipjet system fades the engines
    // toward.
    // Particle color fades from the engine's base color to this as the effect
    // level increases.
    private static final Color COLOR_FULL = new Color(105,255,205,255);

    // an instance of SkipjetParticleFX customized for this system
    private static final SkipjetParticleFX myParticleFX = new SkipjetParticleFX(
            PARTICLE_BASE_SIZE, PARTICLE_BASE_DURATION, PARTICLE_BASE_BRIGHTNESS,
            PARTICLE_BASE_CHANCE, PARTICLE_VELOCITY_MULT, CONE_ANGLE, COLOR_FULL
    );

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		} else {
			stats.getMaxSpeed().modifyFlat(id, 500f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 1200f * effectLevel);
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("increased engine power", false);
		}
		return null;
	}
}
