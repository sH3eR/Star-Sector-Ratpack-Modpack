package data.missions.riseofthephoenix;

import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "MSS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 10);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Mayasurian navy with heavy fighter complement");
		api.setFleetTagline(FleetSide.ENEMY, "Hegemony fleet under Commodore Jensulte");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("MSS Garuda and Vishnu must survive");
		api.addBriefingItem("Maintain tactical awareness and use superior mobility to choose your battles.");
		api.addBriefingItem("Remember: If you engage the enemy flagship in a fair fight, you will lose.");
		
		// Friendly ships
		api.addToFleet(FleetSide.PLAYER, "MSS_Vishnu_Standard", FleetMemberType.SHIP, "MSS Iowa", true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Mokarran_Standard", FleetMemberType.SHIP, "MSS Vishnu", true);
		api.addToFleet(FleetSide.PLAYER, "conquest_Standard", FleetMemberType.SHIP, "MSS Garuda", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Arjuna_Standard", FleetMemberType.SHIP, "MSS Arjuna", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Javanicus_B_Standard", FleetMemberType.SHIP, "MSS Stingray", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Rhea_Standard", FleetMemberType.SHIP, "MSS Nebulosa", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Garuda_Standard", FleetMemberType.SHIP, "MSS Nebulosa", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Moa_Standard", FleetMemberType.SHIP, "MSS Moa", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Gyrfalcon_Standard", FleetMemberType.SHIP, "MSS Gyr", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Excalibur_Standard", FleetMemberType.SHIP, "MSS Excalibur", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Sarissa_Standard", FleetMemberType.SHIP, "MSS Dundee", false);
		api.addToFleet(FleetSide.PLAYER, "heron_Strike", FleetMemberType.SHIP, "MSS Razorback", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Winghead_Standard", FleetMemberType.SHIP, "MSS Slugger", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Patriot_Standard", FleetMemberType.SHIP, "MSS Stripes", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Hammerhead_TLH", FleetMemberType.SHIP, "MSS Taishaku", false);	
		api.addToFleet(FleetSide.PLAYER, "MSS_Argentavis_Standard", FleetMemberType.SHIP, false);		
		api.addToFleet(FleetSide.PLAYER, "MSS_Spartan_Standard", FleetMemberType.SHIP, false);	
		api.addToFleet(FleetSide.PLAYER, "MSS_Sunder_TLH", FleetMemberType.SHIP, false);		
		api.addToFleet(FleetSide.PLAYER, "MSS_Scaramouche_Standard", FleetMemberType.SHIP, false);		
		api.addToFleet(FleetSide.PLAYER, "MSS_Shikra_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Stingray_Escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Tiburo_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Loyalist_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Carnwennan_Standard", FleetMemberType.SHIP, false);					
		api.addToFleet(FleetSide.PLAYER, "MSS_Nailer_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Mako_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Perryi_F_Standard", FleetMemberType.SHIP, false);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("MSS Vishnu");
		api.defeatOnShipLoss("MSS Garuda");

		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, "HSS Naga", true);
		api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, "HSS Cobra", false);	
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "mora_Support", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, fa/lse);
//		api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false);
		
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 15; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2000);
		
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.4f, "nav_buoy");
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.6f, "nav_buoy");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.3f, "comm_relay");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.7f, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.5f, "sensor_array");
		
		// Add an asteroid field
		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
								20f, 70f, 50);
		
		// Add some planets.  These are defined in data/config/planets.json.
		api.addPlanet(0, 0, 200f, "irradiated", 350f, true);
	}

}

