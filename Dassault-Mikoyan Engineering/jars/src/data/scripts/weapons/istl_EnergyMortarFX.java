package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class istl_EnergyMortarFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final Color BOOM_COLOR = new Color(50,225,200,255);
    private static final Color NEBULA_COLOR = new Color(75,255,175,255);
    private static final float NEBULA_SIZE = 4f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 12f;
    private static final float NEBULA_DUR = 0.45f;
    private static final float NEBULA_RAMPUP = 0.1f;
    
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
        engine.addSmoothParticle(proj_location, ship_velocity, NEBULA_SIZE * 5, 0.75f, NEBULA_RAMPUP, NEBULA_DUR / 2, BOOM_COLOR);
        engine.spawnExplosion(proj_location, ship_velocity,  BOOM_COLOR, NEBULA_SIZE * 7, NEBULA_DUR * 0.75f);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
       //do nothing here
    }
}