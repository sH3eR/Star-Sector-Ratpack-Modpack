package data.missions.thewolfpack;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
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
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Random;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false, 5);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ESCAPE, true, 5);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Mercenary raiders");
		api.setFleetTagline(FleetSide.ENEMY, "Hegemony convoy with escort");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("The Hegemony convoy will attempt to flee towards the top of the map");
		api.addBriefingItem("Controlling the Nav Buoys is critical to preventing a quick escape");
		api.addBriefingItem("Ordering a fleetwide search & destroy will make your ships more aggressive");
		api.addBriefingItem("Ordering your ships to eliminate a target will make them more aggressive");
//		api.addBriefingItem("Disable as many enemy ships as you can");
//		api.addBriefingItem("The Deimos must survive");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
		//api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP, "Deimos", true);
		FleetMemberAPI member0 = api.addToFleet(FleetSide.PLAYER, "aurora_Balanced", FleetMemberType.SHIP, "Deimos", true);
		member0.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.INDEPENDENT), 1, FleetFactoryV3.getSkillPrefForShip(member0), true, null, true, false, 0, new Random()));
//		api.addToFleet(FleetSide.PLAYER, "hyperion_Strike", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "hyperion_Strike", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "hyperion_Attack", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "hyperion_Attack", FleetMemberType.SHIP, false);
		
		FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "wolf_Strike", FleetMemberType.SHIP, false);
        member.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.INDEPENDENT), 1, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, false, 0, new Random()));
		member.getCaptain().setPersonality(Personalities.STEADY);
		FleetMemberAPI member2 = api.addToFleet(FleetSide.PLAYER, "wolf_Strike", FleetMemberType.SHIP, false);
        member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.INDEPENDENT), 1, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 0, new Random()));
		member2.getCaptain().setPersonality(Personalities.STEADY);
		FleetMemberAPI member3 = api.addToFleet(FleetSide.PLAYER, "wolf_Strike", FleetMemberType.SHIP, false);
        member3.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.INDEPENDENT), 1, FleetFactoryV3.getSkillPrefForShip(member3), true, null, true, false, 0, new Random()));
		member3.getCaptain().setPersonality(Personalities.STEADY);
		FleetMemberAPI member4 = api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);
        member4.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.INDEPENDENT), 1, FleetFactoryV3.getSkillPrefForShip(member4), true, null, true, false, 0, new Random()));
		member4.getCaptain().setPersonality(Personalities.STEADY);
		//api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "sunder_CS", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "sunder_CS", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "vigilance_FS", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "vigilance_FS", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "heron_Strike", FleetMemberType.SHIP, false);
		
		//api.defeatOnShipLoss("Deimos");
		//api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
		// Set up the enemy fleet.
		FleetMemberAPI member5 = api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false);
		member5.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 3, FleetFactoryV3.getSkillPrefForShip(member5), true, null, true, true, 1, new Random()));
		member5.getCaptain().setPersonality(Personalities.AGGRESSIVE);
		FleetMemberAPI member6 = api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, false);
		member6.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 3, FleetFactoryV3.getSkillPrefForShip(member6), true, null, true, true, 1, new Random()));
		member6.getCaptain().setPersonality(Personalities.AGGRESSIVE);
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "dram_Light", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false, CrewXPLevel.GREEN);
		//api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false, CrewXPLevel.GREEN);
		api.addToFleet(FleetSide.ENEMY, "buffalo_hegemony_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "buffalo_hegemony_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "buffalo_hegemony_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "buffalo_hegemony_Standard", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false);
		
		
		// Set up the map.
		float width = 18000f;
		float height = 24000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 7; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 800f; 
			api.addNebula(x, y, radius);
		}
		
		api.addObjective(minX + width * 0.25f, minY + 5500, "nav_buoy");
		api.addObjective(minX + width * 0.75f, minY + 5500, "sensor_array");
		
		
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		
		api.addObjective(minX + width * 0.3f, minY + height * 0.75f, "comm_relay");
		api.addObjective(minX + width * 0.7f, minY + height * 0.7f, "nav_buoy");
//		api.addObjective(minX + width * 0.7f + 1000, minY + height * 0.25f - 1000, "comm_relay");
		//api.addObjective(minX + width * 0.8f, minY + height * 0.75f, "nav_buoy");
		//api.addObjective(minX + width * 0.2f, minY + height * 0.25f, "nav_buoy");
		
		//api.getContext().setInitialEscapeRange(3500);
		
		BattleCreationContext context = new BattleCreationContext(null, null, null, null);
		context.setInitialEscapeRange(7000f);
		api.addPlugin(new EscapeRevealPlugin(context));
                api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void init(CombatEngineAPI engine) {
			}
			public void advance(float amount, List events) {
                            if (Global.getCombatEngine().isPaused()) {
                                return;
                            }
                            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                                if (ship.getCustomData().get("poopystinky") == null) {
									if (ship.getCaptain() != null && ship.getOwner() == 0 && ship.getCaptain().getStats().getSkillsCopy().size() > 4) {
                                        String text = "";
                                        for (int u = 4; u < ship.getCaptain().getStats().getSkillsCopy().size(); u++) {
											if (u < ship.getCaptain().getStats().getSkillsCopy().size()-1) {text = text+(((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getLevel() > 1 ?  ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+"+, " :  ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+", ");} else {text = text+(((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getLevel() > 1 ? ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+"+." :  ((MutableCharacterStatsAPI.SkillLevelAPI) ship.getCaptain().getStats().getSkillsCopy().get(u)).getSkill().getName()+".");}
                                        }
									if (ship.getFleetMember() != null) {
									Global.getCombatEngine().getCombatUI().addMessage(1, ship.getFleetMember(), Misc.getPositiveHighlightColor(), ship.getName(), Misc.getTextColor(), "", Global.getSettings().getColor("standardTextColor"), "is skilled in "+text);}
                                    }
                                    ship.setCurrentCR(ship.getCurrentCR()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //Properly adds the max CR, for some reason it cannot be caught as FleetMemberAPI or this would have been easier...
                                    ship.setCRAtDeployment(ship.getCRAtDeployment()+ship.getMutableStats().getMaxCombatReadiness().getModifiedValue()); //This only affects the "score" result of said mission, but the algorithm is mostly 100% since you have to basically LOSE ships to lose score. I don't think this needs setting, but eh couldn't help but tried.
                                    ship.setCustomData("poopystinky", true); //Fires once per ship.
                                }
                            }
                        }
		});
	}
}
