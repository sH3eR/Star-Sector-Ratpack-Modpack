// By Tartiflette.
package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import data.scripts.util.MSS_utils;
import data.scripts.weapons.MSS_grenadelauncherscript;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class MSS_grenadestickyAI implements MissileAIPlugin, GuidedMissileAI
{

    private final MissileAPI missile;
    private CombatEngineAPI engine;
    private CombatEntityAPI target;
    private CombatEntityAPI anchor;
    private Vector2f offset = new Vector2f();
    private float angle = 0;
    private boolean runOnce = false, playedWindup;

    // Generally taken from TADA's mining pike code, with some modifications

    //////////////////////
    // DATA COLLECTING //
    //////////////////////

    public MSS_grenadestickyAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        playedWindup = false;
    }

    //////////////////////
    // MAIN AI LOOP //
    //////////////////////

    @Override
    public void advance(float amount)
    {

        if (engine != Global.getCombatEngine())
        {
            this.engine = Global.getCombatEngine();
        }

        // skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused())
        {
            return;
        }

        // visual effect
        Vector2f loc = new Vector2f(offset);

        if (!runOnce)
        {
            runOnce = true;
            List<CombatEntityAPI> list = ((MSS_grenadelauncherscript) missile.getWeapon().getEffectPlugin()).getHITS();

            if (list.isEmpty())
            {
                missile.flameOut();
                return;
            }

            // get the anchor
            float range = 1000000;
            for (CombatEntityAPI e : list)
            {
                if (MathUtils.getDistanceSquared(missile, e) < range)
                {
                    target = e;
                    anchor = e; // some scripts change the target so I can't really use that for the anchor
                }
            }

            if (anchor == null)
            {
                return;
            }

            // put the anchor in the weapon's detonation list
            ((MSS_grenadelauncherscript) missile.getWeapon().getEffectPlugin()).setDetonation(anchor);

            offset = new Vector2f(missile.getLocation());
            Vector2f.sub(offset, new Vector2f(anchor.getLocation()), offset);
            VectorUtils.rotate(offset, -anchor.getFacing(), offset);

            angle = MathUtils.getShortestRotation(anchor.getFacing(), missile.getFacing());
            return;
        } else
        {
            if (anchor == null
                    || ((MSS_grenadelauncherscript) missile.getWeapon().getEffectPlugin()).getDetonation(anchor))
            {
                missile.setCollisionClass(CollisionClass.MISSILE_FF);
                return;
            }
        }

        // stuck effect

        VectorUtils.rotate(offset, anchor.getFacing(), loc);
        Vector2f.add(loc, anchor.getLocation(), loc);
        missile.getLocation().set(loc);
        missile.setFacing(anchor.getFacing() + angle);

        // detonation
        if (missile.getElapsed() > 0.25f && !playedWindup)
        {
            Global.getSoundPlayer().playSound("MSS_thumper_windup", 1.0f, 0.5f, missile.getLocation(), anchor.getVelocity());
            playedWindup = true;
        }
        if (missile.getElapsed() > 1.95f)
        {
            missile.setCollisionClass(CollisionClass.MISSILE_FF);
            MSS_utils.plasmaEffects((DamagingProjectileAPI)missile);
            // if detonation fails
            if (missile.getElapsed() > 2.25f)
                engine.removeEntity(missile);
        }
    }

    @Override
    public CombatEntityAPI getTarget()
    {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }

    public void init(CombatEngineAPI engine)
    {
    }

}