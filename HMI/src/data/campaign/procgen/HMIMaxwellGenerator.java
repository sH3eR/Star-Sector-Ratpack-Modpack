package data.campaign.procgen;

import java.util.*;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.themes.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.world.MoteParticleScript;
import data.campaign.fleets.HMIScavFleetRouteManager;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.PlanetaryShield;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames.NamePick;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import static com.fs.starfarer.api.characters.FullName.Gender.MALE;
import static com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.MEM_FLAG;


public class HMIMaxwellGenerator {

	public static String HMI_MAXWELL1_KEY = "$hmi_MaxwellStation1";
	public static String HMI_MAXWELL2_KEY = "$hmi_MaxwellStation2";
	public static String HMI_MAXWELL3_KEY = "$hmi_MaxwellStation3";

	private static Float random_var = null;

	public static void genAlways() {
		setUpMaxwellSystem();
		setUpMaxwellSystem2();
		setUpMaxwellSystem3();
	}


	//Code used with permission courtesy from Dal and starficz from Knights of Ludd

	public static StarSystemAPI addMaxwellStation1(long seed) {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>(new Random(seed));
		float width = Global.getSettings().getFloat("sectorWidth");
		float height = Global.getSettings().getFloat("sectorHeight");
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.getStar() == null || system.getStar().getTypeId().equals(StarTypes.NEUTRON_STAR) || system.getStar().getTypeId().equals(StarTypes.BLACK_HOLE) || system.getStar().getTypeId().equals(StarTypes.BLUE_SUPERGIANT)) continue;
			if (!system.hasTag(Tags.THEME_REMNANT_SECONDARY)) continue;
			if (system.isNebula()) continue;
			if (system.hasPulsar()) continue;
			if (system.hasBlackHole()) continue;
			if (system.hasTag("IndEvo_SystemHasArtillery")) continue;
			if (system.hasTag("HMI_MAXWELL_PICK")) continue;
			if (system.getPlanets().isEmpty()) continue;

