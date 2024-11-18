package DE.scripts.world.systems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.skills.OfficerTraining;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;

import static DE.scripts.Gen.addMarketplace;
import static DE.scripts.DE_ModPlugin.DEenablefortressmode;



public class DE_Andor {
    boolean isPAGSM = Global.getSettings().getModManager().isModEnabled("PAGSM");
    //public static boolean DEenablefortressmode = Global.getSettings().getBoolean("DEenablefortressmode");
    private boolean done = false;

    public void generate(SectorAPI sector) {

            StarSystemAPI system = sector.createStarSystem("Andor");
            LocationAPI hyper = Global.getSector().getHyperspace();
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

            system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

            // create the star and generate the hyperspace anchor for this system
            PlanetAPI star = system.initStar("andor",
                    StarTypes.BLUE_SUPERGIANT, // id in planets.json
                    2000f, // size of star
                    1000, // extent of corona outside star
                    8f, // solar wind burn level
                    0.5f, // flare probability
                    2.0f); // CR loss multiplier, good values are in the range of 1-5
            float radius = 10000;
            clearDeepHyper(star, radius);

            // Farnau - Hot.png
            PlanetAPI andor1 = system.addPlanet("andor1", star, "Farnau", "barren", 0, 250, 2500, 60);
            andor1.setCustomDescriptionId("planet_farnau");
            andor1.getMarket().addCondition(Conditions.VERY_HOT);
            andor1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
            andor1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
            andor1.getMarket().addCondition(Conditions.HIGH_GRAVITY);
            andor1.getMarket().addCondition(Conditions.ORE_RICH);
            andor1.getMarket().addCondition(Conditions.RARE_ORE_RICH);
            andor1.getMarket().addCondition(Conditions.VOLATILES_TRACE);

            SectorEntityToken relay = system.addCustomEntity("andor_relay", "Andoran Hypernet Relay", "comm_relay", "sindrian_diktat");
            relay.setCircularOrbitPointingDown(star, 100 - 60,  // angle
                    5000, // orbit radius
                    100); // orbit days
            relay.setCustomDescriptionId("andor_relay");

            // Andoran Gate - How those tourists got there in record time
            SectorEntityToken andoran_gate = system.addCustomEntity("andoran_gate", // unique id
                    "Andoran Gate", // name - if null, defaultName from custom_entities.json will be used
                    "inactive_gate", // type of object, defined in custom_entities.json
                    null); // faction(don't touch this)
            andoran_gate.setCircularOrbit(star, 100 + 60, 5000, 100);

            JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("andor_inner_jump_point", "Andor Inner Jump Point");
            OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 0, 500, 30);
            jumpPoint.setOrbit(orbit);
            jumpPoint.setStandardWormholeToHyperspaceVisual();
            jumpPoint.setCircularOrbit(star, 60, 4500, 100);
            system.addEntity(jumpPoint);

            // Ryzan - 4th due to me being too lazy to change all of Bardonia's(and moons) variables
            PlanetAPI andor4 = system.addPlanet("andor4", star, "Ryzan", "gas_giant", 100, 400, 5000, 100);
            andor4.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
            andor4.getMarket().addCondition(Conditions.HIGH_GRAVITY);
            andor4.getMarket().addCondition(Conditions.EXTREME_WEATHER);
            andor4.getMarket().addCondition(Conditions.HOT);
            andor4.getMarket().addCondition(Conditions.VOLATILES_PLENTIFUL);

