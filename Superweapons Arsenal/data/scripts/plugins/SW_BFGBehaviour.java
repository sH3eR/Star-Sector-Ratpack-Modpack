package data.scripts.plugins;

import com.fs.starfarer.api.Global;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import org.lazywizard.lazylib.combat.CombatUtils;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.awt.Color;

public class SW_BFGBehaviour extends BaseEveryFrameCombatPlugin {
	
	private static final Color COLOR = new Color(70, 200, 70, 150);
	private static final float SEARCH_RANGE = 1000f;
	float Counter = 0f;
		
    @Override
    public void advance(float amount, List events) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused()) {return;}
		
		Counter = Counter + amount;
		
		if (Counter > 0.33f){
			Counter = 0f;
			
			for (DamagingProjectileAPI proj : engine.getProjectiles()){
				if (proj.getWeapon() != null)
				if (proj.getWeapon().getSpec().hasTag("sw_bfg")){
					
					Vector2f point = proj.getLocation();
					WeaponAPI weapon = proj.getWeapon();
					float damage = weapon.getDamage().getDamage() / 6.6f;		//Base Shock damage is 15%
						
					if (!proj.didDamage() && !proj.isFading() && !proj.isExpired()){
						for (ShipAPI test : CombatUtils.getShipsWithinRange(point, SEARCH_RANGE)){
							if (test != weapon.getShip() && test.isAlive() && !test.isAlly() && weapon.getShip().getOriginalOwner() != test.getOriginalOwner()){
								
								if (!test.isStationModule()){
									engine.spawnEmpArc(proj.getSource(), point, test, test,
										DamageType.ENERGY,
										damage / 3f, 							//Shock damage is reduced to 33% of its original value but will occur 3 times every second, This is done to reduce overall effectiveness against armor			
										10f, 																	
										100000f, 
										"BFG_Shock",
										40f, 	
										COLOR,
										COLOR.brighter());
								}
								else{
									engine.spawnEmpArc(proj.getSource(), point, test, test,
										DamageType.ENERGY,
										(damage / 3f) * 0.4f,					//Damage to Stations is much lower
										10f, 																	
										100000f, 
										"BFG_Shock",
										30f, 	
										COLOR,
										COLOR.brighter());
								}
							}
						}
					} 	
				}
			}
		}		
	}
}
