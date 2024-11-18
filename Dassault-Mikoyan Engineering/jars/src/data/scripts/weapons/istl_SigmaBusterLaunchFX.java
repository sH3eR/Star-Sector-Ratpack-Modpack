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

public class istl_SigmaBusterLaunchFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final float OFFSET = 0f; // Offset on weapon sprite; shoud be set to turret offset
    private static final Color BOOM_COLOR = new Color(50,225,200,255);
    private static final Color NEBULA_COLOR = new Color(75,255,175,255);
    private static final float NEBULA_SIZE = 7f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 15f;
    private static final float NEBULA_DUR = 0.45f;
    private static final float NEBULA_RAMPUP = 0.1f;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        float ship_facing = ship.getFacing();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f muzzle_location = MathUtils.getPointOnCircumference(weapon_location, OFFSET, ship_facing);

        Vector2f explosion_offset = DMEUtils.translate_polar(weapon_location, OFFSET + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        engine.addSwirlyNebulaParticle(explosion_offset,
            ship_velocity,
            NEBULA_SIZE,
            NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0.2f,
            NEBULA_DUR,
            NEBULA_COLOR,
            true
        );
        engine.addSmoothParticle(explosion_offset, ship_velocity, NEBULA_SIZE * 4, 0.75f, NEBULA_RAMPUP, NEBULA_DUR * 0.25f, BOOM_COLOR);
        engine.spawnExplosion(explosion_offset, ship_velocity,  BOOM_COLOR, NEBULA_SIZE * 6, NEBULA_DUR / 2);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
       //do nothing here
    }
}