            // Ryzan trojans - Small, because Bardonia stole most of them
            // L4 asteroids were all cleared to make way for the Gate
            SectorEntityToken ryzanL5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                            200f, // min radius
                            300f, // max radius
                            10, // min asteroid count
                            20, // max asteroid count
                            5f, // min asteroid radius
                            10f, // max asteroid radius
                            "Ryzan L5 Asteroids")); // null for default name

            ryzanL5.setCircularOrbit(star, 100 - 60, 5000, 100);

            // Bardonia - Wish you were there(before you know)
            PlanetAPI andor2 = system.addPlanet("andor2", star, "Bardonia", "ice_giant", 0, 600, 8000, 2120);
            andor2.setCustomDescriptionId("planet_bardonia"); // Custom description from descriptions.csv
            andor2.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE); // Add conditions(UNINHABITED PLANETS ONLY)
            andor2.getMarket().addCondition(Conditions.EXTREME_WEATHER);
            andor2.getMarket().addCondition(Conditions.HIGH_GRAVITY);
            andor2.getMarket().addCondition(Conditions.COLD);
            andor2.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);

            PlanetAPI a2a = system.addPlanet("hannover", andor2, "Hannover", "barren", 0, 40, 800, 25);
            a2a.setCustomDescriptionId("planet_bardonia_moon_generic");
            a2a.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2a.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2a.getMarket().addCondition(Conditions.COLD);
            a2a.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2a.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

            PlanetAPI a2b = system.addPlanet("obrithyll", andor2, "Obrithyll", "barren", 0, 50, 900, 30);
            a2b.setCustomDescriptionId("planet_bardonia_moon_generic");
            a2b.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2b.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2b.getMarket().addCondition(Conditions.COLD);
            a2b.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2b.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

            PlanetAPI a2c = system.addPlanet("vermillion", andor2, "Vermillion", "barren", 0, 45, 1000, 20);
            a2c.setCustomDescriptionId("planet_bardonia_moon_generic");
            a2c.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2c.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2c.getMarket().addCondition(Conditions.COLD);
            a2c.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2c.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

            PlanetAPI a2d = system.addPlanet("dralahad", andor2, "Dralahad", "barren", 0, 40, 1100, 50);
            a2d.setCustomDescriptionId("planet_bardonia_moon_generic");
            a2d.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2d.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2d.getMarket().addCondition(Conditions.COLD);
            a2d.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2d.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

            PlanetAPI a2e = system.addPlanet("falken", andor2, "Falken", "barren", 0, 45, 1200, 60);
            a2e.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
            a2e.applySpecChanges();
            a2e.setInteractionImage("illustrations", "vacuum_colony");
        if (!DEenablefortressmode) {
            a2e.setCustomDescriptionId("planet_bardonia_moon_pirate");
            MarketAPI a2e_market = addMarketplace(
                    "pirates",
                    a2e,
                    null,
                    "Falken",
                    3,

                    new ArrayList<>(
                            Arrays.asList(
                                    Conditions.POPULATION_3,
                                    Conditions.THIN_ATMOSPHERE,
                                    Conditions.METEOR_IMPACTS,
                                    Conditions.COLD,
                                    Conditions.ORE_SPARSE,
                                    Conditions.RARE_ORE_SPARSE,
                                    Conditions.OUTPOST,
                                    Conditions.FRONTIER
                            )
                    ),

                    new ArrayList<>(
                            Arrays.asList(
                                    Submarkets.SUBMARKET_OPEN,
                                    Submarkets.SUBMARKET_STORAGE,
                                    Submarkets.SUBMARKET_BLACK
                            )
                    ),
                    new ArrayList<>(
                            Arrays.asList(
                                    Industries.POPULATION,
                                    Industries.SPACEPORT,
                                    Industries.GROUNDDEFENSES,
                                    Industries.MINING
                            )
                    ),
                    //tariffs
                    0.3f,
                    //freeport
                    false,
                    //junk and chatter
                    true);
        } else {
            a2e.setCustomDescriptionId("planet_bardonia_moon_pirate_fortress");
            MarketAPI a2e_market = addMarketplace(
                    "pirates",
                    a2e,
                    null,
                    "Falken",
                    5,

                    new ArrayList<>(
                            Arrays.asList(
                                    Conditions.POPULATION_5,
                                    Conditions.THIN_ATMOSPHERE,
                                    Conditions.METEOR_IMPACTS,
                                    Conditions.COLD,
                                    Conditions.ORE_SPARSE,
                                    Conditions.RARE_ORE_SPARSE,
                                    Conditions.OUTPOST,
                                    Conditions.FRONTIER
                            )
                    ),

                    new ArrayList<>(
                            Arrays.asList(
                                    Submarkets.SUBMARKET_OPEN,
                                    Submarkets.SUBMARKET_STORAGE,
                                    Submarkets.SUBMARKET_BLACK
                            )
                    ),
                    new ArrayList<>(
                            Arrays.asList(
                                    Industries.POPULATION,
                                    Industries.SPACEPORT,
                                    Industries.GROUNDDEFENSES,
                                    Industries.MINING,
                                    Industries.PATROLHQ,
                                    "commerce",
                                    Industries.ORBITALSTATION
                            )
                    ),
                    //tariffs
                    0.18f,
                    //freeport
                    true,
                    //junk and chatter
                    true);
        }

            PlanetAPI a2f = system.addPlanet("areska", andor2, "Areska", "barren", 0, 40, 1300, 55);
            a2f.setCustomDescriptionId("planet_bardonia_moon_generic");
            a2f.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2f.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2f.getMarket().addCondition(Conditions.COLD);
            a2f.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2f.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

            PlanetAPI a2g = system.addPlanet("savras", andor2, "Savras", "barren", 0, 80, 1400, 100);
            a2g.setCustomDescriptionId("planet_savras");
            a2g.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2g.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2g.getMarket().addCondition(Conditions.COLD);

            // Savras Debris Ring
            system.addAsteroidBelt(andor2, 50, 1400, 60, 100, 100, Terrain.ASTEROID_BELT, "Savras Remnant");
            system.addRingBand(andor2, "misc", "rings_asteroids0", 256f, 0, Color.white, 150f, 1400, 100f);
            system.addRingBand(andor2, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 1400, 100f);
            //system.addRingBand(a2, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 1100, 80f);

            PlanetAPI a2h = system.addPlanet("orilin", andor2, "Orilin", "barren", 0, 60, 1500, 120);
            a2h.setCustomDescriptionId("planet_bardonia_moon_generic");
            a2h.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2h.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2h.getMarket().addCondition(Conditions.COLD);
            a2h.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2h.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

            PlanetAPI a2i = system.addPlanet("krakonia", andor2, "Krakonia", "barren", 0, 50, 1600, 95);
            a2i.setCustomDescriptionId("planet_bardonia_moon_generic");
            a2i.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
            a2i.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            a2i.getMarket().addCondition(Conditions.COLD);
            a2i.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2i.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);

            PlanetAPI a2j = system.addPlanet("ravaryea", andor2, "Ravaryea", "terran-eccentric", 0, 70, 1700, 150);
        if (!DEenablefortressmode) {
            a2j.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
            a2j.applySpecChanges();
            if (!isPAGSM) {
                a2j.setCustomDescriptionId("planet_ravaryea");
            } else {
                a2j.setCustomDescriptionId("planet_ravaryea_PAGSM");
            }
            MarketAPI a2j_market = addMarketplace(
                    "sindrian_diktat",
                    a2j,
                    null,
                    "Ravaryea",
                    4,

                    new ArrayList<>(
                            Arrays.asList(
                                    Conditions.POPULATION_4,
                                    Conditions.ORGANICS_COMMON,
                                    Conditions.FARMLAND_ADEQUATE,
                                    Conditions.ORE_SPARSE,
                                    Conditions.RARE_ORE_SPARSE,
                                    Conditions.HABITABLE
                            )
                    ),

                    new ArrayList<>(
                            Arrays.asList(
                                    Submarkets.SUBMARKET_OPEN,
                                    Submarkets.SUBMARKET_STORAGE,
                                    Submarkets.SUBMARKET_BLACK
                            )
                    ),
                    new ArrayList<>(
                            Arrays.asList(
                                    Industries.POPULATION,
                                    Industries.SPACEPORT,
                                    Industries.MINING,
                                    Industries.FARMING,
                                    Industries.PATROLHQ,
                                    Industries.ORBITALSTATION,
                                    Industries.GROUNDDEFENSES
                            )
                    ),
                    //tariffs
                    0.18f,
                    //freeport
                    false,
                    //junk and chatter
                    true);
            //terran-eccentric NOT terran_eccentric
        } else {
            a2j.getMarket().addCondition(Conditions.ORGANICS_COMMON);
            a2j.getMarket().addCondition(Conditions.FARMLAND_ADEQUATE);
            a2j.getMarket().addCondition(Conditions.ORE_SPARSE);
            a2j.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
            a2j.getMarket().addCondition(Conditions.HABITABLE);
            a2j.getMarket().addCondition(Conditions.COLD);
            a2j.getMarket().addCondition(Conditions.EXTREME_WEATHER);
            a2j.setCustomDescriptionId("planet_ravaryea_fortress");
            // a2j.setCustomDescriptionId("planet_ravaryea_fortress_colonized");
        }

            // Ring - There's more to it than meets the eye
            system.addRingBand(andor2, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 256f, 700, 60f, Terrain.RING, "Bardonia Ring");
            system.addRingBand(andor2, "misc", "rings_dust0", 256f, 0, Color.LIGHT_GRAY, 256f, 800, 70f, Terrain.RING, "Bardonia Ring");
            system.addRingBand(andor2, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 256f, 900, 80f, Terrain.RING, "Bardonia Ring");
            system.addRingBand(andor2, "misc", "rings_dust0", 256f, 0, Color.LIGHT_GRAY, 256f, 1000, 90f, Terrain.RING, "Bardonia Ring");
            system.addRingBand(andor2, "misc", "rings_dust0", 256f, 3, Color.LIGHT_GRAY, 256f, 1100, 100f, Terrain.RING, "Bardonia Ring");
            system.addRingBand(andor2, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 256f, 1200, 110f, Terrain.RING, "Bardonia Ring");

            // Bardonia trojans
            SectorEntityToken bardoniaL4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                            400f, // min radius
                            600f, // max radius
                            40, // min asteroid count
                            60, // max asteroid count
                            4f, // min asteroid radius
                            16f, // max asteroid radius
                            "Bardonia L4 Asteroids")); // null for default name

            SectorEntityToken bardoniaL5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                            400f, // min radius
                            500f, // max radius
                            20, // min asteroid count
                            30, // max asteroid count
                            4f, // min asteroid radius
                            16f, // max asteroid radius
                            "Bardonia L5 Asteroids")); // null for default name

            bardoniaL4.setCircularOrbit(star, 60, 8000, 2120);
            bardoniaL5.setCircularOrbit(star, -60, 8000, 2120);

            // L4 jump point
            JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("bardonia_L4_jump", "Bardonia L4 Jump Point");
            OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(star, 60, 8000, 2120);
            jumpPoint2.setOrbit(orbit2);
            jumpPoint2.setStandardWormholeToHyperspaceVisual();
            jumpPoint2.setCircularOrbit(star, 60, 8000, 2120);
            system.addEntity(jumpPoint2);

            // L5 moon
            PlanetAPI L5moon = system.addPlanet("fornois", star, "Fornois", "barren-bombarded", -60, 50, 8000, 2120);
            L5moon.setCustomDescriptionId("planet_fornois");
            L5moon.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
            L5moon.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            L5moon.getMarket().addCondition(Conditions.COLD);
            L5moon.getMarket().addCondition(Conditions.ORE_MODERATE);
            L5moon.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
            L5moon.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);

            // system.addRingBand(L5moon, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 150, 60f, Terrain.RING, "Dust Ring");
            // system.addRingBand(L5moon, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 150, 80f);
            system.addRingBand(L5moon, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 250, 100f, Terrain.RING, "Dust Ring");
            // system.addRingBand(L5moon, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 250, 120f);
            system.addRingBand(L5moon, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 350, 140f, Terrain.RING, "Dust Ring");
            // system.addRingBand(L5moon, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 350, 160f);
            system.addRingBand(L5moon, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 450, 180f, Terrain.RING, "Dust Ring");


            /* The asteroid belt - some notable large ones? */
            system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 10000, 220f, null, null);
            system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 10000, 226f, null, null);
            system.addAsteroidBelt(star, 200, 10000, 256f, 150, 250, Terrain.ASTEROID_BELT, "Andoran Belt");

        if (!DEenablefortressmode) {
            SectorEntityToken stationviewport = system.addCustomEntity("andor_viewport", "Andoran Viewport", "station_side04", "sindrian_diktat");
            stationviewport.setInteractionImage("illustrations", "cargo_loading"); // What picture the market uses when you interact with it
            stationviewport.setCircularOrbitPointingDown(star, 45, 11000, 360);
            stationviewport.setCustomDescriptionId("andor_viewport");
            MarketAPI stationviewport_market = addMarketplace(
                    "sindrian_diktat",
                    stationviewport,
                    null,
                    "Andoran Viewport",
                    4,

                    new ArrayList<>(
                            Arrays.asList(
                                    Conditions.POPULATION_4,
                                    Conditions.OUTPOST,
                                    Conditions.CLOSED_IMMIGRATION
                            )
                    ),

                    new ArrayList<>(
                            Arrays.asList(
                                    Submarkets.SUBMARKET_OPEN,
                                    Submarkets.GENERIC_MILITARY,
                                    Submarkets.SUBMARKET_STORAGE,
                                    Submarkets.SUBMARKET_BLACK
                            )
                    ),
                    new ArrayList<>(
                            Arrays.asList(
                                    Industries.POPULATION,
                                    Industries.SPACEPORT,
                                    Industries.HEAVYINDUSTRY,
                                    Industries.MILITARYBASE,
                                    Industries.BATTLESTATION,
                                    "lionsguard",
                                    Industries.GROUNDDEFENSES
                            )
                    ),
                    //tariffs
                    0.3f,
                    //freeport
                    false,
                    //junk and chatter
                    true);
        } else {
            SectorEntityToken stationviewport = system.addCustomEntity("andor_viewport", "Andoran Viewport", "station_side04", "neutral");
            stationviewport.setCircularOrbitPointingDown(star, 45, 11000, 360);
            stationviewport.setCustomDescriptionId("andor_viewport_fortress");
            stationviewport.setInteractionImage("illustrations", "abandoned_station");

            Misc.setAbandonedStationMarket("stationviewport_market", stationviewport);
        }

            // Ismara ripoff
            PlanetAPI andor3 = system.addPlanet("andor3", star, "Lennox", "cryovolcanic", 0, 175, 11500, 600);
            andor3.setCustomDescriptionId("planet_lennox");
            andor3.getMarket().addCondition(Conditions.RUINS_SCATTERED);
            andor3.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
            andor3.getMarket().addCondition(Conditions.VERY_COLD);
            andor3.getMarket().addCondition(Conditions.POOR_LIGHT);
            andor3.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);
            andor3.getMarket().addCondition(Conditions.ORE_MODERATE);
            andor3.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);

            // Ryzan Supercomplex - Tech Duinn ripoff
        if (!DEenablefortressmode) {
            SectorEntityToken a4a = system.addCustomEntity("ryzan_supercomplex", "Ryzan Supercomplex", "station_lowtech3", "sindrian_diktat");
            a4a.setInteractionImage("illustrations", "urban03");
            a4a.setCircularOrbitPointingDown(andor4, 45, 550, 60);
            if (!isPAGSM) {
                a4a.setCustomDescriptionId("ryzan_supercomplex");
            } else {
                a4a.setCustomDescriptionId("ryzan_supercomplex_PAGSM");
            }
            MarketAPI a4a_market = addMarketplace(
                    "sindrian_diktat",
                    a4a,
                    null,
                    "Ryzan Supercomplex",
                    6,

                    new ArrayList<>(
                            Arrays.asList(
                                    Conditions.POPULATION_6,
                                    Conditions.NO_ATMOSPHERE,
                                    Conditions.HOT,
                                    Conditions.VOLATILES_PLENTIFUL,
                                    //Conditions.URBANIZED_POLITY,
                                    Conditions.FRONTIER
                            )
                    ),

                    new ArrayList<>(
                            Arrays.asList(
                                    Submarkets.SUBMARKET_OPEN,
                                    Submarkets.GENERIC_MILITARY,
                                    Submarkets.SUBMARKET_STORAGE,
                                    Submarkets.SUBMARKET_BLACK
                            )
                    ),
                    new ArrayList<>(
                            Arrays.asList(
                                    Industries.POPULATION,
                                    Industries.MEGAPORT,
                                    Industries.HEAVYBATTERIES,
                                    Industries.STARFORTRESS,
                                    Industries.WAYSTATION,
                                    Industries.MINING,
                                    Industries.REFINING,
                                    Industries.HIGHCOMMAND,
                                    Industries.FUELPROD
                            )
                    ),
                    //tariffs
                    0.3f,
                    //freeport
                    false,
                    //junk and chatter
                    true);
        } else {
            SectorEntityToken a4a = system.addCustomEntity("ryzan_supercomplex", "Ryzan Supercomplex", "station_side02", "neutral");
            a4a.setCircularOrbitPointingDown(andor4, 45, 550, 60);
            a4a.setCustomDescriptionId("ryzan_supercomplex_fortress");
            a4a.setInteractionImage("illustrations", "abandoned_station3");
        }

        if (!DEenablefortressmode) {
            // Admiral
            PersonAPI admiral = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
            admiral.setId("de_admiral2");
            admiral.setPostId(Ranks.POST_FLEET_COMMANDER);
            admiral.setRankId(Ranks.POST_FLEET_COMMANDER);
            admiral.setPersonality(Personalities.AGGRESSIVE);
            admiral.getStats().setLevel(6);
            // fleet commander stuff
            admiral.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
            admiral.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
            admiral.getStats().setSkillLevel(Skills.OFFICER_MANAGEMENT, 1);
            // officer stuff
            admiral.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
            admiral.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
            admiral.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            admiral.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
            admiral.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
            admiral.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
            ip.addPerson(admiral);

            // Me again here
            PersonAPI person1 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
            person1.setId("de_generic_officer_andor1");
            person1.setPostId(Ranks.POST_OFFICER);
            person1.setRankId(Ranks.POST_OFFICER);
            person1.setPersonality(Personalities.AGGRESSIVE);
            person1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
            person1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            person1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            person1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
            person1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
            person1.getStats().setLevel(5);

            PersonAPI person2 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
            person2.setId("de_generic_officer_andor2");
            person2.setPostId(Ranks.POST_OFFICER);
            person2.setRankId(Ranks.POST_OFFICER);
            person2.setPersonality(Personalities.AGGRESSIVE);
            person2.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
            person2.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            person2.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            person2.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
            person2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
            person2.getStats().setLevel(5);

            PersonAPI person3 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
            person3.setId("de_generic_officer_andor3");
            person3.setPostId(Ranks.POST_OFFICER);
            person3.setRankId(Ranks.POST_OFFICER);
            person3.setPersonality(Personalities.AGGRESSIVE);
            person3.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
            person3.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            person3.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            person3.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
            person3.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
            person3.getStats().setLevel(5);

            PersonAPI reckless1 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
            reckless1.setId("de_generic_officer_andor_reckless1");
            reckless1.setPostId(Ranks.POST_OFFICER);
            reckless1.setRankId(Ranks.POST_OFFICER);
            reckless1.setPersonality(Personalities.RECKLESS);
            reckless1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
            reckless1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            reckless1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            reckless1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
            reckless1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
            reckless1.getStats().setLevel(5);

            PersonAPI reckless2 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
            reckless2.setId("de_generic_officer_andor_reckless2");
            reckless2.setPostId(Ranks.POST_OFFICER);
            reckless2.setRankId(Ranks.POST_OFFICER);
            reckless2.setPersonality(Personalities.RECKLESS);
            reckless2.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
            reckless2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            reckless2.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            reckless2.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
            reckless2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
            reckless2.getStats().setLevel(5);

            // Should add superfleet 2 to Ryzan Supercomplex(The Lion's Cubs) - about 1/2 the size of superfleet 1
            if (!isPAGSM) {
                FleetParamsV3 params3 = new FleetParamsV3(
                        system.getEntityById("ryzan_supercomplex").getMarket(), // add a source(has to be from a MarketAPI)
                        null, // loc in hyper; don't need if have market
                        "sindrian_diktat",
                        2f, // quality override route.getQualityOverride()
                        FleetTypes.PATROL_LARGE,
                        1f, // combatPts(minimal so special ships can be added)(500f otherwise)
                        0f, // freighterPts
                        0f, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        0f// qualityMod
                );
                params3.officerNumberMult = 2f;
                params3.officerLevelBonus = 2;
                params3.officerNumberBonus = 2;
                params3.officerLevelLimit = Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
                params3.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
                params3.averageSMods = 0;
                params3.commander = Global.getSector().getImportantPeople().getPerson("de_admiral2");
                params3.flagshipVariantId = "executor_Elite";
                CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params3);
                if (fleet == null || fleet.isEmpty()) return;
                fleet.setFaction("sindrian_diktat", true);
                fleet.getFlagship().setShipName("SDS Eye of Andrada");
                fleet.getFlagship().setId("executor_Elite"); // executor
                fleet.getFleetData().addFleetMember("executor_Elite").setCaptain(person1);
                fleet.getFleetData().addFleetMember("executor_Elite").setCaptain(person2);
                fleet.getFleetData().addFleetMember("executor_Elite").setCaptain(person3);
                fleet.getFleetData().addFleetMember("eagle_LG_Support");
                fleet.getFleetData().addFleetMember("eagle_LG_Support");
                fleet.getFleetData().addFleetMember("eagle_LG_Support");
                fleet.getFleetData().addFleetMember("champion_LMane");
                fleet.getFleetData().addFleetMember("champion_LMane");
                fleet.getFleetData().addFleetMember("heron_LPatience");
                fleet.getFleetData().addFleetMember("heron_LPatience");
                fleet.getFleetData().addFleetMember("heron_LPatience");
                fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless1);
                fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless2);
                fleet.getFleetData().addFleetMember("fury_LFury");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.setNoFactionInName(true);
                fleet.setName("The Lion's Cubs");
                // a1.getMarket().getContainingLocation().addEntity(fleet);
                system.getEntityById("ryzan_supercomplex").getContainingLocation().addEntity(fleet);
                fleet.setAI(Global.getFactory().createFleetAI(fleet));
                //fleet.setMarket(system.getEntityById("ryzan_supercomplex").getMarket());
                fleet.setLocation(system.getEntityById("ryzan_supercomplex").getLocation().x, system.getEntityById("ryzan_supercomplex").getLocation().y);
                fleet.setFacing((float) Math.random() * 360f);
                fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, system.getEntityById("ryzan_supercomplex"), (float) Math.random() * 90000f, null);
            } else {
                // The Manager's Interns(PAGSM variant)
                FleetParamsV3 params3 = new FleetParamsV3(
                        system.getEntityById("ryzan_supercomplex").getMarket(), // add a source(has to be from a MarketAPI)
                        null, // loc in hyper; don't need if have market
                        "sindrian_diktat",
                        2f, // quality override route.getQualityOverride()
                        FleetTypes.PATROL_LARGE,
                        1f, // combatPts(minimal so special ships can be added)(500f otherwise)
                        0f, // freighterPts
                        0f, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        0f// qualityMod
                );
                params3.officerNumberMult = 2f;
                params3.officerLevelBonus = 2;
                params3.officerNumberBonus = 2;
                params3.officerLevelLimit = Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
                params3.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
                params3.averageSMods = 0;
                params3.commander = Global.getSector().getImportantPeople().getPerson("de_admiral2");
                params3.flagshipVariantId = "sfcskyrend_Beamer";
                CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params3);
                if (fleet == null || fleet.isEmpty()) return;
                fleet.setFaction("sindrian_diktat", true);
                fleet.getFlagship().setShipName("SFS Eye of Andrada");
                fleet.getFlagship().setId("sfcskyrend_Beamer"); // executor
                fleet.getFleetData().addFleetMember("sfcskyrend_Beamer").setCaptain(person1);
                fleet.getFleetData().addFleetMember("sfciapetus_Mixed").setCaptain(person2);
                fleet.getFleetData().addFleetMember("sfciapetus_Mixed").setCaptain(person3);
                fleet.getFleetData().addFleetMember("sfccrius_Pressure");
                fleet.getFleetData().addFleetMember("sfccrius_Pressure");
                fleet.getFleetData().addFleetMember("sfccrius_Pressure");
                fleet.getFleetData().addFleetMember("sfccrius_Pressure");
                fleet.getFleetData().addFleetMember("sfccrius_Pressure");
                fleet.getFleetData().addFleetMember("sfcarke_Suppression");
                fleet.getFleetData().addFleetMember("sfcarke_Suppression");
                fleet.getFleetData().addFleetMember("sfcarke_Suppression");
                fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless1);
                fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless2);
                fleet.getFleetData().addFleetMember("fury_LFury");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.getFleetData().addFleetMember("centurion_LG_Assault");
                fleet.setNoFactionInName(true);
                fleet.setName("The Manager's Interns");
                // a1.getMarket().getContainingLocation().addEntity(fleet);
                system.getEntityById("ryzan_supercomplex").getContainingLocation().addEntity(fleet);
                fleet.setAI(Global.getFactory().createFleetAI(fleet));
                //fleet.setMarket(system.getEntityById("ryzan_supercomplex").getMarket());
                fleet.setLocation(system.getEntityById("ryzan_supercomplex").getLocation().x, system.getEntityById("ryzan_supercomplex").getLocation().y);
                fleet.setFacing((float) Math.random() * 360f);
                fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, system.getEntityById("ryzan_supercomplex"), (float) Math.random() * 90000f, null);
            }
        }
            // Super Saturn - Originally in Askonia, but moved to Andor for convenience's sake
            PlanetAPI erreichen = system.addPlanet("erreichen", star, "Erreichen", "ice_giant", 30, 500, 15000, 2000);
            erreichen.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE); // Add conditions(UNINHABITED PLANETS ONLY)
            erreichen.getMarket().addCondition(Conditions.EXTREME_WEATHER);
            erreichen.getMarket().addCondition(Conditions.DARK);
            erreichen.getMarket().addCondition(Conditions.VERY_COLD);
            erreichen.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
            erreichen.setCustomDescriptionId("planet_erreichen");
