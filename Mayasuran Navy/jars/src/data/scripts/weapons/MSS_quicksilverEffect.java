//By Tartiflette

package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class MSS_quicksilverEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce = false, boost = false;
    private ShipAPI ship;
    private final String ID = "MSS_quicksilverBoost";


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused()) {
            return;
        }

        if (!runOnce) {
            runOnce = true;
            ship = weapon.getShip();
        }

        //PHASE BOOST
        if (ship.isPhased()) {
            if (!boost) {
                boost = true;
                ship.getMutableStats().getMaxSpeed().modifyFlat(ID, 100);
                ship.getMutableStats().getAcceleration().modifyMult(ID, 4);
                ship.getMutableStats().getDeceleration().modifyMult(ID, 4);
                ship.getMutableStats().getMaxTurnRate().modifyMult(ID, 6);
                ship.getMutableStats().getTurnAcceleration().modifyMult(ID, 6);
            }
        } else if (boost) {
            boost = false;
            ship.getMutableStats().getMaxSpeed().unmodify(ID);
            ship.getMutableStats().getAcceleration().unmodify(ID);
            ship.getMutableStats().getDeceleration().unmodify(ID);
            ship.getMutableStats().getMaxTurnRate().unmodify(ID);
            ship.getMutableStats().getTurnAcceleration().unmodify(ID);
        }
    }
}
