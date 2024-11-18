package data.scripts.ix;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import data.scripts.util.MagicCampaign;

public class IXSystemCreation {

	private static int FAC_COUNT = 0;
	
	//increase population of starting IX and TW colonies if modded faction count meets lunalib threshold
	private static boolean isFortress() {
		return (("Challenging").equals(LunaSettings.getString("EmergentThreats_IX_Revival", "ix_difficulty_setting")));
	}

	public static void generate(SectorAPI sector) {

		//make the system
		StarSystemAPI system = sector.createStarSystem("Zorya");
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

		//create the star
		PlanetAPI star = system.initStar("ix_zorya", // unique id for this star
				StarTypes.RED_GIANT,  // id in planets.json
				1100f, // radius (in pixels at default zoom)
				600); // corona radius, from star edge
		star.setCustomDescriptionId("ix_zorya_sun");
		
		system.setLightColor(new Color(255, 200, 210)); // light color in entire system, affects all entities

		//get rid of the hyperspace around the star
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);

		float minRadius = plugin.getTileSize() * 4f;
		float radius = system.getMaxRadiusInHyperspace() * 1.4f;
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

		//add the important planet,
		PlanetAPI planet1 = system.addPlanet("ix_zorya_piorun", star, "Piorun", "terran", 235, 100, 3000, 100);
		planet1.setFaction("ix_battlegroup");
		planet1.setCustomDescriptionId("ix_zorya_piorun");
		planet1.setInteractionImage("illustrations", "ix_piorun_illus");
		
		int pop = 6;
		if (isFortress()) pop ++;
		MarketAPI market = Global.getFactory().createMarket("ix_piorun_market", planet1.getName(), pop);
		market.setFactionId("ix_battlegroup");
		market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market.setPrimaryEntity(planet1);
		market.addCondition(Conditions.HOT);
		market.addCondition(Conditions.ORE_MODERATE);
		market.addCondition(Conditions.RARE_ORE_MODERATE);
		market.addCondition(Conditions.ORGANICS_ABUNDANT);
		market.addCondition(Conditions.VOLATILES_TRACE);
		market.addCondition(Conditions.FARMLAND_POOR);
		market.addCondition(Conditions.HABITABLE);
		
		if (isFortress()) market.addCondition(Conditions.POPULATION_7);
		else market.addCondition(Conditions.POPULATION_6);
		
