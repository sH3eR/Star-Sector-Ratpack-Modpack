package data.scripts.weapons.MagicGuidance;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.combat.CombatUtils;
import java.util.ArrayList;
import java.util.List;

//This is a template for the scrpt that a weapon calls as an EveryFrameEffect to guide its projectiles.

public class istl_MagicGuidanceWeaponScript implements EveryFrameWeaponEffectPlugin {

    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<DamagingProjectileAPI>();

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI source = weapon.getShip();
        ShipAPI target = null;

        if(source.getWeaponGroupFor(weapon)!=null ){
            //Autofire management.
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //if autofire is on for this weapon group.
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon)){ //if the autofire group isn't selected.
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            }
            else {
                target = source.getShipTarget();
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj)
            		&& engine.isEntityInPlay(proj) && !proj.didDamage()) {
                engine.addPlugin(new istl_MagicGuidanceProjScript(proj, target));
                alreadyRegisteredProjectiles.add(proj);
            }
        }

        //Tidy up the list of registered projectiles.
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }
    }
}
