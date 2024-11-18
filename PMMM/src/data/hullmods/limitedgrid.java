package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class limitedgrid extends BaseHullMod {
    private IntervalUtil flameInterval = new IntervalUtil(2f,2f);
    private IntervalUtil deathInterval = new IntervalUtil (2.5f,2.5f);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getFluxTracker().getFluxLevel() > 0.9f) {
            flameInterval.advance(amount);
            deathInterval.advance(amount);

            if (flameInterval.intervalElapsed()) {
                ship.getFluxTracker().forceOverload(2f);
                ship.getEngineController().forceFlameout();
                Global.getSoundPlayer().playLoop("emp_loop", ship, 0.4f,0.7f, ship.getLocation(), ship.getVelocity());
            }
            if (deathInterval.intervalElapsed()) {
                ship.setOwner(100);
                ship.setHulk(true);
                ship.setHitpoints(0f);
            }
        }
    }
}