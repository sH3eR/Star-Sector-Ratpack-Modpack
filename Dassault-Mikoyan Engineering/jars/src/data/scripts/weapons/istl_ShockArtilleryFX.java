package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.DMEUtils;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class istl_ShockArtilleryFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final float OFFSET = 18f;
    //Explosion flash
    private static final Color FLASH_COLOR = new Color(140,125,255,255);
    private static final float FLASH_SIZE = 60f;
    private static final float FLASH_DUR = 0.2f;
    //Particle stream
    private static final float FIRE_DURATION = 0.24f; // Firing cycle time
    private static final float PARTICLE_COUNT = 9f; // Base particle count
    private static final Color PARTICLE_COLOR = new Color(75,100,255,200); // Particle color

    private float elapsed = 0f;

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        float ship_facing = ship.getFacing();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f muzzle_location = MathUtils.getPointOnCircumference(weapon_location, OFFSET, ship_facing);

        Vector2f explosion_offset = DMEUtils.translate_polar(weapon_location, (OFFSET + 4f) + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, FLASH_SIZE, FLASH_DUR);
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
            Vector2f particle_offset = DMEUtils.translate_polar(weapon_location, OFFSET, weapon.getCurrAngle());
            float size, speed, angle;
            Vector2f velocity;
            // more particles to start with, fewer later on
            int particle_count_this_frame = (int) (PARTICLE_COUNT * (FIRE_DURATION - elapsed));
            for (int x = 0; x < particle_count_this_frame; x++) {
                size = DMEUtils.get_random(3f, 9f);
                speed = DMEUtils.get_random(75f, 150f);
                angle = weapon.getCurrAngle() + DMEUtils.get_random(-45f, 45f);
                velocity = DMEUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.5f, 0.6f, PARTICLE_COLOR);
            }
        } else {
            elapsed = 0f;
        }
    }
}
