package data.scripts.weapons.ai;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

//written carefully by MesoTroniK;
//tenderly fucked thither and yon by the capricious winds of fate;
//and gently put to rest at a hopefully-acceptable balance point by HarmfulMechanic

public class istl_Itano_AMM_AI extends istl_BaseMissile
{
    private static final float ENGINE_DEAD_TIME_MAX = 0.2f;// Max time until engine burn starts
    private static final float ENGINE_DEAD_TIME_MIN = 0.12f;// Min time until engine burn starts
    private static final float LEAD_GUIDANCE_FACTOR = 0.6f;
    private static final float LEAD_GUIDANCE_FACTOR_FROM_ECCM = 0.3f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.1f;
    private static final float WEAVE_START_DISTANCE = 1000f; // Weaving starts at this distance and gets stronger the closer it is
    private static final float WEAVE_SINE_A_AMPLITUDE = 18f; // Degrees offset
    private static final float WEAVE_SINE_A_PERIOD = 8f;
    private static final float WEAVE_SINE_B_AMPLITUDE = 30f; // Degrees offset
    private static final float WEAVE_SINE_B_PERIOD = 12f;
    private static final Vector2f ZERO = new Vector2f();
    private boolean aspectLocked = true;
    private float engineDeadTime;
    private float timeAccum = 0f;
    private final float weaveSineAPhase;
    private final float weaveSineBPhase;

    public istl_Itano_AMM_AI(MissileAPI missile, ShipAPI launchingShip)
    {
        super(missile, launchingShip);

        weaveSineAPhase = (float) (Math.random() * Math.PI * 2.0);
        weaveSineBPhase = (float) (Math.random() * Math.PI * 2.0);
        engineDeadTime = MathUtils.getRandomNumberInRange(ENGINE_DEAD_TIME_MIN, ENGINE_DEAD_TIME_MAX);
    }

    @Override
    public void advance(float amount)
    {
        if (missile.isFizzling() || missile.isFading())
        {
            return;
        }

        if (engineDeadTime > 0f)
        {
            engineDeadTime -= amount;
            return;
        }

        timeAccum += amount;

        if (!acquireTarget(amount))
        {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float guidance = LEAD_GUIDANCE_FACTOR;
        if (missile.getSource() != null)
        {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * LEAD_GUIDANCE_FACTOR_FROM_ECCM;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), missile.getVelocity().length(), target.getLocation(), target.getVelocity());
        if (guidedTarget == null)
        {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float weaveSineA = WEAVE_SINE_A_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_A_PERIOD) + weaveSineAPhase);
        float weaveSineB = WEAVE_SINE_B_AMPLITUDE * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_B_PERIOD) + weaveSineBPhase);
        float weaveOffset = (weaveSineA + weaveSineB) * (1f - Math.min(1f, distance / WEAVE_START_DISTANCE));

        float angularDistance;
        if (aspectLocked)
        {
            angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget) + weaveOffset));
        }
        else
        {
            angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                    MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget)));
        }
        float absDAng = Math.abs(angularDistance);

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        if (aspectLocked && absDAng > 60f)
        {
            aspectLocked = false;
        }

        if (!aspectLocked && absDAng <= 45f)
        {
            aspectLocked = true;
        }

        if (aspectLocked)
        {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        if (absDAng < 5)
        {
            float MFlightAng = VectorUtils.getAngle(ZERO, missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20)
            {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)
        {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }

    @Override
    protected boolean acquireTarget(float amount)
    {
        if (!isTargetValidAlternate(target))
        {
            if (target instanceof ShipAPI)
            {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive())
                {
                    return false;
                }
            }
            setTarget(findBestTarget());
            if (target == null)
            {
                setTarget(findBestTargetAlternate());
            }
            if (target == null)
            {
                return false;
            }
        }
        else
        {
            if (!isTargetValid(target))
            {
                if (target instanceof ShipAPI)
                {
                    ShipAPI ship = (ShipAPI) target;
                    if (ship.isPhased() && ship.isAlive())
                    {
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

    // This is some bullshit weighted random picker that favors larger ships
    @Override
    protected ShipAPI findBestTarget()
    {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++)
        {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValid(tmp))
            {
                mod = 0f;
            }
            else
            {
                switch (tmp.getHullSize())
                {
                    default:
                    case FIGHTER:
                        mod = 150f;
                        break;
                    case FRIGATE:
                        mod = 75f;
                        break;
                    case DESTROYER:
                        mod = 35f;
                        break;
                    case CRUISER:
                        mod = 7f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 1f;
                        break;
                }
            }
            weight = (1500f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight)
            {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    protected ShipAPI findBestTargetAlternate()
    {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++)
        {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValidAlternate(tmp))
            {
                mod = 0f;
            }
            else
            {
                switch (tmp.getHullSize())
                {
                    default:
                    case FIGHTER:
                        mod = 125f;
                        break;
                    case FRIGATE:
                        mod = 75f;
                        break;
                    case DESTROYER:
                        mod = 50f;
                        break;
                    case CRUISER:
                        mod = 10f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 1f;
                        break;
                }
            }
            weight = (1500f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight)
            {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    protected boolean isTargetValidAlternate(CombatEntityAPI target)
    {
        return super.isTargetValid(target);
    }
}
