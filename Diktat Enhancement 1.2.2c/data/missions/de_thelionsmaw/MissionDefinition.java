package data.missions.de_thelionsmaw;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
	public static boolean isPAGSM = Global.getSettings().getModManager().isModEnabled("PAGSM");
	public void defineMission(MissionDefinitionAPI api) {

		
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false, 5);
		if (!isPAGSM) {
			api.initFleet(FleetSide.ENEMY, "SDS", FleetGoal.ATTACK, true);
		} else {
			api.initFleet(FleetSide.ENEMY, "SFS", FleetGoal.ATTACK, true);
		}

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Erreichen Freeport Defenders");
	if (!isPAGSM) {
		api.setFleetTagline(FleetSide.ENEMY, "Grand Sindrian Diktat Invasion Fleet");
	} else {
		api.setFleetTagline(FleetSide.ENEMY, "Grand Sindrian Fuel Company 'Fuel Inspection Fleet'");
	}
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("Use the Battlestation to distract or lure enemies");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		api.addToFleet(FleetSide.PLAYER, "station2_Standard", FleetMemberType.SHIP, "Erreichen Freeport", false);
		api.addToFleet(FleetSide.PLAYER, "odyssey_Balanced", FleetMemberType.SHIP, "ISS Epiphany", true);
		api.addToFleet(FleetSide.PLAYER, "heron_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "fury_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "venture_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "drover_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "omen_PD", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kite_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kite_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "wayfarer_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "cerberus_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "cerberus_Standard", FleetMemberType.SHIP, false);
		
		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
		if (!isPAGSM) {
			api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "conquest_Elite", FleetMemberType.SHIP, "SDS Andrada's Will", true);
			api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "harbinger_Strike", FleetMemberType.SHIP, "SDS The Lion's Hammer", false);
			api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, false);
		} else {
			api.addToFleet(FleetSide.ENEMY, "sfciapetus_Mixed", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcepimetheus_Pulser", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfciapetus_Mixed", FleetMemberType.SHIP, "SFS Fuel Or Bust", true);
			api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcbia_Hunter", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcbia_Hunter", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfchydria_Pressure", FleetMemberType.SHIP, "SFS The Manager's Maul", false);
			api.addToFleet(FleetSide.ENEMY, "sfcarke_Suppression", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcarke_Suppression", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcarke_Suppression", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcarke_Suppression", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcarke_Suppression", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcbia_Hunter", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcbia_Hunter", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcdram_assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcdram_assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcslent_Defense", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcslent_Defense", FleetMemberType.SHIP, false);
		}
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;

		// Add an asteroid field
		api.addAsteroidField(minX + width/2f, minY + height/2f, 0, 8000f,
								20f, 70f, 200);
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(6000f);
			}
			public void advance(float amount, List events) {
			}
		});
		
	}

}




