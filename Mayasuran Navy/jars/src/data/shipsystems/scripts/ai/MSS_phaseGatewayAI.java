package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.loading.WingRole;
import com.fs.starfarer.api.util.IntervalUtil;


import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MSS_phaseGatewayAI implements ShipSystemAIScript
{

    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private IntervalUtil timer;
    private int mode;

    static final float AI_UPDATE_INTERVAL = 0.5f; // updates will occur randomly between this number and double this number seconds
    static final float MAXIMUM_RANGE = 1600f;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
        timer = new IntervalUtil(AI_UPDATE_INTERVAL, AI_UPDATE_INTERVAL * 2);

        int numFighters = 0;
        int numBombers = 0;
        int numSupport = 0;

        for (FighterWingAPI wing : ship.getAllWings())
        {
            WingRole role = wing.getRole();
            if (role.equals(WingRole.ASSAULT) || role.equals(WingRole.INTERCEPTOR) || role.equals(WingRole.FIGHTER))
                numFighters++;
            else if (role.equals(WingRole.SUPPORT))
                numSupport++;
            else if (role.equals(WingRole.BOMBER))
                numBombers++;
        }

        if (numFighters >= numBombers && numFighters >= numSupport)
            mode = 0;
        else if (numSupport >= numFighters && numSupport >= numBombers)
            mode = 1;
        else if (numBombers >= numFighters && numBombers >= numSupport)
            mode = 2;
        else
            mode = 0;

    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
    {
        if (engine.isPaused() || ship.getFluxLevel() > 0.1f)
            return;

        timer.advance(amount);
        if (timer.intervalElapsed() && system.getCooldownRemaining() == 0 && target instanceof ShipAPI)
        {
            Vector2f currentLoc = getAverageLocation();
            float distance = MathUtils.getDistance(ship.getLocation(), target.getLocation());
            Vector2f predictedLoc = new Vector2f(
                    ship.getLocation().x + ((target.getLocation().x - ship.getLocation().x) / distance) * MAXIMUM_RANGE,
                    ship.getLocation().y + ((target.getLocation().y - ship.getLocation().y) / distance) * MAXIMUM_RANGE);

            // mode 0: fighters and interceptors
            if (mode == 0 && !ship.isPullBackFighters() && MathUtils.getDistanceSquared(currentLoc, target.getLocation()) > MathUtils.getDistanceSquared(predictedLoc, target.getLocation()))
            {
                ship.useSystem();
                return;
            }
            // mode 1: support fighters
            else if (mode == 1 && target.getOwner() == ship.getOwner() && !ship.isPullBackFighters() && MathUtils.getDistanceSquared(currentLoc, target.getLocation()) > MathUtils.getDistanceSquared(predictedLoc, target.getLocation()))
            {
                ship.useSystem();
                return;
            }
            // mode 2: bombers
            else if (mode == 2)
            {
                if (!ship.isPullBackFighters() && MathUtils.getDistanceSquared(currentLoc, target.getLocation()) > MathUtils.getDistanceSquared(predictedLoc, target.getLocation()))
                {
                    ship.useSystem();
                } else if (ship.isPullBackFighters() && MathUtils.getDistance(ship, currentLoc) > 500)
                {
                    ship.useSystem();
                }
            }
        }
    }

    private Vector2f getAverageLocation()
    {
        float x = 0;
        float y = 0;
        int n = 0;
        for (FighterWingAPI wing : ship.getAllWings())
        {
            for (ShipAPI fighter : wing.getWingMembers())
            {
                n++;
                x += fighter.getLocation().x;
                y += fighter.getLocation().y;
            }
        }
        if (n == 0)
            n++;
        return new Vector2f(x / (float) n, y / (float) n);
    }
}
