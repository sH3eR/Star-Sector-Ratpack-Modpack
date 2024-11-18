package bcom;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.SotfModPlugin;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public final class BcomBattleCreationPlugin implements com.fs.starfarer.api.campaign.BattleCreationPlugin {

    private float width, height, prevXDir = 0, prevYDir = 0, coronaIntensity = 0f;

    private List<String> objs = null;

    private BattleCreationContext context;

    private MissionDefinitionAPI loader;

    private StarCoronaTerrainPlugin corona = null;

    private PulsarBeamTerrainPlugin pulsar = null;

    private static final String COMM = "comm_relay", SENSOR = "sensor_array", NAV = "nav_buoy";

    private static final float xPad = 2000, yPad = 3000, SINGLE_PLANET_MAX_DIST = 1000f;

    public interface NebulaTextureProvider { String getNebulaTex(); String getNebulaMapTex(); }


    public void initBattle(final BattleCreationContext context, final MissionDefinitionAPI loader) {

        this.context = context;
        this.loader = loader;
        CampaignFleetAPI playerFleet = context.getPlayerFleet(),
                otherFleet = context.getOtherFleet();
        FleetGoal playerGoal = context.getPlayerGoal(), enemyGoal = context.getOtherGoal();

        Random random = Misc.getRandom(Misc.getSalvageSeed(otherFleet)
                * (long) otherFleet.getFleetData().getNumMembers(), 23);

        boolean playerEscaping = playerGoal == FleetGoal.ESCAPE,
                enemyEscaping = enemyGoal == FleetGoal.ESCAPE;
        boolean escape = playerEscaping || enemyEscaping;

        int maxFleetPoints = (int) Global.getSettings().getFloat("maxNoObjectiveBattleSize");
        int playerFleetPoints = 0; int enemyFleetPoints = 0;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy())
            if (playerEscaping || member.canBeDeployedForCombat())
                playerFleetPoints += member.getUnmodifiedDeploymentPointsCost();
        for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy())
            if (playerEscaping || member.canBeDeployedForCombat())
                enemyFleetPoints += member.getUnmodifiedDeploymentPointsCost();

        int smaller = Math.min(playerFleetPoints, enemyFleetPoints);
        boolean withObjectives = smaller > maxFleetPoints;
        if (!context.objectivesAllowed) withObjectives = false;

        int numObjectives = 0;
        if (withObjectives)
            numObjectives = (playerFleetPoints + enemyFleetPoints > maxFleetPoints + 70)
                    ? 4 : 3 + random.nextInt(2);
        numObjectives = Math.min(numObjectives, 4); // shouldn't be possible, but..

        int baseCommandPoints = (int) Global.getSettings().getFloat("startingCommandPoints");
        loader.initFleet(FleetSide.PLAYER, "ISS", playerGoal, false,
                context.getPlayerCommandPoints() - baseCommandPoints,
                (int) playerFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);
        loader.initFleet(FleetSide.ENEMY, "", enemyGoal, true,
                (int) otherFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);

        List<FleetMemberAPI> playerShips =
                playerFleet.getFleetData().getCombatReadyMembersListCopy();
        List<FleetMemberAPI> enemyShips = otherFleet.getFleetData().getCombatReadyMembersListCopy();
        if (playerGoal == FleetGoal.ESCAPE)
            playerShips = playerFleet.getFleetData().getMembersListCopy();
        if (enemyGoal == FleetGoal.ESCAPE)
            enemyShips = otherFleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : playerShips) loader.addFleetMember(FleetSide.PLAYER, member);
        for (FleetMemberAPI member : enemyShips) loader.addFleetMember(FleetSide.ENEMY, member);

        width = 18000f; height = 18000f;

        boolean playerHasStation = false; boolean enemyHasStation = false;
        for (FleetMemberAPI curr : playerFleet.getFleetData().getMembersListCopy())
            if (curr.isStation()) { playerHasStation = true; break; }
        for (FleetMemberAPI curr : context.getOtherFleet().getFleetData().getMembersListCopy())
            if (curr.isStation()) { enemyHasStation = true; break; }


        if (withObjectives || playerHasStation || enemyHasStation) {
            width = 24000f; height = (numObjectives == 2) ? 14000f : 18000f;
            width *= Settings.getMapSizeFactor();
            height *= Settings.getMapSizeFactor();
        }else{
            width *= Settings.getMapSizeFactorNoObjectives();
            height *= Settings.getMapSizeFactorNoObjectives();
        }


        createMap(random);

        context.setInitialDeploymentBurnDuration(3.5f);
        context.setNormalDeploymentBurnDuration(6f);
        context.setEscapeDeploymentBurnDuration(1.5f);

        if (escape) {
            try {
                addEscapeObjectives(random);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            context.setInitialEscapeRange(
                    Global.getSettings().getFloat("escapeStartDistance")
            ); context.setFlankDeploymentDistance(
                    Global.getSettings().getFloat("escapeFlankDistance")
            ); loader.addPlugin(new EscapeRevealPlugin(context));
        } else {
            if (withObjectives) {
                try {
                    addObjectives(loader, numObjectives, random);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                context.setStandoffRange(height * Settings.getStandoffFactorWithObjectives());
            } else context.setStandoffRange(height * Settings.getStandoffFactorWithoutObjectives());
            context.setFlankDeploymentDistance(height/2f); // matters for Force Concentration
        }
    }

    public void afterDefinitionLoad(final CombatEngineAPI engine) {
        if (coronaIntensity > 0 && (corona != null || pulsar != null)) {
            String name = "Corona";
            if (pulsar != null) name = pulsar.getTerrainName();
            else if (corona != null) name = corona.getTerrainName();

            final String name2 = name;

            final Object key1 = new Object(), key2 = new Object();
            final String icon =
                    Global.getSettings().getSpriteName("ui", "icon_tactical_cr_penalty");
            engine.addPlugin(new BaseEveryFrameCombatPlugin() {
                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    engine.maintainStatusForPlayerShip(key1, icon, name2, "reduced peak time", true);
                    engine.maintainStatusForPlayerShip(key2, icon, name2, "faster CR degradation", true);
                }
            });
        }
    }

    private void createMap(final Random random) {
        loader.initMap(-width /2f, width /2f, -height /2f, height /2f);

        CampaignFleetAPI playerFleet = context.getPlayerFleet();
        String nebulaTex = null, nebulaMapTex = null;
        boolean inNebula = false, privateFromCorona = false;
        for (CustomCampaignEntityAPI entity :
                playerFleet.getContainingLocation().getCustomEntitiesWithTag(
                        Tags.PROTECTS_FROM_CORONA_IN_BATTLE)
        )
            if (Misc.getDistance(entity.getLocation(), playerFleet.getLocation())
                    <= entity.getRadius() + playerFleet.getRadius() + 10f)
            {
                privateFromCorona = true;
                break;
            }

        float numRings = 0;

        Color coronaColor = null;
        for (CampaignTerrainAPI terrain : playerFleet.getContainingLocation().getTerrainCopy()) {
            if (terrain.getPlugin() instanceof NebulaTextureProvider) {
                if (terrain.getPlugin().containsEntity(playerFleet)) {
                    inNebula = true;
                    if (terrain.getPlugin() instanceof NebulaTextureProvider) {
                        NebulaTextureProvider provider = (NebulaTextureProvider) terrain.getPlugin();
                        nebulaTex = provider.getNebulaTex();
                        nebulaMapTex = provider.getNebulaMapTex();
                    }
                } else {
                    if (nebulaTex == null) {
                        if (terrain.getPlugin() instanceof NebulaTextureProvider) {
                            NebulaTextureProvider provider =
                                    (NebulaTextureProvider) terrain.getPlugin();
                            nebulaTex = provider.getNebulaTex();
                            nebulaMapTex = provider.getNebulaMapTex();
                        }
                    }
                }
            } else if (terrain.getPlugin() instanceof StarCoronaTerrainPlugin
                    && pulsar == null
                    && !privateFromCorona)
            {
                StarCoronaTerrainPlugin plugin = (StarCoronaTerrainPlugin) terrain.getPlugin();
                if (plugin.containsEntity(playerFleet)) {
                    float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation()),
                            angle = Misc.getAngleInDegrees(terrain.getLocation(),
                                    playerFleet.getLocation());
                    Color color = plugin.getAuroraColorForAngle(angle);
                    intensity = 0.4f + 0.6f * intensity;
                    int alpha = (int)(80f * intensity);
                    color = Misc.setAlpha(color, alpha);
                    if (coronaColor == null || coronaColor.getAlpha() < alpha) {
                        coronaColor = color;
                        coronaIntensity = intensity;
                        corona = plugin;
                    }
                }
            } else if (terrain.getPlugin() instanceof PulsarBeamTerrainPlugin
                    && !privateFromCorona)
            {
                PulsarBeamTerrainPlugin plugin = (PulsarBeamTerrainPlugin) terrain.getPlugin();
                if (plugin.containsEntity(playerFleet)) {
                    float angle = Misc.getAngleInDegreesStrict(terrain.getLocation(),
                            playerFleet.getLocation());
                    Color color = plugin.getPulsarColorForAngle(angle);
                    float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation());
                    intensity = 0.4f + 0.6f * intensity;
                    int alpha = (int)(80f * intensity);
                    color = Misc.setAlpha(color, alpha);
                    if (coronaColor == null || coronaColor.getAlpha() < alpha) {
                        coronaColor = color;
                        coronaIntensity = intensity;
                        pulsar = plugin;
                        corona = null;
                    }
                }
            } else if (terrain.getType().equals(Terrain.RING)
                    && terrain.getPlugin().containsEntity(playerFleet)) numRings++;
        }
        if (nebulaTex != null) {
            loader.setNebulaTex(nebulaTex);
            loader.setNebulaMapTex(nebulaMapTex);
        }

        if (coronaColor != null) loader.setBackgroundGlowColor(coronaColor);

        int numNebula = 15;
        if (inNebula) numNebula = 100;
        if (!inNebula && playerFleet.isInHyperspace()) numNebula = 0;

        for (int i = 0; i < numNebula; i++) {
            float x = random.nextFloat() * width - width / 2,
                    y = random.nextFloat() * height - height / 2,
                    radius = 100f + random.nextFloat() * 400f;
            if (inNebula) radius += 100f + 500f * random.nextFloat();
            loader.addNebula(x, y, radius);
        }

        float numAsteroidsWithinRange = countNearbyAsteroids(playerFleet);
        int numAsteroids = 0;
        float sMultiplier=1;
        int numRingAsteroids;
        //code to add asteroids removed. The only remaining code is for ring asteroids as there is no API options to add them.
            numAsteroids = Math.min(400, (int) ((numAsteroidsWithinRange + 1f) * 20f));
            numRingAsteroids = (int) Math.min((numRings * 300 + (numRings * 600f) * random.nextFloat()), 1500);
        if(!Settings.isEnableFlyinAsteroids()) {
            loader.addAsteroidField(0, 0, random.nextFloat() * 360f, width,
                    20f * sMultiplier, 70f * sMultiplier, numAsteroids);
        }
        if (numRings > 0) {
            loader.addRingAsteroids(0, 0, random.nextFloat() * 360f, width,
                    100f*sMultiplier, 200f*sMultiplier, numRingAsteroids);
        }

        loader.setBackgroundSpriteName(
                playerFleet.getContainingLocation().getBackgroundTextureFilename()
        );
        loader.setHyperspaceMode(
                playerFleet.getContainingLocation() == Global.getSector().getHyperspace()
        );

        addClosestPlanet();
    }

    private void addClosestPlanet() {
        float bgWidth = 2048f, bgHeight = 2048f;

        CampaignFleetAPI playerFleet = context.getPlayerFleet();
        PlanetAPI planet = getClosestPlanet(playerFleet);
        if (planet == null) return;

        float dist = Vector2f.sub(playerFleet.getLocation(), planet.getLocation(),
                new Vector2f()).length() - planet.getRadius();
        dist = Math.max(dist, 0);
        float baseRadius = planet.getRadius(),
                scaleFactor = 1.5f,
                minRadius = 100f,
                maxRadius = 500f,
                maxDist = Math.max(SINGLE_PLANET_MAX_DIST - planet.getRadius(), 1);

        boolean playerHasStation = false; boolean enemyHasStation = false;
        for (FleetMemberAPI curr : playerFleet.getFleetData().getMembersListCopy())
            if (curr.isStation()) { playerHasStation = true; break; }
        for (FleetMemberAPI curr : context.getOtherFleet().getFleetData().getMembersListCopy())
            if (curr.isStation()) { enemyHasStation = true; break; }

        float planetYOffset = 0;

        if (playerHasStation) planetYOffset = -bgHeight / 2f * 0.5f;
        if (enemyHasStation) planetYOffset = bgHeight / 2f * 0.5f;

        float f = (maxDist - dist) / maxDist * 0.65f + 0.35f,
                radius = baseRadius * f * scaleFactor;
        if (radius > maxRadius) radius = maxRadius;
        if (radius < minRadius) radius = minRadius;
        loader.setPlanetBgSize(bgWidth * f, bgHeight * f);
        loader.addPlanet(0f, planetYOffset, radius, planet, 0f, true);
    }

    private void addObjectives(final MissionDefinitionAPI loader,
                               final int num,
                               final Random random) throws JSONException, IOException {
        if (num <=3){
            objs = new ArrayList<String>(Arrays.asList(SENSOR, NAV));
        }else {
            objs = new ArrayList<String>(Arrays.asList(SENSOR, SENSOR, NAV, NAV));
        }

        Logger log = Logger.getLogger(this.getClass().getName());

        WeightedRandomPicker<String> priorityObjectives = new WeightedRandomPicker<>(random);
        WeightedRandomPicker<String> standardObjectives = new WeightedRandomPicker<>(random);

        if(Global.getSettings().getModManager().isModEnabled("secretsofthefrontier")){
            //get extra objectives and add to objs. For center objective, add at .5, .5
            WeightedRandomPicker<JSONObject> objectivePicker = getSotfObjectivePicker(context.getPlayerFleet(), context.getOtherFleet());

            for (int i = 0; i < num; i++) {
                JSONObject obj = objectivePicker.pick(random);
                if (obj.optBoolean("central_spawn", false)) {
                    try {
                        priorityObjectives.add(obj.getString("obj_id"));
                    } catch (JSONException ex) {
                        log.error(ex);
                    }
                } else {
                    try {
                        standardObjectives.add(obj.getString("obj_id"));
                    } catch (JSONException ex) {
                        log.error(ex);
                    }
                }
            }

        }else{
            priorityObjectives.add(COMM, 1f);
            if(num>3){
                priorityObjectives.add(COMM, 1f);
            }
            standardObjectives.addAll(objs);

        }
        if (num == 2) { // minimum is 3 now, so this shouldn't happen
//            objs = new ArrayList<String>(Arrays.asList(SENSOR, SENSOR, NAV, NAV, COMM));
            addObjectiveAt(0.25f, 0.5f, 0f, 0f, random);
            addObjectiveAt(0.75f, 0.5f, 0f, 0f, random);
        } else if (num == 3) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                addObjectiveAt(0.75f, 0.5f, 1f, 1f, priorityObjectives.pickAndRemove(), random);
                standardObjectives.addAll(priorityObjectives);
                addObjectiveAt(0.25f, 0.7f, 1f, 1f, standardObjectives.pickAndRemove(),random);
                addObjectiveAt(0.25f, 0.3f, 1f, 1f, standardObjectives.pickAndRemove(),random);
            } else if (r < 0.67f) {
                addObjectiveAt(0.25f, 0.5f, 1f, 1f, priorityObjectives.pickAndRemove(), random);
                standardObjectives.addAll(priorityObjectives);
                addObjectiveAt(0.75f, 0.7f, 1f, 1f, standardObjectives.pickAndRemove(),random);
                addObjectiveAt(0.75f, 0.3f, 1f, 1f, standardObjectives.pickAndRemove(),random);
            } else {
                if (random.nextFloat() < 0.5f) {
                    addObjectiveAt(0.5f, 0.5f, 1f, 1f, priorityObjectives.pickAndRemove(), random);
                    standardObjectives.addAll(priorityObjectives);
                    addObjectiveAt(0.22f, 0.7f, 1f, 1f, standardObjectives.pickAndRemove(),random);
                    addObjectiveAt(0.78f, 0.3f, 1f, 1f, standardObjectives.pickAndRemove(),random);
                } else {
                    addObjectiveAt(0.5f, 0.5f, 1f, 1f, priorityObjectives.pickAndRemove(), random);
                    standardObjectives.addAll(priorityObjectives);
                    addObjectiveAt(0.22f, 0.3f, 1f, 1f, standardObjectives.pickAndRemove(),random);
                    addObjectiveAt(0.78f, 0.7f, 1f, 1f, standardObjectives.pickAndRemove(),random);
                }
            }
        } else if (num == 4) {
            float r = random.nextFloat();
            if (r < 0.33f) {
//                String [] maybeRelays = pickCommRelays(2, 2, false, true, true, false, random);
                addObjectiveAt(0.25f, 0.75f, 2f, 1f, priorityObjectives.pickAndRemove(), random);
                standardObjectives.addAll(priorityObjectives);
                addObjectiveAt(0.75f, 0.25f, 2f, 1f, standardObjectives.pickAndRemove(), random);
                addObjectiveAt(0.25f, 0.25f, 2f, 1f, standardObjectives.pickAndRemove(), random);
                addObjectiveAt(0.75f, 0.75f, 2f, 1f, standardObjectives.pickAndRemove(), random);
            } else if (r < 0.67f) {
//                String [] maybeRelays = pickCommRelays(1, 2, true, false, true, false, random);
                addObjectiveAt(0.25f, 0.5f, 1f, 1f, priorityObjectives.pickAndRemove(), random);
                standardObjectives.addAll(priorityObjectives);
                addObjectiveAt(0.5f, 0.75f, 1f, 1f, standardObjectives.pickAndRemove(), random);
                addObjectiveAt(0.75f, 0.5f, 1f, 1f, standardObjectives.pickAndRemove(), random);
                addObjectiveAt(0.5f, 0.25f, 1f, 1f, standardObjectives.pickAndRemove(), random);
            } else {
                if (random.nextFloat() < 0.5f) {
//                    String [] maybeRelays = pickCommRelays(1, 2, true, false, true, false, random);
                    addObjectiveAt(0.6f, 0.4f, 1f, 0f, priorityObjectives.pickAndRemove(), random);
                    standardObjectives.addAll(priorityObjectives);
                    addObjectiveAt(0.25f, 0.25f, 1f, 0f, standardObjectives.pickAndRemove(), random);
                    addObjectiveAt(0.4f, 0.6f, 1f, 0f, standardObjectives.pickAndRemove(), random);
                    addObjectiveAt(0.75f, 0.75f, 1f, 0f, standardObjectives.pickAndRemove(), random);
                } else {
//                    String [] maybeRelays = pickCommRelays(1, 2, false, true, false, true, random);
                    addObjectiveAt(0.6f, 0.6f, 1f, 0f, priorityObjectives.pickAndRemove(), random);
                    standardObjectives.addAll(priorityObjectives);
                    addObjectiveAt(0.25f, 0.75f, 1f, 0f, standardObjectives.pickAndRemove(), random);
                    addObjectiveAt(0.4f, 0.4f, 1f, 0f, standardObjectives.pickAndRemove(), random);
                    addObjectiveAt(0.75f, 0.25f, 1f, 0f, standardObjectives.pickAndRemove(), random);
                }
            }
        }
    }

    private WeightedRandomPicker<JSONObject> getSotfObjectivePicker(CampaignFleetAPI playerFleet, CampaignFleetAPI otherFleet) throws IOException, JSONException {
        Logger log = Logger.getLogger(this.getClass().getName());
        LocationAPI loc = playerFleet.getContainingLocation();
        Random random = Misc.getRandom(Misc.getSalvageSeed(otherFleet) *
                (long)otherFleet.getFleetData().getNumMembers(), 23);
        WeightedRandomPicker<JSONObject> post = new WeightedRandomPicker<JSONObject>(random);
        //post.add("nav_buoy", 1f);
        //post.add("sensor_array", 1f);
        //post.add("comm_relay", 1f);

        //JSONArray objectives = Global.getSettings().getMergedSpreadsheetDataForMod("id", OBJECTIVE_CSV, SotfIDs.SOTF);

        JSONArray objectives = SotfModPlugin.OBJECTIVE_DATA;
        int maxFPForObj = (int) Global.getSettings().getFloat("maxNoObjectiveBattleSize");

        FleetGoal playerGoal = context.getPlayerGoal();
        FleetGoal enemyGoal = context.getOtherGoal();
        boolean escape = playerGoal == FleetGoal.ESCAPE || enemyGoal == FleetGoal.ESCAPE;


        float fpBoth = 0f;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.canBeDeployedForCombat() || playerGoal == FleetGoal.ESCAPE) {
                fpBoth += member.getUnmodifiedDeploymentPointsCost();
            }
        }
        for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy()) {
            if (member.canBeDeployedForCombat() || playerGoal == FleetGoal.ESCAPE) {
                fpBoth += member.getUnmodifiedDeploymentPointsCost();
            }
        }

        //if (objectives == null) {
        //	return post;
        //}
        for(int i = 0; i < objectives.length(); i++) {
            JSONObject row = objectives.getJSONObject(i);
            boolean should_add_objective = true;

            try {
                if (row.getString("hyperspace").equals("true")) {
                    if (!loc.isHyperspace()) {
                        should_add_objective = false;
                    }
                }
                else if (row.getString("hyperspace").equals("false")) {
                    if (loc.isHyperspace()) {
                        should_add_objective = false;
                    }
                }
            } catch (JSONException ex) {
                log.info("no hyperspace setting for objective ID " + row.getString("id"));
            }

            if (!loc.isHyperspace()) {
                StarSystemAPI system = (StarSystemAPI) loc;
                try {
                    if (!row.getString("tag").equals("") && !system.hasTag(row.getString("tag"))) {
                        should_add_objective = false;
                    }
                } catch (JSONException ex) {
                    log.info("no tag set for objective ID " + row.getString("id"));
                }
            } else {
                try {
                    if (!row.getString("tag").equals("")) {
                        should_add_objective = false;
                    }
                } catch (JSONException ex) {
                    log.info("no tag set for objective ID " + row.getString("id"));
                }
            }

            try {
                if (row.getString("escape").equals("true")) {
                    if (!escape) {
                        should_add_objective = false;
                    }
                }
                else if (row.getString("escape").equals("false")) {
                    if (escape) {
                        should_add_objective = false;
                    }
                }
            } catch (JSONException ex) {
                log.info("no escape setting for objective ID " + row.getString("id"));
            }

            try {
                int minFP = row.getInt("minFP");
                if (fpBoth < (maxFPForObj + minFP)) {
                    should_add_objective = false;
                }
            } catch (JSONException ex) {
                log.info("no minFP for objective ID " + row.getString("id"));
            }

            try {
                int maxFP = row.getInt("maxFP");
                if (maxFP == -1) {
                    maxFP = 999999;
                }
                if (fpBoth > (maxFPForObj + maxFP)) {
                    should_add_objective = false;
                }
            } catch (JSONException ex) {
                log.info("no maxFP for objective ID " + row.getString("id"));
            }

            if (should_add_objective) {
                post.add(row, (float) row.getDouble("weight"));
            }
        }
        return post;
    }


    private String [] pickCommRelays(final int min,
                                     final int max,
                                     final boolean comm1,
                                     final boolean comm2,
                                     final boolean comm3,
                                     final boolean comm4,
                                     final Random random)
    {
        String [] result = new String [4];

        WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(random);
        if (comm1) picker.add(0);
        if (comm2) picker.add(1);
        if (comm3) picker.add(2);
        if (comm4) picker.add(3);

        int num = min + random.nextInt(max - min + 1);

        for (int i = 0; i < num && !picker.isEmpty(); i++) result[picker.pickAndRemove()] = COMM;
        return result;
    }

    private void addEscapeObjectives(final Random random) throws JSONException, IOException {
        objs = new ArrayList<>(Arrays.asList(SENSOR, SENSOR, NAV, NAV, COMM));
        Logger log = Logger.getLogger(this.getClass().getName());
        WeightedRandomPicker<String> objectives = new WeightedRandomPicker<>(random);
        if(Global.getSettings().getModManager().isModEnabled("secretsofthefrontier")){
            //get extra objectives and add to objs. For center objective, add at .5, .5
            WeightedRandomPicker<JSONObject> objectivePicker = getSotfObjectivePicker(context.getPlayerFleet(), context.getOtherFleet());

            for (int i = 0; i < 2; i++) {
                JSONObject obj = objectivePicker.pick(random);
                    try {
                        objectives.add(obj.getString("obj_id"));
                    } catch (JSONException ex) {
                        log.error(ex);
                    }
            }

        }else{
            objectives.addAll(objs);
        }




        float r = random.nextFloat();
        if (r < 0.33f) {
            addObjectiveAt(0.25f, 0.25f, 1f, 1f, objectives.pickAndRemove(),random);
            addObjectiveAt(0.75f, 0.75f, 1f, 1f, objectives.pickAndRemove(),random);
        } else if (r < 0.67f) {
            addObjectiveAt(0.75f, 0.25f, 1f, 1f, objectives.pickAndRemove(),random);
            addObjectiveAt(0.25f, 0.75f, 1f, 1f, objectives.pickAndRemove(),random);
        } else {
            addObjectiveAt(0.5f, 0.25f, 4f, 2f, objectives.pickAndRemove(),random);
            addObjectiveAt(0.5f, 0.75f, 4f, 2f, objectives.pickAndRemove(),random);
        }
    }

    private void addObjectiveAt(final float xMult,
                                final float yMult,
                                final float xOff,
                                final float yOff,
                                final Random random) {
        addObjectiveAt(xMult, yMult, xOff, yOff, null, random);
    }

    private void addObjectiveAt(final float xMult,
                                final float yMult,
                                final float xOff,
                                final float yOff,
                                String type,
                                final Random random)
    {
        if (type == null) {
            type = pickAny(random);
            if (objs != null && objs.size() > 0) {
                int index = (int) (random.nextDouble() * objs.size());
                type = objs.remove(index);
            }
        }

        float minX = -width/2 + xPad, minY = -height/2 + yPad,
                x = (width - xPad * 2f) * xMult + minX, y = (height - yPad * 2f) * yMult + minY;
        x = ((int) x / 1000) * 1000f; y = ((int) y / 1000) * 1000f;

        float offsetX = Math.round((random.nextFloat() - 0.5f) * xOff * 1f) * 1000f,
                offsetY = Math.round((random.nextFloat() - 0.5f) * yOff * 1f) * 1000f;

        float xDir = Math.signum(offsetX); float yDir = Math.signum(offsetY);
        if (xDir == prevXDir && xOff > 0) {
            xDir *= -1;
            offsetX = Math.abs(offsetX) * -prevXDir;
        }
        if (yDir == prevYDir && yOff > 0) {
            yDir *= -1;
            offsetY = Math.abs(offsetY) * -prevYDir;
        }

        prevXDir = xDir; prevYDir = yDir;

        x += offsetX; y += offsetY;

        loader.addObjective(x, y, type);

        if (random.nextFloat() > 0.6f) {
            float nebulaSize = random.nextFloat() * 1500f + 500f;
            loader.addNebula(x, y, nebulaSize);
        }
    }

    private String pickAny(final Random random) {
        float r = random.nextFloat();
        if (r < 0.33f) return "nav_buoy";
        else if (r < 0.67f) return "sensor_array";
        return "comm_relay";
    }

    private float countNearbyAsteroids(final CampaignFleetAPI playerFleet) {
        float numAsteroidsWithinRange = 0;
        LocationAPI loc = playerFleet.getContainingLocation();
        if (loc instanceof StarSystemAPI) {
            StarSystemAPI system = (StarSystemAPI) loc;
            List<SectorEntityToken> asteroids = system.getAsteroids();
            for (SectorEntityToken asteroid : asteroids) {
                float range = Vector2f.sub(playerFleet.getLocation(),
                        asteroid.getLocation(),
                        new Vector2f()).length();
                if (range < 300) numAsteroidsWithinRange++;
            }
        } return numAsteroidsWithinRange;
    }

    private PlanetAPI getClosestPlanet(final CampaignFleetAPI playerFleet) {
        LocationAPI loc = playerFleet.getContainingLocation();
        PlanetAPI closest = null;
        float minDist = Float.MAX_VALUE;
        if (loc instanceof StarSystemAPI) {
            StarSystemAPI system = (StarSystemAPI) loc;
            List<PlanetAPI> planets = system.getPlanets();
            Vector2f playerFleetLocation = context.getPlayerFleet().getLocation();
            for (PlanetAPI planet : planets) {
                if (planet.isStar()) continue;
                if (Planets.PLANET_LAVA.equals(planet.getTypeId())) continue;
                if (Planets.PLANET_LAVA_MINOR.equals(planet.getTypeId())) continue;
                if (planet.getSpec().isDoNotShowInCombat()) continue;
                float dist = Vector2f.sub(playerFleetLocation,
                        planet.getLocation(),
                        new Vector2f()).length();
                if (dist < minDist && dist < SINGLE_PLANET_MAX_DIST) {
                    closest = planet;
                    minDist = dist;
                }
            }
        } return closest;
    }
}
