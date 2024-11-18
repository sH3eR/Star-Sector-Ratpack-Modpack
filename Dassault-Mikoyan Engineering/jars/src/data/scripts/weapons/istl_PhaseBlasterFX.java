package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.DMEUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class istl_PhaseBlasterFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    //Nebula particles
    private static final Color NEBULA_COLOR = new Color(15,255,135,200);
    private static final float NEBULA_SIZE = 6f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 9f;
    private static final float NEBULA_DUR = 0.5f;
    private static final float NEBULA_RAMPUP = 0.1f;
    //Flash color
    private static final Color FLASH_COLOR = new Color(0,100,155,255);
    //Particle stream
    private static final float OFFSET = 6f; // Offset on weapon sprite; shoud be set to turret offset
    private static final float FIRE_DURATION = 0.2f; // Firing cycle time
    private static final float PARTICLE_COUNT = 20f; // Base particle count
    private static final Color PARTICLE_COLOR = new Color(75,255,155,255); // Particle color

    private float elapsed = 0f;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        // do visual fx
        engine.addSwirlyNebulaParticle(proj_location,
            ship_velocity,
            NEBULA_SIZE,
            NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0.2f,
            NEBULA_DUR,
            NEBULA_COLOR,
            true
        );
        engine.addSmoothParticle(proj_location,
            ship_velocity,
            NEBULA_SIZE * 4,
            0.75f,
            NEBULA_RAMPUP,
            NEBULA_DUR / 2,
            FLASH_COLOR
        );
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        if (weapon.isFiring()) {
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();

            elapsed += amount;
            // particles
            Vector2f particle_offset = DMEUtils.translate_polar(weapon_location, (OFFSET - 4f), weapon.getCurrAngle());
            float size, speed, angle;
            Vector2f velocity;
            // more particles to start with, fewer later on
            int particle_count_this_frame = (int) (PARTICLE_COUNT * (FIRE_DURATION - elapsed));
            for (int x = 0; x < particle_count_this_frame; x++) {
                size = DMEUtils.get_random(3f, 5f);
                speed = DMEUtils.get_random(5f, 250f);
                angle = weapon.getCurrAngle() + DMEUtils.get_random(-6f, 6f);
                velocity = DMEUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.6f, PARTICLE_COLOR);
            }
        } else {
            elapsed = 0f;
        }
    }
}
