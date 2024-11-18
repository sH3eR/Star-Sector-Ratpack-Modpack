package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class istl_PulserMuzzleFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(125,100,255,175); //Color of muzzle flash explosion
    private static final float FLASH_SIZE = 25f; //explosion size
    private static final float FLASH_DUR = 0.12f;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
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