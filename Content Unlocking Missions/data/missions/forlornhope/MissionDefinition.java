package data.missions.forlornhope;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
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
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
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
		api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false, 2);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The TTS Invincible");
		api.setFleetTagline(FleetSide.ENEMY, "Leading elements of the Hegemony Defense Fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("The TTS Invincible must survive");
		
		// Set up the player's fleet
		FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "paragon_Escort", FleetMemberType.SHIP, "TTS Invincible", true);
            PersonAPI officer = Global.getSettings().createPerson();
			officer.setName(new FullName("Beta Core", "", Gender.ANY));
            officer.setAICoreId(Commodities.BETA_CORE);
            officer.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
			officer.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
			officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2); //officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2); too op?
			officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            //member.getRepairTracker().setCR(0.85f); //Current Workaround
            officer.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
			officer.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
			officer.getStats().setLevel(6);
            officer.setPortraitSprite("graphics/portraits/portrait_ai3b.png");
            officer.setPersonality("reckless");
            member.setCaptain(officer);
		//api.addToFleet(FleetSide.PLAYER, "onslaught_Standard", FleetMemberType.SHIP, "TTS Invincible", true, CrewXPLevel.ELITE);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("TTS Invincible");
		
		FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, false);
		member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 3, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, true, 1, new Random()));
		FleetMemberAPI member3 = api.addToFleet(FleetSide.ENEMY, "dominator_XIV_Elite", FleetMemberType.SHIP, false);
                        PersonAPI daudofficer = Global.getSettings().createPerson();
			daudofficer.getName().setFirst("Baikal");
                        daudofficer.getName().setLast("Daud");
                        daudofficer.setGender(FullName.Gender.MALE);
						daudofficer.setFaction(Factions.HEGEMONY);
                        daudofficer.setPortraitSprite(Global.getSettings().getSpriteName("characters", "baikal"));
                        daudofficer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
                        //member3.getRepairTracker().setCR(0.85f); //Current Workaround
			daudofficer.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
                        daudofficer.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 1);
			daudofficer.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1);
                        daudofficer.getStats().setLevel(4);
                        daudofficer.setPersonality(Personalities.STEADY);
                        member3.setCaptain(daudofficer);
		FleetMemberAPI member4 = api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);
		FleetMemberAPI member5 = api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
		FleetMemberAPI member6 = api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false);
		FleetMemberAPI member7 = api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		FleetMemberAPI member8 = api.addToFleet(FleetSide.ENEMY, "eagle_xiv_Elite", FleetMemberType.SHIP, false);
		PersonAPI raoofficer = Global.getSettings().createPerson();
			raoofficer.getName().setFirst("Orcus");
                        raoofficer.getName().setLast("Rao");
                        raoofficer.setGender(FullName.Gender.MALE);
						raoofficer.setFaction(Factions.HEGEMONY);
                        raoofficer.setPortraitSprite(Global.getSettings().getSpriteName("characters", "orcus_rao"));
                        raoofficer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2); //he already learned this? let's elite it!
			raoofficer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
                        raoofficer.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 1);
			raoofficer.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1);
                        raoofficer.getStats().setLevel(4);
                        raoofficer.setPersonality(Personalities.RECKLESS);
                        member8.setCaptain(raoofficer);
		FleetMemberAPI member9 = api.addToFleet(FleetSide.ENEMY, "falcon_xiv_Escort", FleetMemberType.SHIP, false);
                        PersonAPI solomanofficer = Global.getSettings().createPerson();
			solomanofficer.getName().setFirst("Ardis");
                        solomanofficer.getName().setLast("Soloman");
                        solomanofficer.setGender(FullName.Gender.MALE);
                        solomanofficer.setPortraitSprite(OfficerManagerEvent.pickPortraitPreferNonDuplicate(Global.getSector().getFaction(Factions.HEGEMONY), FullName.Gender.FEMALE));
			solomanofficer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
                        solomanofficer.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 1);
			solomanofficer.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1);
                        solomanofficer.getStats().setLevel(3);
                        solomanofficer.setPersonality(Personalities.AGGRESSIVE);
                        member9.setCaptain(solomanofficer);
		//api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		FleetMemberAPI member10 = api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
		member10.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.HEGEMONY), 2, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, false, 0, new Random()));
		FleetMemberAPI member11 = api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		FleetMemberAPI member12 = api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		FleetMemberAPI member13 = api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		FleetMemberAPI member14 = api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		FleetMemberAPI member15 = api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
		FleetMemberAPI member16 = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		FleetMemberAPI member17 = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		FleetMemberAPI member18 = api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		FleetMemberAPI member19 = api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		FleetMemberAPI member20 = api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		FleetMemberAPI member21 = api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		FleetMemberAPI member22 = api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		
		
		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 15; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 900f; 
			api.addNebula(x, y, radius);
		}
		api.setBackgroundSpriteName("graphics/backgrounds/hyperspace1.jpg");
		//api.setBackgroundSpriteName("graphics/backgrounds/background2.jpg");
		
		//system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
		//api.setBackgroundSpriteName();
		
		// Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
		api.addAsteroidField(0f, 0f, (float) Math.random() * 360f, width,
									20f, 70f, 100);
		
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
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
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(10000f);
			}
		});
	}

}






