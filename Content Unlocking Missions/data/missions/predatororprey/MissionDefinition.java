package data.missions.predatororprey;

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
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Hegemony patrol");
		api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon carrier detachment");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the enemy forces");
		api.addBriefingItem("Retreating enemy fighters will lead you to their carrier");
		api.addBriefingItem("Time your advance against the rhythm of enemy torpedo attacks");
		
		// Set up the player's fleet
		//api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, "HSS Shogun", true);
		FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "dominator_AntiCV", FleetMemberType.SHIP, "HSS Shogun", true);
		PersonAPI daudofficer = Global.getSettings().createPerson();
			daudofficer.getName().setFirst("Baikal");
                        daudofficer.getName().setLast("Daud");
                        daudofficer.setGender(FullName.Gender.MALE);
                        daudofficer.setFaction(Factions.HEGEMONY);
                        daudofficer.setPortraitSprite(Global.getSettings().getSpriteName("characters", "baikal"));
                        daudofficer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
                        member.getRepairTracker().setCR(0.85f); //Current Workaround
			//daudofficer.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
                        daudofficer.getStats().setLevel(1);
                        daudofficer.setPersonality(Personalities.STEADY);
                        member.setCaptain(daudofficer);
		//api.addToFleet(FleetSide.PLAYER, "falcon_xiv_Escort", FleetMemberType.SHIP, "HSS Wyrm", false, CrewXPLevel.VETERAN);
		//api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, "HSS Wyrm", false, CrewXPLevel.VETERAN);
		api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, "HSS Wyrm", false);
		api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, "HSS Qulla", false);
		//api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, "HSS Jake", false);
		//api.addToFleet(FleetSide.PLAYER, "wolf_hegemony_CS", FleetMemberType.SHIP, "HSS Boxer", false, CrewXPLevel.REGULAR);
		//api.addToFleet(FleetSide.PLAYER, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
		
		// Set up the enemy fleet
		FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "astral_Strike", FleetMemberType.SHIP, "TTS Ephemeral", false);
		member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 5, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, true, 3, new Random()));
		//api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, "TTS Cassandra", false);
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 300; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/4;
			
			if (x > -1000 && x < 1500 && y < -1000) continue;
			float radius = 200f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		
		
		api.addObjective(minX + width * 0.7f - 3000, minY + height * 0.65f, "nav_buoy");
		api.addObjective(minX + width * 0.5f, minY + height * 0.35f + 2000, "nav_buoy");
		api.addObjective(minX + width * 0.2f + 3000, minY + height * 0.6f, "sensor_array");
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(12000f);
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
                        }});
			
	}

}






