package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class istl_PulseripperFX implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    //How many rounds per - min and max
    //On a weapon with an even number of barrels, odd numbers are required to make the rounds switch barrels
    public static final int REPLACE_EVERY_MIN = 3;
    public static final int REPLACE_EVERY_MAX = 5;
    //ID of the weapon - not the projectile, but a separate weapon - to use for the tracer
    public static final String REPLACE_WPN_ID = "istl_pulseripper_large";
    //Muzzle flash
    private static final Color FLASH_CORE = new Color(183,95,70,255);
    private static final Color FLASH_FRINGE = new Color(183,95,70,155);    
    private static final float FLASH_SIZE = 20f; //explosion size
    private static final float FLASH_DUR = 0.2f;
    
    private int roundCounter = 0;
    
    @Override
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine)
    {
        //Increment the counter for each shot
        roundCounter++;
        //random shot replace
        int projreplace = MathUtils.getRandomNumberInRange(REPLACE_EVERY_MIN, REPLACE_EVERY_MAX);
        //Spawn a new projectile
        if (roundCounter >= projreplace) {
            roundCounter = 0;
            Vector2f loc = proj.getLocation();
            Vector2f vel = proj.getVelocity();
            DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(),
                    weapon, 
                    REPLACE_WPN_ID,
                    loc,
                    proj.getFacing(), 
                    weapon.getShip().getVelocity()
            );
            //Remove the old projectile
            Global.getCombatEngine().removeEntity(proj);
        }
        // set up for explosions    
        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = proj.getLocation();
        // do visual fx
        engine.addSmoothParticle(proj_location, ship_velocity, FLASH_SIZE * .15f, 1f, 0.3f, FLASH_DUR * 0.75f, FLASH_CORE);
        engine.spawnExplosion(proj_location, ship_velocity, FLASH_FRINGE, FLASH_SIZE, FLASH_DUR);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
       //do nothing here
    }
}