// 
// Decompiled by Procyon v0.5.36
// 

package DE.combat;

import com.fs.starfarer.api.combat.*;

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.combat.DamageType.ENERGY;

public class TargeAutolaserBeamEffect implements BeamEffectPlugin
{
   private static final float EXTRA_DAMAGE_OTHERS = 0;
   private static final float EXTRA_DAMAGE_MINES = 5000;
    private static final float EXTRA_DAMAGE_FIGHTERS = 100;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        Vector2f point = beam.getRayEndPrevFrame();
        if (target instanceof MissileAPI && ((MissileAPI)target).isMine()){
            engine.applyDamage(target, point, EXTRA_DAMAGE_MINES, ENERGY, 100f, false, false, beam.getSource());
        } else if ((target instanceof ShipAPI && ((ShipAPI)target).isFighter()) || (target instanceof MissileAPI)) {
            engine.applyDamage (target, point, EXTRA_DAMAGE_FIGHTERS, ENERGY, 100f, false, false, beam.getSource());
        } else {
            engine.applyDamage (target, point, EXTRA_DAMAGE_OTHERS, ENERGY, 100f, false, false, beam.getSource());
        }
//			Global.getSoundPlayer().playLoop("system_emp_emitter_loop",
//											 beam.getDamageTarget(), 1.5f, beam.getBrightness() * 0.5f,
//											 beam.getTo(), new Vector2f());
    }
}
