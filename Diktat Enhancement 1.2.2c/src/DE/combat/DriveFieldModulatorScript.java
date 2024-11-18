// a bit of this is relying on Tartiflette's code - I'd probably credit him for this rather than me. I just adapted it into a slightly more straightforward format
// credits to Raccoon and Arsenal Expansion for this lol
package DE.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.combat.entities.Ship;
import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DriveFieldModulatorScript extends BaseShipSystemScript {

    private ShipAPI ship;
    private ShipSystemAPI system;
    protected Object STATUSKEY1 = new Object();
    private final IntervalUtil timer = new IntervalUtil(3f, 4f);
    private List<ShipAPI> buffed = new ArrayList<>();
    public final float BUFF_RANGE = 0.25f;
    public static final Color JITTER_COLOR = new Color(200,75,200,200);
    public static final Color JITTER_COLOR_UNDER = new Color(200,75,200,100);

    protected void maintainStatus(ShipAPI ship, State state, float effectLevel) {
        float level = effectLevel;
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        float level = effectLevel;

        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        boolean visible = MagicRender.screenCheck(0.1f, ship.getLocation());

        maintainStatus(ship, state, effectLevel);

        // range
        List<ShipAPI> nearby = AIUtils.getNearbyAllies(ship, 1000);
        List<ShipAPI> previous = new ArrayList<>(buffed);

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if (state == State.COOLDOWN || state == State.IDLE) {
            unapply(stats, id);
            return;
        }

        if (state == State.ACTIVE || state == State.IN) {

            //for (ShipAPI affected : nearby){
            //    applyBuff(affected, ship, level, visible);
            //}

            MagicRender.objectspace(
                    // this needs to be loaded in settings.json, and then you can just stick in the file path
                    Global.getSettings().getSprite("graphics/DE/fx/de_drivecircle.png"),
                    // what the ring will be centered on. In this case, the ship using the system. what "ship" means
                    // was determined in the second part of this public void thingamajig.
                    ship,
                    new Vector2f(),
                    new Vector2f(),
                    // the radius of the circle will be about half this
                    new Vector2f(2500, 2500),
                    new Vector2f(-100, -100),
                    0,
                    120,
                    // set to true to make the ring turn with the ship
                    false,
                    // the color, obviously. I don't think the alpha value really works, instead I'm using a
                    // very transparent sprite
                    new Color(200,75,200,50),
                    true,
                    0.5f,
                    0f,
                    0.5f,
                    true
            );
        }

        if (state == State.ACTIVE) {

            if(!nearby.isEmpty()){
                for(ShipAPI affected : nearby){
                    //new affected ship
                    if(!previous.contains(affected)){
                        applyBuff(affected, ship, level, visible);
                        buffed.add(affected);
                    }
                    //affected ship already present
                    if(previous.contains(affected)){
                        previous.remove(affected);
                        //in case another Ascension tried to remove the Leech form that ship
                        applyBuff(affected, ship, level, visible);
                    }
                }
                //remaining ships get unleeched
                if(!previous.isEmpty()){
                    for(ShipAPI s : previous){
                        buffed.remove(s);
                        unapplyBuff(s);
                    }
                }
            } else if(!buffed.isEmpty()){
                //no ship in range, make sure to unleech all ships
                for(ShipAPI affected : buffed){
                    unapplyBuff(affected);
                }
                buffed.clear();
            }

        }
    }

    private void applyBuff(ShipAPI ship, ShipAPI source, float level, boolean visible){
        ship.setJitter(ship, JITTER_COLOR, 0.1f, 3, 5);
        ship.setJitterUnder(ship, JITTER_COLOR_UNDER, 0.1f, 3, 8);
        switch (ship.getHullSize()) {
            case CAPITAL_SHIP:
                ship.getMutableStats().getMaxSpeed().modifyFlat("drivefieldmodulator", 10);
                ship.getMutableStats().getMaxTurnRate().modifyFlat("drivefieldmodulator", 5);
                ship.getMutableStats().getAcceleration().modifyFlat("drivefieldmodulator", 20);
                break;
            case CRUISER:
                ship.getMutableStats().getMaxSpeed().modifyFlat("drivefieldmodulator", 20);
                ship.getMutableStats().getMaxTurnRate().modifyFlat("drivefieldmodulator", 10);
                ship.getMutableStats().getAcceleration().modifyFlat("drivefieldmodulator", 30);
                break;
            default:
                ship.getMutableStats().getMaxSpeed().modifyFlat("drivefieldmodulator", 30);
                ship.getMutableStats().getMaxTurnRate().modifyFlat("drivefieldmodulator", 15);
                ship.getMutableStats().getAcceleration().modifyFlat("drivefieldmodulator", 50);
                break;
        }
    }

    private void unapplyBuff(ShipAPI ship){
        ship.getMutableStats().getMaxSpeed().unmodify("drivefieldmodulator");
        ship.getMutableStats().getMaxTurnRate().unmodify("drivefieldmodulator");
        ship.getMutableStats().getAcceleration().unmodify("drivefieldmodulator");
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("enhancing allies' drive fields", false);
        }
        return null;
    }
}
