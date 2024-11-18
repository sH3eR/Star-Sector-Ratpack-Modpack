package data.scripts.world.systems;


import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RingBandAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.istl_Commodities;
import data.campaign.ids.istl_Conditions;
import data.campaign.ids.istl_Entities;
import data.campaign.ids.istl_Factions;
import data.campaign.ids.istl_Tags;
import data.campaign.ids.istl_Terrain;
import data.campaign.procgen.themes.BladeBreakerStationFleetManager;
import data.campaign.procgen.themes.BladeBreakerThemeGenerator;
import static data.campaign.procgen.themes.BladeBreakerThemeGenerator.addBladeBreakerStationInteractionConfig;
import static data.scripts.world.SigmaUtils.ISTL_GUARDIAN_ACOLYTE;
import static data.scripts.world.SigmaUtils.ISTL_GUARDIAN_DMG;
import static data.scripts.world.SigmaUtils.ISTL_GUARDIAN_STD;
import static data.scripts.world.SigmaUtils.level;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.Random;

public class BessonConstellation {

    // used to keep the random location consistent between seeds, helpful for save transfer
    Random characterSaveSeed = StarSystemGenerator.random;
    Random random = new Random(characterSaveSeed.nextLong());
    // to be honest the double random should be overkill, yet it seems to make the distribution more "random"  
    // I would blame that normal generation seems to use about 10% of the possible seeds
    float selector = random.nextFloat();

    // generates an elliptical area, from where a random coordinate is selected for system generation
    // in this case the area is set to the sector's southwestern quadrant, instead of the whole sector
    // this is in order to avoid finding this sytem becoming a "find a needle in a haystack" task, as the star's name will also be random
    float spawnXradius = 3200f; // horizontal ellipse radius
    float spawnYradius = 1800f; // vertical ellipse radius
    float spawnXoffset = 6000f; // circular area X axis origin
    float spawnYoffset = -38400f; // circular area Y axis origin

    //values used to select a coordinate inside the ellipse
    //use squared radii, otherwise the distribution will be clustered in the origin
    float selectionXradiusSq = selector * spawnXradius * spawnXradius;
    float selectionYradiusSq = selector * spawnYradius * spawnYradius;
    float selectionAngle = selector * 360f;

    public float hsLocationX = (float) (sqrt(selectionXradiusSq) * cos(selectionAngle));
    public float hsLocationY = (float) (sqrt(selectionYradiusSq) * sin(selectionAngle));

    float A1Xoffset = (float) (-500 + Math.random() * 500f);
    float A1Yoffset = (float) (1000 + Math.random() * -500f);

    float A2Xoffset = (float) (-3000 + Math.random() * -1000f);
    float A2Yoffset = (float) (1500 + Math.random() * 1500f);

    float A3Xoffset = (float) (1500 + Math.random() * 1500f);
    float A3Yoffset = (float) (1250 + Math.random() * 1250f);

    float A4Xoffset = (float) (3000 + Math.random() * 1000f);
    float A4Yoffset = (float) (-2000 + Math.random() * -1500f);
    
    float A5Xoffset = (float) (A3Xoffset + 1250 +  Math.random() * 500f);
    float A5Yoffset = (float) (A3Yoffset - 1000 + Math.random() * -750f);

    // name list for random renaming
    String[] strings = {"Giraud", "Druilet", "Jodorowsky", "Bourgeon", "Mézières", "Tardi", "Dionnet"};
    int nameSelector = random.nextInt(strings.length);      // random name selector
    public String StarName = strings[nameSelector];
        
    //Random derelict radius 
    public static float radius_star = 350f;
    public static float radius_station = 2600f;
    public static float radius_acolyte = 1450f;
    public static float radius_variation = 400f;
    public static float radius_derelictvar = 700f;
    
        public void generate(SectorAPI sector) {

        LocationAPI hyper = Global.getSector().getHyperspace();

        StarAge istl_constellation_Age = StarAge.ANY;

        if (selector < 0.33f) {
            istl_constellation_Age = StarAge.YOUNG;
        }
        if (selector >= 0.33f && selector < 0.66f) {
            istl_constellation_Age = StarAge.AVERAGE;
        }
        if (selector >= 0.66f) {
            istl_constellation_Age = StarAge.OLD;
        }

        // create the constellation nebula       
        Constellation istl_constellation_Besson = new Constellation(
                Constellation.ConstellationType.NORMAL, istl_constellation_Age
        );

        NameGenData data = new NameGenData("null", "null");
        ProcgenUsedNames.NamePick constname = new ProcgenUsedNames.NamePick(data, strings[nameSelector], "null");
        istl_constellation_Besson.setNamePick(constname);      // sets the new star name
        
        StarSystemAPI system_besson = sector.createStarSystem("Alpha " + StarName);
        StarSystemAPI system_two = sector.createStarSystem("Beta " + StarName);
        StarSystemAPI system_three = sector.createStarSystem("Gamma " + StarName);
        StarSystemAPI system_four = sector.createStarSystem("Delta " + StarName);
        StarSystemAPI system_five = sector.createStarSystem("Epsilon " + StarName);
        
        istl_constellation_Besson.getSystems().add(sector.getStarSystem("Alpha " + StarName));
        istl_constellation_Besson.getSystems().add(sector.getStarSystem("Beta " + StarName));
        istl_constellation_Besson.getSystems().add(sector.getStarSystem("Gamma " + StarName));
        istl_constellation_Besson.getSystems().add(sector.getStarSystem("Delta " + StarName));
        istl_constellation_Besson.getSystems().add(sector.getStarSystem("Epsilon " + StarName));

        sector.getStarSystem("Alpha " + StarName).setConstellation(istl_constellation_Besson);
        sector.getStarSystem("Beta " + StarName).setConstellation(istl_constellation_Besson);
        sector.getStarSystem("Gamma " + StarName).setConstellation(istl_constellation_Besson);
        sector.getStarSystem("Delta " + StarName).setConstellation(istl_constellation_Besson);
        sector.getStarSystem("Epsilon " + StarName).setConstellation(istl_constellation_Besson);
        
        /////////////////
        //BESSON SYSTEM//
        /////////////////
        system_besson.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
                
                SectorEntityToken besson_nebula = Misc.addNebulaFromPNG("data/campaign/terrain/eos_nebula.png",
			0, 0, // center of nebula
			system_besson, // location to add to
                        "terrain", "istl_nebula_sigma", // "nebula_blue", // texture to use, uses xxx_map for map
                        4, 4, StarAge.OLD); // number of cells in texture
		
		// create the star and generate the hyperspace anchor for this system
		// Besson, an enormous blue giant
		PlanetAPI besson_star = system_besson.initStar("besson", // unique id for this star 
			"star_blue_giant",  // id in planets.json
			1350f, // radius (in pixels at default zoom)
                        (hsLocationX + spawnXoffset + A1Xoffset), // hyper space location x axis
                        (hsLocationY + spawnYoffset + A1Yoffset), // hyper space location y axis
                        900); // corona radius, from star edge
                                                                                    
		system_besson.setLightColor(new Color(210, 230, 255)); // light color in entire system_besson, affects all entities
                
                //This is where I add tags and a beacon.
                system_besson.addTag(istl_Tags.THEME_BREAKER);
                system_besson.addTag(istl_Tags.THEME_BREAKER_MAIN);
                system_besson.addTag(istl_Tags.THEME_BREAKER_RESURGENT);
                system_besson.addTag(istl_Tags.THEME_BREAKER_HOMEWORLD);
                system_besson.addTag(Tags.THEME_UNSAFE);
                system_besson.addTag(Tags.THEME_HIDDEN);
                BladeBreakerThemeGenerator.addBeacon(system_besson, BladeBreakerThemeGenerator.BladeBreakerSystemType.RESURGENT);
                
                //Mod tags.
                system_besson.addTag("sun_sl_hidden");
                
		// Inner Asteroid belt.
		system_besson.addAsteroidBelt(besson_star, 75, 2400, 100, 30, 90, Terrain.ASTEROID_BELT, null);
		system_besson.addRingBand(besson_star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 2350, 300f, Terrain.ASTEROID_BELT, null);
                
                // Some debris in the inner system_besson.
                DebrisFieldTerrainPlugin.DebrisFieldParams params1 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    360f, // field radius - should not go above 1000 for performance reasons
                    1.2f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days 
                    0f); // days the field will keep generating glowing pieces
                params1.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params1.baseSalvageXP = 500; // base XP for scavenging in field
                SectorEntityToken debrisInner1 = Misc.addDebrisField(system_besson, params1, StarSystemGenerator.random);
                debrisInner1.setSensorProfile(800f);
                debrisInner1.setDiscoverable(true);
                debrisInner1.setCircularOrbit(besson_star, 360*(float)Math.random(), 2375, 210f);
                debrisInner1.setId("besson_debrisInner1");
                
                // Add a magnetic field.
                SectorEntityToken besson_field1 = system_besson.addTerrain(Terrain.MAGNETIC_FIELD,
			new MagneticFieldTerrainPlugin.MagneticFieldParams(700f, // terrain effect band width 
			2850, // terrain effect middle radius
			besson_star, // entity that it's around
			2500f, // visual band start
			3200f, // visual band end
			new Color(50, 30, 100, 30), // base color
			0.3f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
			new Color(50, 20, 110, 130),
			new Color(150, 30, 120, 150), 
			new Color(200, 50, 130, 190),
			new Color(250, 70, 150, 240),
			new Color(200, 80, 130, 255),
			new Color(75, 0, 160), 
			new Color(127, 0, 255)
			));
                besson_field1.setCircularOrbit(besson_star, 0, 0, 120);
                
