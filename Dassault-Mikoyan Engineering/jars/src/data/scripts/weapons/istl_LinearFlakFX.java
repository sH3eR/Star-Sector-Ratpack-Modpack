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

public class istl_LinearFlakFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    //Explosion flash
    private static final Color FLASH_COLOR = new Color(255,155,75,225); //Color of muzzle flash explosion
    private static final float FLASH_SIZE = 4f; //explosion size
    private static final float FLASH_DUR = 0.2f;

    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        //shotgun effect
        Vector2f loc = proj.getLocation();
        Vector2f vel = proj.getVelocity();
        int shotCount = (5);
        for (int j = 0; j < shotCount; j++) {
            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(
                                                                         25f, 75f));
            randomVel.x += vel.x;
            randomVel.y += vel.y;
            //spec + "_clone" means this will call the weapon (not projectile! you need a separate weapon) with the id "($projectilename)_clone".
            engine.spawnProjectile(proj.getSource(), proj.getWeapon(), "istl_linearflakshot_clone", loc, proj.getFacing(),
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

    }
}
