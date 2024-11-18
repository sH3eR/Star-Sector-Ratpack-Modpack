package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;

public class istl_SigmaBeamAblationEffect implements BeamEffectPlugin {

	private boolean done = false;
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		if (done) return;
		
		CombatEntityAPI target = beam.getDamageTarget();
		boolean first = beam.getWeapon().getBeams().indexOf(beam) == 0;
		if (target != null && beam.getBrightness() >= 1f && first) {
			Vector2f point = beam.getTo();
			float maxDist = 0f;
			for (BeamAPI curr : beam.getWeapon().getBeams()) {
				maxDist = Math.max(maxDist, Misc.getDistance(point, curr.getTo()));
    }
			if (maxDist < 15f) {
				DamagingProjectileAPI e = engine.spawnDamagingExplosion(createExplosionSpec(), beam.getSource(), point);
				e.addDamagedAlready(target);
				done = true;
			}
		}
	}

	public DamagingExplosionSpec createExplosionSpec() {
		float damage = 60f;
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.05f, // duration
				16f, // radius
				9f, // coreRadius
				damage, // maxDamage
				damage / 2f, // minDamage
				CollisionClass.PROJECTILE_FF, // collisionClass
				CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
				7f, // particleSizeMin
				3f, // particleSizeRange
				0.6f, // particleDuration
				12, // particleCount
				new Color(75,255,175,255), // particleColor
				new Color(25, 255, 125, 155)  // explosionColor
		);

		spec.setDamageType(DamageType.ENERGY);
		spec.setUseDetailedExplosion(false);
		spec.setSoundSetId("istl_energy_crit_sm");
		return spec;		
    }
}




