package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MSS_NitroStats extends BaseShipSystemScript {


    public static float SPEED_BONUS = 600f;
    private Color color = new Color(255, 90, 75, 255);
    private static float randAngleRange = 4f;

    private boolean runOnce = false;

    //protected Object STATUSKEY1 = new Object(); // Gotta have this for the additional status data to attach to.


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        //ShipSystemAPI sys = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            ship.getEngineController().fadeToOtherColor(this, color, new Color(0, 0, 0, 0), effectLevel, 0.67f);
            ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel);
        }

        //if (ship.getSystem() instanceof ShipSystemAPI) {
        //    sys = ship.getSystem();
        //    float cooldown = sys.getCooldownRemaining();
        //    if (cooldown <= 0f) {
        //        for (WeaponAPI w : ship.getAllWeapons()) {
        //            if (w.getId().contains("dara_canisterbay_faux_")) {
        //                w.getAnimation().setFrame(0);
        //            }
        //        }
        //    }
        //}


        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
            if (!runOnce) {
                for (WeaponAPI w : ship.getAllWeapons()) {
                    if (w.getId().contains("MSS_canisterbay_faux_")) {
                        //w.getAnimation().setFrame(1);
                        float angle = w.getCurrAngle() + MathUtils.getRandomNumberInRange(randAngleRange, -randAngleRange);

                        Global.getCombatEngine().spawnProjectile(ship, w, "MSS_canisterbay",
                                w.getLocation(),
                                angle,
                                ship.getVelocity());
                        Global.getSoundPlayer().playSound("MSS_nitro_eject",
                                1f,
                                1f,
                                w.getLocation(),
                                w.getShip().getVelocity());
                        Vector2f velocity = new Vector2f((float) Math.cos(Math.toRadians((double) w.getCurrAngle())), (float) Math.sin(Math.toRadians((double) w.getCurrAngle())));
                        velocity = Vector2f.add(velocity, ship.getVelocity(), new Vector2f(0f, 0f));
                        Global.getCombatEngine().spawnExplosion(w.getLocation(),
                                velocity,
                                color,
                                60,
                                0.5f);

                        runOnce = true;
                    }
                }
            }


        } else {
            stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 1200f * effectLevel);

            runOnce = false;
        }
        //ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
        //ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel);

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            if (index == 0) {
                return new StatusData("ejecting fuel cells...", true);
            }
        } else {
            if (index == 0) {
                return new StatusData("+" + (int)SPEED_BONUS + " top speed", false);
            } else if (index == 1) {
                return new StatusData("reduced handling", true);
            }
        }
        return null;
    }
}