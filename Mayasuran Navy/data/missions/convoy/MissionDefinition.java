package data.missions.convoy;

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
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true, 10);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "MSS Poseidon at the head of a survey fleet");
		api.setFleetTagline(FleetSide.ENEMY, "A dangerous foe");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		
		// Friendly ships
		api.addToFleet(FleetSide.PLAYER, "MSS_Pelican_Standard", FleetMemberType.SHIP, "MSS Poseidon", true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Iapetus_Standard", FleetMemberType.SHIP, "MSS Zeus", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Pilgrim_Standard", FleetMemberType.SHIP, "MSS Artemis", false);
		api.addToFleet(FleetSide.PLAYER, "gemini_Standard", FleetMemberType.SHIP, "MSS Phobos", false);
		api.addToFleet(FleetSide.PLAYER, "wayfarer_Standard", FleetMemberType.SHIP, "MSS Apollo", false);
		// Mark player flagship as essential

		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "guardian_Standard", FleetMemberType.SHIP, "ISS Foolish Mistake", true);
		
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 25; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 400f + (float) Math.random() * 1000f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.2f + 400 + 3000, minY + height * 0.2f + 400 + 2000, "sensor_array");
		api.addObjective(minX + width * 0.4f + 2000, minY + height * 0.7f, "sensor_array");
		api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.7f, "comm_relay");
		api.addObjective(minX + width * 0.2f + 3000, minY + height * 0.5f, "nav_buoy");
		api.addObjective(minX + width * 0.85f - 3000, minY + height * 0.4f, "nav_buoy");
		
		//api.addPlanet(0, 0, 500f, "ice_giant", 300f, true);
	}

}