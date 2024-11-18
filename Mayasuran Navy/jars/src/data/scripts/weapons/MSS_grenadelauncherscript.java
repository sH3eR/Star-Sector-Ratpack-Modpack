package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

import org.apache.log4j.Logger;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.*;

public class MSS_grenadelauncherscript implements EveryFrameWeaponEffectPlugin
{

    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();

    private boolean runOnce = false, empty = false;

    private Map<CombatEntityAPI, Boolean> detonation = new HashMap<>();
    private List<CombatEntityAPI> hit = new ArrayList<>();

    static Logger log = Global.getLogger(MSS_grenadelauncherscript.class);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {

        if (!runOnce)
        {
            runOnce = true;
            detonation.clear();
            hit.clear();
        }

        if (engine.isPaused())
        {
            return;
        }
        
        //And clean up our registered projectile list
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getMissilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
                // the railgun rounds handle the effect plugin application when they split
                if (proj.getWeapon().getId().contains("MSS_big"))
                    continue;
                engine.addPlugin(new MSS_grenadeprojectileeffect(proj));
                alreadyRegisteredProjectiles.add(proj);
            }
        }
        if (!detonation.isEmpty())
        {
            for (Iterator<CombatEntityAPI> iter = detonation.keySet().iterator(); iter.hasNext();)
            {
                CombatEntityAPI entry = iter.next();
                if (detonation.get(entry))
                {
                    iter.remove();
                }
            }
        }
    }

    public void putHIT(CombatEntityAPI target)
    {
        hit.add(target);
    }

    public List<CombatEntityAPI> getHITS()
    {
        return hit;
    }

    public void setDetonation(CombatEntityAPI target)
    {
        hit.remove(target);
        if (!detonation.containsKey(target))
        {
            detonation.put(target, false);
        }
    }

    public boolean getDetonation(CombatEntityAPI target)
    {
        if (detonation.containsKey(target))
        {
            return detonation.get(target);
        } else
        {
            return true;
        }
    }

    public void applyDetonation(CombatEntityAPI target)
    {
        if (detonation.containsKey(target))
        {
            detonation.put(target, true);
        }
    }
}
