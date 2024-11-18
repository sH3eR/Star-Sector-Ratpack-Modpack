package data.scripts.weapons;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

import org.lwjgl.util.vector.Vector2f;


public class MSS_SwappableRailgunHEProjOnHitEffect implements OnHitEffectPlugin
{
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null) {
            return;
        }
        
        if (!shieldHit) 
        {
            // graphical only, zero damage
            DamagingExplosionSpec explosionSpec = new DamagingExplosionSpec(0.1f, 
                MSS_SwappableRailgunHEProjEffect.RADIUS, 
                MSS_SwappableRailgunHEProjEffect.CORE_RADIUS,
                0f, 
                0f, 
                CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
                CollisionClass.HITS_SHIPS_AND_ASTEROIDS, 
                3.0f, 
                3.0f, 
                0.25f, 
                100, 
                MSS_SwappableRailgunHEProjEffect.EXPLOSION_COLOR, 
                MSS_SwappableRailgunHEProjEffect.EXPLOSION_COLOR);
        explosionSpec.setDamageType(DamageType.HIGH_EXPLOSIVE);
        engine.spawnDamagingExplosion(explosionSpec, projectile.getSource(), projectile.getLocation(), false);
        Global.getSoundPlayer().playSound("mine_explosion", 1.0f, 1.0f, projectile.getLocation(), projectile.getVelocity());
        }
    }
}
