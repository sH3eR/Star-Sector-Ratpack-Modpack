package data.missions.direstraits;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
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

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ESCAPE, false, 5);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true, 5);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Hegemony relief fleet with mercenary escort");
		api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon containment task force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("ISS Black Star must survive");
		api.addBriefingItem("At least 25% of the Hegemony forces must escape");
		
		// Set up the player's fleet
		FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "hammerhead_Elite", FleetMemberType.SHIP, "ISS Black Star", true);
        PersonAPI raoofficer = Global.getSettings().createPerson();
		raoofficer.getName().setFirst("Orcus");
        raoofficer.getName().setLast("Rao");
        raoofficer.setGender(FullName.Gender.MALE);
		raoofficer.setFaction(Factions.HEGEMONY);
        raoofficer.setPortraitSprite(Global.getSettings().getSpriteName("characters", "orcus_rao"));
        raoofficer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
		raoofficer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
        raoofficer.getStats().setLevel(2);
        raoofficer.setPersonality(Personalities.RECKLESS);
        member.setCaptain(raoofficer);
		FleetMemberAPI member2 = api.addToFleet(FleetSide.PLAYER, "dominator_Outdated", FleetMemberType.SHIP, "HSS Temblor", false);
        member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 4, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 0, new Random()));
        member2.getCaptain().setPersonality(Personalities.AGGRESSIVE);
		FleetMemberAPI member3 = api.addToFleet(FleetSide.PLAYER, "monitor_Escort", FleetMemberType.SHIP, "HSS Aspis", false);
		member3.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 1, FleetFactoryV3.getSkillPrefForShip(member3), true, null, true, false, 0, new Random()));
		member3.getCaptain().setPersonality(Personalities.CAUTIOUS);
		FleetMemberAPI member4 = api.addToFleet(FleetSide.PLAYER, "monitor_Escort", FleetMemberType.SHIP, "HSS Aegis", false);
		member4.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 1, FleetFactoryV3.getSkillPrefForShip(member4), true, null, true, false, 0, new Random()));
		member4.getCaptain().setPersonality(Personalities.CAUTIOUS);
		api.addToFleet(FleetSide.PLAYER, "buffalo2_FS", FleetMemberType.SHIP, "HSS Archer", false).getCaptain().setPersonality(Personalities.TIMID);
		api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, "HSS Gadfly", false).getCaptain().setPersonality(Personalities.CAUTIOUS);
		api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, "HSS Midge", false).getCaptain().setPersonality(Personalities.CAUTIOUS);
		api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, "Daisy", false).getCaptain().setPersonality(Personalities.CAUTIOUS);
		api.addToFleet(FleetSide.PLAYER, "hound_Standard", FleetMemberType.SHIP, "Lucy", false).getCaptain().setPersonality(Personalities.CAUTIOUS);
		//api.addToFleet(FleetSide.PLAYER, "atlas_Standard", FleetMemberType.SHIP, "Charles", false).getCaptain().setPersonality(Personalities.TIMID);
		api.addToFleet(FleetSide.PLAYER, "colossus_Standard", FleetMemberType.SHIP, "Chuck", false).getCaptain().setPersonality(Personalities.TIMID);
		api.addToFleet(FleetSide.PLAYER, "colossus_Standard", FleetMemberType.SHIP, "Charlie", false).getCaptain().setPersonality(Personalities.TIMID);
		
		api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.CAUTIOUS);
		api.addToFleet(FleetSide.PLAYER, "valkyrie_Elite", FleetMemberType.SHIP, "HSS Hrund", false).getCaptain().setPersonality(Personalities.STEADY);
		api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
		api.addToFleet(FleetSide.PLAYER, "tarsus_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
		api.addToFleet(FleetSide.PLAYER, "hermes_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
		api.addToFleet(FleetSide.PLAYER, "mercury_PD", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("ISS Black Star");
		//api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
		// Set up the enemy fleet
		api.getDefaultCommander(FleetSide.ENEMY).getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
		FleetMemberAPI member5 = api.addToFleet(FleetSide.ENEMY, "astral_Strike", FleetMemberType.SHIP, "TTS August", false);
		member5.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 3, FleetFactoryV3.getSkillPrefForShip(member5), true, null, true, true, 2, new Random()));
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, "TTS Stheno", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, "TTS Euryale", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, "TTS Euryale", false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false); // I hate you, Omen. You're so mean.
		
		
		// Set up the map.
		float width = 18000f;
		float height = 24000f;
		
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 15; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		
		api.addNebula(minX + width * 0.8f, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.8f, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.8f, minY + height * 0.6f, 2000);
		
		api.addObjective(minX + width * 0.8f, minY + height * 0.4f, "sensor_array");
		api.addObjective(minX + width * 0.8f, minY + height * 0.6f, "nav_buoy");
		api.addObjective(minX + width * 0.3f, minY + height * 0.3f, "nav_buoy");
		api.addObjective(minX + width * 0.3f, minY + height * 0.7f, "sensor_array");
		api.addObjective(minX + width * 0.2f, minY + height * 0.5f, "comm_relay");

		api.addAsteroidField(minX + width * 0.5f, minY + height, 270, width,
								20f, 70f, 50);
		
		api.addPlanet(0, 0, 200f, "barren", 0f, true);
		
		BattleCreationContext context = new BattleCreationContext(null, null, null, null);
		context.setInitialEscapeRange(7000f);
		api.addPlugin(new EscapeRevealPlugin(context));
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