			float w = 1f;
			if (system.hasTag(Tags.THEME_INTERESTING)) w *= 10f;
			if (system.hasTag(Tags.THEME_INTERESTING_MINOR)) w *= 5f;
			if (system.getLocation().getX() <= width/-2 + 5000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 10000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 20000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 35000) w *= 5f; //West bias
			if (system.hasSystemwideNebula()) w *= 2f;
			if (Misc.getNumStableLocations(system) < 1) w *= 0.1;
			if (Misc.getNumStableLocations(system) < 2) w *= 0.5;
			picker.add(system, w);
		}
		return picker.pick();
	}

	public static void setUpMaxwellSystem() {
		Random random = new Random();

		String entID = "hmi_maxwell1";
		StarSystemAPI home = addMaxwellStation1(Long.parseLong(Global.getSector().getSeedString().substring(3)));
		if (home == null) {;
			return;
		}

		home.addTag(Tags.THEME_SPECIAL);
		home.addTag("HMI_MAXWELL1");
		home.addTag("HMI_MAXWELL_PICK");

		Global.getSector().getPersistentData().put(HMI_MAXWELL1_KEY, home);
		Global.getSector().getMemoryWithoutUpdate().set(HMI_MAXWELL1_KEY, home.getId());

		List<BaseThemeGenerator.OrbitGap> gaps = BaseThemeGenerator.findGaps(home.getCenter(), 2000, 20000, 800);
		float orbitRadius = 7000;
		if (!gaps.isEmpty()) {
			orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
		}
		float radius = 500f + 200f * random.nextFloat();
		float area = radius * radius * 3.14f;
		int count = (int) (area / 80000f);
		count *= 2;
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		float angle = random.nextFloat() * 360f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);


		SectorEntityToken station = home.addCustomEntity(
				"hmi_station_maxwell1",
				"Maxwell Investigatory Station",
				"hmi_station_maxwell",
				"hmi_maxwell");

		station.setCircularOrbit(home.getCenter(), angle, orbitRadius, orbitDays);

		if (station == null) {
			return;
		}

		station.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		station.addTag("HMI_STATION_MAXWELL_PICK");
		station.setId("bhmi_maxwellstation1_id");
		station.setCustomDescriptionId("hmi_maxwell1_desc");

		MarketAPI marketMaxwell = Global.getFactory().createMarket("maxwell_market1", station.getName(), 3);
		marketMaxwell.setSize(3);
		marketMaxwell.setHidden(true);
		marketMaxwell.setFreePort(true);
		marketMaxwell.setFactionId("hmi_maxwell");

		marketMaxwell.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

		marketMaxwell.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
		marketMaxwell.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, true);
		marketMaxwell.addTag(Tags.MARKET_NO_OFFICER_SPAWN);

		marketMaxwell.getMemoryWithoutUpdate().set(MemFlags.MARKET_HAS_CUSTOM_INTERACTION_OPTIONS, true);


		// probably not necessary
		marketMaxwell.addCondition(Conditions.POPULATION_3);
		marketMaxwell.addIndustry(Industries.POPULATION);
		marketMaxwell.addIndustry(Industries.SPACEPORT);
		marketMaxwell.addIndustry(Industries.BATTLESTATION_HIGH);
		marketMaxwell.addIndustry(Industries.WAYSTATION);
		marketMaxwell.addIndustry(Industries.PATROLHQ);
		marketMaxwell.addIndustry(Industries.ORBITALWORKS);
		marketMaxwell.addIndustry("HMIMAX_Supply");

		marketMaxwell.getTariff().modifyFlat("default_tariff", marketMaxwell.getFaction().getTariffFraction());

		marketMaxwell.addSubmarket("hmi_maxwell_open");
		marketMaxwell.addSubmarket("hmi_maxwell_low");
		marketMaxwell.addSubmarket("hmi_maxwell_med");
		marketMaxwell.addSubmarket("hmi_maxwell_high");

		marketMaxwell.setPrimaryEntity(station);
		station.setMarket(marketMaxwell);

		station.setSensorProfile(1f);
		station.setDiscoverable(true);
		station.getDetectedRangeMod().modifyFlat("gen", 5000f);

		station.getMemoryWithoutUpdate().set("$nex_unbuyable", true);

		marketMaxwell.reapplyIndustries();
		marketMaxwell.setEconGroup(marketMaxwell.getId());
		Global.getSector().getEconomy().addMarket(marketMaxwell, true);





