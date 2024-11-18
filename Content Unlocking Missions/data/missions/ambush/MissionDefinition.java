package data.missions.ambush;

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
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false, 5);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Tri-Tachyon phase group Gamma III");
		api.setFleetTagline(FleetSide.ENEMY, "Hegemony special anti-raider patrol force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("Use Sabot SRMs to overload tough targets before finishing them off with Reaper torpedos");
		api.addBriefingItem("Remember: Your armor can safely absorb hits from anti-fighter missiles");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		//api.addToFleet(FleetSide.PLAYER, "harbinger_Strike", FleetMemberType.SHIP, "TTS Invisible Hand", true, CrewXPLevel.VETERAN);
		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
		api.addToFleet(FleetSide.PLAYER, "doom_Strike", FleetMemberType.SHIP, "TTS Invisible Hand", true);
		api.addToFleet(FleetSide.PLAYER, "afflictor_Strike", FleetMemberType.SHIP, "TTS Antithesis", false);
		api.addToFleet(FleetSide.PLAYER, "shade_Assault", FleetMemberType.SHIP, "TTS Blind Consequence", false);

		api.defeatOnShipLoss("TTS Invisible Hand");
		
		// Set up the enemy fleet.
        api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
		//api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
		FleetMemberAPI member = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		FleetMemberAPI member3 = api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, "HSS Judicature", true);
                PersonAPI officer1 = Global.getSettings().createPerson();
                officer1.setPortraitSprite(OfficerManagerEvent.pickPortraitPreferNonDuplicate(Global.getSector().getFaction(Factions.HEGEMONY), officer1.getGender()));
		officer1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
                officer1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
		officer1.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
                officer1.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
                officer1.getStats().setLevel(4);
                officer1.setPersonality(Personalities.AGGRESSIVE);
                member3.setCaptain(officer1);
		FleetMemberAPI member4 = api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, "HSS Executor", false);
                PersonAPI officer2 = Global.getSettings().createPerson();
                officer2.setPortraitSprite(OfficerManagerEvent.pickPortraitPreferNonDuplicate(Global.getSector().getFaction(Factions.HEGEMONY), officer2.getGender()));
		officer2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
                officer2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
		officer2.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
                officer2.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
                officer2.getStats().setLevel(4);
                officer2.setPersonality(Personalities.AGGRESSIVE);
                member4.setCaptain(officer2);
		FleetMemberAPI member5 = api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		FleetMemberAPI member6 = api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		FleetMemberAPI member7 = api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
		FleetMemberAPI member8 = api.addToFleet(FleetSide.ENEMY, "mule_Standard", FleetMemberType.SHIP, false);
		FleetMemberAPI member9 = api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
				
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		api.addNebula(minX + width * 0.5f - 300, minY + height * 0.5f, 1000);
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
				engine.getContext().enemyDeployAll = true;
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




