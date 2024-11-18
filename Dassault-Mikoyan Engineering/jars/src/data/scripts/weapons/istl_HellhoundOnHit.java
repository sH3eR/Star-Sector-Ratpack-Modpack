package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class istl_HellhoundOnHit implements OnHitEffectPlugin
{
    // amount of extra damage dealt
    private static final int DAMAGE = 250;
    // All the good stuff to make the nebula particles behave
    private static final Color EXPLOSION_COLOR = new Color(125, 155, 255, 200);
    private static final Color NEBULA_COLOR = new Color(75, 105, 255, 255);
    private static final float NEBULA_SIZE = 40f * (0.75f + (float) Math.random() * 0.5f);
    private static final float NEBULA_SIZE_MULT = 18f;
    private static final float NEBULA_DUR = 2.0f;
    private static final float NEBULA_RAMPUP = 0.2f;
    
    //a nice explosion sound
    private static final String SFX = "istl_hellhound_crit";
    //Crit damage
    @Override
    public void onHit(DamagingProjectileAPI projectile,
            CombatEntityAPI target,
            Vector2f point,
            boolean shieldHit,
            ApplyDamageResultAPI damageResult, 
            CombatEngineAPI engine)
    {
        // check whether or not we want to apply critical damage
        if (target instanceof ShipAPI && !shieldHit)
        {
            dealArmorDamage(projectile, (ShipAPI) target, point);
        }
        // get the target's velocity to render the crit FX - renders on shield hits, too.
        Vector2f v_target = new Vector2f(target.getVelocity());
        // do visual effects
        engine.spawnExplosion(point,
            target.getVelocity(),
            EXPLOSION_COLOR, // color of the explosion
            90f, // sets the size of the explosion
            0.8f // how long the explosion lingers for
        );
        engine.addNebulaParticle(point,
            target.getVelocity(),
            NEBULA_SIZE,
            NEBULA_SIZE_MULT,
            NEBULA_RAMPUP,
            0.3f,
            NEBULA_DUR,
            NEBULA_COLOR,
            true
        );
        //play a sound
        Global.getSoundPlayer().playSound(SFX, 1f, 1f, target.getLocation(), target.getVelocity());
    }
    
    public static void dealArmorDamage(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point) {
		CombatEngineAPI engine = Global.getCombatEngine();

		ArmorGridAPI grid = target.getArmorGrid();
		int[] cell = grid.getCellAtLocation(point);
		if (cell == null) return;
		
		int gridWidth = grid.getGrid().length;
		int gridHeight = grid.getGrid()[0].length;
		
		float damageTypeMult = DisintegratorEffect.getDamageTypeMult(projectile.getSource(), target);
		
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
				float damage = DAMAGE * damMult * damageTypeMult;
				damage = Math.min(damage, armorInCell);
				if (damage <= 0) continue;
				
				target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
				damageDealt += damage;
			}
		}
		
		if (damageDealt > 0) {
			if (Misc.shouldShowDamageFloaty(projectile.getSource(), target)) {
				engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, projectile.getSource());
			}
			target.syncWithArmorGridState();
		}
	}
}