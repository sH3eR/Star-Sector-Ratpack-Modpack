package data.scripts.ix.util;

import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class ParticleBeamOnHitUtil {

	private static DamageType PROTON_DAMAGE_TYPE = DamageType.HIGH_EXPLOSIVE;
	private static DamageType NEUTRON_DAMAGE_TYPE = DamageType.KINETIC;
	private static DamageType ELECTRON_DAMAGE_TYPE = DamageType.ENERGY;

	public static void apply(CombatEngineAPI engine, ShipAPI target, Vector2f point, BeamAPI beam, float damage, DamageType type, boolean isShieldHit) {
		if (type == PROTON_DAMAGE_TYPE) {
			dealArmorDamage (engine, target, point, beam, damage);
		}
		else if (type == NEUTRON_DAMAGE_TYPE) {
			engine.applyDamage(target, point, damage, type, 0, false, false, beam.getSource());
		}
		else if (type == ELECTRON_DAMAGE_TYPE) {
			boolean isPierceHit = false;
			if (isShieldHit) {
				//determine if charge is shield piercing based on flux
				float pierceChance = target.getHardFluxLevel(); //+ 0.1f for +10% pierce chance
				pierceChance *= target.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
				isPierceHit = (float) Math.random() < pierceChance;
			}
			if (!isShieldHit || isPierceHit) {
				//generate emp arc
				engine.spawnEmpArcPierceShields(beam.getSource(), point, target, target, type, 1, damage, 100000f, "tachyon_lance_emp_impact", 20f, beam.getFringeColor(), beam.getCoreColor());
			}
		}
	}
	
	private static void dealArmorDamage(CombatEngineAPI engine, ShipAPI target, Vector2f point, BeamAPI beam, float bonusDamage) {
		ArmorGridAPI grid = target.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return;
		
		int gridWidth = grid.getGrid().length;
		int gridHeight = grid.getGrid()[0].length;
		
		float damageDealt = 0f;
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if ((i == 2 || i == -2) && (j == 2 || j == -2)) continue; // skip corners
				
				int cx = cell[0] + i;
				int cy = cell[1] + j;
				
				if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
				
				float damMult = 1/30f;
				if (i == 0 && j == 0) {
					damMult = 1/15f;
				} else if (i <= 1 && i >= -1 && j <= 1 && j >= -1) { // S hits
					damMult = 1/15f;
				} else { // T hits
					damMult = 1/30f;
				}
				
				float armorInCell = grid.getArmorValue(cx, cy);
				float damage = bonusDamage * damMult;
				damage = Math.min(damage, armorInCell);
				if (damage <= 0) continue;
				
				target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
				damageDealt += damage;
			}
		}
		
		if (damageDealt > 0) {
			engine.addFloatingDamageText(point, bonusDamage, Misc.MOUNT_BALLISTIC, target, beam.getSource());
			target.syncWithArmorGridState();
		}
	}
}