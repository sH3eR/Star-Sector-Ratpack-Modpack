package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class MSS_utils
{
    // code provided by tomatopaste
    public static boolean isEntityInArc(CombatEntityAPI entity, Vector2f center, float centerAngle, float arcDeviation)
    {
        if (entity instanceof ShipAPI)
        {
            Vector2f point = getNearestPointOnShipBounds((ShipAPI) entity, center);
            return Misc.isInArc(centerAngle, arcDeviation * 2f, center, point);
        } else
        {
            return Misc.isInArc(centerAngle, arcDeviation * 2f, center,
                    getNearestPointOnCollisionRadius(entity, center));
        }
    }

    public static Vector2f getNearestPointOnShipBounds(ShipAPI ship, Vector2f point)
    {
        BoundsAPI bounds = ship.getExactBounds();
        if (bounds == null)
        {
            return getNearestPointOnCollisionRadius(ship, point);
        } else
        {
            Vector2f closest = ship.getLocation();
            float distSquared = 0f;
            for (BoundsAPI.SegmentAPI segment : bounds.getSegments())
            {
                Vector2f tmpcp = MathUtils.getNearestPointOnLine(point, segment.getP1(), segment.getP2());
                float distSquaredTemp = MathUtils.getDistanceSquared(tmpcp, point);
                if (distSquaredTemp < distSquared)
                {
                    distSquared = distSquaredTemp;
                    closest = tmpcp;
                }
            }
            return closest;
        }
    }

    public static Vector2f getNearestPointOnCollisionRadius(CombatEntityAPI entity, Vector2f point)
    {
        return MathUtils.getPointOnCircumference(entity.getLocation(), entity.getCollisionRadius(),
                VectorUtils.getAngle(entity.getLocation(), point));
    }

    /**
     * Draws some plasma-arc kinda effects. Used for Iruiru grenade detonations.
     * @param source
     */
    public static void plasmaEffects(DamagingProjectileAPI source)
    {
        final Color FRINGE_COLOR = new Color(46, 91, 255);
        final int NUM_ARCS = 4;
        Vector2f from = MathUtils.getRandomPointInCircle(source.getLocation(), 20f);
        CombatEngineAPI engine = Global.getCombatEngine();
        for (int i = 0; i < NUM_ARCS; i++)
        {
            engine.spawnEmpArcVisual(from, null, MathUtils.getRandomPointInCircle(from, 200f), null,
                    10f, FRINGE_COLOR, Color.white);
        }
    }
}
