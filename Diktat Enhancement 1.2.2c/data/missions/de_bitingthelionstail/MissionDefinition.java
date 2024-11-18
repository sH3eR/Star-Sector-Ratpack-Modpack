package data.missions.de_bitingthelionstail;

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
		api.setFleetTagline(FleetSide.PLAYER, "Erreichen Elite Task Force");
		if (!isPAGSM) {
			api.setFleetTagline(FleetSide.ENEMY, "Sindrian Diktat Forward Fleet");
		} else {
			api.setFleetTagline(FleetSide.ENEMY, "Sindrian Fuel Company 'Fuel Inspectors'");
		}
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("Use your forces' heightened mobility to outrun the comparatively slower enemy forces");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		//api.addToFleet(FleetSide.PLAYER, "harbinger_Strike", FleetMemberType.SHIP, "TTS Invisible Hand", true, CrewXPLevel.VETERAN);
		api.addToFleet(FleetSide.PLAYER, "hyperion_Strike", FleetMemberType.SHIP, "ISS The Grasp of Helios", true);
		api.addToFleet(FleetSide.PLAYER, "fury_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "shrike_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "medusa_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "medusa_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "scarab_Experimental", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "scarab_Experimental", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "shade_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "afflictor_Strike", FleetMemberType.SHIP, false);

		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
		if (!isPAGSM) {
			api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, true);
			api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "dominator_Outdated", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "kite_Standard", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "kite_Standard", FleetMemberType.SHIP, false);
		} else{
			api.addToFleet(FleetSide.ENEMY, "sfciapetus_Mixed", FleetMemberType.SHIP, true);
			api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcbia_Hunter", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcbia_Hunter", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcbia_Hunter", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfctahlanbento_Pulser", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfccoeus_Patrol", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcdram_assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcdram_assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcslent_Offense", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "sfcslent_Offense", FleetMemberType.SHIP, false);
		}
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;


		
		// Add an asteroid field
		api.addAsteroidField(minX + width/2f, minY + height/2f, 0, 8000f,
								20f, 70f, 100);
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(6000f);
			}
			public void advance(float amount, List events) {
			}
		});
		
	}

}




