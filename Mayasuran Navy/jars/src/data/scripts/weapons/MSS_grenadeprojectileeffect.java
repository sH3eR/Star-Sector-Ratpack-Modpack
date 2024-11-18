package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

import data.scripts.util.MSS_utils;

import java.awt.Color;
import java.util.List;

public class MSS_grenadeprojectileeffect extends BaseEveryFrameCombatPlugin
{

    private static final Color EXPLOSION_COLOR = new Color(46, 91, 255);
    private static final float CORE_RADIUS = 175f;
    private static final float RADIUS = 225f;
    private static final float ARMING_TIME = 0.75f;

    private DamagingProjectileAPI proj;
    private float currentTargetDist, previousTargetDist, timer;
    private CombatEntityAPI currentTarget;
    private boolean wentBoom;
    private CombatEngineAPI engine = Global.getCombatEngine();

    // THIS SCRIPT IS FOR THE PROX FUSE
    // the sticky code is in the grenade AI script, onhit script, and weapon everyframe script

    public MSS_grenadeprojectileeffect(DamagingProjectileAPI proj) {
        this.proj = proj;
        currentTarget = null;
        currentTargetDist = 0;
        previousTargetDist = 0;
        timer = 0;
        wentBoom = false;

    }

    @Override
    public void advance(float amount, List<InputEventAPI> events)
    {
        if (wentBoom || proj == null || proj.didDamage() || proj.isFading()
                || !Global.getCombatEngine().isEntityInPlay(proj))
        {
            Global.getCombatEngine().removePlugin(this);
            return;
        }
        if (Global.getCombatEngine() == null)
            return;
        if (Global.getCombatEngine().isPaused())
            return;

        timer += amount;
        currentTarget = AIUtils.getNearestEnemy(proj);
        if (currentTarget != null)
        {
            // only blow up if we're farther away from a target than we used to be, and the target is within the explosion radius
            currentTargetDist = MathUtils.getDistanceSquared(proj.getLocation(), currentTarget.getLocation());
            if (previousTargetDist != 0 && currentTargetDist >= previousTargetDist  && timer > ARMING_TIME && currentTargetDist < RADIUS * RADIUS)
            {
                explode();
                wentBoom = true;
                return;
            }
            previousTargetDist = currentTargetDist;
        }
        boolean forceFade = MathUtils.getDistanceSquared(proj.getLocation(), proj.getWeapon().getLocation()) > proj.getWeapon().getRange() * proj.getWeapon().getRange() * 1.1f;
        if (proj.isFading() || (proj instanceof MissileAPI && ((MissileAPI)proj).isFizzling()) || timer > 8.0f || forceFade)
        {
            engine.addSmoothParticle(proj.getLocation(), proj.getVelocity(), 100, 0.5f, 0.25f, Color.blue);
            engine.addHitParticle(proj.getLocation(), proj.getVelocity(), 100, 1f, 0.1f, Color.white);
            engine.removeEntity(proj);
            Global.getCombatEngine().removePlugin(this);
            return;
        }
    }

    private void explode()
    {
        DamagingExplosionSpec explosionSpec = new DamagingExplosionSpec(0.1f, RADIUS, CORE_RADIUS,
                proj.getDamageAmount(), proj.getDamageAmount() * 0.25f, CollisionClass.MISSILE_FF,
                CollisionClass.MISSILE_FF, 3.0f, 3.0f, 0.25f, 100, EXPLOSION_COLOR, EXPLOSION_COLOR);
        explosionSpec.setDamageType(DamageType.HIGH_EXPLOSIVE);
        engine.spawnDamagingExplosion(explosionSpec, proj.getSource(), proj.getLocation(), false);
        MSS_utils.plasmaEffects(proj);
        Global.getSoundPlayer().playSound("MSS_thumper_explosion", 1.0f, 1.0f, proj.getLocation(), proj.getVelocity());
        engine.removeEntity(proj);
        engine.removePlugin(this);
    }
}
