package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class istl_GradMissileAI extends istl_BaseMissile
{
    private static final float ENGINE_DEAD_TIME_MAX = 1f;   // Max time until engine burn starts
    private static final float ENGINE_DEAD_TIME_MIN = 0.5f; // Min time until engine burn starts
    private static final float FIRE_INACCURACY = 2f; // Set-once for entire shot lifetime leading offset
    private static final float AIM_THRESHOLD = 0.25f; // Multiplied by collision radius, how much it can be off by when deciding to MIRV
    private static final float MIRV_DISTANCE = 600f;
    private static final float TIME_BEFORE_CAN_MIRV = 1.0f; // Min time before can MIRV
    private static final float FLARE_OFFSET = -9f; // Set to engine location matched to missile projectile file
    private static final Color FLARE_COLOR = new Color(200, 165, 55, 255);
    private static final Color SMOKE_COLOR = new Color(155, 145, 135, 150);
    private static final boolean STAGE_ONE_EXPLODE = true;
    private static final boolean STAGE_ONE_FLARE = false; // Glow particle visual when second stage is litup
    //private static final boolean STAGE_ONE_TRANSFER_DAMAGE = false; // Only used for missile submunitions, which this is not
    private static final boolean STAGE_ONE_TRANSFER_MOMENTUM = true;
    private static final float SUBMUNITION_VELOCITY_MOD_MAX = 250f; // Max fudged extra velocity added to the submunitions
    private static final float SUBMUNITION_VELOCITY_MOD_MIN = 50f; // Min fudged extra velocity added to the submunitions
    private static final int NUMBER_SUBMUNITIONS = 4;
    private static final float SUBMUNITION_RELATIVE_OFFSET = 6f; // How much each submunition's aim point is offset relative to others
    private static final float SUBMUNITION_INACCURACY = 1f; // How much much random offset from the ^ aim point
    private static final String STAGE_TWO_WEAPON_ID = "istl_TBM_subForAI";
    private static final String STAGE_TWO_SOUND_ID = "devastator_explosion";
    private static final float VELOCITY_DAMPING_FACTOR = 0.15f;
    private static final float WEAVE_FALLOFF_DISTANCE = 1500f; // Weaving stops entirely at 0 distance
    private static final float WEAVE_SINE_A_AMPLITUDE = 16f; // Degrees offset
    private static final float WEAVE_SINE_A_PERIOD = 8f;
    private static final float WEAVE_SINE_B_AMPLITUDE = 32f; // Degrees offset
    private static final float WEAVE_SINE_B_PERIOD = 16f;
    private static final Vector2f ZERO = new Vector2f();
    private float engineDeadTimer;
    private float timeAccum = 0f;
    private final float weaveSineAPhase;
    private final float weaveSineBPhase;
    private final float inaccuracy;
    private boolean readyToFly = false;
    protected final float eccmMult;

    public istl_GradMissileAI(MissileAPI missile, ShipAPI launchingShip)
    {
        super(missile, launchingShip);

        weaveSineAPhase = (float) (Math.random() * Math.PI * 2.0);
        weaveSineBPhase = (float) (Math.random() * Math.PI * 2.0);

        engineDeadTimer = MathUtils.getRandomNumberInRange(ENGINE_DEAD_TIME_MIN, ENGINE_DEAD_TIME_MAX);

        eccmMult = 0.5f; // How much ECCM affects FIRE_INACCURACY

        inaccuracy = MathUtils.getRandomNumberInRange(-FIRE_INACCURACY, FIRE_INACCURACY);
    }

    public float getInaccuracyAfterECCM()
    {
        float eccmEffectMult = 1;
        if (launchingShip != null)
        {
            eccmEffectMult = 1 - eccmMult * launchingShip.getMutableStats().getMissileGuidance().getModifiedValue();
        }
        if (eccmEffectMult < 0)
        {
            eccmEffectMult = 0;
        }

        return inaccuracy * eccmEffectMult;
    }

    /**
     * Returns true if a line extrapolated {@code distance} ahead of the
     * missile's current position intersects a circle centered on the target's
     * midpoint. The circle's radius is equal to its collision radius *
     * {@code AIM_THRESHOLD}.
     *
     * @param missilePos
     * @param targetPos Target position (not necessarily the target ship's
     * actual position, more usually the computed intercept point)
     * @param distance Distance between missile and target.
     * @param heading Missile's heading.
     * @param radius Target's collision radius.
     * @return
     */
    public boolean isWithinMIRVAngle(Vector2f missilePos, Vector2f targetPos,
            float distance, float heading, float radius)
    {
        Vector2f endpoint = MathUtils.getPointOnCircumference(missilePos, distance, heading);
        radius = radius * AIM_THRESHOLD;

        return CollisionUtils.getCollides(missilePos, endpoint, targetPos, radius);
    }

    @Override
    public void advance(float amount)
    {
        if (Global.getCombatEngine().isPaused())
        {
            return;
        }

        if (missile.isFading() || missile.isFizzling())
        {
            return;
        }

        boolean mirvNow = false;

        // Do not fly forwards until we have finished engineDeadTimer
        if (!readyToFly)
        {
            if (engineDeadTimer > 0f)
            {
                engineDeadTimer -= amount;
                if (engineDeadTimer <= 0f)
                {
                    readyToFly = true;
                }
            }
        }

        timeAccum += amount;

        // If we have a valid target, turn to face desired intercept point
        if (acquireTarget(amount))
        {
            float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
            Vector2f guidedTarget = intercept(missile.getLocation(), missile.getMaxSpeed(), target.getLocation(), target.getVelocity());
            if (guidedTarget == null)
            {
                Vector2f projection = new Vector2f(target.getVelocity());
                float scalar = distance / (missile.getVelocity().length() + 1f);
                projection.scale(scalar);
                guidedTarget = Vector2f.add(target.getLocation(), projection, null);
            }

            float weaveSineA = WEAVE_SINE_A_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_A_PERIOD) + weaveSineAPhase);
            float weaveSineB = WEAVE_SINE_B_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_B_PERIOD) + weaveSineBPhase);
            float weaveOffset = (weaveSineA + weaveSineB) * Math.min(1f, distance / WEAVE_FALLOFF_DISTANCE);

            float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget) + getInaccuracyAfterECCM() + weaveOffset));
            float absDAng = Math.abs(angularDistance);

            // Apply thrust, but only if engine dead time is over
            if (readyToFly)
            {
                missile.giveCommand(ShipCommand.ACCELERATE);
            }

            missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

            if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)
            {
                missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
            }

            float neededDist = MIRV_DISTANCE + target.getCollisionRadius() + missile.getCollisionRadius();

            // MIRV when we have a target and are in range
            if ((timeAccum >= TIME_BEFORE_CAN_MIRV) && (target.getCollisionClass() != CollisionClass.NONE)
                    && (distance <= neededDist)
                    && isWithinMIRVAngle(missile.getLocation(), guidedTarget, distance,
                            missile.getFacing(), target.getCollisionRadius()))
            {
                mirvNow = true;
            }
        }

        // Launch submunitions
        if (mirvNow)
        {
            Vector2f submunitionVelocityMod = new Vector2f(0, MathUtils.getRandomNumberInRange(
                    SUBMUNITION_VELOCITY_MOD_MAX, SUBMUNITION_VELOCITY_MOD_MIN));

            float initialOffset = -(NUMBER_SUBMUNITIONS - 1) / 2f * SUBMUNITION_RELATIVE_OFFSET;
            DamagingProjectileAPI submunition = null;
            for (int i = 0; i < NUMBER_SUBMUNITIONS; i++)
            {
                float angle = missile.getFacing() + initialOffset + i * SUBMUNITION_RELATIVE_OFFSET
                        + MathUtils.getRandomNumberInRange(-SUBMUNITION_INACCURACY, SUBMUNITION_INACCURACY);
                if (angle < 0f)
                {
                    angle += 360f;
                }
                else if (angle >= 360f)
                {
                    angle -= 360f;
                }

                Vector2f vel = STAGE_ONE_TRANSFER_MOMENTUM ? missile.getVelocity() : ZERO;
                Vector2f boost = VectorUtils.rotate(submunitionVelocityMod, missile.getFacing());
                vel.translate(boost.x, boost.y);
                submunition = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(launchingShip,
                        missile.getWeapon(), STAGE_TWO_WEAPON_ID,
                        missile.getLocation(), angle, vel);
                submunition.setFromMissile(true);
            }

            // Only used for missile submunitions, which this is not
            /*
		// Transfer any damage the missile has incurred if so desired
		if (STAGE_ONE_TRANSFER_DAMAGE)
		{
			submunition.setEmpResistance(missile.getEmpResistance());
			float damageToDeal = missile.getMaxHitpoints() - missile.getHitpoints();
			if (damageToDeal > 0f)
			{
				Global.getCombatEngine().applyDamage(submunition, missile.getLocation(), damageToDeal,
						DamageType.FRAGMENTATION, 0f, true, false, missile.getSource());
			}
		}
             */
            Global.getSoundPlayer().playSound(STAGE_TWO_SOUND_ID, 1f, 1f, missile.getLocation(), missile.getVelocity());

            // GFX on the spot of the switcheroo if desired
            // Remove old missile
            if (STAGE_ONE_EXPLODE)
            {
                Global.getCombatEngine().addSmokeParticle(missile.getLocation(), missile.getVelocity(), 60f, 0.75f, 0.75f, SMOKE_COLOR);
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f,
                        DamageType.FRAGMENTATION, 0f, false, false, missile);
            }
            else if (STAGE_ONE_FLARE)
            {
                Vector2f offset = new Vector2f(FLARE_OFFSET, 0f);
                VectorUtils.rotate(offset, missile.getFacing(), offset);
                Vector2f.add(offset, missile.getLocation(), offset);
                Global.getCombatEngine().addHitParticle(offset, missile.getVelocity(), 100f, 0.5f, 0.25f, FLARE_COLOR);
                Global.getCombatEngine().removeEntity(missile);
            }
            else
            {
                Global.getCombatEngine().removeEntity(missile);
            }
        }
    }

    @Override
    protected boolean acquireTarget(float amount)
    {
        // If our current target is totally invalid, look for a new one
        if (!isTargetValid(target))
        {
            if (target instanceof ShipAPI)
            {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive())
                {
                    // We were locked onto a ship that has now phased, do not attempt to acquire a new target
                    return false;
                }
            }
            // Look for a target that is not a drone or fighter, if available
            setTarget(findBestTarget(false));
            // No such target, look again except this time we allow drones and fighters
            if (target == null)
            {
                setTarget(findBestTarget(true));
            }
            if (target == null)
            {
                return false;
            }
        }

        // If our target is valid but a drone or fighter, see if there's a bigger ship we can aim for instead
        else
        {
            if (isDroneOrFighter(target))
            {
                if (target instanceof ShipAPI)
                {
                    ShipAPI ship = (ShipAPI) target;
                    if (ship.isPhased() && ship.isAlive())
                    {
                        // We were locked onto a ship that has now phased, do not attempt to acquire a new target
                        return false;
                    }
                }
                CombatEntityAPI newTarget = findBestTarget();
                if (newTarget != null)
                {
                    target = newTarget;
                }
            }
        }
        return true;
    }

    @Override
    protected ShipAPI findBestTarget()
    {
        return findBestTarget(false);
    }

    /**
     * This is some bullshit weighted random picker that favors larger ships
     *
     * @param allowDroneOrFighter True if looking for an alternate target
     * (normally it refuses to target fighters or drones)
     * @return
     */
    protected ShipAPI findBestTarget(boolean allowDroneOrFighter)
    {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++)
        {
            ShipAPI tmp = ships.get(i);
            float mod;
            // This is a valid target if:
            //   It is NOT a (drone or fighter), OR we're in alternate mode
            //   It passes the valid target check
            boolean valid = allowDroneOrFighter || !isDroneOrFighter(target);
            valid = valid && isTargetValid(tmp);
            if (!valid)
            {
                continue;
            }
            else
            {
                switch (tmp.getHullSize())
                {
                    default:
                    case FIGHTER:
                        mod = 1f;
                        break;
                    case FRIGATE:
                        mod = 10f;
                        break;
                    case DESTROYER:
                        mod = 50f;
                        break;
                    case CRUISER:
                        mod = 100f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 125f;
                        break;
                }
            }
            weight = (4000f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight)
            {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    protected boolean isDroneOrFighter(CombatEntityAPI target)
    {
        if (target instanceof ShipAPI)
        {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isFighter() || ship.isDrone())
            {
                return true;
            }
        }
        return false;
    }
}
