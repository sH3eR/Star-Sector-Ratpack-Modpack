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

public class istl_SigmaPulserFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final float OFFSET = 0f; // Offset on weapon sprite; shoud be set to turret offset
    private static final Color FLASH_COLOR = new Color(75,255,175,255); //Color of muzzle flash explosion
    private static final float FLASH_SIZE = 30f; //Size of muzzle flash explosion
    private static final float FLASH_DUR = 0.1f;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        Vector2f weapon_location = weapon.getLocation();
        ShipAPI ship = weapon.getShip();
        float ship_facing = ship.getFacing();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f muzzle_location = MathUtils.getPointOnCircumference(weapon_location, OFFSET, ship_facing);

        Vector2f explosion_offset = DMEUtils.translate_polar(weapon_location, OFFSET + ((0.05f * 100f) - 2f), weapon.getCurrAngle());
        engine.spawnExplosion(explosion_offset, ship.getVelocity(), FLASH_COLOR, FLASH_SIZE, FLASH_DUR);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
       //do nothing here
    }
}