package data.scripts.weapons.ai;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MSS_plasmabombAI implements MissileAIPlugin, GuidedMissileAI
{
    private static final float ARMING_TIME = 0.5f;
    private static final float AI_UPDATE_INTERVAL = 0.2f;
    private static final float SPLIT_RANGE = 500f;
    private static final float SPLIT_DEVIATION_DEG = 60f;
    private static final int NUM_SUB_PROJ = 3;
    private static final String SPLIT_SOUND = "plasma_cannon_fire";
    private static final String SUB_PROJ_WEAPON = "MSS_plasmabomb_pulse";

    private final MissileAPI missile;
    private CombatEntityAPI target;
    private IntervalUtil interval;
    private float aliveTime;
    

    public MSS_plasmabombAI(MissileAPI missile, ShipAPI launchingShip)
    {
        this.missile = missile;
        this.aliveTime = 0;
        interval = new IntervalUtil(AI_UPDATE_INTERVAL, AI_UPDATE_INTERVAL * 2f);
    }

    @Override
    public void advance(float amount) {
        if (missile.isFading()) {
            return;
        }
    
        interval.advance(amount);
        
        aliveTime += amount;
        if (aliveTime < ARMING_TIME * 2f)
            missile.giveCommand(ShipCommand.ACCELERATE);

        if (interval.intervalElapsed() && aliveTime > ARMING_TIME) {
            float facingAngle = VectorUtils.getFacing(missile.getVelocity());
            ShipAPI target = null;
            boolean split = false;
            float angleToTarget = 0f;
            List<ShipAPI> ships = AIUtils.getNearbyEnemies(missile, SPLIT_RANGE);
            for (ShipAPI ship : ships) {
                if (ship.isAlive()) {
                    float baseAngle = VectorUtils.getAngle(missile.getLocation(), ship.getLocation());
                    float distance = MathUtils.getDistance(missile.getLocation(), ship.getLocation());

                    Vector2f widthFromLeft = new Vector2f(ship.getCollisionRadius() * 2f, 0f);
                    VectorUtils.rotate(widthFromLeft, baseAngle + 90f, widthFromLeft);
                    Vector2f.add(widthFromLeft, ship.getLocation(), widthFromLeft);
                    float apparentWidthLeft = Misc.getTargetingRadius(widthFromLeft, ship, false);
                    float widthAngleLeft = (float) Math.toDegrees(Math.atan2(apparentWidthLeft, distance));

                    Vector2f widthFromRight = new Vector2f(ship.getCollisionRadius() * 2f, 0f);
                    VectorUtils.rotate(widthFromRight, baseAngle - 90f, widthFromRight);
                    Vector2f.add(widthFromRight, ship.getLocation(), widthFromRight);
                    float apparentWidthRight = Misc.getTargetingRadius(widthFromRight, ship, false);
                    float widthAngleRight = (float) Math.toDegrees(Math.atan2(apparentWidthRight, distance));

                    float angleDistance = MathUtils.getShortestRotation(facingAngle, baseAngle);
                    if (angleDistance > 0f) {
                        if (Math.abs(angleDistance) <= (SPLIT_DEVIATION_DEG + widthAngleRight)) {
                            split = true;
                            angleToTarget = baseAngle;
                            target = ship;
                            break;
                        }
                    } else {
                        if (Math.abs(angleDistance) <= (SPLIT_DEVIATION_DEG + widthAngleLeft)) {
                            split = true;
                            angleToTarget = baseAngle;
                            target = ship;
                            break;
                        }
                    }
                }
            }
            
            if (split) {

                Global.getSoundPlayer().playSound(SPLIT_SOUND, 1f, 1f, missile.getLocation(), Misc.ZERO);

                for (int i = 0; i < NUM_SUB_PROJ; i++) {
                    Vector2f spreadVel = MathUtils.getPointOnCircumference(missile.getVelocity(), MathUtils.getRandomNumberInRange(0f, missile.getMaxSpeed() * 0.5f), MathUtils.getRandomNumberInRange(0f, 360f));
                    float rotateAngle = MathUtils.getShortestRotation(VectorUtils.getFacing(spreadVel), VectorUtils.getAngle(missile.getLocation(), target.getLocation()));
                    VectorUtils.rotate(spreadVel, rotateAngle + Misc.random.nextFloat() * 20f - 10f);
                    float facing = angleToTarget + MathUtils.getRandomNumberInRange(-10f, 10f);
                    Vector2f startPos = MathUtils.getPointOnCircumference(missile.getLocation(), MathUtils.getRandomNumberInRange(0f, 8f), MathUtils.getRandomNumberInRange(0f, 360f));

                    // spawn projectiles
                    DamagingProjectileAPI newProj = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(missile.getSource(), missile.getWeapon(), SUB_PROJ_WEAPON, startPos, facing, spreadVel);
                    newProj.setFromMissile(true);
                }
                // destroy missile
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f,
                DamageType.FRAGMENTATION, 0f, false, false, missile);
            }
        }
    }

    @Override
    public void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }

    @Override
    public CombatEntityAPI getTarget()
    {
        return target;
    }
}
