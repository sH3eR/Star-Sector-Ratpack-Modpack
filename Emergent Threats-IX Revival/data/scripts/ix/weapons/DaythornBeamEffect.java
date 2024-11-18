package data.scripts.ix.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import org.lazywizard.lazylib.MathUtils;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import data.scripts.ix.util.ParticleBeamOnHitUtil;
import data.scripts.util.MagicRender;

public class DaythornBeamEffect implements BeamEffectPlugin {
    
	private boolean hasFired = false;
	private boolean runOnce = false;
	
	private IntervalUtil timer = new IntervalUtil(1.0f, 1.0f);
	
	private static String PROTON_HULLMOD = "ix_dawnstar_proton";
	private static String NEUTRON_HULLMOD = "ix_dawnstar_neutron";
	private static String ELECTRON_HULLMOD = "ix_dawnstar_electron";
	
	private boolean IS_PROTON = false;
	private boolean IS_NEUTRON = false;
	private boolean IS_ELECTRON = false;
	
	//private static float PROTON_DAMAGE = 50f;
	private static float PROTON_DAMAGE = 10f;
	private static float NEUTRON_DAMAGE = 50f;
	private static float ELECTRON_DAMAGE = 150f; //emp
	
	private static DamageType PROTON_DAMAGE_TYPE = DamageType.HIGH_EXPLOSIVE;
	private static DamageType NEUTRON_DAMAGE_TYPE = DamageType.KINETIC;
	private static DamageType ELECTRON_DAMAGE_TYPE = DamageType.ENERGY;
	
	private static Color PROTON_COLOR = Color.RED.darker();
	private static Color NEUTRON_COLOR = Color.MAGENTA.darker();
	private static Color ELECTRON_COLOR = Color.BLUE;
	private static Color DEFAULT_COLOR = new Color (255,140,0,255);
	private Color FRINGE_COLOR = DEFAULT_COLOR;

	@Override
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (engine.isPaused()) return;
		
		if (!runOnce) {
			ShipVariantAPI variant = beam.getSource().getVariant();
			if (variant.hasHullMod(PROTON_HULLMOD)) {
				FRINGE_COLOR = PROTON_COLOR;
				IS_PROTON = true;
			}
			else if (variant.hasHullMod(NEUTRON_HULLMOD)) {
				FRINGE_COLOR = NEUTRON_COLOR;
				IS_NEUTRON = true;
			}
			else if (variant.hasHullMod(ELECTRON_HULLMOD)) {
				FRINGE_COLOR = ELECTRON_COLOR;
				IS_ELECTRON = true;
			}
			beam.setFringeColor(FRINGE_COLOR);
			beam.getWeapon().ensureClonedSpec();
			runOnce = true;
		}
		
		if (beam.getFringeColor().equals(DEFAULT_COLOR)) return;
		
		if (beam.getBrightness() == 1) {
			Vector2f start = beam.getFrom();
			Vector2f end = beam.getRayEndPrevFrame();
			if (MathUtils.getDistanceSquared(start, end) == 0) return;
			
			timer.advance(amount);
			
			if (timer.intervalElapsed()) {
				CombatEntityAPI target = beam.getDamageTarget();
				boolean first = beam.getWeapon().getBeams().indexOf(beam) == 0;
				if (target instanceof ShipAPI && first) {
					boolean isShieldHit = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
					applyOnHitEffect(engine, (ShipAPI) target, end, beam, isShieldHit);	
					
				}
				timer.setElapsed(0);
			}
		}
	}
	
	private void applyOnHitEffect(CombatEngineAPI engine, ShipAPI target, Vector2f point, BeamAPI beam, boolean isShieldHit) {
		float damage = 0f;
		DamageType type = ELECTRON_DAMAGE_TYPE;
		boolean isApplying = false;
		if (IS_PROTON && !isShieldHit) {
			damage = PROTON_DAMAGE;
			type = PROTON_DAMAGE_TYPE;
			isApplying = true;
		}
		else if (IS_NEUTRON && isShieldHit) {
			damage = NEUTRON_DAMAGE;
			type = NEUTRON_DAMAGE_TYPE;
			isApplying = true;
		}
		else if (IS_ELECTRON) {
			damage = ELECTRON_DAMAGE;
			type = ELECTRON_DAMAGE_TYPE;
			isApplying = true;
		}
		if (!isApplying) return;
		ParticleBeamOnHitUtil.apply(engine, target, point, beam, damage, type, isShieldHit);
	}
}