package data.missions.xiv;

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
		api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "MSS", FleetGoal.ATTACK, true, 10);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Hegemony XIV Fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Mayasurian Elite Defense Force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Eliminate the Mayasuran fleet.");
		
		// Friendly ships
		api.addToFleet(FleetSide.ENEMY, "MSS_Mokarran_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Javanicus_B_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Winghead_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Sarissa_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Sarissa_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Heron_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Heron_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Falcon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Falcon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Argentavis_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Hammerhead_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Sunder_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_vigilance_m_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Nailer_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "MSS_Mako_Elite", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.PLAYER, "onslaught_xiv_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "onslaught_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "legion_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "legion_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "eagle_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "eagle_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "falcon_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "falcon_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 15000f;
		float height = 15000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlanet(0, 0, 50f, "star_yellow", 250f, true);
		
	}

}
