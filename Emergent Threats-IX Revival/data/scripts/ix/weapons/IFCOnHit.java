package data.scripts.ix.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import data.scripts.ix.util.ShapedExplosionUtil;

public class IFCOnHit implements OnHitEffectPlugin {

	private static float DMG_MULT = 0.5f;

	public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		engine.applyDamage(target, point, proj.getDamageAmount() * DMG_MULT, DamageType.ENERGY, 0, false, false, proj.getSource());
		
		Global.getSoundPlayer().playSound("ifc_ix_hit", 1f, 1f, point, new Vector2f());
		
		ShapedExplosionUtil.spawnShapedExplosion(
				proj.getLocation(), 
				proj.getFacing(), 
				100f,
				new Color(50,200,50,150),
				"medium");
	}
}