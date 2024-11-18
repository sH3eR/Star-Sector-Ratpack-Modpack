package data.missions.afistfulofcredits;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.BattleObjectives;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.List;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 3);
//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);
		
		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "'Just an honest trader trying to make a living, officer.'");
		api.setFleetTagline(FleetSide.ENEMY, "No-good two-timing 'High Rad' Moon Salazar in a rustbucket mule");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Show 'High Rad' Moon what it means to break a deal");
		api.addBriefingItem("Don't lose 'Stranger II' - it's my most valuable possession");
		api.addBriefingItem("Moon's ship has delicate engine mods; use Salamander Missiles to leave her adrift");
		
		boolean testMode = false;
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		//api.addToFleet(FleetSide.PLAYER, "station_small_Standard", FleetMemberType.SHIP, "Test Station", false);
		if (!testMode) {
			api.addToFleet(FleetSide.PLAYER, "lasher_Standard", FleetMemberType.SHIP, "Stranger II", true);
			api.addToFleet(FleetSide.PLAYER, "hound_d_Standard", FleetMemberType.SHIP, "Milk Run", false);
			
			// Set up the enemy fleet.
			
			FleetMemberAPI member = api.addToFleet(FleetSide.ENEMY, "mule_d_pirates_Smuggler", FleetMemberType.SHIP, "Cherenkov Bloom", false);
			//member.getRepairTracker().setCR(0.85f); //Current Workaround; not really, the everyframescript below should add that in...
            PersonAPI officer = OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.PIRATES), 1, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, false, 0, new Random());
			//AI rolled Combat Endurance? Give them Target Analysis.
			if (officer.getStats().getSkillLevel(Skills.COMBAT_ENDURANCE) > 0) {officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);}
			officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            officer.getStats().setLevel(2);
			officer.getName().setFirst("Moon");
            officer.getName().setLast("Salazar");
            officer.setGender(FullName.Gender.FEMALE);
            officer.setPortraitSprite(OfficerManagerEvent.pickPortraitPreferNonDuplicate(Global.getSector().getFaction(Factions.PIRATES), FullName.Gender.FEMALE));
            officer.setPersonality("reckless");
            member.setCaptain(officer);
			api.defeatOnShipLoss("Stranger II");
		}
		
		if (testMode) {
//			FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "omen_PD", FleetMemberType.SHIP, "Milk Run", true);
//			member.getCaptain().getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
//			member.getCaptain().getStats().setSkillLevel(Skills.SHIELD_MODULATION, 2);
//			member.getCaptain().getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
			
			api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, "Stranger II", true);
	//		PersonAPI person = new AICoreOfficerPluginImpl().createPerson(Commodities.ALPHA_CORE, null, null);
	//		member.setCaptain(person);
			
			api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, "Cherenkov Bloom", false);
			api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, null, false);
			api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, null, false);
			
			api.addObjective(0, 4000, BattleObjectives.SENSOR_JAMMER);
			api.addObjective(4000, 0, BattleObjectives.COMM_RELAY);
			api.addObjective(-3000, -2000, BattleObjectives.NAV_BUOY);
		}
		
		// Set up the map.
		float width = 12000f;
		float height = 12000f;
		
		if (testMode) {
			width += 4000;
			height += 8000;
		}
		
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
		api.addPlanet(0, 0, 50f, StarTypes.RED_GIANT, 250f, true);
                
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
			}
			public void advance(float amount, List events) {
                            if (Global.getCombatEngine().isPaused()) {
                                return;
                            }
                            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                                if (ship.getCustomData().get("poopystinky") == null) {
                                    //ship.getCaptain().getStats().getSkillsCopy().
                                    //Global.getCombatEngine().getCombatUI().addMessage(1, ship.getFleetMember(), Misc.getNegativeHighlightColor(), ship.getCaptain().getNameString(), Misc.getTextColor(), ": ", Global.getSettings().getColor("standardTextColor"), "");
                                    ship.setCurrentCR(ship.getCurrentCR()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //Properly adds the max CR, for some reason it cannot be caught as FleetMemberAPI or this would have been easier...
                                    ship.setCRAtDeployment(ship.getCRAtDeployment()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //This only affects the "score" result of said mission, but the algorithm is mostly 100% since you have to basically LOSE ships to lose score. I don't think this needs setting, but eh couldn't help but tried.
                                    ship.setCustomData("poopystinky", true); //Fires once per ship?
                                }
                            }
                        }
		});
		
	}
        

}
