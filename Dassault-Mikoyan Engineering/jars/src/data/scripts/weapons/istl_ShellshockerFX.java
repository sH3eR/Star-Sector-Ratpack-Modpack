package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class istl_ShellshockerFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final Color NEBULA_COLOR = new Color(100,110,255,255);
    private static final float NEBULA_SIZE = 9f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 11f;
    private static final float NEBULA_DUR = 0.75f;
    private static final float NEBULA_RAMPUP = 0.15f;
    
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
        engine.addSmoothParticle(proj_location, ship_velocity, NEBULA_SIZE * 2, 0.75f, NEBULA_RAMPUP, NEBULA_DUR / 2, NEBULA_COLOR);
        engine.spawnExplosion(proj_location, ship_velocity, NEBULA_COLOR, (NEBULA_SIZE * 2.5f), (NEBULA_DUR * 0.6f));
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
    }
}
