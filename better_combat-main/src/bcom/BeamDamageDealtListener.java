package bcom;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

public class BeamDamageDealtListener implements DamageDealtModifier {
    @Override
    public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
        if(param instanceof BeamAPI) {
            WeaponAPI weapon = ((BeamAPI) param).getWeapon();
            if (null != weapon) {
                WeaponSpecAPI weaponSpec = weapon.getSpec();
                Vector2f from = ((BeamAPI) param).getFrom();
                float dist = Misc.getDistance(from, point);
                float range = weapon.getRange();

                float offset = Settings.getBeamDropoffOffset(weaponSpec)*range;
                if(dist>offset){
                    dist = dist-offset;
                    float ratio =1-(dist/range);
                    float amount = (float) (Math.pow(ratio,Settings.getBeamDropoffExp(weaponSpec)) * Settings.getBeamDropoffOffMult(weaponSpec));
                    damage.getModifier().modifyMult("bcom", amount);
                }
            }
        }
        return null;
    }
}
