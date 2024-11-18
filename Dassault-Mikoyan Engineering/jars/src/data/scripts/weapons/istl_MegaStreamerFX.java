package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.DMEUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.FastTrig;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author HarmfulMechanic
 * Based on scripts by Trylobot, Uomoz and Cycerin
 * Implements Nicke535's beam oscillation script as well
 */
public class istl_MegaStreamerFX implements EveryFrameWeaponEffectPlugin {
    
    private static final float FIRE_DURATION = 2.4f; // Firing cycle time
    private static final Color FLASH_COLOR = new Color(75,255,175,255);
    private static final float OFFSET = 2f;
    private static final Color PARTICLE_COLOR = new Color(75,255,175,255); // Particle color

    private float elapsed = 0f;
    
    private final float oscillationTimePrim = 0.12f;
    private final float oscillationTimeSec = 0.3f;

    //Instantiates variables we will use later
    private float counter = 0f;
    private boolean runOnce = true;
    private Map<Integer, BeamAPI> beamMap = new HashMap<Integer, BeamAPI>();
    private Map<Integer, Float> oscillationWidthMap = new HashMap<Integer, Float>();

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        if (weapon.isFiring()) {
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            // explosion (frame 0 only)
            if (elapsed <= 0f) {
                Vector2f explosion_offset = DMEUtils.translate_polar(weapon_location, OFFSET +
                                                                          ((0.05f * 100f) - 2f), weapon.getCurrAngle());
                engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, 75f, 0.4f);
            }

            elapsed += amount;

            // particles
            Vector2f particle_offset = DMEUtils.translate_polar(weapon_location, OFFSET, weapon.getCurrAngle());
            float size, speed, angle;
            Vector2f velocity;
            // more particles to start with, fewer later on
            int particle_count_this_frame = (int) (7f * (FIRE_DURATION - elapsed));
            for (int x = 0; x < particle_count_this_frame; x++) {
                size = DMEUtils.get_random(3f, 12f);
                speed = DMEUtils.get_random(200f, 400f);
                angle = weapon.getCurrAngle() + DMEUtils.get_random(-7f, 7f);
                velocity = DMEUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.3f, PARTICLE_COLOR);
            }
        } else {
            elapsed = 0f;
        }
        //Don't run if we are paused, or our if weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }
        //Resets the maps and variables if we are not firing
        if (weapon.getChargeLevel() <= 0) {
            counter = 0f;
            beamMap.clear();
            oscillationWidthMap.clear();
            runOnce = true;
            return;
        }
        //If we are firing, start the code and change variables
        if (weapon.getChargeLevel() > 0f && runOnce) {
            int counterForBeams = 0;
            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getWeapon() == weapon) {
                    if (!beamMap.containsValue(beam)) {
                        beamMap.put(counterForBeams, beam);
                        counterForBeams++;
                    }
                }
            }

            if (!beamMap.isEmpty()) {
                runOnce = false;
            }
        }
        //Advances our time counter
        counter += amount;
        //Get the beams, and flicker in sync.
        int counterForBeams = 0;
        for (Integer i : beamMap.keySet()) {
            BeamAPI beam = beamMap.get(i);
            //Instantiates the width map, if necessary
            if (oscillationWidthMap.get(i) == null) {
                oscillationWidthMap.put(i, beam.getWidth());
            }
            //Figures out which oscillation phase the beam should be in: has two parts, with different oscillation frequencies
            float radCountPrim = counter * 2f * (float)Math.PI  / oscillationTimePrim;
            float radCountSec = counter * 2f * (float)Math.PI / oscillationTimeSec;
            float oscillationPhasePrim = ((float)FastTrig.sin(radCountPrim) * 0.4f) + 0.6f;
            float oscillationPhaseSec = ((float)FastTrig.sin(radCountSec) * 0.2f) + 0.8f;
            //Then, we calculate how the visuals of the beam should be modified, depending on oscillation phase
            float visMult = oscillationPhasePrim * oscillationPhaseSec;
            //Finally, modifies our beam depending on the visual multiplier
            beam.setWidth(oscillationWidthMap.get(i) * visMult);
            //For counting which beam we are on
            counterForBeams++;
        }
    }
}
