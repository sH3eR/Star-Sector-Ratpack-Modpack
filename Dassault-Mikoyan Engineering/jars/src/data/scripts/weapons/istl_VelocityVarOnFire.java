package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

public class istl_VelocityVarOnFire implements OnFireEffectPlugin {

    //Velocity values
    private static final float VELOCITYMIN = 0.92f;
    private static final float VELOCITYMAX = 1.08f;
           
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        projectile.getVelocity().scale(MathUtils.getRandomNumberInRange(VELOCITYMIN, VELOCITYMAX));
    }
}