package data.missions.de_curiositycaughtthepheonix;

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

	public void defineMission(MissionDefinitionAPI api) {

		boolean isHMI = Global.getSettings().getModManager().isModEnabled("HMI");
		
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false, 5);
		api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Hegemony Task Force 11th Jangala");
		api.setFleetTagline(FleetSide.ENEMY, "Remnant Automated Defense Systems");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("Use Kinetic weapons and missiles to overpower the advanced shielding of the Remnants");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		//api.addToFleet(FleetSide.PLAYER, "harbinger_Strike", FleetMemberType.SHIP, "TTS Invisible Hand", true, CrewXPLevel.VETERAN);
		api.addToFleet(FleetSide.PLAYER, "onslaught_Standard", FleetMemberType.SHIP, "HSS Tirpin", true);
		api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dominator_AntiCV", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "eradicator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "condor_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "condor_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "vanguard_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "vanguard_Attack", FleetMemberType.SHIP, false);

		api.defeatOnShipLoss("HSS Tirpin");
		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
		if (!isHMI) {
			api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, "TTDS Aleph Null", true);
			api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
		} else {
			api.addToFleet(FleetSide.ENEMY, "hmi_coelum_attack", FleetMemberType.SHIP, "TTDS Aleph Null", true);
			api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "revelio_cs", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "aureole_sabot", FleetMemberType.SHIP, false);
			api.addToFleet(FleetSide.ENEMY, "aureole_sabot", FleetMemberType.SHIP, false);
		}

		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		api.addNebula(minX + width * 0.5f - 300, minY + height * 0.5f, 1000);
		api.addNebula(minX + width * 0.5f + 300, minY + height * 0.5f, 1000);
		api.addNebula(minX + width * 0.5f + 300, minY + height * 0.5f, 1000);
		api.addNebula(minX + width * 0.5f + 300, minY + height * 0.5f, 1000);
		
		for (int i = 0; i < 5; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
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




