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

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

import data.scripts.util.MSS_utils;

import java.awt.Color;
import java.util.List;

public class MSS_SwappableRailgunHEProjEffect extends BaseEveryFrameCombatPlugin
{
    // these are used to set the on-hit explosion as well
    public static final Color EXPLOSION_COLOR = Color.red;
    public static final float CORE_RADIUS = 75f;
    public static final float RADIUS = 175f;
    private static final float ARMING_TIME = 0.5f;

    private DamagingProjectileAPI proj;
    private float currentTargetDist, previousTargetDist, timer;
    private CombatEntityAPI currentTarget, previousTarget;
    private boolean wentBoom;
    private CombatEngineAPI engine = Global.getCombatEngine();

    // THIS SCRIPT IS FOR THE PROX FUSE
    // the sticky code is in the grenade AI script, onhit script, and weapon everyframe script

    public MSS_SwappableRailgunHEProjEffect(DamagingProjectileAPI proj) {
        this.proj = proj;
        currentTarget = null;
        previousTarget = null;
        currentTargetDist = 0;
        previousTargetDist = 0;
        timer = 0;
        wentBoom = false;

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
        currentTarget = AIUtils.getNearestEnemy(proj);
        // ignore fighters
        if (currentTarget instanceof ShipAPI && previousTarget instanceof ShipAPI)
        {
            if (((ShipAPI)currentTarget).getHullSize().equals(HullSize.FIGHTER) || ((ShipAPI)previousTarget).getHullSize().equals(HullSize.FIGHTER))
                return;
        }
        // if we're moving away from the closest point on the closest target, and that point is within the explosion radius, go boom
        if (currentTarget != null)
        {
            // this is probably computationally intensive, but there should never be very many of these projectiles
            if (currentTarget instanceof ShipAPI)
                currentTargetDist = MathUtils.getDistanceSquared(proj.getLocation(), MSS_utils.getNearestPointOnShipBounds((ShipAPI)currentTarget, proj.getLocation()));
            else
                currentTargetDist = MathUtils.getDistanceSquared(proj.getLocation(), currentTarget.getLocation());
            if (previousTargetDist != 0 && currentTargetDist >= previousTargetDist && timer > ARMING_TIME && currentTargetDist < RADIUS * RADIUS)
            {
                explode();
                wentBoom = true;
                return;
            }
            previousTargetDist = currentTargetDist;
            previousTarget = currentTarget;
        }
    }

    private void explode()
    {
        DamagingExplosionSpec explosionSpec = new DamagingExplosionSpec(0.1f, RADIUS, CORE_RADIUS,
                proj.getDamageAmount()*0.5f, proj.getDamageAmount() * 0.25f, CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
                CollisionClass.HITS_SHIPS_AND_ASTEROIDS, 3.0f, 3.0f, 0.25f, 100, EXPLOSION_COLOR, EXPLOSION_COLOR);
        explosionSpec.setDamageType(DamageType.HIGH_EXPLOSIVE);
        engine.spawnDamagingExplosion(explosionSpec, proj.getSource(), proj.getLocation(), false);
        Global.getSoundPlayer().playSound("mine_explosion", 1.0f, 1.0f, proj.getLocation(), proj.getVelocity());
        engine.removeEntity(proj);
        engine.removePlugin(this);
    }
}