                // Fuel dump for Nex start
                SectorEntityToken fueldump = DerelictThemeGenerator.addSalvageEntity(system_besson, Entities.SUPPLY_CACHE, Factions.DERELICT);
                fueldump.setId("besson_fueldump");
                fueldump.setCircularOrbit(besson_star, 360*(float)Math.random(), 3750, 300f);
                fueldump.setDiscoverable(Boolean.TRUE);
                
                // Derelict survey probe around Besson.
                SectorEntityToken shipDerelict2 = DerelictThemeGenerator.addSalvageEntity(system_besson, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT);
                shipDerelict2.setId("besson_probe1");
                shipDerelict2.setCircularOrbit(besson_star, 360*(float)Math.random(), 4250, 320f);
                Misc.setDefenderOverride(shipDerelict2, new DefenderDataOverride("blade_breakers", 1f, 3, 7));

                
                // Stable location in inner system_besson.
                SectorEntityToken besson_inner_loc = system_besson.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
		besson_inner_loc.setCircularOrbitPointingDown(besson_star, 360*(float)Math.random(), 4800, 300f);
                
                // Le Monde des Mille Couleurs - Lenze, the enigmatic source of the Sigma Event.
                PlanetAPI lenze = system_besson.addPlanet("istl_planet_lenze", besson_star, "Lenze", "istl_sigmaworld", 145, 150, 5600, -180);
                //lenze.getSpec().setAtmosphereColor(new Color(200,145,255,255));
		//lenze.getSpec().setCloudColor(new Color(200,185,255,200));
		//lenze.getSpec().setIconColor(new Color(145,195,255,255));
		lenze.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "aurorae"));
		lenze.getSpec().setGlowColor(new Color(165, 215, 255, 255));
		lenze.getSpec().setUseReverseLightForGlow(true);
		lenze.getSpec().setShieldTexture(Global.getSettings().getSpriteName("industry", "istl_shield_texture"));
		lenze.getSpec().setShieldThickness(0.06f);
		lenze.getSpec().setShieldColor(new Color(255,255,255,100));
                lenze.applySpecChanges();
                
                //SectorEntityToken lenzeStation = system_besson.addCustomEntity("istl_lenze_port", "Lenze Atoll", "station_breaker", "blade_breakers");
                //lenzeStation.setCircularOrbitPointingDown(system_besson.getEntityById("istl_planet_lenze"), 180, 350, 35);
                //lenzeStation.setInteractionImage("illustrations", "orbital");
                
                // Fake market for conditions.
                    Misc.initConditionMarket(lenze);
                    lenze.getMarket().addCondition(istl_Conditions.SIGMARAD);
                    lenze.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
                    lenze.getMarket().addCondition(Conditions.EXTREME_WEATHER);
                    lenze.getMarket().addCondition(Conditions.IRRADIATED);
                    lenze.getMarket().addCondition(Conditions.ORE_ULTRARICH);
                    lenze.getMarket().addCondition(Conditions.RARE_ORE_RICH);
                    lenze.getMarket().addCondition(Conditions.RUINS_SCATTERED);
                
                // Lenze's various crazyass FX and radiation belts.
                    // Strangelets and particles.
                    system_besson.addRingBand(lenze, "misc", "rings_dust0", 96f, 1, Color.white, 32f, 180, 75f, Terrain.ASTEROID_BELT, null);
                    // Add sigma distortion belt 
                    system_besson.addCorona(lenze, istl_Terrain.CORONA_SIGMA,
                            150f, // radius outside planet
                            15f, // burn level of "wind"
                            0f, // flare probability
                            2f // CR loss mult while in it
                            );
                    // Purple.
                    SectorEntityToken lenze_field1 = system_besson.addTerrain(Terrain.MAGNETIC_FIELD,
                            new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, // terrain effect band width 
                            190, // terrain effect middle radius
                            lenze, // entity that it's around
                            145f, // visual band start
                            345f, // visual band end
                            new Color(50, 30, 100, 120), // base color, increment brightness down by 15 each time
                            0.5f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                            new Color(50, 20, 110, 130), // Standard aurora colors.
                            new Color(150, 30, 120, 150), 
                            new Color(200, 50, 130, 190),
                            new Color(250, 70, 150, 240),
                            new Color(200, 80, 130, 255),
                            new Color(75, 0, 160), 
                            new Color(127, 0, 255)
                            ));
                    lenze_field1.setCircularOrbit(lenze, 0, 0, 70);
                        // Green-blue inner layer. Very optional.
                        //SectorEntityToken lenze_field4 = system_besson.addTerrain(Terrain.MAGNETIC_FIELD,
                                //new MagneticFieldTerrainPlugin.MagneticFieldParams(360f, // terrain effect band width 
                                //230, // terrain effect middle radius
                                //lenze, // entity that it's around
                                //185f, // visual band start, increment by 15
                                //505f, // visual band end
                                //new Color(30, 105, 120, 90), // base color, increment brightness down by 15 each time
                                //0.3f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                                //new Color(25, 90, 90, 130), // We're winging it here.
                                //new Color(40, 120, 120, 150), 
                                //new Color(50, 135, 135, 190),
                                //new Color(60, 150, 150, 240),
                                //new Color(70, 175, 175, 255),
                                //new Color(40, 0, 125), 
                                //new Color(45, 100, 100)
                                //));
                        //lenze_field4.setCircularOrbit(lenze, 0, 0, 100);
                    // Pale blue auroral shocks.
                    SectorEntityToken lenze_field2 = system_besson.addTerrain(Terrain.MAGNETIC_FIELD,
                            new MagneticFieldTerrainPlugin.MagneticFieldParams(600f, // terrain effect band width 
                            300, // terrain effect middle radius
                            lenze, // entity that it's around
                            150f, // visual band start, increment by 15
                            750f, // visual band end
                            new Color(10, 25, 60, 5), // base color, increment brightness down by 15 each time
                            0.1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                            new Color(25, 60, 150, 120), // We're winging it here.
                            new Color(40, 90, 180, 135), 
                            new Color(50, 105, 195, 165),
                            new Color(60, 120, 210, 185),
                            new Color(70, 145, 225, 195),
                            new Color(10, 0, 125), 
                            new Color(15, 50, 100)
                            ));
                    lenze_field2.setCircularOrbit(lenze, 0, 0, 85);
                    // Bright blue-white boundary layer.
                    SectorEntityToken lenze_field3 = system_besson.addTerrain(Terrain.MAGNETIC_FIELD,
                            new MagneticFieldTerrainPlugin.MagneticFieldParams(90f, // terrain effect band width 
                            185, // terrain effect middle radius
                            lenze, // entity that it's around
                            140f, // visual band start, increment by 15
                            230f, // visual band end
                            new Color(165, 215, 255, 120), // base color, increment brightness down by 15 each time
                            0f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                            new Color(25, 60, 150, 100), // We're winging it here.
                            new Color(40, 90, 180, 115), 
                            new Color(50, 105, 195, 145),
                            new Color(60, 120, 210, 160),
                            new Color(70, 145, 225, 170),
                            new Color(10, 0, 125), 
                            new Color(15, 50, 100)
                            ));
                    lenze_field3.setCircularOrbit(lenze, 0, 0, 120);
                    
                lenze.setCustomDescriptionId("planet_lenze");
                //lenzeStation.setCustomDescriptionId("station_lenze");

                
                // Add a warning beacon for Lenze - reference the Daedaleon beacon code in the Eos Exodus system_besson file and the entry in rules.csv.
		CustomCampaignEntityAPI beacon = system_besson.addCustomEntity(null, null, "istl_bladebreaker_beacon", Factions.NEUTRAL);
		//CustomCampaignEntityAPI beacon = system_besson.addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
		beacon.setCircularOrbitPointingDown(lenze, 0, 500, -35);
		beacon.getMemoryWithoutUpdate().set("$istl_lenzewarn", true);
		//Misc.setWarningBeaconGlowColor(beacon, Global.getSector().getFaction(Factions.DASSAULT).getBrightUIColor());
		//Misc.setWarningBeaconPingColor(beacon, Global.getSector().getFaction(Factions.DASSAULT).getBrightUIColor());
		//And then use $istl_lenzewarn as a condition for custom interaction text in rules.csv.
            
                // Spawn defense fleets around Lenze. Should be naaasty.
                FleetMemberAPI member1 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ISTL_GUARDIAN_STD);
                member1.getRepairTracker().setCR(member1.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet1 = FleetFactoryV3.createEmptyFleet("blade_breakers", "battlestation", null);

                //float radius_fleet1 = radius_star + radius_station + radius_variation * (float) Math.random();

                fleet1.getFleetData().addFleetMember(member1);
                fleet1.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet1.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet1.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet1.setStationMode(Boolean.valueOf(true));
                addBladeBreakerStationInteractionConfig(fleet1);
                system_besson.addEntity(fleet1);

                fleet1.clearAbilities();
                fleet1.addAbility("transponder");
                fleet1.getAbility("transponder").activate();
                fleet1.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet1.setCircularOrbitWithSpin(
                        lenze, // focus
                60,// angle
                300, // orbitRadius, was radius_fleet1, useful if random location in system_besson
                28f, // orbitDays
                7f, // minSpin
                12f // maxSpin
                );
                fleet1.setAI(null);
                PersonAPI commander1 = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("blade_breakers"), level, true);
                commander1.getStats().setSkillLevel("gunnery_implants", 3.0F);
                FleetFactoryV3.addCommanderSkills(commander1, fleet1, random);
                fleet1.setCommander(commander1);
                fleet1.getFlagship().setCaptain(commander1);
                
                FleetMemberAPI member2 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ISTL_GUARDIAN_STD);
                member2.getRepairTracker().setCR(member2.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet2 = FleetFactoryV3.createEmptyFleet("blade_breakers", "battlestation", null);

                //float radius_fleet1 = radius_star + radius_station + radius_variation * (float) Math.random();

                fleet2.getFleetData().addFleetMember(member2);
                fleet2.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet2.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet2.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet2.setStationMode(Boolean.valueOf(true));
                addBladeBreakerStationInteractionConfig(fleet2);
                system_besson.addEntity(fleet2);

                fleet2.clearAbilities();
                fleet2.addAbility("transponder");
                fleet2.getAbility("transponder").activate();
                fleet2.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet2.setCircularOrbitWithSpin(
                        lenze, // focus
                180,// angle
                300, // orbitRadius, was radius_fleet1, useful if random location in system_besson
                28f, // orbitDays
                7f, // minSpin
                12f // maxSpin
                );
                fleet2.setAI(null);
                PersonAPI commander2 = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("blade_breakers"), level, true);
                commander2.getStats().setSkillLevel("gunnery_implants", 3.0F);
                FleetFactoryV3.addCommanderSkills(commander2, fleet2, random);
                fleet2.setCommander(commander2);
                fleet2.getFlagship().setCaptain(commander2);
                
                FleetMemberAPI member3 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ISTL_GUARDIAN_STD);
                member3.getRepairTracker().setCR(member3.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet3 = FleetFactoryV3.createEmptyFleet("blade_breakers", "battlestation", null);

                //float radius_fleet1 = radius_star + radius_station + radius_variation * (float) Math.random();

                fleet3.getFleetData().addFleetMember(member3);
                fleet3.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet3.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet3.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet3.setStationMode(Boolean.valueOf(true));
                addBladeBreakerStationInteractionConfig(fleet3);
                system_besson.addEntity(fleet3);

                fleet3.clearAbilities();
                fleet3.addAbility("transponder");
                fleet3.getAbility("transponder").activate();
                fleet3.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet3.setCircularOrbitWithSpin(
                        lenze, // focus
                300,// angle
                300, // orbitRadius, was radius_fleet1, useful if random location in system_besson
                28f, // orbitDays
                7f, // minSpin
                12f // maxSpin
                );
                fleet3.setAI(null);
                PersonAPI commander3 = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("blade_breakers"), level, true);
                commander3.getStats().setSkillLevel("gunnery_implants", 3.0F);
                FleetFactoryV3.addCommanderSkills(commander3, fleet3, random);
                fleet3.setCommander(commander3);
                fleet3.getFlagship().setCaptain(commander3);
                
                int maxFleets = 7 + this.random.nextInt(2);

                BladeBreakerStationFleetManager guardian1Fleets = new BladeBreakerStationFleetManager(
                fleet1, 1.0f, 0, maxFleets, 10.0f, 9, 27);
                BladeBreakerStationFleetManager guardian2Fleets = new BladeBreakerStationFleetManager(
                fleet2, 1.0f, 0, maxFleets, 10.0f, 12, 36);
                BladeBreakerStationFleetManager guardian3Fleets = new BladeBreakerStationFleetManager(
                fleet3, 1.0f, 0, maxFleets, 10.0f, 9, 27);
                system_besson.addScript(guardian1Fleets);
                system_besson.addScript(guardian2Fleets);
                system_besson.addScript(guardian3Fleets);
                
                // Inner jump point ---------------
                JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("besson_inner_jump", ("Alpha " + StarName + " Jump-point"));
		jumpPoint1.setCircularOrbit( system_besson.getEntityById("besson"), 300, 7200, 240);
		//jumpPoint1.setRelatedPlanet(lenze);
		system_besson.addEntity(jumpPoint1);       
                
                // More asteroid belts.
                system_besson.addAsteroidBelt(besson_star, 90, 7750, 480, 95, 120, Terrain.ASTEROID_BELT,  "Nikita Belt");
		system_besson.addRingBand(besson_star, "misc", "rings_dust0", 256f, 0, Color.gray, 256f, 7600, 105f, null, null);
		system_besson.addRingBand(besson_star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 7720, 125f, null, null);
                
                system_besson.addAsteroidBelt(besson_star, 90, 8400, 540, 105, 135, Terrain.ASTEROID_BELT,  "Paradise Belt");
		system_besson.addRingBand(besson_star, "misc", "rings_dust0", 256f, 0, Color.gray, 256f, 8450, 128f, null, null);
                
                // Some more debris fields. Random locations.
                DebrisFieldTerrainPlugin.DebrisFieldParams params2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    360f, // field radius - should not go above 1000 for performance reasons
                    1.2f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days 
                    0f); // days the field will keep generating glowing pieces
                params2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params2.baseSalvageXP = 500; // base XP for scavenging in field
                SectorEntityToken debrisInner2 = Misc.addDebrisField(system_besson, params2, StarSystemGenerator.random);
                debrisInner2.setSensorProfile(800f);
                debrisInner2.setDiscoverable(true);
                debrisInner2.setCircularOrbit(besson_star, 360*(float)Math.random(), 7720, 180f);
                debrisInner2.setId("besson_debrisInner2");
                
                DebrisFieldTerrainPlugin.DebrisFieldParams params3 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    240f, // field radius - should not go above 1000 for performance reasons
                    1.0f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days 
                    0f); // days the field will keep generating glowing pieces
                params3.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params3.baseSalvageXP = 350; // base XP for scavenging in field
                SectorEntityToken debrisInner3 = Misc.addDebrisField(system_besson, params3, StarSystemGenerator.random);
                debrisInner3.setSensorProfile(600f);
                debrisInner3.setDiscoverable(true);
                debrisInner3.setCircularOrbit(besson_star, 360*(float)Math.random(), 7720, 180f);
                debrisInner3.setId("besson_debrisInner3");
                
                DebrisFieldTerrainPlugin.DebrisFieldParams params4 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    360f, // field radius - should not go above 1000 for performance reasons
                    1.2f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days 
                    0f); // days the field will keep generating glowing pieces
                params4.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params4.baseSalvageXP = 500; // base XP for scavenging in field
                SectorEntityToken debrisInner4 = Misc.addDebrisField(system_besson, params4, StarSystemGenerator.random);
                debrisInner4.setSensorProfile(800f);
                debrisInner4.setDiscoverable(true);
                debrisInner4.setCircularOrbit(besson_star, 360*(float)Math.random(), 8425, 210f);
                debrisInner4.setId("besson_debrisInner4");         
                
                // Relay at L3.
                SectorEntityToken besson_relay = system_besson.addCustomEntity("besson_relay", // unique id
                        "Alpha " + StarName + " Relay", // name - if null, defaultName from custom_entities.json will be used
                        "comm_relay", // type of object, defined in custom_entities.json
                        Factions.NEUTRAL); // faction
                besson_relay.setCircularOrbitPointingDown(system_besson.getEntityById("besson"), 120, 9600, 400);     
                
                // Stable location at L1.
                SectorEntityToken besson_l1_loc = system_besson.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
		besson_l1_loc.setCircularOrbitPointingDown(besson_star, 300, 9600, 400f);

                // Ice giant at L3.
                PlanetAPI marat = system_besson.addPlanet("marat", besson_star, "Marat", "ice_giant", 120, 350, 12800, 400);
                marat.setCustomDescriptionId("planet_marat");
                    
                    // Thin ring
                    system_besson.addRingBand(marat, "misc", "rings_dust0", 256f, 0, Color.white, 128f, 450, 135f, null, null);
                    // Moon #1
                    PlanetAPI marat1 = system_besson.addPlanet("istl_planet_marat1", marat, "Marat I", "cryovolcanic", 75, 45, 540, 180);
                    // Add fixed conditions to Marat 1.
                    Misc.initConditionMarket(marat1);
                    marat1.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
                    marat1.getMarket().addCondition(Conditions.LOW_GRAVITY);
                    marat1.getMarket().addCondition(Conditions.COLD);
                    marat1.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
                    //marat1.setCustomDescriptionId("planet_marat1");
                    // Thick ring
                    system_besson.addRingBand(marat, "misc", "rings_special0", 256f, 1, new Color(180,180,180,255), 160f, 760, 30f, Terrain.RING, null); 
                    // Moon #2
                    PlanetAPI marat2 = system_besson.addPlanet("istl_planet_marat2", marat, "Marat II", "frozen", 135, 90, 1100, 200);
                    // Add fixed conditions to Marat 2.
                    Misc.initConditionMarket(marat2);
                    marat2.getMarket().addCondition(Conditions.COLD);
                    marat2.getMarket().addCondition(Conditions.ORE_SPARSE);
                    marat2.getMarket().addCondition(Conditions.VOLATILES_TRACE);
                    marat2.getMarket().addCondition(Conditions.ORGANICS_COMMON);
                    marat2.getMarket().addCondition(Conditions.RUINS_WIDESPREAD);
                    //marat2.setCustomDescriptionId("planet_marat2");
                    // Thin ring again
                    system_besson.addRingBand(marat, "misc", "rings_dust0", 256f, 0, Color.gray, 192f, 1350, 160f, null, null);
                    // Moon #3
                    PlanetAPI marat3 = system_besson.addPlanet("istl_planet_marat3", marat, "Marat III", "barren", 215, 60, 1560, 220);
                    // Add fixed conditions to Marat 3.
                    Misc.initConditionMarket(marat3);
                    marat3.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                    marat3.getMarket().addCondition(Conditions.LOW_GRAVITY);
                    marat3.getMarket().addCondition(Conditions.VERY_COLD);
                    marat3.getMarket().addCondition(Conditions.ORE_MODERATE);
                    marat3.getMarket().addCondition(Conditions.RUINS_SCATTERED);
                    //marat3.setCustomDescriptionId("planet_marat3");
                    
                    // Survey ship.
                    SectorEntityToken shipDerelict1 = DerelictThemeGenerator.addSalvageEntity(system_besson, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
                    shipDerelict1.setId("besson_surveyship");
                    shipDerelict1.setCircularOrbit(marat, 225, 600, 180f);
                    Misc.setDefenderOverride(shipDerelict1, new DefenderDataOverride("blade_breakers", 1f, 7, 15));
                    
                    // Add a domain probe orbiting further out.
                    SectorEntityToken shipDerelict3 = DerelictThemeGenerator.addSalvageEntity(system_besson, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT);
                    shipDerelict3.setId("besson_probe2");
                    shipDerelict3.setCircularOrbit(marat, 360*(float)Math.random(), 1960, 270f);
                    Misc.setDefenderOverride(shipDerelict3, new DefenderDataOverride("blade_breakers", 1f, 3, 7));
                    
                // Besson's secondary, Lucille.
                PlanetAPI besson_star_b = system_besson.addPlanet("lucille", besson_star, "Alpha " + StarName + " B", "star_browndwarf", 300, 560, 12800, 400);
		system_besson.setSecondary(besson_star_b);
                //besson_star_b.setCustomDescriptionId("star_lucille");
                    
                    // Let's throw in a magnetic field
                    SectorEntityToken lucille_field1 = system_besson.addTerrain(Terrain.MAGNETIC_FIELD,
                            new MagneticFieldTerrainPlugin.MagneticFieldParams(200f, // terrain effect band width 
                            660, // terrain effect middle radius
                            besson_star_b, // entity that it's around
                            560f, // visual band start
                            760f, // visual band end
                            new Color(50, 30, 100, 60), // base color
                            0.6f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                            new Color(50, 20, 110, 130),
                            new Color(150, 30, 120, 150), 
                            new Color(200, 50, 130, 190),
                            new Color(250, 70, 150, 240),
                            new Color(200, 80, 130, 255),
                            new Color(75, 0, 160), 
                            new Color(127, 0, 255)
                            ));
                    lucille_field1.setCircularOrbit(besson_star_b, 0, 0, 100);
               
                    // Lucille's dust ring, ooh la la.
                    system_besson.addRingBand(besson_star_b, "misc", "rings_dust0", 256f, 1, Color.gray, 256f, 960, 100f);
                    system_besson.addAsteroidBelt(besson_star_b, 120, 960, 300, 200, 300, Terrain.ASTEROID_BELT, "Laplace Stream");
                          
                    // And another magnetic field
                    SectorEntityToken lucille_field2 = system_besson.addTerrain(Terrain.MAGNETIC_FIELD,
                            new MagneticFieldTerrainPlugin.MagneticFieldParams(120f, // terrain effect band width 
                            1140, // terrain effect middle radius
                            besson_star_b, // entity that it's around
                            1080f, // visual band start
                            1200f, // visual band end
                            new Color(50, 30, 100, 30), // base color
                            0.3f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                            new Color(50, 20, 110, 130),
                            new Color(150, 30, 120, 150), 
                            new Color(200, 50, 130, 190),
                            new Color(250, 70, 150, 240),
                            new Color(200, 80, 130, 255),
                            new Color(75, 0, 160), 
                            new Color(127, 0, 255)
                            ));
                    lucille_field2.setCircularOrbit(besson_star_b, 0, 0, 100); 
                    
                    // Lucille needs some moons.
                        // First moon.
                        PlanetAPI lucille1 = system_besson.addPlanet("istl_planet_lucille1", besson_star_b, "Alpha " + StarName + " B I", "barren-bombarded", 360*(float)Math.random(), 40, 1280, 140);
                            // Add fixed conditions to Lucille I.
                            Misc.initConditionMarket(lucille1);
                            lucille1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                            lucille1.getMarket().addCondition(Conditions.LOW_GRAVITY);
                            lucille1.getMarket().addCondition(Conditions.METEOR_IMPACTS);
                            //lucille1.setCustomDescriptionId("planet_lucille1");

                        // Second moon. The habitable one.
                        PlanetAPI lucille2 = system_besson.addPlanet("istl_planet_lucille2", besson_star_b, "Source", "terran-eccentric", 360*(float)Math.random(), 60, 1600, 180);
                            // Add fixed conditions to Source.
                            Misc.initConditionMarket(lucille2);
                            lucille2.getMarket().addCondition(Conditions.HABITABLE);
                            lucille2.getMarket().addCondition(Conditions.LOW_GRAVITY);
                            lucille2.getMarket().addCondition(Conditions.FARMLAND_RICH);
                            lucille2.getMarket().addCondition(Conditions.RUINS_WIDESPREAD);
                            lucille2.setCustomDescriptionId("planet_source");
                            
                        // A Blade Breaker research station in orbit. Removed
//                        SectorEntityToken stationDerelict2 = DerelictThemeGenerator.addSalvageEntity(system_besson, istl_Entities.STATION_RESEARCH_BREAKER, Factions.DERELICT);
//                            stationDerelict2.setId("besson_derelict2");
//                            stationDerelict2.setCircularOrbit(lucille2, 360*(float)Math.random(), 240, 75f);
//                            Misc.setDefenderOverride(stationDerelict2, new DefenderDataOverride("blade_breakers", 1f, 15, 36));
                        
                        // Derelict Tempest.
                        addDerelict(system_besson, lucille2, "tempest_righthand", ShipRecoverySpecial.ShipCondition.GOOD, 120f, true, "the_deserter");
                    
                    // Lucille trojans.
                    SectorEntityToken lucilleL4 = system_besson.addTerrain(Terrain.ASTEROID_FIELD,
                            new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                            920f, // min radius
                            1280f, // max radius
                            40, // min asteroid count
                            72, // max asteroid count
                            8f, // min asteroid radius 
                            24f, // max asteroid radius
                            "Alpha " + StarName + " B L4 Shoal Zone")); // null for default name
                    
                    SectorEntityToken lucilleL5 = system_besson.addTerrain(Terrain.ASTEROID_FIELD,
                            new AsteroidFieldTerrainPlugin.AsteroidFieldParams(
                            920f, // min radius
                            1280f, // max radius
                            40, // min asteroid count
                            72, // max asteroid count
                            8f, // min asteroid radius 
                            24f, // max asteroid radius
                            "Alpha " + StarName + " B L5 Shoal Zone")); // null for default name

                    lucilleL4.setCircularOrbit(besson_star, 360f, 12800, 400);
                    lucilleL5.setCircularOrbit(besson_star, 240f, 12800, 400);
                    
                    // Mining station at L4.
                    SectorEntityToken stationDerelict1 = DerelictThemeGenerator.addSalvageEntity(system_besson, istl_Entities.STATION_MINING_BREAKER, istl_Factions.BREAKERS);
                    stationDerelict1.setId("besson_derelict1");
                    stationDerelict1.setCircularOrbit(besson_star, 360, 12800, 400f);
                    Misc.setDefenderOverride(stationDerelict1, new DefenderDataOverride("blade_breakers", 1f, 5, 11));
                    CargoAPI extraStation1Salvage = Global.getFactory().createCargo(true);
                    extraStation1Salvage.addCommodity(istl_Commodities.SIGMA_MATTER_HIGH, 1);
                    extraStation1Salvage.addCommodity(istl_Commodities.SIGMA_MATTER_LOW, 3);
                    extraStation1Salvage.addCommodity(istl_Commodities.SIGMA_MATTER_UNSTABLE, 6);
                    BaseSalvageSpecial.addExtraSalvage(extraStation1Salvage, stationDerelict1.getMemoryWithoutUpdate(), -1);                    
                    
                    // Something fun at L5.
                    SectorEntityToken l5scrap = DerelictThemeGenerator.addSalvageEntity(system_besson, istl_Entities.WEAPONS_CACHE_BREAKER, istl_Factions.BREAKERS);
                    l5scrap.setId("besson_l5scrap");
                    l5scrap.setCircularOrbit(besson_star, 240, 12800, 400);
                    //Misc.setDefenderOverride(l5scrap, new DefenderDataOverride(Factions.DERELICT, 1, 2, 0));
                    Misc.setDefenderOverride(l5scrap, new DefenderDataOverride("blade_breakers", 1f, 3, 7));
                    l5scrap.setDiscoverable(Boolean.TRUE);

                    //Derelict Lilin.
                    addDerelict(system_besson, l5scrap, "istl_bbminerunner_std", ShipRecoverySpecial.ShipCondition.AVERAGE, 180f, true, "the_deserter");

        
                // Abandoned research station at L2.
                SectorEntityToken stationDerelict3 = DerelictThemeGenerator.addSalvageEntity(system_besson, istl_Entities.STATION_RESEARCH_BREAKER, istl_Factions.BREAKERS);
		stationDerelict3.setId("besson_derelict3");
		stationDerelict3.setCircularOrbit(besson_star, 300, 16000, 400f);
                //Misc.setDefenderOverride(stationDerelict2, new DefenderDataOverride(Factions.DERELICT, 1f, 4, 12));
		Misc.setDefenderOverride(stationDerelict3, new DefenderDataOverride("blade_breakers", 1f, 15, 36));
		CargoAPI extraStation2Salvage = Global.getFactory().createCargo(true);
		extraStation2Salvage.addCommodity(Commodities.ALPHA_CORE, 1);
                extraStation2Salvage.addCommodity(istl_Commodities.SIGMA_MATTER_LOW, 2);
		BaseSalvageSpecial.addExtraSalvage(extraStation2Salvage, stationDerelict3.getMemoryWithoutUpdate(), -1);
                
                // Planetoid for spacing?
                PlanetAPI besson4 = system_besson.addPlanet("istl_planet_besson4", besson_star, "Alpha " + StarName + " IV", "barren", 360*(float)Math.random(), 60, 17600, 480);
                
                // Random assignment for Nevsky Polis - hack on this more later.
                //String[] random_assign = {"thing1", "thing2", "thing3"};
                //int orbitSelector = random.nextInt(random_assign.length);      // random name selector
                //public String RandomOrbit = strings[orbitSelector];
                
                // Nevsky Polis - no longer at L1, orbits the outer system_besson. Random range with a ring.
                float radius_nevsky = 18400f + 800*(float)Math.random();
                SectorEntityToken bessonHabitat = system_besson.addCustomEntity("besson_habitat", "Nevsky Polis", "station_side07", "neutral");
                system_besson.addRingBand(besson_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, radius_nevsky, 520f, Terrain.RING, "Outer Band");
                bessonHabitat.setCircularOrbitWithSpin(system_besson.getEntityById("besson"), 360*(float)Math.random(), radius_nevsky, 480f, 9, 21);
                bessonHabitat.setDiscoverable(true);
                bessonHabitat.setDiscoveryXP(3500f);
                bessonHabitat.setSensorProfile(0.3f);
                                
                    // Abandoned marketplace for Nevsky Polis.
                    bessonHabitat.getMemoryWithoutUpdate().set("$abandonedStation", true);
                    Misc.setAbandonedStationMarket("abandoned_habitat_market", bessonHabitat);
                    bessonHabitat.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSpecial(new SpecialItemData("istl_sigmatech_package", null), 1f);
                    bessonHabitat.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSpecial(new SpecialItemData("istl_deserter_package", null), 1f);
                    bessonHabitat.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSpecial(new SpecialItemData("istl_bladewood_package", null), 1f);
                    //bessonHabitat.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "istl_stormkestrel_proto_test", "Unit 01");
                    //bessonHabitat.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "istl_curse_proto_test", "Unit 02");
                    
                    bessonHabitat.setCustomDescriptionId("station_besson");
                
                // Procgen makes you strong.
                float radiusAfter = StarSystemGenerator.addOrbitingEntities(system_besson, besson_star, StarAge.AVERAGE,
                        2, 5, // min/max entities to add
                        21600, // radius to start adding at 
                        4, // name offset - next planet will be <system_besson name> <roman numeral of this parameter + 1>
                        true); // whether to use custom or system_besson-name based names
                
                // Add a nebula to the system_besson.
		//StarSystemGenerator.addSystemwideNebula(system_besson, StarAge.OLD);
                
                // generates hyperspace destinations for in-system_besson jump points
		system_besson.autogenerateHyperspaceJumpPoints(true, true);

                cleanup(system_besson);        
        
        /////////////////
        //TWO SYSTEM   //
        /////////////////
        system_two.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI two_star = system_two.initStar(
                "Beta " + StarName, // unique id for this star
                StarTypes.ORANGE, // id in planets.json
                345f, // radius (in pixels at default zoom)
                (hsLocationX + spawnXoffset + A2Xoffset), // hyper space location x axis
                (hsLocationY + spawnYoffset + A2Yoffset), // hyper space location y axis
                255 // corona radius, from star edge
        );

        system_two.setLightColor(new Color(255, 225, 205)); // light color in entire system, affects all entities

            //This is where I add tags.
            system_two.addTag(istl_Tags.THEME_BREAKER);
            system_two.addTag(istl_Tags.THEME_BREAKER_MAIN);
            system_two.addTag(istl_Tags.THEME_BREAKER_SUPPRESSED);
            system_two.addTag(Tags.THEME_UNSAFE);
            BladeBreakerThemeGenerator.addBeacon(system_two, BladeBreakerThemeGenerator.BladeBreakerSystemType.SUPPRESSED);
        
            //Mod tags.
            system_two.addTag("sun_sl_hidden");
            
        two_star.setName("Beta " + StarName);
        system_two.setName("Beta " + StarName + " Star System");

        StarSystemGenerator.addSystemwideNebula(system_two, istl_constellation_Age);
        
            // Add single Acolyte
            FleetMemberAPI member4 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ISTL_GUARDIAN_ACOLYTE);
                member4.getRepairTracker().setCR(member4.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet4 = FleetFactoryV3.createEmptyFleet("blade_breakers", "battlestation", null);

                float radius_fleet4 = radius_star + (radius_acolyte + 1200f) + radius_variation * (float) Math.random();

                fleet4.getFleetData().addFleetMember(member4);
                fleet4.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet4.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet4.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet4.setStationMode(Boolean.valueOf(true));
                addBladeBreakerStationInteractionConfig(fleet4);
                system_two.addEntity(fleet4);

                fleet4.clearAbilities();
                fleet4.addAbility("transponder");
                fleet4.getAbility("transponder").activate();
                fleet4.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet4.setCircularOrbitWithSpin(
                    two_star, // focus
                    0f + (float) Math.random() * 5f,// angle
                    radius_fleet4, // orbitRadius
                    240f, // orbitDays
                    7f, // minSpin
                    12f // maxSpin
                    );
                fleet4.setAI(null);
                PersonAPI commander4 = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("blade_breakers"), level, true);
                commander4.getStats().setSkillLevel("gunnery_implants", 3.0F);
                FleetFactoryV3.addCommanderSkills(commander4, fleet4, random);
                fleet4.setCommander(commander4);
                fleet4.getFlagship().setCaptain(commander4);
                
                BladeBreakerStationFleetManager guardian4Fleets = new BladeBreakerStationFleetManager(
                fleet4, 1.0f, 0, maxFleets, 10.0f, 4, 12);
                system_two.addScript(guardian4Fleets);        
                
        //Prototype Curse.
        float radius_curse = radius_fleet4 - 2450f;
        system_two.addRingBand(two_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, radius_curse, 90f, Terrain.RING, "Karres Dust Band");
        addDerelict(system_two, two_star, "istl_curse_proto_test", ShipRecoverySpecial.ShipCondition.GOOD, radius_curse, true, "the_deserter");
                
        //Add a few more derelicts to the Cursed Dust Band, including a few prototype Imps (one recoverable).
                addDerelict(system_two, two_star, "istl_imp_proto_test", ShipRecoverySpecial.ShipCondition.BATTERED, radius_curse - 100, true, "the_deserter");                
                addDerelict(system_two, two_star, "starliner_Standard", ShipRecoverySpecial.ShipCondition.GOOD, radius_curse + 75f, true, "independent");                
                addDerelict(system_two, two_star, "istl_puddlejumper_mk1_std", ShipRecoverySpecial.ShipCondition.WRECKED, radius_curse - 75f, false, "independent");                
                addDerelict(system_two, two_star, "mudskipper2_CS", ShipRecoverySpecial.ShipCondition.BATTERED, radius_curse - 25f, true, "independent");      
                addDerelict(system_two, two_star, "wolf_Assault", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_curse + 25f, false, "independent");
                addDerelict(system_two, two_star, "afflictor_Strike", ShipRecoverySpecial.ShipCondition.WRECKED, radius_curse + 50f, false, "independent");
                addDerelict(system_two, two_star, "nebula_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, radius_curse + 125f, false, "independent");
                addDerelict(system_two, two_star, "tarsus_Standard", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_curse - 50f, false, "independent");
        
        //Add another asteroid ring
        system_two.addRingBand(two_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, radius_curse + 300, 115f, Terrain.RING, "Tears Of Laureline");
        system_two.addRingBand(two_star, "misc", "rings_dust0", 256f, 2, Color.white, 256f, radius_curse + 350, 120f, Terrain.RING, "Tears Of Laureline");
        system_two.addRingBand(two_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, radius_curse + 400, 125f, Terrain.RING, "Tears Of Laureline");

        //Laureline, a jungle world red in tooth and claw.
        PlanetAPI laureline = system_two.addPlanet("istl_planet_laureline", two_star, "Laureline", "jungle", 300, 120, 6400, 210f);
        laureline.setCustomDescriptionId("planet_laureline");
        laureline.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
        laureline.getSpec().setGlowColor( new Color(255,75,25,255) );
        laureline.getSpec().setUseReverseLightForGlow(true);
                
                //Add fixed conditions to Laureline.
                Misc.initConditionMarket(laureline);
                laureline.getMarket().addCondition(Conditions.HABITABLE);
                laureline.getMarket().addCondition(Conditions.HOT);
                laureline.getMarket().addCondition(Conditions.DECIVILIZED);
                laureline.getMarket().addCondition(Conditions.RUINS_VAST);
                laureline.getMarket().addCondition(Conditions.INIMICAL_BIOSPHERE);
                laureline.getMarket().addCondition(Conditions.FARMLAND_RICH);
                laureline.getMarket().addCondition(Conditions.ORGANICS_COMMON);
                
                //Add L4 and L5 stable locations for Laureline.
                SectorEntityToken laureline_l4_loc = system_two.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
                laureline_l4_loc.setCircularOrbitPointingDown(two_star, 240, 6400, 210f);
                
                SectorEntityToken laureline_l5_loc = system_two.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
                laureline_l5_loc.setCircularOrbitPointingDown(two_star, 0, 6400, 210f);
        
                //Add a derelict habitat opposite Laureline.
                SectorEntityToken stationDerelict = DerelictThemeGenerator.addSalvageEntity(system_two, Entities.ORBITAL_HABITAT, Factions.DERELICT);
		stationDerelict.setId("two_derelict");
		stationDerelict.setCircularOrbit(two_star, 120, 6400, 210f);
		Misc.setDefenderOverride(stationDerelict, new DefenderDataOverride("blade_breakers", 1f, 2, 8));
		CargoAPI extraStation3Salvage = Global.getFactory().createCargo(true);
		extraStation3Salvage.addCommodity(Commodities.BETA_CORE, 2);
		extraStation3Salvage.addCommodity(istl_Commodities.SIGMA_MATTER_UNSTABLE, 3);
		BaseSalvageSpecial.addExtraSalvage(extraStation3Salvage, stationDerelict.getMemoryWithoutUpdate(), -1);
                
        //Add a last brief asteroid ring
        system_two.addRingBand(two_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 7200, 240f, Terrain.RING, "Dust Band");
        
        float two_Outer = StarSystemGenerator.addOrbitingEntities(
                system_two,
                two_star,
                istl_constellation_Age,
                3, 5, // min/max entities to add
                8000, // radius to start adding at 
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true // whether to use custom or system-name based names  
        );

        system_two.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system_two);        
        
        /////////////////
        //THREE SYSTEM //
        /////////////////
        system_three.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI three_star = system_three.initStar(
                "Gamma " + StarName, // unique id for this star
                StarTypes.RED_DWARF, // id in planets.json
                315f, // radius (in pixels at default zoom)
                (hsLocationX + spawnXoffset + A3Xoffset), // hyper space location x axis
                (hsLocationY + spawnYoffset + A3Yoffset), // hyper space location y axis
                255 // corona radius, from star edge
        );

        system_three.setLightColor(new Color(255, 225, 205)); // light color in entire system, affects all entities

            //This is where I add tags.
            system_three.addTag(istl_Tags.THEME_BREAKER);
            system_three.addTag(istl_Tags.THEME_BREAKER_MAIN);
            system_three.addTag(istl_Tags.THEME_BREAKER_SUPPRESSED);
            system_three.addTag(Tags.THEME_UNSAFE);
            //BladeBreakerThemeGenerator.addBeacon(system_three, BladeBreakerThemeGenerator.BladeBreakerSystemType.SUPPRESSED);
        
            //Mod tags.
            system_three.addTag("sun_sl_hidden");
            
        three_star.setName("Gamma " + StarName);
        system_three.setName("Gamma " + StarName + " Star System");

        StarSystemGenerator.addSystemwideNebula(system_three, istl_constellation_Age);
        
            // Add single Acolyte
            FleetMemberAPI member5 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ISTL_GUARDIAN_ACOLYTE);
                member5.getRepairTracker().setCR(member5.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet5 = FleetFactoryV3.createEmptyFleet("blade_breakers", "battlestation", null);

                float radius_fleet5 = radius_star + radius_acolyte + radius_variation * (float) Math.random();

                fleet5.getFleetData().addFleetMember(member5);
                fleet5.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet5.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet5.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet5.setStationMode(Boolean.valueOf(true));
                addBladeBreakerStationInteractionConfig(fleet5);
                system_three.addEntity(fleet5);

                fleet5.clearAbilities();
                fleet5.addAbility("transponder");
                fleet5.getAbility("transponder").activate();
                fleet5.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet5.setCircularOrbitWithSpin(
                    three_star, // focus
                    0f + (float) Math.random() * 5f,// angle
                    radius_fleet5, // orbitRadius
                    240f, // orbitDays
                    7f, // minSpin
                    12f // maxSpin
                    );
                fleet5.setAI(null);
                PersonAPI commander5 = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("blade_breakers"), level, true);
                commander5.getStats().setSkillLevel("gunnery_implants", 3.0F);
                FleetFactoryV3.addCommanderSkills(commander5, fleet5, random);
                fleet5.setCommander(commander5);
                fleet5.getFlagship().setCaptain(commander5);
                
                BladeBreakerStationFleetManager guardian5Fleets = new BladeBreakerStationFleetManager(
                fleet5, 1.0f, 0, maxFleets, 10.0f, 5, 14);
                system_three.addScript(guardian5Fleets);        
                
        float three_Outer = StarSystemGenerator.addOrbitingEntities(
                system_three,
                three_star,
                istl_constellation_Age,
                4, 6, // min/max entities to add
                2100, // radius to start adding at 
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true // whether to use custom or system-name based names  
        );

        system_three.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system_three);
        
        /////////////////
        //FOUR SYSTEM  //   Mostly empty, just dangerous
        /////////////////
        system_four.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI four_star = system_four.initStar(
                "Delta " + StarName, // unique id for this star
                StarTypes.WHITE_DWARF, // id in planets.json
                375f, // radius (in pixels at default zoom)
                (hsLocationX + spawnXoffset + A4Xoffset), // hyper space location x axis
                (hsLocationY + spawnYoffset + A4Yoffset), // hyper space location y axis
                255 // corona radius, from star edge
        );

        system_four.setLightColor(new Color(255, 225, 205)); // light color in entire system, affects all entities

            //This is where I add tags.
            system_four.addTag(istl_Tags.THEME_BREAKER);
            system_four.addTag(istl_Tags.THEME_BREAKER_MAIN);
            //system_four.addTag(istl_Tags.THEME_BREAKER_DESTROYED);
            system_four.addTag(istl_Tags.THEME_BREAKER_SUPPRESSED);
            system_four.addTag(Tags.THEME_UNSAFE);
            //BladeBreakerThemeGenerator.addBeacon(system_four, BladeBreakerThemeGenerator.BladeBreakerSystemType.DESTROYED);
        
            //Mod tags.
            system_four.addTag("sun_sl_hidden");
            
        four_star.setName("Delta " + StarName);
        system_four.setName("Delta " + StarName + " Star System");

        StarSystemGenerator.addSystemwideNebula(system_four, istl_constellation_Age);
        
            //Accretion disk code. Implement your own blank texture in place of 'istl_blankring'.
            system_four.addRingBand(four_star, "misc", "istl_blankring", 256f, 0, Color.BLACK, 2160f, 1900, 69f, Terrain.RING, "Delta " + StarName + " Disk");
            
            float spiralFactor = 3f + StarSystemGenerator.random.nextFloat() * 2f;
            
            RingBandAPI accretionDisk1 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_dust0", 256f, 0, Color.WHITE, 256f, 2500, 54f);
            accretionDisk1.setSpiral(true);        
            accretionDisk1.setMinSpiralRadius(150);
            accretionDisk1.setSpiralFactor(spiralFactor);
            
            RingBandAPI accretionDisk2 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_dust0", 256f, 1, Color.WHITE, 256f, 2550, 64f);
            accretionDisk2.setSpiral(true);        
            accretionDisk2.setMinSpiralRadius(250);
            accretionDisk2.setSpiralFactor(spiralFactor);
            
            RingBandAPI accretionDisk3 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_ice0", 256f, 0, Color.WHITE, 256f, 2600, 74f);
            accretionDisk3.setSpiral(true);        
            accretionDisk3.setMinSpiralRadius(300);
            accretionDisk3.setSpiralFactor(spiralFactor);
            
            RingBandAPI accretionDisk4 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_ice0", 256f, 1, Color.WHITE, 256f, 2750, 84f);
            accretionDisk4.setSpiral(true);        
            accretionDisk4.setMinSpiralRadius(350);
            accretionDisk4.setSpiralFactor(spiralFactor);
            
            RingBandAPI accretionDisk5 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_dust0", 256f, 0, Color.WHITE, 256f, 2800, 54f);
            accretionDisk5.setSpiral(true);        
            accretionDisk5.setMinSpiralRadius(150);
            accretionDisk5.setSpiralFactor(spiralFactor);
            
            RingBandAPI accretionDisk6 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_dust0", 256f, 1, Color.WHITE, 256f, 2850, 64f);
            accretionDisk6.setSpiral(true);        
            accretionDisk6.setMinSpiralRadius(250);
            accretionDisk6.setSpiralFactor(spiralFactor);
            
            RingBandAPI accretionDisk7 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_ice0", 256f, 0, Color.WHITE, 256f, 2900, 74f);
            accretionDisk7.setSpiral(true);        
            accretionDisk7.setMinSpiralRadius(300);
            accretionDisk7.setSpiralFactor(spiralFactor);
            
            RingBandAPI accretionDisk8 = (RingBandAPI) 
                system_four.addRingBand(four_star, "misc", "rings_ice0", 256f, 1, Color.WHITE, 256f, 2950, 84f);
            accretionDisk8.setSpiral(true);        
            accretionDisk8.setMinSpiralRadius(350);
            accretionDisk8.setSpiralFactor(spiralFactor);
            
            // Add semi-random mining station
            float radius_stationMining1  = radius_star + 400f + (2 * radius_variation) * (float) Math.random();
            
            SectorEntityToken stationMining1 = DerelictThemeGenerator.addSalvageEntity(system_four, istl_Entities.STATION_MINING_BREAKER, istl_Factions.BREAKERS);
            stationMining1.setId("four_mining1");
            stationMining1.setCircularOrbit(four_star, 360*(float)Math.random(), radius_stationMining1, 320f);
            Misc.setDefenderOverride(stationMining1, new DefenderDataOverride("blade_breakers", 1f, 3, 7));
            CargoAPI extraMining1Salvage = Global.getFactory().createCargo(true);
            extraMining1Salvage.addCommodity(istl_Commodities.SIGMA_MATTER_LOW, 2);
            extraMining1Salvage.addCommodity(istl_Commodities.SIGMA_MATTER_UNSTABLE, 5);
            BaseSalvageSpecial.addExtraSalvage(extraMining1Salvage, stationMining1.getMemoryWithoutUpdate(), -1); 
                    
            // Add single Acolyte
            FleetMemberAPI member6 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ISTL_GUARDIAN_ACOLYTE);
                member6.getRepairTracker().setCR(member6.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet6 = FleetFactoryV3.createEmptyFleet("blade_breakers", "battlestation", null);

                float radius_fleet6 = radius_star + 3025f + radius_variation * (float) Math.random();

                fleet6.getFleetData().addFleetMember(member6);
                fleet6.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet6.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet6.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet6.setStationMode(Boolean.valueOf(true));
                addBladeBreakerStationInteractionConfig(fleet6);
                system_four.addEntity(fleet6);

                fleet6.clearAbilities();
                fleet6.addAbility("transponder");
                fleet6.getAbility("transponder").activate();
                fleet6.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet6.setCircularOrbitWithSpin(
                    four_star, // focus
                    0f + (float) Math.random() * 5f,// angle
                    radius_fleet6, // orbitRadius
                    240f, // orbitDays
                    7f, // minSpin
                    12f // maxSpin
                    );
                fleet6.setAI(null);
                PersonAPI commander6 = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("blade_breakers"), level, true);
                commander6.getStats().setSkillLevel("gunnery_implants", 3.0F);
                FleetFactoryV3.addCommanderSkills(commander6, fleet6, random);
                fleet6.setCommander(commander6);
                fleet6.getFlagship().setCaptain(commander6);
                
                BladeBreakerStationFleetManager guardian6Fleets = new BladeBreakerStationFleetManager(
                fleet6, 1.0f, 0, maxFleets, 10.0f, 3, 9);
                system_four.addScript(guardian5Fleets);

        float four_Outer = StarSystemGenerator.addOrbitingEntities(
                system_four,
                four_star,
                istl_constellation_Age,
                4, 5, // min/max entities to add
                4000, // radius to start adding at 
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true // whether to use custom or system-name based names  
        );

        system_four.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system_four);
        
        /////////////////
        //FIVE SYSTEM  //   Chernov! Chervy! El Artificial Fucking Topo!
        /////////////////
        system_five.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");

        // create the star and generate the hyperspace anchor for this system
        PlanetAPI five_star = system_five.initStar(
                "Epsilon " + StarName, // unique id for this star
                StarTypes.RED_GIANT, // id in planets.json
                1200f, // radius (in pixels at default zoom)
                (hsLocationX + spawnXoffset + A5Xoffset), // hyper space location x axis
                (hsLocationY + spawnYoffset + A5Yoffset), // hyper space location y axis
                800 // corona radius, from star edge
        );

        system_five.setLightColor(new Color(255, 210, 200)); // light color in entire system, affects all entities

            //This is where I add tags.
            system_five.addTag(istl_Tags.THEME_BREAKER);
            system_five.addTag(istl_Tags.THEME_BREAKER_MAIN);
            system_five.addTag(istl_Tags.THEME_BREAKER_SUPPRESSED);
            system_five.addTag(Tags.THEME_UNSAFE);
            BladeBreakerThemeGenerator.addBeacon(system_five, BladeBreakerThemeGenerator.BladeBreakerSystemType.SUPPRESSED);
        
            //Mod tags.
            system_five.addTag("sun_sl_hidden");
            
        five_star.setName("Epsilon " + StarName);
        system_five.setName("Epsilon " + StarName + " Star System");

        StarSystemGenerator.addSystemwideNebula(system_five, istl_constellation_Age);
        
            // Add single damaged Preceptor.
            FleetMemberAPI member7 = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ISTL_GUARDIAN_DMG);
                member7.getRepairTracker().setCR(member7.getRepairTracker().getMaxCR());
                CampaignFleetAPI fleet7 = FleetFactoryV3.createEmptyFleet("blade_breakers", "battlestation", null);

                float radius_fleet7 = 1400f + 2000f + radius_variation * (float) Math.random();

                fleet7.getFleetData().addFleetMember(member7);
                fleet7.getMemoryWithoutUpdate().set("$cfai_makeAggressive", Boolean.valueOf(true));
                fleet7.getMemoryWithoutUpdate().set("$cfai_noJump", Boolean.valueOf(true));
                fleet7.getMemoryWithoutUpdate().set("$cfai_makeAllowDisengage", Boolean.valueOf(true));
                fleet7.setStationMode(Boolean.valueOf(true));
                addBladeBreakerStationInteractionConfig(fleet7);
                system_five.addEntity(fleet7);

                fleet7.clearAbilities();
                fleet7.addAbility("transponder");
                fleet7.getAbility("transponder").activate();
                fleet7.getDetectedRangeMod().modifyFlat("gen", 1000.0F);
                fleet7.setCircularOrbitWithSpin(
                    five_star, // focus
                    0f + (float) Math.random() * 5f,// angle
                    radius_fleet7, // orbitRadius
                    240f, // orbitDays
                    7f, // minSpin
                    12f // maxSpin
                    );
                fleet7.setAI(null);
                PersonAPI commander7 = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction("blade_breakers"), level, true);
                commander7.getStats().setSkillLevel("gunnery_implants", 3.0F);
                FleetFactoryV3.addCommanderSkills(commander7, fleet7, random);
                fleet7.setCommander(commander7);
                fleet7.getFlagship().setCaptain(commander7);
                
                BladeBreakerStationFleetManager guardian7Fleets = new BladeBreakerStationFleetManager(
                fleet7, 1.0f, 0, maxFleets, 10.0f, 7, 18);
                system_five.addScript(guardian7Fleets);

            // Dzerzhinsky, a hot gas giant with several moons.
            PlanetAPI yod2 = system_five.addPlanet("istl_planet_yod2", five_star, "Dzerzhinsky", "gas_giant", 300, 450, 2800, 300f);
                    yod2.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
                    yod2.getSpec().setGlowColor(new Color(235,38,8,145));
                    yod2.getSpec().setUseReverseLightForGlow(true);
                    yod2.getSpec().setAtmosphereThickness(0.5f);
                    yod2.getSpec().setCloudRotation(15f);
                    yod2.getSpec().setAtmosphereColor(new Color(138,118,255,145));
                    yod2.getSpec().setPitch(-5f);
                    yod2.getSpec().setTilt(30f);
                    // Add the toposphere shield - current shields are additive render only, disabled.
                    //yod2.getSpec().setShieldTexture(Global.getSettings().getSpriteName("industry", "istl_dysonshell"));
                    //yod2.getSpec().setShieldThickness(0.12f);
                    //yod2.getSpec().setShieldColor(new Color(255,255,255,255));
                    yod2.applySpecChanges();             

                    // Add fixed conditions to Dzerzhinsky.
                    Misc.initConditionMarket(yod2);
                        yod2.getMarket().addCondition(Conditions.VERY_HOT);
                        yod2.getMarket().addCondition(Conditions.EXTREME_WEATHER);
                        yod2.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
                        yod2.getMarket().addCondition(Conditions.HIGH_GRAVITY);
                        yod2.getMarket().addCondition(Conditions.VOLATILES_DIFFUSE);
                        yod2.getMarket().addCondition(Conditions.ORGANICS_TRACE);
                        yod2.setCustomDescriptionId("planet_yod2");
                        
                    // Add Ion Storm to Dzerzhinsky.
                    system_five.addCorona(yod2, Terrain.CORONA_AKA_MAINYU,
                            500f, // radius outside planet
                            5f, // burn level of "wind"
                            0f, // flare probability
                            1f // CR loss mult while in it
                            );    
                        
                    // First moon.
                    PlanetAPI yod2a = system_five.addPlanet("istl_planet_yod2a", yod2, "Dzerzhinsky A", "barren", 180, 30, 660, 50f);
                        yod2a.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "barren02"));
                        yod2a.getSpec().setAtmosphereThickness(0.15f);
                        yod2a.getSpec().setAtmosphereColor(new Color(138,118,255,175));
                        yod2a.applySpecChanges();    

                    // Add fixed conditions to Dzerzhinsky A.
                    Misc.initConditionMarket(yod2a);
                        yod2a.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                        yod2a.getMarket().addCondition(Conditions.LOW_GRAVITY);
                        yod2a.getMarket().addCondition(Conditions.HOT);
                        yod2a.getMarket().addCondition(Conditions.ORE_MODERATE);
                    //yod2a.setCustomDescriptionId("planet_yod2a");
                    
                    // Abandoned research station opposite Dzerzhinsky A.
                    SectorEntityToken stationDerelict4 = DerelictThemeGenerator.addSalvageEntity(system_five, istl_Entities.STATION_RESEARCH_BREAKER, istl_Factions.BREAKERS);
                    stationDerelict4.setId("yod_derelict4");
                    stationDerelict4.setCircularOrbitPointingDown(yod2, 0, 650, 50f);
                    //Misc.setDefenderOverride(stationDerelict4, new DefenderDataOverride(Factions.DERELICT, 1f, 4, 12));
                    Misc.setDefenderOverride(stationDerelict4, new DefenderDataOverride("blade_breakers", 1f, 15, 36));
                    
                    // Second moon.
                    PlanetAPI yod2b = system_five.addPlanet("istl_planet_yod2b", yod2, "Dzerzhinsky B", "barren-desert", 360*(float)Math.random(), 75, 900, 90f);
                        yod2b.getSpec().setAtmosphereThickness(0.35f);
                        yod2b.getSpec().setCloudRotation(10f);
                        yod2b.getSpec().setAtmosphereColor(new Color(138,118,255,245));
                        yod2b.applySpecChanges();
                    
                    // Add fixed conditions to Dzerzhinsky B.
                    Misc.initConditionMarket(yod2b);
                        yod2b.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
                        yod2b.getMarket().addCondition(Conditions.HOT);
                        yod2b.getMarket().addCondition(Conditions.ORE_SPARSE);
                        yod2b.getMarket().addCondition(Conditions.RUINS_SCATTERED);
                        yod2b.getMarket().addCondition(Conditions.POLLUTION);
                    //yod2a.setCustomDescriptionId("planet_yod2b");
                    // Add Ion Storm to second moon.
                        system_five.addCorona(yod2b, Terrain.CORONA_AKA_MAINYU,
				150f, // radius outside planet
				2f, // burn level of "wind"
				0f, // flare probability
				0.5f // CR loss mult while in it
				); 
		
                // Yod magnetic field
		SectorEntityToken field = system_five.addTerrain(Terrain.MAGNETIC_FIELD,
		new MagneticFieldTerrainPlugin.MagneticFieldParams(600f, // terrain effect band width 
			3450, // terrain effect middle radius
			five_star, // entity that it's around
			3150f, // visual band start
			3750f, // visual band end
			new Color(50, 20, 100, 40), // base color
			1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
			new Color(50, 20, 110, 130),
			new Color(150, 30, 120, 150), 
			new Color(200, 50, 130, 190),
			new Color(250, 70, 150, 240),
			new Color(200, 80, 130, 255),
			new Color(75, 0, 160), 
			new Color(127, 0, 255)
			));
		field.setCircularOrbit(five_star, 0, 0, 150);
                
                //Prototype Sparrowhawk and its ring. This should become a random assigner of... considerable complexity... later.
                float radius_sparrowhawk = 4200f;
                system_five.addRingBand(five_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, radius_sparrowhawk, 180f, Terrain.RING, "Kestrel's Rest");
                addDerelict(system_five, five_star, "istl_stormkestrel_proto_test", ShipRecoverySpecial.ShipCondition.GOOD, radius_sparrowhawk, true, "the_deserter");                
                
                //Add a few more derelicts to Kestrel's Rest, including a few prototype Imps (one recoverable).
                addDerelict(system_five, five_star, "istl_imp_proto_test", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_sparrowhawk - 100f, true, "the_deserter");                
                addDerelict(system_five, five_star, "istl_imp_proto_test", ShipRecoverySpecial.ShipCondition.WRECKED, radius_sparrowhawk + 125f, true, "the_deserter"); 
                addDerelict(system_five, five_star, "istl_imp_proto_test", ShipRecoverySpecial.ShipCondition.BATTERED, radius_sparrowhawk + 50f, true, "the_deserter");                
                addDerelict(system_five, five_star, "nebula_Standard", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_sparrowhawk + 150f, true, "independent");                
                addDerelict(system_five, five_star, "nebula_Standard", ShipRecoverySpecial.ShipCondition.BATTERED, radius_sparrowhawk + 100f, false, "independent");                
                addDerelict(system_five, five_star, "istl_sevastopol_mk1_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_sparrowhawk + 250f, false, "independent");
                addDerelict(system_five, five_star, "shrike_p_Attack", ShipRecoverySpecial.ShipCondition.BATTERED, radius_sparrowhawk - 125f, true, "independent");
                addDerelict(system_five, five_star, "valkyrie_Elite", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_sparrowhawk - 50f, true, "independent");                
                addDerelict(system_five, five_star, "wolf_CS", ShipRecoverySpecial.ShipCondition.WRECKED, radius_sparrowhawk - 75f, false, "independent");                
                addDerelict(system_five, five_star, "istl_puddlejumper_mk1_std", ShipRecoverySpecial.ShipCondition.BATTERED, radius_sparrowhawk + 75F, true, "independent");        
                addDerelict(system_five, five_star, "istl_puddlejumper_mk1_std", ShipRecoverySpecial.ShipCondition.AVERAGE, radius_sparrowhawk + 25F, false, "independent");        
                
            // Chernov, a lava world with an artificial toposphere. Spooky.
            PlanetAPI yod3 = system_five.addPlanet("istl_planet_yod3", five_star, "Chernov", "lava_minor", 120, 200, 5200, 400f);
                
                Misc.initConditionMarket(yod3);
                yod3.getMarket().addCondition(Conditions.HOT);
                yod3.getMarket().addCondition(Conditions.HIGH_GRAVITY);
                yod3.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
                yod3.getMarket().addCondition(Conditions.ORE_ABUNDANT);
                yod3.getMarket().addCondition(Conditions.RARE_ORE_RICH);
                
            PlanetAPI yod3shell = system_five.addPlanet("istl_planet_yod3shell", five_star, "Artificial Toposphere", "istl_dysonshell", 120, 240, 5200, 400f);

                Misc.initConditionMarket(yod3shell);
                yod3shell.getMarket().addCondition(Conditions.HOT);
                yod3shell.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                yod3shell.getMarket().addCondition(Conditions.LOW_GRAVITY);
                yod3shell.getMarket().addCondition(Conditions.ORE_SPARSE);
                yod3shell.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
                
                // Add a shield to the gaps in the toposphere.
                yod3shell.getSpec().setShieldTexture(Global.getSettings().getSpriteName("industry", "istl_dysonshield"));
                yod3shell.getSpec().setShieldThickness(0f);
                yod3shell.getSpec().setShieldColor(new Color(255,255,255,75));
                yod3shell.applySpecChanges();
                
                //Chervy, Chernov's tiny moon.
                PlanetAPI yod3a = system_five.addPlanet("istl_planet_yod3a", yod3, "Chervy", "barren_castiron", 360*(float)Math.random(), 45, 400, 60f);
                
                Misc.initConditionMarket(yod3a);
                yod3a.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
                yod3a.getMarket().addCondition(Conditions.LOW_GRAVITY);
                yod3a.getMarket().addCondition(Conditions.ORE_RICH);
                yod3a.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
                
                // Stable location opposite Chernov.
                SectorEntityToken chernov_loc = system_five.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
                chernov_loc.setCircularOrbitPointingDown(five_star, 300, 5200, 400f);
                
        float yod_Outer = StarSystemGenerator.addOrbitingEntities(
                system_five,
                five_star,
                istl_constellation_Age,
                3, 5, // min/max entities to add
                6000, // radius to start adding at 
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true // whether to use custom or system-name based names  
        );

        system_five.autogenerateHyperspaceJumpPoints(true, true);

        cleanup(system_five);
        }
        
    void cleanup(StarSystemAPI system) {
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

    private void addDerelict(StarSystemAPI system_besson, 
            SectorEntityToken focus, 
            String variantId, 
            ShipRecoverySpecial.ShipCondition condition, 
            float orbitRadius, 
            boolean recoverable,
            String factionIdForShipName) {
        DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = BaseThemeGenerator.addSalvageEntity(system_besson, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        float orbitDays = orbitRadius / (10f + (float) Math.random() * 5f);
        ship.setCircularOrbit(focus, (float) Math.random() * 360f, orbitRadius, orbitDays);

        if (recoverable) {
            SalvageSpecialAssigner.ShipRecoverySpecialCreator creator = new SalvageSpecialAssigner.ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
    }
}
