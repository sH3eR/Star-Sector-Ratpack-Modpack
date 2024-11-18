package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class istl_ScatterLaserFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(25,175,255,255); //Color of muzzle flash explosion
    private static final float FLASH_SIZE = 75f; //explosion size
    private static final float FLASH_DUR = 0.2f;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        Vector2f loc = proj.getLocation();
        Vector2f vel = proj.getVelocity();
        int shotCount1 = (4); //Scatter projectiles
        for (int j = 0; j < shotCount1; j++) {
            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(
                                                                         30f, 60f));
            randomVel.x += vel.x;
            randomVel.y += vel.y;
            //spec + "_clone" means this will call the weapon (not projectile! you need a separate weapon) with the id "($projectilename)_clone".
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "istl_scatterlaser_sub", loc, proj.getFacing(),
                                   randomVel);
        }
        int shotCount2 = (1); //Core projectile
        for (int j = 0; j < shotCount2; j++) {
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "istl_scatterlaser_core", loc, proj.getFacing(),
                                   null);
        }
        int shotCount3 = (8); //Micro projectiles
        for (int j = 0; j < shotCount3; j++) {
            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(
                                                                         60f, 120f));
            randomVel.x += vel.x;
            randomVel.y += vel.y;
            //spec + "_clone" means this will call the weapon (not projectile! you need a separate weapon) with the id "($projectilename)_clone".
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "istl_scatterlaser_submicro", loc, proj.getFacing(),
                                   randomVel);
        }
        engine.removeEntity(proj);
        // set up for explosions    
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        // do visual fx
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, FLASH_SIZE, FLASH_DUR);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
       //do nothing here
    }
}