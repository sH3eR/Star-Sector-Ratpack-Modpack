package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WingRole;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class phasegatewaystats extends BaseShipSystemScript
{
    private static final Color JITTER_COLOR = new Color(100, 165, 255, 150);
    private static final float MAXIMUM_RANGE = 1600f;
    private boolean runOnce = false;

    // Main apply loop
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel)
    {
        // Don't run when paused
        if (Global.getCombatEngine().isPaused())
        {
            return;
        }

        // Ensures we have a ship
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI)
        {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();

        } else
        {
            return;
        }
        ShipAPI target = ship.getShipTarget();
        Vector2f destination = new Vector2f(ship.getMouseTarget());
        if (!runOnce && ship.getAIFlags() != null && target != null)
        {
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
                destination = MathUtils.getRandomPointOnCircumference(target.getLocation(),
                    target.getShieldRadiusEvenIfNoShield() + 100);
            else if (numSupport >= numFighters && numSupport >= numBombers)
                destination = target.getLocation();
            else if (numBombers >= numFighters && numBombers >= numSupport)
            {
                if (!ship.isPullBackFighters())
                    destination = MathUtils.getRandomPointOnCircumference(target.getLocation(),
                        target.getShieldRadiusEvenIfNoShield() + 300);
                else
                    destination = ship.getLocation();
            }

        }

        // INSERT ACTUAL EFFECTS HERE
        // If we are active, add some jitter to all fighters
        if (effectLevel > 0)
        {
            List<ShipAPI> allFighters = new ArrayList<ShipAPI>();
            for (FighterWingAPI wing : ship.getAllWings())
            {
                for (ShipAPI fighter : wing.getWingMembers())
                {
                    allFighters.add(fighter);
                }
            }
            for (ShipAPI fighter : allFighters)
            {
                fighter.setJitter(id, JITTER_COLOR, effectLevel, 3, 20f * effectLevel);
                // If we are fully charged, teleport every fighter
                if (!runOnce && effectLevel >= 1f)
                {
                    Vector2f posToTeleportTo = MathUtils.getRandomPointInCircle(destination, 250f);
                    if (MathUtils.getDistanceSquared(posToTeleportTo, ship.getLocation()) > MAXIMUM_RANGE
                            * MAXIMUM_RANGE)
                    {
                        float distance = MathUtils.getDistance(posToTeleportTo, ship.getLocation());
                        posToTeleportTo = new Vector2f(
                                ship.getLocation().x
                                        + ((posToTeleportTo.x - ship.getLocation().x) / distance) * MAXIMUM_RANGE,
                                ship.getLocation().y
                                        + ((posToTeleportTo.y - ship.getLocation().y) / distance) * MAXIMUM_RANGE); // place where you modify posToTeleportTo
                    }
                    fighter.getLocation().setX(posToTeleportTo.x);
                    fighter.getLocation().setY(posToTeleportTo.y);
                }
            }

        }
        // Ensures we only run the teleporter once
        if (effectLevel >= 1f)

        {
            runOnce = true;
        }
    }

    // Unapply function
    public void unapply(MutableShipStatsAPI stats, String id)
    {
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI)
        {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else
        {
            return;
        }

        // PUT "RESET" STUFF HERE: THIS IS CALLED EACH TIME THE SYSTEM IS DEACTIVATED
        runOnce = false;
    }
}