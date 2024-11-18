package DE.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class TemporalOverdriveStats extends BaseShipSystemScript {
    //Code Courtesy of the Titan code from Interstellar Imperium by Dark Revenant, and the Crusher Drive System from Tahlan by Nia and maybe Tart
    //Parts of code Courtesy of Luddic Enhancement by Alfonzo


    public static final float MAX_TIME_MULT = 5f;
    public static final float MIN_TIME_MULT = 0.1f;
    public static final float DAM_MULT = 0.1f;

    public static final Color JITTER_COLOR = new Color(90,165,255,55);
    public static final Color JITTER_UNDER_COLOR = new Color(90,165,255,155);

    public static final float SPEED_BOOST = 180f;

    private boolean exploded = false;
    private final IntervalUtil interval = new IntervalUtil(0.2f, 0.2f);
    private final IntervalUtil interval2 = new IntervalUtil(20f, 20f);
    private SoundAPI sound = null;
    private boolean started = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI ship = (ShipAPI) stats.getEntity();
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        
        float jitterLevel = effectLevel;
        float jitterRangeBonus = 0;
        float maxRangeBonus = 10f;
        if (state == State.IN) {
            jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
            if (jitterLevel > 1) {
                jitterLevel = 1f;
            }
            jitterRangeBonus = jitterLevel * maxRangeBonus;
        } else if (state == State.ACTIVE) {
            jitterLevel = 1f;
            jitterRangeBonus = maxRangeBonus;
        } else if (state == State.OUT) {
            jitterRangeBonus = jitterLevel * maxRangeBonus;
        }
        jitterLevel = (float) Math.sqrt(jitterLevel);
        effectLevel *= effectLevel;

        ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);

        IntervalUtil interval3 = new IntervalUtil(0.01f,0.01f);

            if (!ship.isAlive()) return;

            interval3.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (interval3.intervalElapsed()) {
                for (WeaponAPI w : ship.getAllWeapons()) {
                    if (w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {
                        w.setAmmo(w.getAmmo() + 5);
                    }
                    w.setRefireDelay(0.05f);
                }
            }

        float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
        stats.getTimeMult().modifyMult(id, shipTimeMult);

        // remove the death effects in case they persist
        stats.getCRLossPerSecondPercent().unmodify(id);
        stats.getPeakCRDuration().unmodify(id);

        // make ship reckless and angy while amped up
        if (!player) {
            ShipAIPlugin AItype = ship.getShipAI();
            ShipAIConfig config = AItype.getConfig();

            if (AItype == null) {
                return;
            }

            if (config == null) {
                return;
            }

            if (state == State.ACTIVE) {
                config.personalityOverride = Personalities.RECKLESS;
            } else {
                return;
            }
        }

        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			if (ship.areAnyEnemiesInRange()) {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			} else {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 2f / shipTimeMult);
//			}
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }

        ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
        ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);

        if (ship == null) {
            return;
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        ShipAPI target = ship.getShipTarget();
        float turnrate = ship.getMaxTurnRate()*2;

        interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
        ship.setJitter(stats.getEntity(), JITTER_COLOR, effectLevel, 5 + Math.round(effectLevel * 5f), effectLevel * 5f, 10f + (effectLevel * 20f));

        //CombatEntityAPI entity = stats.getEntity();
        Vector2f point = new Vector2f(ship.getLocation());
        point.x += ship.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);
        point.y += ship.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);
        //TemporalOverdrivePlugin.explode(ship, 1f);
        //interval2.setInterval(20f, 20f);
        interval2.advance(engine.getElapsedInLastFrame());
        //interval2.intervalElapsed()
        if (state == State.OUT || interval2.intervalElapsed()) {
            engine.applyDamage(
                    ship,
                    point,
                    Float.MAX_VALUE,
                    DamageType.HIGH_EXPLOSIVE,
                    0f,
                    true,
                    false,
                    ship);
            engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), Color.WHITE, 100f, 10f);
            // just in case the ship doesn't explode
            stats.getCRLossPerSecondPercent().modifyMult(id, 20f);
            stats.getPeakCRDuration().modifyMult(id, 0.02f);
        }

        if (!started) {
            started = true;

            float distanceToHead = MathUtils.getDistance(stats.getEntity(),
                    Global.getCombatEngine().getViewport().getCenter());
            float refDist = 1000f;
            float vol = refDist / Math.max(refDist, distanceToHead);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Time flow altered greatly", false);
        }
        if (index == 1) {
            return new StatusData("Weapon banks supercharged", false);
        }
        if (index == 2) {
            return new StatusData("Temporal stresses will destroy the ship in 20 seconds", true);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);
    }
    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        if(
                target!=null
                        &&
                        (!target.isDrone()||!target.isFighter())
                        &&
                        MathUtils.isWithinRange(ship, target, 3000f)
                        &&
                        target.getOwner()!=ship.getOwner()
        ){
            return target;
        } else {
            return null;
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

}