/*      // The old, cringe ring system
        system.addAsteroidBelt(erreichen, 300, 1700, 2000, 150, 170, Terrain.ASTEROID_BELT, "Erreichen Super Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 700, 40f);
        system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 200f, 700, 40f, Terrain.RING, "Dust Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 900, 50f);
        system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 200f, 900, 50f, Terrain.RING, "Dust Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 1100, 70f);
        system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 200f, 1100, 70f, Terrain.RING, "Dust Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 1300, 80f);
        system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 200f, 1300, 80f, Terrain.RING, "Dust Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 1500, 90f);
        system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.LIGHT_GRAY, 200f, 1500, 90f, Terrain.RING, "Dust Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 1700, 100f);
        system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.WHITE, 200f, 1700, 100f, Terrain.RING, "Cloud Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 1900, 110f);
        system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.WHITE, 200f, 1900, 110f, Terrain.RING, "Cloud Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 2100, 120f);
        system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.WHITE, 200f, 2100, 120f, Terrain.RING, "Cloud Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 2300, 130f);
        system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.WHITE, 200f, 2300, 130f, Terrain.RING, "Cloud Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 2500, 140f);
        system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.WHITE, 200f, 2500, 140f, Terrain.RING, "Cloud Ring");
        // system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.WHITE, 200f, 2700, 150f);
        system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.WHITE, 200f, 2700, 150f, Terrain.RING, "Cloud Ring");
*/
            // Ring system definitely not ripped from Magec
            system.addAsteroidBelt(erreichen, 100, 1000, 300, 150, 250, Terrain.ASTEROID_BELT, null);
            system.addAsteroidBelt(erreichen, 100, 1550, 150, 150, 250, Terrain.ASTEROID_BELT, null);
            system.addAsteroidBelt(erreichen, 100, 2000, 100, 150, 250, Terrain.ASTEROID_BELT, null);
            system.addAsteroidBelt(erreichen, 100, 2550, 150, 150, 250, Terrain.ASTEROID_BELT, null);

            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 700, 80f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 900, 100f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 1100, 130f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 1300, 80f);

            // add one ring that covers all of the above
            SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(600 + 256, 1000, null, "Erreichen Super Ring"));
            ring.setCircularOrbit(erreichen, 0, 0, 100);

            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 1400, 40f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 1500, 80f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 1600, 120f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 1700, 160f);

            // add one ring that covers all of the above
            ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(200 + 256, 1550, null, "Erreichen Super Ring"));
            ring.setCircularOrbit(erreichen, 0, 0, 100);