//		TODO - Add interactions about Maxwell - Introductory Speech you can replay
		for (PersonAPI curr : marketMaxwell.getPeopleCopy()) {
				marketMaxwell.removePerson(curr);
				marketMaxwell.getCommDirectory().removePerson(curr);
		}

		PersonAPI person = Global.getFactory().createPerson();
		person.setId("maxwell_admin_smart");
		person.setImportance(PersonImportance.VERY_HIGH);
		person.setFaction("hmi_maxwell");
		person.setGender(MALE);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_TRADER);
		person.getName().setFirst("Maxwell");
		person.getName().setLast("Smart");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "maxwell_smart"));
		marketMaxwell.getCommDirectory().addPerson(person, 0);
		marketMaxwell.addPerson(person);

		int maxFleets = 3 + StarSystemGenerator.random.nextInt(2);

		MaxwellStationFleetManager MaxwellDefenseFleets = new MaxwellStationFleetManager(
				station, 1.0f, 0, maxFleets, 15.0f, 25, 60);
		home.addScript(MaxwellDefenseFleets);
	}


	public static StarSystemAPI addMaxwellStation2(long seed) {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>(new Random(seed));
		float width = Global.getSettings().getFloat("sectorWidth");
		float height = Global.getSettings().getFloat("sectorHeight");
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.getStar() == null || system.getStar().getTypeId().equals(StarTypes.NEUTRON_STAR) || system.getStar().getTypeId().equals(StarTypes.BLACK_HOLE) || system.getStar().getTypeId().equals(StarTypes.BLUE_SUPERGIANT)) continue;
			if (!system.hasTag(Tags.THEME_REMNANT_SECONDARY)) continue;
			if (system.isNebula()) continue;
			if (system.hasPulsar()) continue;
			if (system.hasBlackHole()) continue;
			if (system.hasTag("IndEvo_SystemHasArtillery")) continue;
			if (system.hasTag("HMI_MAXWELL_PICK")) continue;
			if (system.getPlanets().isEmpty()) continue;

			float w = 1f;
			if (system.hasTag(Tags.THEME_INTERESTING)) w *= 10f;
			if (system.hasTag(Tags.THEME_INTERESTING_MINOR)) w *= 5f;
			if (system.getLocation().getX() <= width/-2 + 5000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 10000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 20000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 35000) w *= 5f; //West bias
			if (system.hasSystemwideNebula()) w *= 2f;
			if (Misc.getNumStableLocations(system) < 1) w *= 0.1;
			if (Misc.getNumStableLocations(system) < 2) w *= 0.5;
			picker.add(system, w);
		}
		return picker.pick();
	}

	public static void setUpMaxwellSystem2() {
		Random random = new Random();

		String entID = "hmi_maxwell2";
		StarSystemAPI home = addMaxwellStation2(Long.parseLong(Global.getSector().getSeedString().substring(3)));
		if (home == null) {;
			return;
		}

		home.addTag(Tags.THEME_SPECIAL);
		home.addTag("HMI_MAXWELL2");
		home.addTag("HMI_MAXWELL_PICK");

		Global.getSector().getPersistentData().put(HMI_MAXWELL2_KEY, home);
		Global.getSector().getMemoryWithoutUpdate().set(HMI_MAXWELL2_KEY, home.getId());

		List<BaseThemeGenerator.OrbitGap> gaps = BaseThemeGenerator.findGaps(home.getCenter(), 2000, 20000, 800);
		float orbitRadius = 7000;
		if (!gaps.isEmpty()) {
			orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
		}
		float radius = 500f + 200f * random.nextFloat();
		float area = radius * radius * 3.14f;
		int count = (int) (area / 80000f);
		count *= 2;
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		float angle = random.nextFloat() * 360f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);


		SectorEntityToken station = home.addCustomEntity(
				"hmi_station_maxwell2",
				"Maxwell Investigatory Station",
				"hmi_station_maxwell",
				"hmi_maxwell");

		station.setCircularOrbit(home.getCenter(), angle, orbitRadius, orbitDays);

		if (station == null) {
			return;
		}

		station.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		station.addTag("HMI_STATION_MAXWELL_PICK");
		station.setId("bhmi_maxwellstation2_id");
		station.setCustomDescriptionId("hmi_maxwell1_desc");

		MarketAPI marketMaxwell = Global.getFactory().createMarket("maxwell_market2", station.getName(), 3);
		marketMaxwell.setSize(3);
		marketMaxwell.setHidden(true);
		marketMaxwell.setFreePort(true);
		marketMaxwell.setFactionId("hmi_maxwell");

		marketMaxwell.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

		marketMaxwell.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
		marketMaxwell.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, true);
		marketMaxwell.addTag(Tags.MARKET_NO_OFFICER_SPAWN);

		marketMaxwell.getMemoryWithoutUpdate().set(MemFlags.MARKET_HAS_CUSTOM_INTERACTION_OPTIONS, true);


		// probably not necessary
		marketMaxwell.addCondition(Conditions.POPULATION_3);
		marketMaxwell.addIndustry(Industries.POPULATION);
		marketMaxwell.addIndustry(Industries.SPACEPORT);
		marketMaxwell.addIndustry(Industries.BATTLESTATION_HIGH);
		marketMaxwell.addIndustry(Industries.WAYSTATION);
		marketMaxwell.addIndustry(Industries.PATROLHQ);
		marketMaxwell.addIndustry(Industries.ORBITALWORKS);
		marketMaxwell.addIndustry("HMIMAX_Supply");

		marketMaxwell.getTariff().modifyFlat("default_tariff", marketMaxwell.getFaction().getTariffFraction());

		marketMaxwell.addSubmarket("hmi_maxwell_open");
		marketMaxwell.addSubmarket("hmi_maxwell_low");
		marketMaxwell.addSubmarket("hmi_maxwell_med");
		marketMaxwell.addSubmarket("hmi_maxwell_high");

		marketMaxwell.setPrimaryEntity(station);
		station.setMarket(marketMaxwell);

		station.setSensorProfile(1f);
		station.setDiscoverable(true);
		station.getDetectedRangeMod().modifyFlat("gen", 5000f);

		station.getMemoryWithoutUpdate().set("$nex_unbuyable", true);

		marketMaxwell.reapplyIndustries();
		marketMaxwell.setEconGroup(marketMaxwell.getId());
		Global.getSector().getEconomy().addMarket(marketMaxwell, true);


