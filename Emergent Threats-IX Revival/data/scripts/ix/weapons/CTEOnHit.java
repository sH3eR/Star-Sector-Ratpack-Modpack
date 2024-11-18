package data.scripts.ix.weapons;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import data.scripts.ix.util.DistanceUtil;
import data.scripts.ix.util.ShapedExplosionUtil;

public class CTEOnHit implements OnFireEffectPlugin, OnHitEffectPlugin {
	
	private static float DMG_MULT = 0.5f;
	private static float EMP_MULT = 1.0f;
	private static float TRIGGER_DISTANCE_NORM = 800f;
	private static float TRIGGER_DISTANCE_GOOD = 1000f;
	private static float TRIGGER_DISTANCE_BEST = 1200f;
	private static float TRIGGER_DISTANCE_ABYSS = 1400f;
	
	private static String PROJECTOR_ID = "vice_adaptive_entropy_projector";
	private static String PROJECTOR_A_ID = "vice_adaptive_entropy_projector_abyssal";
	
	private static Color CORE_COLOR = new Color(200,200,255,255);	
	private static Color EMP_PURPLE = new Color(220,50,255,255);
	private static Color NULL_COLOR = new Color(0,0,0,0);
	
    public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
		
		ShipAPI ship = weapon.getShip();
		
		Vector2f projLoc = MathUtils.getPointOnCircumference(proj.getLocation(), 12f, weapon.getCurrAngle());
        engine.addHitParticle(
				projLoc,
				ship.getVelocity(),
				70.0f, //size
				1.0f, //brightness
				1.3f, //duration
				EMP_PURPLE);
		
		engine.addHitParticle(
				proj.getLocation(),
				ship.getVelocity(),
				40.0f, //size
				2.5f, //brightness
				1.0f, //duration
				CORE_COLOR);
				
		float angle = weapon.getCurrAngle() - 180f;
		Vector2f loc = MathUtils.getPointOnCircumference(weapon.getLocation(), 18f, angle);
		Vector2f shipVelocity = weapon.getShip().getVelocity();
		float speed = (float) Math.sqrt(shipVelocity.lengthSquared());
		ShapedExplosionUtil.spawnShapedExplosion(loc, angle, speed, EMP_PURPLE, "CTE");
    }
	
	private String getCoreType (ShipAPI ship) {
		ShipVariantAPI v = ship.getVariant();
		String coreId = null;
		try { 
			coreId = ship.getFleetMember().getCaptain().getAICoreId();
		}
		catch (Exception e) {
			return "other";
		}
		if (("rat_chronos_core").equals(coreId) || (v.hasHullMod("rat_chronos_conversion"))) return "chronos";
		else if (("rat_cosmos_core").equals(coreId) || (v.hasHullMod("rat_cosmos_conversion"))) return "cosmos";
		else return "other";
	}
	
	//synthesis xo Spacetime Analytics bonuses should only apply to player ships
	private boolean isSpacetimeAnalyticsActive(ShipAPI ship) {
		if (ship.getFleetMember() == null || ship.getFleetMember().getOwner() != 0) return false;
		return Global.getSector().getMemoryWithoutUpdate().is("$xo_spacetime_analytics_is_active", true);
	}
	
	private boolean isInPulseRange(Vector2f source, Vector2f point, ShipAPI ship) {
		ShipVariantAPI variant = ship.getVariant();
		float maxDistance = TRIGGER_DISTANCE_NORM;
		if (variant.hasHullMod(PROJECTOR_A_ID)) {
			if (getCoreType(ship).equals("cosmos")) maxDistance = TRIGGER_DISTANCE_ABYSS;
			else if (isSpacetimeAnalyticsActive(ship)) maxDistance = TRIGGER_DISTANCE_BEST;
			else maxDistance = TRIGGER_DISTANCE_GOOD;
		}
		else if (variant.hasHullMod(PROJECTOR_ID)) {
			if (isSpacetimeAnalyticsActive(ship)) maxDistance = TRIGGER_DISTANCE_BEST;
			else maxDistance = TRIGGER_DISTANCE_GOOD;
		}
		return DistanceUtil.getDistance(source, point) <= maxDistance;
	}

	public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, 
						ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		ShipAPI thisShip = proj.getSource();
		
		Vector2f weaponLoc = MathUtils.getPoint(proj.getWeapon().getLocation(), 14f, proj.getWeapon().getCurrAngle());
		
		if (target instanceof ShipAPI && isInPulseRange(weaponLoc, point, thisShip)) {
			engine.spawnEmpArc(
					thisShip, weaponLoc, thisShip, target,
					DamageType.ENERGY, 
					proj.getDamageAmount() * DMG_MULT, // damage
					proj.getDamageAmount() * EMP_MULT, // emp 
					100000f, // max range 
					"",
					20f,
					NULL_COLOR, //EMP_PURPLE,
					NULL_COLOR //CORE_COLOR
			);
			//for double anchoring
			engine.spawnEmpArcVisual(
					weaponLoc, 
					thisShip, 
					point, 
					null,
					20f, 
					EMP_PURPLE,
					CORE_COLOR
			);
			engine.spawnEmpArcVisual(
					weaponLoc, 
					thisShip, 
					point, 
					null,
					20f, 
					EMP_PURPLE,
					CORE_COLOR
			);
			Global.getSoundPlayer().playSound("cte_tw_pulse", 1f, 1f, point, new Vector2f());
		}
		else Global.getSoundPlayer().playSound("cte_tw_hit", 1f, 1f, point, new Vector2f());
		
		MagicLensFlare.createSharpFlare(
				engine,
				proj.getSource(),
				point,
				10,		//thickness
				600,	//length
				0,		//angle
				EMP_PURPLE,	//fringe
				CORE_COLOR	//core
		);
		MagicLensFlare.createSharpFlare(
				engine,
				proj.getSource(),
				point,
				8,		//thickness
				400,	//length
				60,		//angle
				EMP_PURPLE,	//fringe
				CORE_COLOR	//core
		);
		MagicLensFlare.createSharpFlare(
				engine,
				proj.getSource(),
				point,
				8,		//thickness
				400,	//length
				300,		//angle
				EMP_PURPLE,	//fringe
				CORE_COLOR	//core
		);
	}
}