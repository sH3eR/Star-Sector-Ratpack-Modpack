package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class istl_GradTBMLaunchFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(255,175,125,255); //Color of muzzle flash explosion
    private static final float FLASH_SIZE = 40f; //Size of muzzle flash explosion
    private static final float FLASH_DUR = 0.15f;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        // set up for explosions    
        ShipAPI ship = weapon.getShip();
        Vector2f proj_location = proj.getLocation();
        Vector2f ship_velocity = ship.getVelocity();
        // do visual fx
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, FLASH_SIZE, FLASH_DUR);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
       //do nothing here
    }
}