//		TODO - Add interactions about Maxwell - Introductory Speech you can replay
		for (PersonAPI curr : marketMaxwell.getPeopleCopy()) {
			marketMaxwell.removePerson(curr);
			marketMaxwell.getCommDirectory().removePerson(curr);
		}

		PersonAPI person = Global.getFactory().createPerson();
		person.setId("maxwell_admin_sheffield");
		person.setImportance(PersonImportance.VERY_HIGH);
		person.setFaction("hmi_maxwell");
		person.setGender(MALE);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_TRADER);
		person.getName().setFirst("Maxwell");
		person.getName().setLast("Sheffield");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "maxwell_sheffield"));
		marketMaxwell.getCommDirectory().addPerson(person, 0);
		marketMaxwell.addPerson(person);

		int maxFleets = 3 + StarSystemGenerator.random.nextInt(2);

		MaxwellStationFleetManager MaxwellDefenseFleets = new MaxwellStationFleetManager(
				station, 1.0f, 0, maxFleets, 15.0f, 25, 60);
		home.addScript(MaxwellDefenseFleets);
	}


	public static StarSystemAPI addMaxwellStation3(long seed) {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>(new Random(seed));
		float width = Global.getSettings().getFloat("sectorWidth");
		float height = Global.getSettings().getFloat("sectorHeight");
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.getStar() == null || system.getStar().getTypeId().equals(StarTypes.NEUTRON_STAR) || system.getStar().getTypeId().equals(StarTypes.BLACK_HOLE) || system.getStar().getTypeId().equals(StarTypes.BLUE_SUPERGIANT)) continue;
			if (!system.hasTag(Tags.THEME_REMNANT_SECONDARY)) continue;
			if (system.isNebula()) continue;
			if (system.hasPulsar()) continue;
			if (system.hasBlackHole()) continue;
			if (system.hasTag("IndEvo_SystemHasArtillery")) continue;
			if (system.hasTag("HMI_MAXWELL_PICK")) continue;
			if (system.getPlanets().isEmpty()) continue;

			float w = 1f;
			if (system.hasTag(Tags.THEME_INTERESTING)) w *= 10f;
			if (system.hasTag(Tags.THEME_INTERESTING_MINOR)) w *= 5f;
			if (system.getLocation().getX() <= width/-2 + 5000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 10000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 20000) w *= 5f; //West bias
			if (system.getLocation().getX() <= width/-2 + 35000) w *= 5f; //West bias
			if (system.hasSystemwideNebula()) w *= 2f;
			if (Misc.getNumStableLocations(system) < 1) w *= 0.1;
			if (Misc.getNumStableLocations(system) < 2) w *= 0.5;
			picker.add(system, w);
		}
		return picker.pick();
	}

	public static void setUpMaxwellSystem3() {
		Random random = new Random();

		String entID = "hmi_maxwell2";
		StarSystemAPI home = addMaxwellStation3(Long.parseLong(Global.getSector().getSeedString().substring(3)));
		if (home == null) {;
			return;
		}

		home.addTag(Tags.THEME_SPECIAL);
		home.addTag("HMI_MAXWELL3");
		home.addTag("HMI_MAXWELL_PICK");

		Global.getSector().getPersistentData().put(HMI_MAXWELL3_KEY, home);
		Global.getSector().getMemoryWithoutUpdate().set(HMI_MAXWELL3_KEY, home.getId());

		List<BaseThemeGenerator.OrbitGap> gaps = BaseThemeGenerator.findGaps(home.getCenter(), 2000, 20000, 800);
		float orbitRadius = 7000;
		if (!gaps.isEmpty()) {
			orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
		}
		float radius = 500f + 200f * random.nextFloat();
		float area = radius * radius * 3.14f;
		int count = (int) (area / 80000f);
		count *= 2;
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		float angle = random.nextFloat() * 360f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);


		SectorEntityToken station = home.addCustomEntity(
				"hmi_station_maxwell3",
				"Maxwell Investigatory Station",
				"hmi_station_maxwell",
				"hmi_maxwell");

		station.setCircularOrbit(home.getCenter(), angle, orbitRadius, orbitDays);

		if (station == null) {
			return;
		}

		station.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		station.addTag("HMI_STATION_MAXWELL_PICK");
		station.setId("bhmi_maxwellstation3_id");
		station.setCustomDescriptionId("hmi_maxwell1_desc");

		MarketAPI marketMaxwell = Global.getFactory().createMarket("maxwell_market3", station.getName(), 3);
		marketMaxwell.setSize(3);
		marketMaxwell.setHidden(true);
		marketMaxwell.setFreePort(true);
		marketMaxwell.setFactionId("hmi_maxwell");

		marketMaxwell.setSurveyLevel(MarketAPI.SurveyLevel.FULL);


		marketMaxwell.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
		marketMaxwell.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, true);
		marketMaxwell.addTag(Tags.MARKET_NO_OFFICER_SPAWN);

		marketMaxwell.getMemoryWithoutUpdate().set(MemFlags.MARKET_HAS_CUSTOM_INTERACTION_OPTIONS, true);


		// probably not necessary
		marketMaxwell.addCondition(Conditions.POPULATION_3);
		marketMaxwell.addIndustry(Industries.POPULATION);
		marketMaxwell.addIndustry(Industries.SPACEPORT);
		marketMaxwell.addIndustry(Industries.BATTLESTATION_HIGH);
		marketMaxwell.addIndustry(Industries.WAYSTATION);
		marketMaxwell.addIndustry(Industries.PATROLHQ);
		marketMaxwell.addIndustry(Industries.ORBITALWORKS);
		marketMaxwell.addIndustry("HMIMAX_Supply");

		marketMaxwell.getTariff().modifyFlat("default_tariff", marketMaxwell.getFaction().getTariffFraction());

		marketMaxwell.addSubmarket("hmi_maxwell_open");
		marketMaxwell.addSubmarket("hmi_maxwell_low");
		marketMaxwell.addSubmarket("hmi_maxwell_med");
		marketMaxwell.addSubmarket("hmi_maxwell_high");

		marketMaxwell.setPrimaryEntity(station);
		station.setMarket(marketMaxwell);

		station.setSensorProfile(1f);
		station.setDiscoverable(true);
		station.getDetectedRangeMod().modifyFlat("gen", 5000f);

		station.getMemoryWithoutUpdate().set("$nex_unbuyable", true);

		marketMaxwell.reapplyIndustries();
		marketMaxwell.setEconGroup(marketMaxwell.getId());
		Global.getSector().getEconomy().addMarket(marketMaxwell, true);


//		TODO - Add interactions about Maxwell - Introductory Speech you can replay
		for (PersonAPI curr : marketMaxwell.getPeopleCopy()) {
			marketMaxwell.removePerson(curr);
			marketMaxwell.getCommDirectory().removePerson(curr);
		}

		PersonAPI person = Global.getFactory().createPerson();
		person.setId("maxwell_admin_klinger");
		person.setImportance(PersonImportance.VERY_HIGH);
		person.setFaction("hmi_maxwell");
		person.setGender(MALE);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_TRADER);
		person.getName().setFirst("Maxwell");
		person.getName().setLast("Klinger");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "maxwell_klinger"));
		marketMaxwell.getCommDirectory().addPerson(person, 0);
		marketMaxwell.addPerson(person);

		int maxFleets = 3 + StarSystemGenerator.random.nextInt(2);

		MaxwellStationFleetManager MaxwellDefenseFleets = new MaxwellStationFleetManager(
				station, 1.0f, 0, maxFleets, 15.0f, 25, 60);
		home.addScript(MaxwellDefenseFleets);
	}
}
















