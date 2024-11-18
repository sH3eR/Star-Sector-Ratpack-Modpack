package data.shipsystems.scripts.ai;

import javax.lang.model.util.ElementScanner6;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import data.hullmods.MSS_AmmoSwapHullmod.AmmoSwapMode;

public class MSS_AmmoSwapSystemAI implements ShipSystemAIScript
{

    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private IntervalUtil timer;
    private int mode;
    AmmoSwapMode desiredMode;
    AmmoSwapMode currentMode;

    static final float AI_UPDATE_INTERVAL = 0.5f; // updates will occur randomly between this number and double this number seconds
    static final float MAXIMUM_RANGE = 1600f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
        desiredMode = AmmoSwapMode.KINETIC;
        currentMode = getMode(system);
        timer = new IntervalUtil(AI_UPDATE_INTERVAL, AI_UPDATE_INTERVAL * 2);
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
    {
        if (engine.isPaused() || ship.getFluxLevel() > 0.1f)
            return;

        timer.advance(amount);
        currentMode = getMode(system);
        if (timer.intervalElapsed() && target instanceof ShipAPI)
        {
            ShipAPI targetShip = (ShipAPI) target;
            if (targetShip.getShield() instanceof ShieldAPI)
            {
                if (targetShip.getHullSize().equals(HullSize.FIGHTER)
                        || targetShip.getHullSize().equals(HullSize.FRIGATE)
                        || targetShip.getShield().getType() == ShieldType.PHASE)
                    desiredMode = AmmoSwapMode.CLUSTER;
                else if (targetShip.getFluxTracker().isOverloadedOrVenting()
                        || targetShip.getShield().getType() == ShieldType.NONE)
                    desiredMode = AmmoSwapMode.HE;
                else
                    desiredMode = AmmoSwapMode.KINETIC;
            } else if (targetShip.getHullSize().equals(HullSize.FIGHTER)
                    || targetShip.getHullSize().equals(HullSize.FRIGATE))
            {
                desiredMode = AmmoSwapMode.CLUSTER;
            } else
            {
                desiredMode = AmmoSwapMode.HE;
            }

        }
        if (desiredMode != currentMode && system.getState() == SystemState.IDLE)
            ship.useSystem();
    }

    private AmmoSwapMode getMode(ShipSystemAPI system)
    {
        switch (system.getAmmo())
        {
            case 1:
                return AmmoSwapMode.KINETIC;
            case 2:
                return AmmoSwapMode.HE;
            case 3:
                return AmmoSwapMode.CLUSTER;
            default:
                return AmmoSwapMode.KINETIC;
        }
    }
}
