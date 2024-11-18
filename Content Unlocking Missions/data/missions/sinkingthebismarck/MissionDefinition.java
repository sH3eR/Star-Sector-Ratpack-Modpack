package data.missions.sinkingthebismarck;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Tri-Tachyon recon detachment and supply craft");
		api.setFleetTagline(FleetSide.ENEMY, "HSS Bismar");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Destroy the HSS Bismar");
		api.addBriefingItem("The TTS Chimera is a valuable prototype and must survive");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		api.addToFleet(FleetSide.PLAYER, "hyperion_Strike", FleetMemberType.SHIP, "TTS Chimera", true);
		//api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false, CrewXPLevel.VETERAN);
		api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.PLAYER, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
		api.addToFleet(FleetSide.PLAYER, "gemini_Standard", FleetMemberType.SHIP, false);
		
		// Set up the enemy fleet.
		FleetMemberAPI member = api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, "HSS Bismar", false);
        PersonAPI officer = OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 5, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, true, 3, new Random());
		officer.getName().setFirst("Kane");
        officer.getName().setLast("Gleise");
        officer.setGender(FullName.Gender.MALE);
        officer.setPortraitSprite(OfficerManagerEvent.pickPortraitPreferNonDuplicate(Global.getSector().getFaction(Factions.HEGEMONY), FullName.Gender.MALE));
        officer.getStats().setLevel(5);
        officer.setPersonality(Personalities.RECKLESS);
        member.setCaptain(officer);
		//api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false, CrewXPLevel.VETERAN);
		//api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false, CrewXPLevel.VETERAN);
		
		api.defeatOnShipLoss("TTS Chimera");
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add nebula clouds
		api.addNebula(minX + width * 0.4f, minY + height * 0.5f, 1000);
		api.addNebula(minX + width * 0.5f, minY + height * 0.5f, 1200);
		api.addNebula(minX + width * 0.6f, minY + height * 0.5f, 1400);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 5; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, 
						 "sensor_array");
		api.addObjective(minX + width * 0.2f + 3000, minY + height * 0.25f + 2000, 
						 "nav_buoy");
		api.addObjective(minX + width * 0.8f - 3000, minY + height * 0.75f - 2000, 
						 "nav_buoy");
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(10000f);
			}
			public void advance(float amount, List events) {
                            if (Global.getCombatEngine().isPaused()) {
                                return;
                            }
                            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                                if (ship.getCustomData().get("poopystinky") == null) {
                                    ship.setCurrentCR(ship.getCurrentCR()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //Properly adds the max CR, for some reason it cannot be caught as FleetMemberAPI or this would have been easier...
                                    ship.setCRAtDeployment(ship.getCRAtDeployment()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //This only affects the "score" result of said mission, but the algorithm is mostly 100% since you have to basically LOSE ships to lose score. I don't think this needs setting, but eh couldn't help but tried.
                                    ship.setCustomData("poopystinky", true); //Fires once per ship.
                                }
                            }
			}
		});
		
		
		api.addPlanet(0, 0, 50f, StarTypes.RED_GIANT, 250f, true);
	}

}
