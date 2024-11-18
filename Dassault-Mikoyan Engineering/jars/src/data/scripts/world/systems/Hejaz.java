package data.scripts.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.istl_Entities;
import data.campaign.ids.istl_Factions;
import data.campaign.ids.istl_Tags;

public class Hejaz {

	public void generate(SectorAPI sector) {	
		
		StarSystemAPI system = sector.createStarSystem("Hejaz");
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background1.jpg");
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI hejaz_star = system.initStar("hejaz", // unique id for this star 
                        "star_red_dwarf",  // id in planets.json
                        300f, 		  // radius (in pixels at default zoom)
                        150, // corona
                        3f, // solar wind burn level
                        0.2f, // flare probability
                        1.0f); // CR loss multiplier, good values are in the range of 1-5
		
		system.setLightColor(new Color(240, 225, 200)); // light color in entire system, affects all entities
        
                //Add vanilla system tags.
                system.addTag(Tags.THEME_CORE);
                system.addTag(Tags.THEME_CORE_UNPOPULATED);
                system.addTag(Tags.THEME_HIDDEN);
                //Add mod tags.
                system.addTag(istl_Tags.THEME_BREAKER_NO_FLEETS);
        
		// Gate of Hejaz
		SectorEntityToken gate = system.addCustomEntity("hejaz_gate", // unique id
			"Hejaz Gate", // name - if null, defaultName from custom_entities.json will be used
			"inactive_gate", // type of object, defined in custom_entities.json
			null); // faction

		gate.setCircularOrbit(system.getEntityById("hejaz"), 360*(float)Math.random(), 1500, 150f);
                        
		system.addAsteroidBelt(hejaz_star, 100, 2250, 500, 290, 310, Terrain.ASTEROID_BELT,  "Hejaz Inner Belt");
		system.addRingBand(hejaz_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 2200, 275f, null, null);
		system.addRingBand(hejaz_star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 2300, 245f, null, null);
		
                // Add debris to Beta belt
                DebrisFieldTerrainPlugin.DebrisFieldParams params1 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    450f, // field radius - should not go above 1000 for performance reasons
                    1.5f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days 
                    0f); // days the field will keep generating glowing pieces
                params1.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params1.baseSalvageXP = 500; // base XP for scavenging in field
                SectorEntityToken debrisBeta1 = Misc.addDebrisField(system, params1, StarSystemGenerator.random);
                debrisBeta1.setSensorProfile(1000f);
                debrisBeta1.setDiscoverable(true);
                debrisBeta1.setCircularOrbit(hejaz_star, 360*(float)Math.random(), 2250, 150f);
                debrisBeta1.setId("hejaz_debris1");
                
                addDerelict(system, hejaz_star, "istl_tereshkova_export", ShipRecoverySpecial.ShipCondition.GOOD, 2250f, true);
                
		system.addAsteroidBelt(hejaz_star, 60, 3250, 500, 290, 310, Terrain.ASTEROID_BELT,  "Hejaz Middle Belt");
		system.addRingBand(hejaz_star, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 3200, 360f, null, null);
		system.addRingBand(hejaz_star, "misc", "rings_ice0", 256f, 2, Color.white, 256f, 3300, 300f, null, null);
                
                DebrisFieldTerrainPlugin.DebrisFieldParams params2 = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                    300f, // field radius - should not go above 1000 for performance reasons
                    1.0f, // density, visual - affects number of debris pieces
                    10000000f, // duration in days 
                    0f); // days the field will keep generating glowing pieces
                params2.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
                params2.baseSalvageXP = 500; // base XP for scavenging in field
                SectorEntityToken debrisBeta2 = Misc.addDebrisField(system, params2, StarSystemGenerator.random);
                debrisBeta2.setSensorProfile(1000f);
                debrisBeta2.setDiscoverable(true);
                debrisBeta2.setCircularOrbit(hejaz_star, 360*(float)Math.random(), 3250, 240f);
                debrisBeta2.setId("hejaz_debris2");
                
                //Qarib, a barren desert world with eventual plot relevance
                PlanetAPI qarib = system.addPlanet("istl_planet_qarib", hejaz_star, "Qarib", "barren-desert", 240, 75, 4000, 180f);
                qarib.getSpec().setCloudRotation(20f);
		qarib.applySpecChanges();
                qarib.setCustomDescriptionId("planet_qarib");
                
                    // Add fixed conditions to Qarib.
                    Misc.initConditionMarket(qarib);
                    qarib.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
                    qarib.getMarket().addCondition(Conditions.LOW_GRAVITY);
                    qarib.getMarket().addCondition(Conditions.HOT);
                    qarib.getMarket().addCondition(Conditions.ORE_SPARSE);
                    qarib.getMarket().getFirstCondition(Conditions.ORE_SPARSE).setSurveyed(true);
                
			// Hejaz jump-point
			JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("hejaz_jump", "Hejaz Bridge");
			jumpPoint1.setCircularOrbit( system.getEntityById("hejaz"), 180, 4000, 180f);
			jumpPoint1.setRelatedPlanet(qarib);
			system.addEntity(jumpPoint1);
                        
                        // Stable location opposite the jump point.
                        SectorEntityToken hejaz_loc = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
                        hejaz_loc.setCircularOrbitPointingDown(hejaz_star, 0, 4000, 180f);
                        
                // Outer dust belt.
                system.addRingBand(hejaz_star, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 4800, 300f, Terrain.RING, "Hejaz Dust Band");
                
                    // And a Blade Breaker habitat.
                    SectorEntityToken stationDerelict1 = DerelictThemeGenerator.addSalvageEntity(system, istl_Entities.ORBITAL_HABITAT_BREAKER, istl_Factions.BREAKERS);
                    stationDerelict1.setId("hejaz_derelict1");
                    stationDerelict1.setCircularOrbit(hejaz_star, 360*(float)Math.random(), 4800, 270f);
                    Misc.setDefenderOverride(stationDerelict1, new DefenderDataOverride("blade_breakers", 1f, 8, 21));      
                    
		float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, hejaz_star, StarAge.OLD,
                        0, 1, // min/max entities to add
			6000, // radius to start adding at 
			1, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
			true); // whether to use custom or system-name based names
		
		system.autogenerateHyperspaceJumpPoints(true, true);
                
                HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
                NebulaEditor editor = new NebulaEditor(plugin);
                float minRadius = plugin.getTileSize() * 2f;

                float radius = system.getMaxRadiusInHyperspace();
                editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
                editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
        }

    private void addDerelict(StarSystemAPI system, 
            SectorEntityToken focus, 
            String variantId, 
            ShipRecoverySpecial.ShipCondition condition, 
            float orbitRadius, 
            boolean recoverable) {
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
}