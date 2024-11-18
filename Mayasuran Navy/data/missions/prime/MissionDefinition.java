package data.missions.prime;

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
		api.setFleetTagline(FleetSide.PLAYER, "Mayasurian Fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Hegemony Targets");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Eliminate the Hegemony fleet.");
		
		// Friendly ships
		api.addToFleet(FleetSide.PLAYER, "MSS_Vishnu_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Victory_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Pegasus_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Mokarran_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Conquest_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Javanicus_B_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Arjuna_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Pelican_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Skyrend_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Winghead_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Aurora_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Rhea_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Garuda_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Champion_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Crusader_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Paladin_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Excalibur_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Moa_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Sarissa_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Heron_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Gryphon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Gyrfalcon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Falcon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Patriot_Elite", FleetMemberType.SHIP, "MSS Stripes", false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Starliner2_Elite", FleetMemberType.SHIP, true);
		api.addToFleet(FleetSide.PLAYER, "MSS_Argentavis_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Spartan_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Scaramouche_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Shikra_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Stingray_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Manta_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Hammerhead_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Sunder_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_vigilance_m_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Carnwennan_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Loyalist_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Tiburo_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Nailer_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Mako_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "MSS_Perryi_F_Elite", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "onslaught_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "legion_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "legion_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eagle_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eagle_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);

		
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
