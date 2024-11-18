//based on original script by Tartiflette
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
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.util.ParticleBeamOnHitUtil;
import data.scripts.util.MagicRender;

public class DawnstarBeamEffect implements BeamEffectPlugin {
    
	private boolean hasFired = false;
	private boolean runOnce = false;
	
	private IntervalUtil timer = new IntervalUtil(0.1f, 0.1f);
	
	//triggers event every n timer cycles, default 10 = 1 second
	private int PULSE_COUNT = 0;
	private static int PULSE_PER_EFFECT_BURST_HEAVY = 10; 
	private static int PULSE_PER_EFFECT_BURST_LIGHT = 15; 
	private int PULSE_PER_EFFECT_BURST = PULSE_PER_EFFECT_BURST_HEAVY;
	
	private static String PROTON_HULLMOD = "ix_dawnstar_proton";
	private static String NEUTRON_HULLMOD = "ix_dawnstar_neutron";
	private static String ELECTRON_HULLMOD = "ix_dawnstar_electron";
	
	private boolean IS_PROTON = false;
	private boolean IS_NEUTRON = false;
	private boolean IS_ELECTRON = false;
	
	//private static float PROTON_DAMAGE = 150;
	private static float PROTON_DAMAGE = 30;
	private static float NEUTRON_DAMAGE = 150f;
	private static float ELECTRON_DAMAGE = 450f; //emp
	
	//must match ParticleBeamOnHitUtil cases
	private static DamageType PROTON_DAMAGE_TYPE = DamageType.HIGH_EXPLOSIVE;
	private static DamageType NEUTRON_DAMAGE_TYPE = DamageType.KINETIC;
	private static DamageType ELECTRON_DAMAGE_TYPE = DamageType.ENERGY;
	
	private static Color PROTON_COLOR = Color.RED.darker();
	private static Color NEUTRON_COLOR = Color.MAGENTA.darker();
	private static Color ELECTRON_COLOR = Color.BLUE;
	private Color FRINGE_COLOR = PROTON_COLOR;
	
	private static float MUZZLE_DISPLACEMENT = 5f;
	
	private static float BEAM_WIDTH_HEAVY = 45f;
	private static float BEAM_WIDTH_LIGHT = 35f;
	private float BEAM_WIDTH = BEAM_WIDTH_HEAVY;
	
	private static float FLARE_CORE_SIZE_HEAVY = 30f;
	private static float FLARE_CORE_SIZE_LIGHT = 20f;
	private float FLARE_CORE_SIZE = FLARE_CORE_SIZE_HEAVY;
	
	private static float FLARE_EDGE_SIZE_HEAVY = 60f;
	private static float FLARE_EDGE_SIZE_LIGHT = 40f;
	private float FLARE_EDGE_SIZE = FLARE_EDGE_SIZE_HEAVY;
	
	/**
	private static float HIT_CORE_SIZE_HEAVY = 60f;
	private static float HIT_CORE_SIZE_LIGHT = 40f;
	private float HIT_CORE_SIZE = HIT_CORE_SIZE_HEAVY;
	
	private static float HIT_EDGE_SIZE_HEAVY = 100f;
	private static float HIT_EDGE_SIZE_LIGHT = 70f;
	private float HIT_EDGE_SIZE = HIT_EDGE_SIZE_HEAVY;
	**/
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (engine.isPaused()) return;
		
		if (!runOnce) {
			ShipVariantAPI variant = beam.getSource().getVariant();
			if (variant.hasHullMod(PROTON_HULLMOD)) {
				IS_PROTON = true;
				FRINGE_COLOR = PROTON_COLOR;
			}
			else if (variant.hasHullMod(NEUTRON_HULLMOD)) {
				IS_NEUTRON = true;
				FRINGE_COLOR = NEUTRON_COLOR;
			}
			else if (variant.hasHullMod(ELECTRON_HULLMOD)) {
				IS_ELECTRON = true;
				FRINGE_COLOR = ELECTRON_COLOR;
			}
			beam.setFringeColor(FRINGE_COLOR);
			
			if (beam.getWeapon().getSize().equals(WeaponSize.MEDIUM)) {
				PULSE_PER_EFFECT_BURST = PULSE_PER_EFFECT_BURST_LIGHT;
				BEAM_WIDTH = BEAM_WIDTH_LIGHT;
				FLARE_CORE_SIZE = FLARE_CORE_SIZE_LIGHT;
				FLARE_EDGE_SIZE = FLARE_EDGE_SIZE_LIGHT;
				//HIT_CORE_SIZE = HIT_CORE_SIZE_LIGHT;
				//HIT_EDGE_SIZE = HIT_EDGE_SIZE_LIGHT;
			}
			beam.getWeapon().ensureClonedSpec();
			runOnce = true;
		}
		
		if (beam.getBrightness() < 1) beam.setWidth(0);
		else if (beam.getBrightness() == 1) {
			Vector2f start = beam.getFrom();
			Vector2f end = beam.getRayEndPrevFrame();
			if (MathUtils.getDistanceSquared(start, end) == 0) return;

			//Visual effect loop reset at end of the script
			if (!hasFired) {
				hasFired = true;
                
				//firing visuals
				Vector2f loc = MathUtils.getPointOnCircumference(start, MUZZLE_DISPLACEMENT, beam.getWeapon().getCurrAngle());
				engine.addHitParticle(
						loc,
						new Vector2f(),
						FLARE_EDGE_SIZE, //size
						1.0f, //brightness
						0.15f, //duration
						FRINGE_COLOR
				);
				
				engine.addHitParticle(
						loc,
						new Vector2f(),
						FLARE_CORE_SIZE,
						1.0f,
						0.15f,
						Color.WHITE
				);
				
				/**
				//on-hit visuals, depreciated since default on-hit is less seizure inducing
				if (beam.getDamageTarget() != null && MagicRender.screenCheck(0.5f, end)) {
					engine.addHitParticle(
							end,
							new Vector2f(),
							HIT_EDGE_SIZE, //size
							1.0f, //brightness
							0.25f, //duration
							FRINGE_COLOR
					);

					engine.addHitParticle(
							end,
							new Vector2f(),
							HIT_CORE_SIZE, //size
							0.5f, //brightness
							0.15f, //duration
							Color.WHITE
					);
				}
				
				//play sound for each pulse, depreciated in favor of continuous loop
				//Global.getSoundPlayer().playSound(FIRE_SOUND, 1f, 1f, start, beam.getSource().getVelocity());
				**/
			}
			
			//can adjust amount of flicker variance by reducing timer component and adding mod
			float timerFactor = Math.min((float) (timer.getElapsed()), 0.1f);
			float width = BEAM_WIDTH * timerFactor * 10f; //scale based on interval 0.1s
			beam.setWidth(width);
			
			timer.advance(amount);
			
			//cycler, also adds charge damage on hit effect
			if (timer.intervalElapsed()) {
				hasFired = false;
				PULSE_COUNT++;
				CombatEntityAPI target = beam.getDamageTarget();
				if (!(target instanceof ShipAPI)) return;
				if (PULSE_COUNT == PULSE_PER_EFFECT_BURST) {
					PULSE_COUNT = 0;
					boolean isShieldHit = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
					applyOnHitEffect(engine, (ShipAPI) target, end, beam, isShieldHit);
				}
			}
		}
        
		if (beam.getWeapon().getChargeLevel() < 1) {
			hasFired = false;
			timer.setElapsed(0);
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