//		system.addRingBand(a2, "misc", "rings1", 256f, 0, Color.white, 256f, 1700, 50f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 0, Color.white, 256f, 1700, 70f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 1, Color.white, 256f, 1700, 90f);
//		system.addRingBand(a2, "misc", "rings1", 256f, 1, Color.white, 256f, 1700, 110f);

            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 1900, 140f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 2000, 180f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 2100, 220f);

            // add one ring that covers all of the above
            ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(200 + 256, 2000, null, "Erreichen Super Ring"));
            ring.setCircularOrbit(erreichen, 0, 0, 100);


            system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 0, Color.white, 256f, 2400, 100f);
            system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 2500, 140f);
            system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.white, 256f, 2600, 160f);
            system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 2700, 180f);

            // add one ring that covers all of the above
            ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(300 + 256, 2550, null, "Erreichen Super Ring"));
            ring.setCircularOrbit(erreichen, 0, 0, 100);

            // Diktat hates free port
            SectorEntityToken neutralStation = system.addCustomEntity("askonia_abandoned_station", "Erreichen Freeport", "station_lowtech3", "neutral");
            neutralStation.setCircularOrbitPointingDown(system.getEntityById("erreichen"), 45, 2800, 100);
            neutralStation.setInteractionImage("illustrations", "abandoned_station2");
            if (!isPAGSM) {
                neutralStation.setCustomDescriptionId("erreichen_freeport");
            } else {
                neutralStation.setCustomDescriptionId("erreichen_freeport_PAGSM");
            }

            Misc.setAbandonedStationMarket("askonia_abandoned_station_market", neutralStation);

            DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    200f, // field radius - should not go above 1000 for performance reasons
                    2f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days
                    0f); // days the field will keep generating glowing pieces
            params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
            params.baseSalvageXP = 1000; // base XP for scavenging in field
            SectorEntityToken freeportDebris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
            freeportDebris.setSensorProfile(500f);
            freeportDebris.setDiscoverable(true);
            freeportDebris.setCircularOrbit(neutralStation, 0f, 0f, 100f);
            freeportDebris.setId("askonia_freeportDebris");

            addDerelict(system, neutralStation, "hammerhead_Balanced", ShipRecoverySpecial.ShipCondition.AVERAGE, 200f, true);
            addDerelict(system, neutralStation, "eagle_Balanced", ShipRecoverySpecial.ShipCondition.BATTERED, 220f, false);
            addDerelict(system, neutralStation, "champion_Escort", ShipRecoverySpecial.ShipCondition.WRECKED, 240f, false);
            addDerelict(system, neutralStation, "centurion_Assault", ShipRecoverySpecial.ShipCondition.AVERAGE, 190f, true);
            addDerelict(system, neutralStation, "tempest_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, 280f, false);
            addDerelict(system, neutralStation, "venture_Balanced", ShipRecoverySpecial.ShipCondition.BATTERED, 250f, true);
            addDerelict(system, neutralStation, "conquest_Elite", ShipRecoverySpecial.ShipCondition.BATTERED, 270f, false);
            addDerelict(system, neutralStation, "drover_Strike", ShipRecoverySpecial.ShipCondition.AVERAGE, 260f, false);
            addDerelict(system, neutralStation, "fury_Attack", ShipRecoverySpecial.ShipCondition.WRECKED, 230f, false);
            addDerelict(system, neutralStation, "kite_Standard", ShipRecoverySpecial.ShipCondition.PRISTINE, 180f, true); // Get trolled
            addDerelict(system, neutralStation, "heron_Attack", ShipRecoverySpecial.ShipCondition.AVERAGE, 300f, true);
            addDerelict(system, neutralStation, "harbinger_Strike", ShipRecoverySpecial.ShipCondition.WRECKED, 310f, false);
            addDerelict(system, neutralStation, "omen_PD", ShipRecoverySpecial.ShipCondition.BATTERED, 290f, true);
            addDerelict(system, erreichen, "hyperion_Strike", ShipRecoverySpecial.ShipCondition.WRECKED, 5000f, false);

            system.addAsteroidBelt(erreichen, 100, 3000, 126, 150, 170, Terrain.ASTEROID_BELT, "Erreichen Outer Debris Field");
            system.addRingBand(erreichen, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 3000, 160f);
            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 3020, 160f);

            // Coalescing planetoid that's practically worthless
            PlanetAPI duden = system.addPlanet("duden", system.getEntityById("erreichen"), "Duden", "barren-bombarded", 150, 30, 3000, 200);
            duden.getMarket().addCondition(Conditions.VERY_COLD);
            duden.getMarket().addCondition(Conditions.DARK);
            duden.getMarket().addCondition(Conditions.METEOR_IMPACTS);
            duden.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
            duden.getMarket().addCondition(Conditions.ORE_ABUNDANT);
            duden.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
            duden.setCustomDescriptionId("planet_duden");

            system.addRingBand(erreichen, "misc", "rings_dust0", 256f, 1, Color.WHITE, 100f, 3100, 200f, Terrain.RING, "Outer Ring");
            system.addRingBand(erreichen, "misc", "rings_ice0", 256f, 1, Color.WHITE, 100f, 3100, 200f, Terrain.RING, "Outer Ring");

            SectorEntityToken andor_outer_relay = system.addCustomEntity("andor_sensor", "Erreichen Ring Listening Post", "sensor_array", "sindrian_diktat");
            andor_outer_relay.setCircularOrbitPointingDown(erreichen, 0,  // angle
                    3300, // orbit radius
                    200); // orbit days

            // Erreichen's fat trojans
            SectorEntityToken erreichenL4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                            500f, // min radius
                            1000f, // max radius
                            50, // min asteroid count
                            100, // max asteroid count
                            4f, // min asteroid radius
                            16f, // max asteroid radius
                            "Erreichen L4 Asteroids")); // null for default name

            SectorEntityToken erreichenL5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                    new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                            500f, // min radius
                            1000f, // max radius
                            50, // min asteroid count
                            100, // max asteroid count
                            4f, // min asteroid radius
                            16f, // max asteroid radius
                            "Erreichen L5 Asteroids")); // null for default name

            erreichenL4.setCircularOrbit(star, 30 + 60, 15000, 2000);
            erreichenL5.setCircularOrbit(star, 30 - 60, 15000, 2000);

            // Abandoned mining project in Erreichen's L4
            SectorEntityToken wreck = DerelictThemeGenerator.addSalvageEntity(system, Entities.STATION_MINING, Factions.DERELICT);
            wreck.setId("andor_remains");
            wreck.setCircularOrbit(star, 30 + 60, 15000, 2000);
            Misc.setDefenderOverride(wreck, new DefenderDataOverride(Factions.DERELICT, 0, 0, 0));
            wreck.setDiscoverable(Boolean.TRUE);

            // Debris field + Nav Buoy in Erreichen's L5
            SectorEntityToken andor_nav_bouy = system.addCustomEntity("andor_nav", "Erreichen L5 Navigation Buoy", "nav_buoy", "sindrian_diktat");
            andor_nav_bouy.setCircularOrbitPointingDown(star, 30 - 60,  // angle
                    15000, // orbit radius
                    2000); // orbit days
            DebrisFieldTerrainPlugin.DebrisFieldParams params2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    500f, // field radius - should not go above 1000 for performance reasons
                    4f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days
                    0f); // days the field will keep generating glowing pieces
            params2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
            params2.baseSalvageXP = 500; // base XP for scavenging in field
            SectorEntityToken L5Debris = Misc.addDebrisField(system, params2, StarSystemGenerator.random);
            L5Debris.setSensorProfile(500f);
            L5Debris.setDiscoverable(true);
            L5Debris.setCircularOrbit(erreichenL5, 0f, 0f, 2000f);
            L5Debris.setId("andor_L5Debris");


            float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                    1, 9, // min/max entities to add
                    17000, // radius to start adding at
                    9, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                    true, // whether to use custom or system-name based names
                    true); // whether to allow habitable worlds

            system.autogenerateHyperspaceJumpPoints(true, true);
        }
        // Function for adding derelicts, just copy-paste this into your system's java file to use it
        private void addDerelict (StarSystemAPI system,
                SectorEntityToken focus,
                String variantId,
                ShipRecoverySpecial.ShipCondition condition,
        float orbitRadius,
        boolean recoverable){
            DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
            SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
            ship.setDiscoverable(true);

            float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
            ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

            if (recoverable) {
                SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
                Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
            }
        }

        // function for clearing deep hyperspace around a system, just copy-paste this into your system's java file to use it
        public static void clearDeepHyper (SectorEntityToken entity,float radius){
            HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
            NebulaEditor editor = new NebulaEditor(plugin);

            float minRadius = plugin.getTileSize() * 2f;
            editor.clearArc(entity.getLocation().x, entity.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
            editor.clearArc(entity.getLocation().x, entity.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
        }

}
