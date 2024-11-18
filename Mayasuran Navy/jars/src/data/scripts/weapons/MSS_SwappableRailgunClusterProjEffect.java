package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import data.scripts.util.MSS_utils;

import java.awt.Color;
import java.util.List;

public class MSS_SwappableRailgunClusterProjEffect extends BaseEveryFrameCombatPlugin
{

    private final int CLUSTER_SPLIT_COUNT = 5;
    private final String CLUSTER_SPLIT_PROJECTILE = "MSS_thumper"; 
    private final float SPLIT_VEL_RANDOMNESS = 50f;
    private final float SPLIT_ANGLE_RANDOMNESS = 60f;
    private final float ARMING_TIME = 0.5f;
    private final float CONE_ANGLE = 30f;
    private final float CONE_RANGE = 250f;

    private DamagingProjectileAPI proj;
    private float timer, splitDuration;
    private boolean wentBoom;
    private CombatEngineAPI engine = Global.getCombatEngine();

    // THIS SCRIPT IS FOR THE PROX FUSE
    // the sticky code is in the grenade AI script, onhit script, and weapon everyframe script

    public MSS_SwappableRailgunClusterProjEffect(DamagingProjectileAPI proj) 
    {
        this.proj = proj;
        timer = 0;
        wentBoom = false;
        // split at 80% range
        splitDuration = (proj.getWeapon().getRange() / proj.getMoveSpeed()) * 0.8f;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events)
    {
        if (Global.getCombatEngine() == null)
            return;
        if (Global.getCombatEngine().isPaused())
            return;
        if (wentBoom || proj == null || proj.didDamage() || proj.isFading() || !Global.getCombatEngine().isEntityInPlay(proj))
        {
            Global.getCombatEngine().removePlugin(this);
            return;
        }

        timer += amount;
        if (timer < ARMING_TIME)
            return;
        if (timer > splitDuration)
        {
            explode();
            wentBoom = true;
            return;
        }
        for (CombatEntityAPI enemy : AIUtils.getNearbyEnemies(proj, CONE_RANGE))
        {
            if (MSS_utils.isEntityInArc(enemy, proj.getLocation(), proj.getFacing(), CONE_ANGLE))
            {
                explode();
                wentBoom = true;
                return;
            }
        }
    }

    private void explode()
    {
        // uses a different script for the grenades, since normally they fizzle out if they get out of range of the firing weapon
        engine.addSmoothParticle(proj.getLocation(), proj.getVelocity(), 250, 0.5f, 0.25f, Color.blue);
        engine.addHitParticle(proj.getLocation(), proj.getVelocity(), 100, 1f, 0.1f, Color.white);
        for (int i = 0; i < CLUSTER_SPLIT_COUNT; i++)
        {
            Vector2f randomness = MathUtils.getRandomPointInCircle(null, SPLIT_VEL_RANDOMNESS);
            DamagingProjectileAPI subProj = (DamagingProjectileAPI) engine.spawnProjectile(proj.getSource(), proj.getWeapon(), CLUSTER_SPLIT_PROJECTILE, MathUtils.getRandomPointInCircle(proj.getLocation(), 25f), proj.getFacing(), null);
            VectorUtils.rotate(subProj.getVelocity(), Misc.random.nextFloat() * SPLIT_ANGLE_RANDOMNESS - SPLIT_ANGLE_RANDOMNESS / 2f);
            Vector2f.add(subProj.getVelocity(), randomness, subProj.getVelocity());
            subProj.getVelocity().scale(1.5f);
            engine.addPlugin(new MSS_SwappableRailgunClusterSubProjEffect(subProj, 3f));
        }
        Global.getSoundPlayer().playSound("mine_explosion", 0.7f, 1.0f, proj.getLocation(), proj.getVelocity());
        engine.removeEntity(proj);
        engine.removePlugin(this);
    }
}