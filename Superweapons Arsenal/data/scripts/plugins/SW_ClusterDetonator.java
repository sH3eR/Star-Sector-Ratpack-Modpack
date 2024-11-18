package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.DistortionShader;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import org.lazywizard.lazylib.combat.CombatUtils;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.awt.Color;
import java.util.List;
import java.lang.Math;
import java.util.*;

public class SW_ClusterDetonator extends BaseEveryFrameCombatPlugin {
	
	private List <MissileAPI> Mines = new ArrayList();
	private float Counter = 0f;
	private static final Color COLOR1 = new Color(100, 100, 225, 95);
    private static final Color COLOR2 = new Color(100, 100, 250, 115);
    private static final Vector2f ZERO = new Vector2f();
	private float AoE = 750f;
	private float TIMER = 0f;
	float INTERVAL = 0f;
	
    @Override
    public void advance(float amount, List events) {
		CombatEngineAPI engine = Global.getCombatEngine();
		TIMER += amount/2f;
		
		if (engine.isPaused()) {return;}
		
		
		//Get mines
		for(MissileAPI p : engine.getMissiles())
			if (!Mines.contains(p) && p.getMaxFlightTime() == 35.5f && p.getHitpoints() > 0f)
					Mines.add(p);
		Collections.shuffle(Mines);		
		
		if (!Mines.isEmpty())
			Counter += amount;

		for (MissileAPI Mine : Mines){
			Vector2f point = Mine.getLocation();
			boolean Detonated = Mine.isExpired() || Mine.getHitpoints() <= 0f;
			
			//lightning effects
			if (!Detonated){
				if (Counter > 0f && Counter < 0.5f)
					Mine.setGlowRadius(40f);
				else
					Mine.setGlowRadius(0f);
			}
			
			//Detonate mines
			if (TIMER > INTERVAL && Mine.getElapsed() > 10f && !Detonated){
				
				//Detonation interval ranges between 0.1 and 0.3 second
				INTERVAL = 0.1f + (0.2f * (float) Math.random());
				
				TIMER = 0f;
				
				
				engine.spawnEmpArc(Mine.getSource(), point, Mine, Mine,
					DamageType.ENERGY,
					150000,
					0f, // emp 
					100000f, // max range 
					"Photon_Explode",
					0f, // thickness
					COLOR1,
					COLOR1);
		
				RippleDistortion ripple = new RippleDistortion(Mine.getLocation(), ZERO);
					ripple.setSize(1000f);
					ripple.setIntensity(200f);
					ripple.setFrameRate(30f);
					ripple.fadeInSize(0.8f);
					ripple.fadeOutIntensity(0.5f);
					
					
				for (ShipAPI target : CombatUtils.getShipsWithinRange(point, AoE)){
					float Damage = 6000f + (9000f * ((AoE - MathUtils.getDistance(target , point)) / AoE)); //Base Damage is 6000 and grows by an additional 9000 based on Proximity.
					Damage = Damage / 3f; //Damage will be dealt over 3 instances.
					
					if (!target.isStationModule())
					for (int i = 0; i <3; i++)
						engine.spawnEmpArc(Mine.getSource(), point, target, target,
							DamageType.KINETIC,
							Damage,
							0f, // emp 
							100000f, // max range 
							"Photon_Explode",
							0f, // thickness
							COLOR1,
							COLOR1);
					else
					for (int i = 0; i <3; i++)
						engine.spawnEmpArc(Mine.getSource(), point, target, target,
							DamageType.KINETIC,
							Damage * 0.5f, 		//Only 50% of damage is dealt to stations and other modules.
							0f, // emp 
							100000f, // max range 
							"Photon_Explode",
							0f, // thickness
							COLOR1,
							COLOR1);
				}
				
				//Aesthetic
				engine.spawnExplosion(point, ZERO, COLOR1, 1700f, 2f);
				engine.spawnExplosion(point, ZERO, COLOR2, 900f, 0.8f);
				Global.getSoundPlayer().playUISound("Photon_Explode", 1f, 1f);
				DistortionShader.addDistortion(ripple);
				
			}
		}
		if (Counter > 1f)
			Counter = 0f;
	}
}