		market.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market.addIndustry(Industries.FARMING);
		market.addIndustry(Industries.MINING, new ArrayList<Commodities>(Arrays.asList(Commodities.ALPHA_CORE)));
		market.addIndustry(Industries.REFINING);
		market.addIndustry(Industries.HEAVYBATTERIES);
		market.addIndustry(Industries.MEGAPORT);
		market.addIndustry(Industries.WAYSTATION);
		market.addIndustry(Industries.MILITARYBASE);
		if (isFortress()) market.getIndustry(Industries.MILITARYBASE).setAICoreId(Commodities.GAMMA_CORE);
		market.addIndustry(Industries.STARFORTRESS_HIGH);

		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.GENERIC_MILITARY);
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

		planet1.setMarket(market);
		Global.getSector().getEconomy().addMarket(market, true);

		//add independent starbase
		SectorEntityToken starbase = system.addCustomEntity("ix_zorya_vertex", "Vertex Station", "station_hightech3", "ix_battlegroup");
		
		//heliosynchronous orbit for station, spire always pointed towards sun
		starbase.setCircularOrbitPointingDown(star, 235, 2500, 100);
		starbase.setCustomDescriptionId("ix_zorya_vertex");
		starbase.setInteractionImage("illustrations", "ix_vertex_illus");
		
		pop = 5;
		if (isFortress()) pop++;
		MarketAPI market_starbase = Global.getFactory().createMarket("ix_vertex_market", starbase.getName(), pop);
		market_starbase.setFactionId("ix_battlegroup");

		market_starbase.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market_starbase.setPrimaryEntity(starbase);

		if (isFortress()) market_starbase.addCondition(Conditions.POPULATION_6);
		else market_starbase.addCondition(Conditions.POPULATION_5);
		market_starbase.addCondition(Conditions.VERY_HOT);
		
		if (isFortress()) market_starbase.addIndustry("tw_tributary_port");
		market_starbase.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market_starbase.addIndustry(Industries.HEAVYBATTERIES);
		market_starbase.addIndustry(Industries.MEGAPORT);
		market_starbase.addIndustry("ix_panopticon");
		market_starbase.addIndustry("ix_fleet_command", new ArrayList<Items>(Arrays.asList(Items.CRYOARITHMETIC_ENGINE)));
		market_starbase.getIndustry("ix_fleet_command").setAICoreId("ix_panopticon_instance");
		market_starbase.addIndustry(Industries.WAYSTATION);
		market_starbase.addIndustry(Industries.ORBITALWORKS, new ArrayList<Items>(Arrays.asList(Items.PRISTINE_NANOFORGE)));
		market_starbase.addIndustry("ix_fuel_production", new ArrayList<Commodities>(Arrays.asList(Commodities.ALPHA_CORE)));
		market_starbase.addIndustry(Industries.STARFORTRESS_HIGH);
		
		market_starbase.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market_starbase.addSubmarket(Submarkets.GENERIC_MILITARY);
		market_starbase.addSubmarket("ix_honor_guard_market"); 
		market_starbase.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market_starbase.getTariff().modifyFlat("default_tariff", market_starbase.getFaction().getTariffFraction());

		starbase.setMarket(market_starbase);
		Global.getSector().getEconomy().addMarket(market_starbase, true);

		//water world
		PlanetAPI planet2 = system.addPlanet("ix_zorya_rusalka", star, "Rusalka", "water", 70, 180, 6000, 150);
		planet2.setFaction("ix_battlegroup");
		planet2.setCustomDescriptionId("ix_zorya_rusalka");
		planet2.setInteractionImage("illustrations", "ix_rusalka_illus");
		
		pop = 5;
		if (isFortress()) pop++;
		MarketAPI market2 = Global.getFactory().createMarket("ix_rusalka_market", planet2.getName(), pop);
		market2.setFactionId("ix_battlegroup");
		market2.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market2.setPrimaryEntity(planet2);

		if (isFortress()) market2.addCondition(Conditions.POPULATION_6);
		else market2.addCondition(Conditions.POPULATION_5);
		market2.addCondition("ix_algae");
		market2.addCondition(Conditions.EXTREME_WEATHER);
		market2.addCondition(Conditions.WATER_SURFACE);
		market2.addCondition(Conditions.HABITABLE);
		
		if (isFortress()) market2.addIndustry(Industries.AQUACULTURE);
		market2.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market2.addIndustry(Industries.COMMERCE);
		market2.addIndustry(Industries.HEAVYBATTERIES);
		market2.addIndustry(Industries.MEGAPORT);
		market2.addIndustry(Industries.WAYSTATION);
		market2.addIndustry(Industries.MILITARYBASE);
		market2.addIndustry(Industries.LIGHTINDUSTRY, new ArrayList<Commodities>(Arrays.asList(Commodities.ALPHA_CORE)));
		market2.addIndustry(Industries.STARFORTRESS_HIGH);
		
		market2.setFreePort(true);
		
		market2.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market2.addSubmarket(Submarkets.GENERIC_MILITARY);
		market2.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market2.getTariff().modifyFlat("default_tariff", sector.getFaction("pirates").getTariffFraction());

		planet2.setMarket(market2);
		Global.getSector().getEconomy().addMarket(market2, true);
		
		//lawless pirate world Scorn
		if (!LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_marzanna_enabled")) {
			PlanetAPI planet3 = system.addPlanet("ix_zorya_scorn", star, "Scorn", "tundra", 140, 165, 8200, 180);
			planet3.setFaction("pirates");
			planet3.setCustomDescriptionId("ix_zorya_scorn");
			planet3.setInteractionImage("illustrations", "ix_scorn_illus");
			
			MarketAPI market3 = Global.getFactory().createMarket("ix_scorn_market", planet3.getName(), 3);
			market3.setFactionId("pirates");
			market3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
			market3.setPrimaryEntity(planet3);

			market3.addCondition(Conditions.ORE_MODERATE);
			market3.addCondition(Conditions.ORGANICS_COMMON);
			market3.addCondition(Conditions.COLD);
			market3.addCondition(Conditions.IRRADIATED);
			market3.addCondition(Conditions.RUINS_VAST);
			market3.addCondition(Conditions.DECIVILIZED);
			market3.addCondition("ix_killsats");
			market3.addCondition(Conditions.POPULATION_3);

			market3.addIndustry(Industries.POPULATION);
			market3.addIndustry(Industries.SPACEPORT);
			try { 
				market3.addIndustry("BOGGLED_DOMAIN_ARCHAEOLOGY");
			}
			catch (Exception e) {
				market3.addIndustry(Industries.TECHMINING);
			} 
		
			market3.setFreePort(true);
		
			market3.addSubmarket(Submarkets.SUBMARKET_OPEN);
			market3.addSubmarket(Submarkets.SUBMARKET_BLACK);
			market3.addSubmarket(Submarkets.SUBMARKET_STORAGE);
			market3.getTariff().modifyFlat("default_tariff", market3.getFaction().getTariffFraction());

			planet3.setMarket(market3);
			Global.getSector().getEconomy().addMarket(market3, true);
		}
		
		//conquered IX world Marzanna
		else {
			PlanetAPI planet3 = system.addPlanet("ix_zorya_marzanna", star, "Marzanna", "tundra", 140, 165, 8200, 180);
			planet3.setFaction("ix_marzanna");
			planet3.setCustomDescriptionId("ix_zorya_marzanna");
			planet3.setInteractionImage("illustrations", "ix_marzanna_illus");
			
			pop = 5;
			if (isFortress()) pop++;
			MarketAPI market3 = Global.getFactory().createMarket("ix_marzanna_market", planet3.getName(), pop);
			market3.setFactionId("ix_battlegroup");
			market3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
			market3.setPrimaryEntity(planet3);

			if (isFortress()) market3.addCondition(Conditions.POPULATION_6);
			else market3.addCondition(Conditions.POPULATION_5);
			market3.addCondition(Conditions.ORE_MODERATE);
			market3.addCondition(Conditions.ORGANICS_COMMON);
			market3.addCondition(Conditions.COLD);
			market3.addCondition(Conditions.POLLUTION);
			market3.addCondition(Conditions.FARMLAND_POOR);
			market3.addCondition("ix_cartel_activity");

			if (isFortress()) market3.addIndustry(Industries.COMMERCE);
			market3.addIndustry(Industries.POPULATION);
			if (isFortress()) market3.addIndustry(Industries.MEGAPORT);
			else market3.addIndustry(Industries.SPACEPORT);
			
			market3.addIndustry(Industries.FARMING);
			market3.addIndustry(Industries.HEAVYINDUSTRY, new ArrayList<Items>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));
			market3.addIndustry(Industries.GROUNDDEFENSES);
			market3.addIndustry("ix_marzanna_base");
			market3.addIndustry(Industries.BATTLESTATION_HIGH);
			
			market3.addSubmarket(Submarkets.SUBMARKET_OPEN);
			market3.addSubmarket(Submarkets.SUBMARKET_BLACK);
			market3.addSubmarket(Submarkets.SUBMARKET_STORAGE);
			market3.getTariff().modifyFlat("default_tariff", market_starbase.getFaction().getTariffFraction());

			planet3.setMarket(market3);
			Global.getSector().getEconomy().addMarket(market3, true);
		}
        
		//add rings
        system.addRingBand(star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 4500, 305f, null, null);
        system.addRingBand(planet1, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 500, 305f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 4600, 295f, null, null);
		system.addAsteroidBelt(star, 40, 4600, 100, 30, 40, Terrain.ASTEROID_BELT, "The White Wall");
		
		//fill rest of system with random planetary bodies
		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                2, 4, // min/max entities to add
                9500, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

		StarSystemGenerator.addSystemwideNebula(system, StarAge.AVERAGE);

		SectorEntityToken loc1 = system.addCustomEntity(null,null, "comm_relay", Factions.NEUTRAL);
		loc1.setCircularOrbitPointingDown(star, 110, 4000, 155);

		SectorEntityToken loc2 = system.addCustomEntity(null,null, "sensor_array", Factions.NEUTRAL);
		loc2.setCircularOrbitPointingDown(star, 180, 7000, 220);

		SectorEntityToken loc3 = system.addCustomEntity(null,null, "nav_buoy", Factions.NEUTRAL);
		loc3.setCircularOrbitPointingDown(star, 140, 5500, 175);
		
		//jeff's memorial
		SectorEntityToken jeff = MagicCampaign.createDerelict(
					"scarab_ix_custom",
					ShipRecoverySpecial.ShipCondition.PRISTINE,
					true,
					1000,
					true,
					star,
					70,                 
					4600,                 
					180);
		jeff.addTag(Tags.NEUTRINO_LOW);
		jeff.setCustomDescriptionId("ix_scarab_wreck");
		jeff.setSensorProfile(25f);
		
		//autogenerate jump points
		system.autogenerateHyperspaceJumpPoints(true, true);
		int node = system.getAutogeneratedJumpPointsInHyper().size() - 1;
		SectorEntityToken anchor = (SectorEntityToken) system.getAutogeneratedJumpPointsInHyper().get(node);
		CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity("ix_zorya_beacon", null, "ix_warning_beacon", "ix_battlegroup");
		beacon.setCircularOrbitPointingDown(anchor, 180, 150, 365f);
		Color color1 = new Color(255,255,255,255);
		Color color2 = new Color(0,255,0,255);
		Misc.setWarningBeaconColors(beacon, color1, color2);
		
		if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_trinity_enabled")) generateTrinity(sector);
	}
	
	private static void generateTrinity(SectorAPI sector) {
		//make the system
		StarSystemAPI system = sector.createStarSystem("Danu");
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.setBackgroundTextureFilename("graphics/backgrounds/background_galatia.jpg");
		
		//create the star
		PlanetAPI star = system.initStar("tw_danu", // unique id for this star
				StarTypes.ORANGE,  // id in planets.json
				650f, // radius (in pixels at default zoom)
				300); // corona radius, from star edge
		system.setLightColor(new Color(255, 230, 200)); // light color in entire system, affects all entities
		
		//get rid of the hyperspace around the star
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		
		float minRadius = plugin.getTileSize() * 4f;
		float radius = system.getMaxRadiusInHyperspace() * 2f;
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
		
		//toxic tech-mining world
		PlanetAPI planet1 = system.addPlanet("tw_danu_gorias", star, "Gorias", "toxic", 340, 125, 2200, 110);
        
		planet1.setFaction("ix_trinity");
		planet1.setCustomDescriptionId("tw_danu_gorias");
		planet1.setInteractionImage("illustrations", "tw_gorias_illus");
		
		int pop = 4;
		if (isFortress()) pop++;
		MarketAPI market = Global.getFactory().createMarket("tw_gorias_market", planet1.getName(), pop);
		market.setFactionId("ix_trinity");
		market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market.setPrimaryEntity(planet1);
		
		if (isFortress()) market.addCondition(Conditions.POPULATION_5);
		else market.addCondition(Conditions.POPULATION_4);
		market.addCondition(Conditions.VERY_HOT);
		market.addCondition(Conditions.TOXIC_ATMOSPHERE);
		market.addCondition(Conditions.DENSE_ATMOSPHERE);
		market.addCondition(Conditions.ORE_ABUNDANT);
		market.addCondition(Conditions.ORGANICS_COMMON);
		market.addCondition(Conditions.RUINS_VAST);
		
		if (isFortress()) market.addIndustry(Industries.COMMERCE);
		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.MEGAPORT);
		market.addIndustry(Industries.MINING);
		market.addIndustry(Industries.ORBITALSTATION_HIGH);
		market.addIndustry(Industries.GROUNDDEFENSES);
		market.addIndustry(Industries.PATROLHQ);
		
		try { 
			market.addIndustry("BOGGLED_DOMAIN_ARCHAEOLOGY");
		}
		catch (Exception e) {
			market.addIndustry(Industries.TECHMINING);
		}
		
		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

		planet1.setMarket(market);
		Global.getSector().getEconomy().addMarket(market, true);
		
		//uninhabited desert ruin world
		PlanetAPI planet2 = system.addPlanet("tw_danu_murias", star, "Murias", "desert1", 40, 170, 4100, 320);
		planet2.setCustomDescriptionId("tw_danu_murias");
		planet2.setInteractionImage("illustrations", "tw_murias_illus");

		MarketAPI market2 = planet2.getMarket();
		
		market2.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market2.setPrimaryEntity(planet2);
		market2.addCondition(Conditions.DECIVILIZED);
		market2.addCondition(Conditions.THIN_ATMOSPHERE);
		market2.addCondition(Conditions.COLD);
		market2.addCondition(Conditions.ORE_SPARSE);
		market2.addCondition(Conditions.RUINS_WIDESPREAD);
		market2.addCondition(Conditions.ORGANICS_TRACE);
		
		planet2.setMarket(market2);
		
		//habitable farming station orbiting Murias
		SectorEntityToken starbase = system.addCustomEntity("tw_danu_murias_station", "Emerald Station", "ix_tw_station", "ix_trinity");
		
		starbase.setCircularOrbitPointingDown(planet2, 180, 300, 50);
		//starbase.setCircularOrbit(star, 45, 3800, 320);
		starbase.setCustomDescriptionId("tw_danu_murias_station");
		starbase.setInteractionImage("illustrations", "tw_station_illus");
		
		pop = 5;
		if (isFortress()) pop++;
		MarketAPI market_starbase = Global.getFactory().createMarket("tw_danu_station_market", starbase.getName(), pop);
		market_starbase.setFactionId("ix_trinity");
		
		market_starbase.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market_starbase.setPrimaryEntity(starbase);

		if (isFortress()) market_starbase.addCondition(Conditions.POPULATION_6);
		else market_starbase.addCondition(Conditions.POPULATION_5);
		
		market_starbase.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market_starbase.addIndustry(Industries.MEGAPORT);
		market_starbase.addIndustry(Industries.WAYSTATION);
		market_starbase.addIndustry("tw_orbital_farms");
		market_starbase.addIndustry(Industries.ORBITALWORKS, new ArrayList<Items>(Arrays.asList(Items. 	CORRUPTED_NANOFORGE)));
		market_starbase.addIndustry(Industries.LIGHTINDUSTRY); 
		market_starbase.addIndustry(Industries.HEAVYBATTERIES);
		if (isFortress()) market_starbase.addIndustry(Industries.MILITARYBASE, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		else market_starbase.addIndustry(Industries.PATROLHQ);
		market_starbase.addIndustry("tw_fleet_embassy");
		market_starbase.getIndustry("tw_fleet_embassy").setAICoreId("ix_panopticon_instance");
		market_starbase.addIndustry(Industries.BATTLESTATION_HIGH);
		
		market_starbase.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market_starbase.addSubmarket(Submarkets.GENERIC_MILITARY);
		market_starbase.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market_starbase.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market_starbase.getTariff().modifyFlat("default_tariff", market_starbase.getFaction().getTariffFraction());

		starbase.setMarket(market_starbase);
		Global.getSector().getEconomy().addMarket(market_starbase, true);
		
		//brown dwarf star
		PlanetAPI star2 = system.addPlanet("tw_danu_finias", star, "Finias", "star_browndwarf", 180, 300, 8500, 540);
		star2.setCustomDescriptionId("tw_danu_finias");
		system.setType(StarSystemGenerator.StarSystemType.BINARY_FAR);
		system.setSecondary(star2);
		
		//cryovolcanic world
		PlanetAPI planet3 = system.addPlanet("tw_danu_falais", star2, "Falais", "cryovolcanic", 360, 70, 1200, 70);
		planet3.setCustomDescriptionId("tw_danu_falais");
		planet3.setFaction("ix_trinity");
		planet3.setInteractionImage("illustrations", "tw_falais_illus");
		
		pop = 5;
		if (isFortress()) pop++;
		MarketAPI market3 = Global.getFactory().createMarket("tw_falais_market", planet3.getName(), pop);
		market3.setFactionId("ix_trinity");
		market3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market3.setPrimaryEntity(planet3);
		
		if (isFortress()) market3.addCondition(Conditions.POPULATION_6);
		else market3.addCondition(Conditions.POPULATION_5);
		market3.addCondition(Conditions.ORE_ABUNDANT);
		market3.addCondition(Conditions.RARE_ORE_RICH);
		market3.addCondition(Conditions.VOLATILES_PLENTIFUL);
		market3.addCondition(Conditions.TECTONIC_ACTIVITY);
		market3.addCondition(Conditions.EXTREME_WEATHER);

		if (isFortress()) market3.addIndustry("ix_fuel_production");
		market3.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market3.addIndustry(Industries.MEGAPORT);
		market3.addIndustry(Industries.WAYSTATION);
		market3.addIndustry(Industries.MINING, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market3.addIndustry(Industries.REFINING);
		market3.addIndustry("tw_cloudburst_academy", new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market3.addIndustry(Industries.HEAVYBATTERIES);
		market3.addIndustry(Industries.BATTLESTATION_HIGH);
		
		market3.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market3.addSubmarket(Submarkets.GENERIC_MILITARY);
		market3.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market3.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market3.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

		planet3.setMarket(market3);
		Global.getSector().getEconomy().addMarket(market3, true);

		//uninhabited asteroid belt planetoid
		PlanetAPI planet4 = system.addPlanet("tw_danu_murias", star, "Bodach", "barren-bombarded", 90, 20, 5500, 360);
		planet4.setCustomDescriptionId("tw_danu_bodach");

		MarketAPI market4 = planet4.getMarket();
		
		market4.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market4.setPrimaryEntity(planet4);
		market4.addCondition(Conditions.NO_ATMOSPHERE);
		market4.addCondition(Conditions.METEOR_IMPACTS);
		market4.addCondition(Conditions.LOW_GRAVITY);
		market4.addCondition(Conditions.ORE_MODERATE);
		market4.addCondition(Conditions.RARE_ORE_SPARSE);
		
		planet4.setMarket(market4);

		//small fuel producing station near planetoid
		SectorEntityToken starbase2 = system.addCustomEntity("tw_danu_bodach_freehold", "Bodach Freehold", "ix_tw_station2", "pirates");
		
		starbase2.setCircularOrbitPointingDown(planet4, 0, 150, 360);
		//starbase2.setCircularOrbitPointingDown(star, 220, 5500, 360);
		starbase2.setCustomDescriptionId("tw_danu_bodach_freehold");
		starbase2.setInteractionImage("illustrations", "tw_station2_illus");
		
		MarketAPI market_starbase2 = Global.getFactory().createMarket("tw_danu_bodach_market", starbase2.getName(), 4);
		market_starbase2.setFactionId("pirates");
		
		market_starbase2.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market_starbase2.setPrimaryEntity(starbase2);

		market_starbase2.addCondition(Conditions.POPULATION_4);
		
		market_starbase2.addIndustry(Industries.POPULATION);
		market_starbase2.addIndustry(Industries.MEGAPORT);
		market_starbase2.addIndustry(Industries.WAYSTATION);
		market_starbase2.addIndustry("ix_fuel_production");
		market_starbase2.addIndustry(Industries.MILITARYBASE, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market_starbase2.addIndustry(Industries.GROUNDDEFENSES);
		market_starbase2.addIndustry(Industries.BATTLESTATION_HIGH);
		
		market_starbase2.setFreePort(true);
		
		market_starbase2.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market_starbase2.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market_starbase2.addSubmarket("tw_arms_bazaar_market"); //sells TW and basic IX weapons
		
		market_starbase2.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market_starbase2.getTariff().modifyFlat("default_tariff", market_starbase2.getFaction().getTariffFraction());
		
		starbase2.setMarket(market_starbase2);
		Global.getSector().getEconomy().addMarket(market_starbase2, true);
		
		//IX Battlegroup star fortress to bolster local defenses and repel blockades
		SectorEntityToken starbase3 = system.addCustomEntity("tw_danu_culmen_station", "Fort Culmen", "ix_tw_station3", "ix_battlegroup");
		
		starbase3.setCircularOrbitPointingDown(star, 70, 10500, 700);
		starbase3.setCustomDescriptionId("tw_danu_fort_culmen");
		starbase3.setInteractionImage("illustrations", "tw_station3_illus");
		
		pop = 4;
		if (isFortress()) pop++;
		MarketAPI market_starbase3 = Global.getFactory().createMarket("tw_danu_culmen_market", starbase3.getName(), pop);
		market_starbase3.setFactionId("ix_battlegroup");
		
		market_starbase3.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		market_starbase3.setPrimaryEntity(starbase3);

		if (isFortress()) market_starbase3.addCondition(Conditions.POPULATION_5);
		else market_starbase3.addCondition(Conditions.POPULATION_4);
		
		if (isFortress()) market_starbase3.addIndustry("tw_tributary_port");
		market_starbase3.addIndustry(Industries.POPULATION, new ArrayList<Commodities>(Arrays.asList(Commodities.GAMMA_CORE)));
		market_starbase3.addIndustry(Industries.MEGAPORT);
		market_starbase3.addIndustry(Industries.WAYSTATION);
		market_starbase3.addIndustry(Industries.HIGHCOMMAND, new ArrayList<Commodities>(Arrays.asList(Commodities.ALPHA_CORE)));
		market_starbase3.addIndustry(Industries.COMMERCE);
		market_starbase3.addIndustry(Industries.HEAVYBATTERIES);
		market_starbase3.addIndustry(Industries.STARFORTRESS_HIGH);
		
		market_starbase3.addSubmarket(Submarkets.SUBMARKET_OPEN);
		market_starbase3.addSubmarket(Submarkets.GENERIC_MILITARY);
		market_starbase3.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market_starbase3.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market_starbase3.getTariff().modifyFlat("default_tariff", market_starbase3.getFaction().getTariffFraction());
		
		starbase3.setMarket(market_starbase3);
		Global.getSector().getEconomy().addMarket(market_starbase3, true);
		
		//generate the rest of the system
		StarSystemGenerator.addSystemwideNebula(system, StarAge.AVERAGE);

		SectorEntityToken loc1 = system.addCustomEntity(null, "Gorias Relay", "comm_relay", Factions.NEUTRAL);
		loc1.setCircularOrbitPointingDown(star, 330, 3600, 110);
		
		SectorEntityToken loc2 = system.addCustomEntity(null,null, "nav_buoy_makeshift", Factions.NEUTRAL);
		loc2.setCircularOrbitPointingDown(star, 250, 9500, 290);
		
		SectorEntityToken loc3 = system.addCustomEntity(null,null, "sensor_array_makeshift", Factions.NEUTRAL);
		loc3.setCircularOrbitPointingDown(star, 220, 12000, 185);
		
		//add rings
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 5500, 300, null, null);
		system.addAsteroidBelt(star, 50, 5500, 100, 200, 250, Terrain.ASTEROID_BELT, "Brigid's Tears");		
		
		//fill rest of system with random planetary bodies
		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.AVERAGE,
                1, 3, // min/max entities to add
                13500, // radius to start adding at
                4, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names

		//add inner jump point
		JumpPointAPI point = Global.getFactory().createJumpPoint("danu_jump_inner", "Murias Jump-point");
		point.setCircularOrbit(star, 340, 3000, 320);
		point.setStandardWormholeToHyperspaceVisual();
		system.addEntity((SectorEntityToken) point);
		
		//add outer jump point
		JumpPointAPI point2 = Global.getFactory().createJumpPoint("danu_jump_outer", "Fringe Jump-point");
		point2.setCircularOrbit(star, 30, 10000, 700);
		point2.setStandardWormholeToHyperspaceVisual();
		system.addEntity((SectorEntityToken) point2);
		
		//autogenerate jump points
		system.autogenerateHyperspaceJumpPoints(true, false);
		
		//generate beacon
		SectorEntityToken anchor = (SectorEntityToken) system.getAutogeneratedJumpPointsInHyper().get(3);
		CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity("ix_danu_beacon", null, "ix_warning_beacon", "ix_battlegroup");
		beacon.setCircularOrbitPointingDown(anchor, 180, 150, 365f);
		Color color1 = new Color(255,255,255,255);
		Color color2 = new Color(0,255,0,255);
		Misc.setWarningBeaconColors(beacon, color1, color2);
	}
}