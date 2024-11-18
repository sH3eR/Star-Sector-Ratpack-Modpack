package DE.scripts;

import DE.scripts.listeners.DE_OmegaListenerFleet;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.skills.OfficerTraining;
import com.fs.starfarer.api.loading.Description;
import de.unkrig.commons.nullanalysis.Nullable;
import exerelin.campaign.SectorManager;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import DE.scripts.Gen;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static DE.scripts.Gen.*;



public class DE_ModPlugin extends BaseModPlugin {
    // public static boolean isExerelin = false;
    public static boolean isPAGSM = Global.getSettings().getModManager().isModEnabled("PAGSM");
    public static boolean isShadowyards = Global.getSettings().getModManager().isModEnabled("shadow_ships");
    public static boolean isIndEvo = Global.getSettings().getModManager().isModEnabled("IndEvo");
    public static String TTAmbassador = "TTambassador";
    public static String PLAmbassador = "PLambassador";
    public static String MIDGARDADMIN = "midgard_admin";
    public static String Lucanus = "lucanus";
    public static boolean DEremovenondiktatfeatures = Global.getSettings().getBoolean("DEremovenondiktatfeatures");
    public static boolean DEenablelitemode = Global.getSettings().getBoolean("DEenablelitemode");
    public static boolean DEenablefortressmode = Global.getSettings().getBoolean("DEenablefortressmode");
    public static boolean DEdisablelobers = Global.getSettings().getBoolean("DEdisablelobers");
    public static boolean DEdisabledrakoncheck = Global.getSettings().getBoolean("DEdisabledrakoncheck");
    private static final Logger log = Global.getLogger(DE_ModPlugin.class);
    public void initDELunaLibSettings() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            //lunalib.lunaSettings.
            DEremovenondiktatfeatures = LunaSettings.getBoolean("Diktat Enhancement", "DEremovenondiktatfeatures").booleanValue();
            DEenablefortressmode = LunaSettings.getBoolean("Diktat Enhancement", "DEenablefortressmode").booleanValue();
            DEenablelitemode = LunaSettings.getBoolean("Diktat Enhancement", "DEenablelitemode").booleanValue();
            DEdisablelobers = LunaSettings.getBoolean("Diktat Enhancement", "DEdisablelobers").booleanValue();
            DEdisabledrakoncheck = LunaSettings.getBoolean("Diktat Enhancement", "DEdisabledrakoncheck").booleanValue();
        }
    }
    // Should replace DE's desc with PAGSM's if PAGSM is enabled
    private void replaceDescriptionOfSpecificMod(String modID, String descID, String newDesc) throws JSONException, IOException {
        SettingsAPI settings = Global.getSettings();
        JSONArray csvData;
        // try loading descriptions.csv; not every mod has one
        try {
            csvData = settings.loadCSV("data/strings/descriptions.csv", modID);
            for (int i = 0; i < csvData.length(); i++) {
                try {
                    JSONObject row = csvData.getJSONObject(i);
                    String id = row.getString("id");
                    String type = row.getString("type");
                    if (id.equals("descID")) {
                        Description.Type descType = getType(type);
                        Description desc = settings.getDescription(id, descType);
                        ;
                        desc.setText1(newDesc);
                    }
                } catch (Exception e) {
                    log.info("MOD_NAME has failed while reading descriptions.csv from " + modID);
                }
            }
        } catch (Exception e) {
            log.info("MOD_NAME  has attempted to load a descriptions.csv from " + modID + " but failed.");
        }


    }

    @Nullable
    public Description.Type getType(String type) {
        switch (type) {
            case "SHIP":
                return Description.Type.SHIP;
            case "WEAPON":
                return Description.Type.WEAPON;
            case "ASTEROID":
                return Description.Type.ASTEROID;
            case "SHIP_SYSTEM":
                return Description.Type.SHIP_SYSTEM;
            case "CUSTOM":
                return Description.Type.CUSTOM;
            case "ACTION_TOOLTIP":
                return Description.Type.ACTION_TOOLTIP;
            case "FACTION":
                return Description.Type.FACTION;
            case "PLANET":
                return Description.Type.PLANET;
            case "RESOURCE":
                return Description.Type.RESOURCE;
            case "TERRAIN":
                return Description.Type.TERRAIN;
            default:
                return null;
        }
    }

    private static void initDE() {
        new DE.scripts.Gen().generate(Global.getSector());
    }

    // Incan god of snow - Lonely TT colony
    private static void initTriTachMarketsYma() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Yma");
        PlanetAPI khuno = system.addPlanet("khuno", system.getEntityById("yma"), "Khuno", "frozen", 280, 150, 11000, 700);
        khuno.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
        khuno.applySpecChanges();
        khuno.setInteractionImage("illustrations", "vacuum_colony");
        khuno.setCustomDescriptionId("planet_khuno");
        MarketAPI khuno_market = addMarketplace(
                "tritachyon",
                khuno,
                null,
                "Khuno",
                4,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_4,
                                Conditions.VERY_COLD,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.VOLATILES_ABUNDANT,
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
                                Industries.ORBITALSTATION_HIGH,
                                Industries.MINING,
                                Industries.PATROLHQ
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);
    }

    // Aztec god of snow - Former TT staging point
    private static void initTriTachMarketsAztlan() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Aztlan");
        PlanetAPI qui = system.addPlanet("qui", system.getEntityById("aztlan"), "Itztlacoliuhqui", "frozen", 280, 150, 6000, 400);
        qui.setCustomDescriptionId("planet_qui");
        qui.getMarket().addCondition(Conditions.DECIVILIZED);
        qui.getMarket().addCondition(Conditions.VERY_COLD);
        qui.getMarket().addCondition(Conditions.DARK);
        qui.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        qui.getMarket().addCondition(Conditions.ORE_MODERATE);
        qui.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
        qui.getMarket().addCondition(Conditions.RUINS_WIDESPREAD);
    }

    // Islamic term for snow/drugs - quite fitting really
    private static void initTriTachMarketsZagan() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Zagan");
        PlanetAPI kukayin = system.addPlanet("kukayin", system.getEntityById("zagan"), "Kukayin", "tundra", 280, 150, 7000, 300);
        kukayin.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        kukayin.applySpecChanges();
        kukayin.setInteractionImage("illustrations", "cargo_loading");
        kukayin.setCustomDescriptionId("planet_kukayin");
        MarketAPI kukayin_market = addMarketplace(
                "independent",
                kukayin,
                null,
                "Kukayin",
                6,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_6,
                                Conditions.HABITABLE,
                                Conditions.COLD,
                                Conditions.FARMLAND_POOR
                                //Conditions.OUTPOST,
                                //Conditions.FRONTIER
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.HEAVYBATTERIES,
                                Industries.BATTLESTATION_HIGH,
                                Industries.MINING,
                                Industries.FARMING,
                                Industries.LIGHTINDUSTRY,
                                Industries.MILITARYBASE
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                true,
                //junk and chatter
                true);
    }

    // Irish god of snow - Binary(as best as can be executed ingame)
    private static void initTriTachMarketsHybrasil() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Hybrasil");

        PlanetAPI cailleach = system.addPlanet("cailleach", system.getEntityById("hybrasil"), "Cailleach", "frozen", 280, 150, 10000, 300);
        cailleach.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        cailleach.applySpecChanges();
        cailleach.setInteractionImage("illustrations", "cargo_loading");
        cailleach.setCustomDescriptionId("planet_cailleach");
        MarketAPI cailleach_market = addMarketplace(
                "tritachyon",
                cailleach,
                null,
                "Cailleach",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.WATER_SURFACE,
                                Conditions.VERY_COLD,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.DARK,
                                Conditions.VOLATILES_ABUNDANT,
                                Conditions.ORE_MODERATE,
                                Conditions.RARE_ORE_MODERATE,
                                //Conditions.OUTPOST,
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
                                "commerce",
                                Industries.AQUACULTURE,
                                Industries.PATROLHQ
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);

        PlanetAPI bheur = system.addPlanet("bheur", system.getEntityById("cailleach"), "Bheur", "frozen", 280, 150, 500, 40);
        bheur.setCustomDescriptionId("planet_bheur");
        bheur.getMarket().addCondition(Conditions.VERY_COLD);
        bheur.getMarket().addCondition(Conditions.DARK);
        bheur.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        bheur.getMarket().addCondition(Conditions.VOLATILES_ABUNDANT);
        bheur.getMarket().addCondition(Conditions.ORE_MODERATE);
        bheur.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
    }

    private static void initTriTachMarketsTyle() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Tyle");
        PlanetAPI aisoyim = system.addPlanet("aisoyim", system.getEntityById("tyle"), "Aisoyimstan", "rocky_ice", 280, 150, 7000, 300);
        aisoyim.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "volturn"));
        aisoyim.applySpecChanges();
        aisoyim.setInteractionImage("illustrations", "cargo_loading");
        aisoyim.setCustomDescriptionId("planet_aisoyim");
        MarketAPI kukayin_market = addMarketplace(
                "persean",
                aisoyim,
                null,
                "Aisoyimstan",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.NO_ATMOSPHERE,
                                Conditions.VERY_COLD,
                                Conditions.DARK,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.ORE_MODERATE,
                                Conditions.RARE_ORE_MODERATE,
                                Conditions.OUTPOST
                                //Conditions.FRONTIER
                        )
                ),

                new ArrayList<>(
                        Arrays.asList(
                                Submarkets.SUBMARKET_OPEN,
                                Submarkets.SUBMARKET_STORAGE,
                                Submarkets.GENERIC_MILITARY,
                                Submarkets.SUBMARKET_BLACK
                        )
                ),
                new ArrayList<>(
                        Arrays.asList(
                                Industries.POPULATION,
                                Industries.SPACEPORT,
                                Industries.GROUNDDEFENSES,
                                Industries.ORBITALSTATION_HIGH,
                                Industries.MINING,
                                "commerce",
                                Industries.MILITARYBASE
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);
    }

    private static void initDiktatRelationChanges() {
        FactionAPI diktat = Global.getSector().getFaction("sindrian_diktat");
        diktat.setRelationship(Factions.HEGEMONY, -0.25f);
        diktat.setRelationship(Factions.LUDDIC_CHURCH, -0.1f);
        diktat.setRelationship(Factions.INDEPENDENT, -0.15f);
        diktat.setRelationship(Factions.TRITACHYON, 0.1f);
    }

    /*private static void makeLucanus() {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        // Retired/Fugitive Admiral - deprecated due to 0.96a's lore changes
        PersonAPI admiral = Global.getFactory().createPerson();
        admiral.setId(Lucanus);
        admiral.setGender(FullName.Gender.MALE);
        admiral.setFaction(Factions.PIRATES);
        admiral.setPostId("lucanuspost");
        admiral.setRankId("lucanusrank");
        admiral.setPersonality(Personalities.STEADY);
        admiral.getName().setFirst("Lucanus");
        admiral.getName().setLast("Proximus");
        admiral.setPortraitSprite(Global.getSettings().getSpriteName("characters", admiral.getId()));
        admiral.setImportance(PersonImportance.VERY_HIGH);
        admiral.setVoice(Voices.SOLDIER);
        admiral.addTag(Tags.CONTACT_UNDERWORLD);
        admiral.addTag(Tags.CONTACT_MILITARY);
        // Old Pride of The Executor commander has some good stats
        admiral.getStats().setLevel(9);
        // fleet commander stuff
        admiral.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
        admiral.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
        admiral.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
        admiral.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
        admiral.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
        admiral.getStats().setSkillLevel(Skills.OFFICER_MANAGEMENT, 1);
        // officer stuff
        admiral.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
        admiral.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        admiral.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        admiral.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        admiral.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        admiral.getStats().setSkillLevel(Skills.HELMSMANSHIP, 3);
        admiral.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 3);
        admiral.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 3);
        admiral.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 3);
        ip.addPerson(admiral);
    }*/

    @Override
    public void onNewGame() {
        initDELunaLibSettings();
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
            initDE();
            initDiktatRelationChanges();
            //makeLucanus();
            if (!DEremovenondiktatfeatures || !DEenablelitemode) {
                initTriTachMarketsYma();
                initTriTachMarketsAztlan();
                initTriTachMarketsZagan();
                initTriTachMarketsHybrasil();
                initTriTachMarketsTyle();
            }

            // Add conquests back to Diktat by giving them the sindrian_diktat tag
            Global.getSettings().getHullSpec("conquest").addTag("sindrian_diktat");

            // Diktat fighter doctrine changes - mostly for lobers
            if (!isPAGSM) {
                Global.getSector().getFaction("sindrian_diktat").addKnownFighter("de_nephrops_wing", false);
                Global.getSector().getFaction("sindrian_diktat").addPriorityFighter("de_nephrops_wing");
                Global.getSector().getFaction("sindrian_diktat").removeKnownFighter("de_metanephrops_wing");
                for (String s : Arrays.asList("no_drop", "no_sell", "no_bp_drop", "no_dealer", "restricted", "no_bp")) {
                    Global.getSettings().getFighterWingSpec("de_metanephrops_wing").addTag(s);
                }
            } else {
                Global.getSector().getFaction("sindrian_diktat").addKnownFighter("de_metanephrops_wing", false);
                Global.getSector().getFaction("sindrian_diktat").addPriorityFighter("de_metanephrops_wing");
                Global.getSector().getFaction("sindrian_diktat").removeKnownFighter("de_nephrops_wing");
                for (String s : Arrays.asList("no_drop", "no_sell", "no_bp_drop", "no_dealer", "restricted", "no_bp")) {
                    Global.getSettings().getFighterWingSpec("de_nephrops_wing").addTag(s);
                }
            }
            // removing lobers :(
            if (DEdisablelobers) {
                for (String s : Arrays.asList("no_drop", "no_sell", "no_bp_drop", "no_bp", "no_dealer", "restricted")) {
                    Global.getSettings().getFighterWingSpec("de_nephrops_wing").addTag(s);
                    Global.getSettings().getFighterWingSpec("de_metanephrops_wing").addTag(s);
                    Global.getSettings().getFighterWingSpec("de_homarus_wing").addTag(s);
                    Global.getSettings().getFighterWingSpec("de_langusta_wing").addTag(s);
                }
                for (String s : Arrays.asList("de_nephrops_wing", "de_metanephrops_wing", "de_homarus_wing")) {
                    Global.getSector().getFaction("sindrian_diktat").removeKnownFighter(s);
                }
                Global.getSector().getFaction("lions_guard").removeKnownFighter("de_homarus_wing");
                Global.getSector().getFaction("pirates").removePriorityFighter("de_langusta_wing");
                Global.getSector().getFaction("pirates").removeKnownFighter("de_langusta_wing");
            }
        }
    }

    // Some conditions(eg. Large Refugee Population) are not added as the game already adds them as hidden conditions that affect markets without explicitly showing themselves on the intel screen
    private static void initAskonia() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Askonia");
        SectorEntityToken a1 = system.getEntityById("sindria");
        if (!isPAGSM) {
            a1.getMarket().addCondition("DE_Lobsters");
            if (!DEenablelitemode) {
                a1.getMarket().addCondition("DE_Orbitalworks");
                a1.getMarket().addCondition("DE_Patrioticfervor");
            }
        } else {
            a1.getMarket().addCondition("DE_Lobsters_PAGSM");
            if (!DEenablelitemode) {
                a1.getMarket().addCondition("DE_Orbitalworks_PAGSM");
                a1.getMarket().addCondition("DE_Patrioticfervor_PAGSM");
            }
        }
        a1.getMarket().addIndustry(Industries.STARFORTRESS);
        a1.getMarket().removeIndustry(Industries.BATTLESTATION, MarketAPI.MarketInteractionMode.LOCAL, true);
        // a1.getMarket().addIndustry(Industries.PLANETARYSHIELD); // Add if you want to suffer
        a1.getMarket().getIndustry(Industries.HIGHCOMMAND).setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE, null));
        a1.getMarket().addSubmarket("de_LGmarket");
        if (!DEenablelitemode) {
            a1.getMarket().getIndustry(Industries.FUELPROD).setImproved(true); // Lion powers - charisma isn't anything to scoff at
        }

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        // Admiral
        PersonAPI admiral = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.MALE);
        admiral.setId("de_admiral");
        admiral.setPostId(Ranks.POST_FLEET_COMMANDER);
        admiral.setRankId("vicestarmarshal");
        admiral.getName().setFirst("Lucanus");
        admiral.getName().setLast("Proximus");
        admiral.setPersonality(Personalities.AGGRESSIVE);
        admiral.getStats().setLevel(9);
        // fleet commander stuff
        admiral.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
        admiral.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
        admiral.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
        admiral.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
        admiral.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
        admiral.getStats().setSkillLevel(Skills.OFFICER_MANAGEMENT, 1);
        // officer stuff
        admiral.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
        admiral.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        admiral.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        admiral.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        admiral.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        admiral.getStats().setSkillLevel(Skills.HELMSMANSHIP, 3);
        admiral.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 3);
        admiral.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 3);
        admiral.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 3);
        ip.addPerson(admiral);

        // I have to make every single one of these bozos manually
        PersonAPI person1 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        person1.setId("de_generic_officer1");
        person1.setPostId(Ranks.POST_OFFICER);
        person1.setRankId(Ranks.POST_OFFICER);
        person1.setPersonality(Personalities.AGGRESSIVE);
        person1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        person1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        person1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        person1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        person1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person1.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person1.getStats().setLevel(7);

        PersonAPI person2 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        person2.setId("de_generic_officer2");
        person2.setPostId(Ranks.POST_OFFICER);
        person2.setRankId(Ranks.POST_OFFICER);
        person2.setPersonality(Personalities.AGGRESSIVE);
        person2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person2.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        person2.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        person2.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        person2.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        person2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person2.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person2.getStats().setLevel(7);

        PersonAPI person3 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        person3.setId("de_generic_officer3");
        person3.setPostId(Ranks.POST_OFFICER);
        person3.setRankId(Ranks.POST_OFFICER);
        person3.setPersonality(Personalities.AGGRESSIVE);
        person3.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person3.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        person3.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        person3.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        person3.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        person3.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person3.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person3.getStats().setLevel(7);

        PersonAPI person4 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        person4.setId("de_generic_officer4");
        person4.setPostId(Ranks.POST_OFFICER);
        person4.setRankId(Ranks.POST_OFFICER);
        person4.setPersonality(Personalities.AGGRESSIVE);
        person4.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person4.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        person4.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        person4.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        person4.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        person4.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person4.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person4.getStats().setLevel(7);

        PersonAPI person5 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        person5.setId("de_generic_officer5");
        person5.setPostId(Ranks.POST_OFFICER);
        person5.setRankId(Ranks.POST_OFFICER);
        person5.setPersonality(Personalities.AGGRESSIVE);
        person5.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person5.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        person5.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        person5.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        person5.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        person5.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person5.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person5.getStats().setLevel(7);

        PersonAPI person6 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        person6.setId("de_generic_officer1");
        person6.setPostId(Ranks.POST_OFFICER);
        person6.setRankId(Ranks.POST_OFFICER);
        person6.setPersonality(Personalities.AGGRESSIVE);
        person6.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person6.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        person6.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        person6.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        person6.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
        person6.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person6.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person6.getStats().setLevel(7);

        PersonAPI reckless1 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        reckless1.setId("de_reckless1");
        reckless1.setPostId(Ranks.POST_OFFICER);
        reckless1.setRankId(Ranks.POST_OFFICER);
        reckless1.setPersonality(Personalities.RECKLESS);
        reckless1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
        reckless1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        reckless1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        reckless1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        reckless1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        reckless1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        reckless1.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        reckless1.getStats().setLevel(7);

        PersonAPI reckless2 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        reckless2.setId("de_reckless2");
        reckless2.setPostId(Ranks.POST_OFFICER);
        reckless2.setRankId(Ranks.POST_OFFICER);
        reckless2.setPersonality(Personalities.RECKLESS);
        reckless2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
        reckless2.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        reckless2.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        reckless2.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        reckless2.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        reckless2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        reckless2.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        reckless2.getStats().setLevel(7);

        PersonAPI reckless3 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
        reckless3.setId("de_reckless3");
        reckless3.setPostId(Ranks.POST_OFFICER);
        reckless3.setRankId(Ranks.POST_OFFICER);
        reckless3.setPersonality(Personalities.RECKLESS);
        reckless3.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
        reckless3.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
        reckless3.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
        reckless3.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
        reckless3.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        reckless3.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        reckless3.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        reckless3.getStats().setLevel(7);

        // A script to protect officers from crewcapture which im not using for now
        /*
        @Override
        public void reportShownInteractionDialog (InteractionDialogAPI dialog) {
            if (dialog.getPlugin() instanceof CaptiveInteractionDialogPlugin) {
                for (EveryFrameScript script : Global.getSector().getScripts()) {
                    if (script instanceof LootAddScript) {
                        List<PersonAPI> LootAddScript= ((LootAddScript) script).captiveOfficers;
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_admiral"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_generic_officer1"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_generic_officer2"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_generic_officer3"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_generic_officer4"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_generic_officer5"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_reckless1"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_reckless2"));
                        LootAddScript.remove(Global.getSector().getImportantPeople().getPerson("de_reckless3"));
                    }
                }
            }
        } */

        // Should add superfleet 1 to Sindria(The Pride of The Executor)(or The Pride of The Manager for PAGSM)
        if (!isPAGSM) {
            FleetParamsV3 params = new FleetParamsV3(
                    a1.getMarket(), // add a source(has to be from a MarketAPI)
                    null, // loc in hyper; don't need if have market
                    "sindrian_diktat",
                    2f, // quality override route.getQualityOverride()
                    FleetTypes.PATROL_LARGE,
                    1f, // combatPts(minimal so special ships can be added)(1000f otherwise)
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f// qualityMod
            );
            params.officerNumberMult = 2f;
            params.officerLevelBonus = 4;
            params.officerNumberBonus = 4;
            params.officerLevelLimit = 10; // Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.averageSMods = 1;
            params.commander = Global.getSector().getImportantPeople().getPerson("de_admiral");
            params.flagshipVariantId = "executor_Elite";
            CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
            if (fleet == null || fleet.isEmpty()) return;
            fleet.setFaction("sindrian_diktat", true);
            fleet.getFlagship().setShipName("SDS Hand of Andrada");
            //fleet.setCommander(Global.getSector().getImportantPeople().getPerson("de_admiral"));
            //fleet.getFlagship().setOwner(1);
            fleet.getFlagship().setId("executor_Elite");
            fleet.getFleetData().addFleetMember("odyssey_LClaw").setCaptain(person1);
            fleet.getFleetData().addFleetMember("odyssey_LClaw").setCaptain(person2);
            fleet.getFleetData().addFleetMember("executor_Elite");
            fleet.getFleetData().addFleetMember("odyssey_LClaw");
            fleet.getFleetData().addFleetMember("conquest_LClaw").setCaptain(person3);
            fleet.getFleetData().addFleetMember("conquest_LClaw").setCaptain(person4);
            fleet.getFleetData().addFleetMember("executor_Elite").setCaptain(person5);
            fleet.getFleetData().addFleetMember("executor_Elite");
            fleet.getFleetData().addFleetMember("executor_Elite");
            fleet.getFleetData().addFleetMember("eagle_LG_Support");
            fleet.getFleetData().addFleetMember("eagle_LG_Support");
            fleet.getFleetData().addFleetMember("eagle_LG_Support");
            fleet.getFleetData().addFleetMember("eagle_LG_Support");
            fleet.getFleetData().addFleetMember("eagle_LG_Support");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("gryphon_LPatience").setCaptain(person6);
            fleet.getFleetData().addFleetMember("heron_LPatience");
            fleet.getFleetData().addFleetMember("heron_LPatience");
            fleet.getFleetData().addFleetMember("heron_LPatience");
            fleet.getFleetData().addFleetMember("heron_LPatience");
            fleet.getFleetData().addFleetMember("heron_LPatience");
            fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless1);
            fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless2);
            fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless3);
            fleet.getFleetData().addFleetMember("fury_LFury");
            fleet.getFleetData().addFleetMember("fury_LFury");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("sunder_LG_Assault");
            fleet.getFleetData().addFleetMember("sunder_LG_Assault");
            fleet.getFleetData().addFleetMember("sunder_LG_Assault");
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
            fleet.setName("The Pride of The Executor");
            // a1.getMarket().getContainingLocation().addEntity(fleet);
            a1.getContainingLocation().addEntity(fleet);
            fleet.setAI(Global.getFactory().createFleetAI(fleet));
            //fleet.setMarket(a1.getMarket());
            fleet.setLocation(a1.getLocation().x, a1.getLocation().y);
            fleet.setFacing((float) Math.random() * 360f);
            fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, a1, (float) Math.random() * 90000f, null);
        } else {
            // The Pride of The Manager
            FleetParamsV3 params = new FleetParamsV3(
                    a1.getMarket(), // add a source(has to be from a MarketAPI)
                    null, // loc in hyper; don't need if have market
                    "sindrian_diktat",
                    2f, // quality override route.getQualityOverride()
                    FleetTypes.PATROL_LARGE,
                    1f, // combatPts(minimal so special ships can be added)(1000f otherwise)
                    0f, // freighterPts
                    0f, // tankerPts
                    0f, // transportPts
                    0f, // linerPts
                    0f, // utilityPts
                    0f// qualityMod
            );
            params.officerNumberMult = 2f;
            params.officerLevelBonus = 4;
            params.officerNumberBonus = 4;
            params.officerLevelLimit = 10; // Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
            params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
            params.averageSMods = 2;
            params.commander = Global.getSector().getImportantPeople().getPerson("de_admiral");
            params.flagshipVariantId = "sfcsuperiapetus_Mixed";
            CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
            if (fleet == null || fleet.isEmpty()) return;
            fleet.setFaction("sindrian_diktat", true);
            fleet.getFlagship().setShipName("SFS Hand of Andrada");
            //fleet.setCommander(Global.getSector().getImportantPeople().getPerson("de_admiral"));
            //fleet.getFlagship().setOwner(1);
            fleet.getFlagship().setId("sfcsuperiapetus_Mixed"); // whoops didnt realize this was a one-off...
            fleet.getFleetData().addFleetMember("sfcskyrend_Beamer").setCaptain(person1);
            fleet.getFleetData().addFleetMember("sfcskyrend_Beamer").setCaptain(person2);
            fleet.getFleetData().addFleetMember("sfcskyrend_Beamer");
            fleet.getFleetData().addFleetMember("sfcepimetheus_Pulser");
            fleet.getFleetData().addFleetMember("sfciapetus_Mixed").setCaptain(person3);
            fleet.getFleetData().addFleetMember("sfciapetus_Mixed").setCaptain(person4);
            fleet.getFleetData().addFleetMember("sfciapetus_Mixed").setCaptain(person5);
            fleet.getFleetData().addFleetMember("sfciapetus_Mixed");
            fleet.getFleetData().addFleetMember("sfciapetus_Mixed");
            fleet.getFleetData().addFleetMember("sfccrius_Pressure");
            fleet.getFleetData().addFleetMember("sfccrius_Pressure");
            fleet.getFleetData().addFleetMember("sfccrius_Pressure");
            fleet.getFleetData().addFleetMember("eagle_LG_Support");
            fleet.getFleetData().addFleetMember("eagle_LG_Support");
            fleet.getFleetData().addFleetMember("sfccrius_Pressure");
            fleet.getFleetData().addFleetMember("sfccrius_Pressure");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("champion_LMane");
            fleet.getFleetData().addFleetMember("gryphon_LPatience").setCaptain(person6);
            fleet.getFleetData().addFleetMember("sfcclepsydra_Standard");
            fleet.getFleetData().addFleetMember("sfcclepsydra_Standard");
            fleet.getFleetData().addFleetMember("sfcarke_Suppression");
            fleet.getFleetData().addFleetMember("sfcarke_Suppression");
            fleet.getFleetData().addFleetMember("sfcarke_Suppression");
            fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless1);
            fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless2);
            fleet.getFleetData().addFleetMember("fury_LFury").setCaptain(reckless3);
            fleet.getFleetData().addFleetMember("fury_LFury");
            fleet.getFleetData().addFleetMember("fury_LFury");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("hammerhead_LG_Elite");
            fleet.getFleetData().addFleetMember("sunder_LG_Assault");
            fleet.getFleetData().addFleetMember("sunder_LG_Assault");
            fleet.getFleetData().addFleetMember("sunder_LG_Assault");
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
            fleet.setName("The Pride of The Manager");
            // a1.getMarket().getContainingLocation().addEntity(fleet);
            a1.getContainingLocation().addEntity(fleet);
            fleet.setAI(Global.getFactory().createFleetAI(fleet));
            //fleet.setMarket(a1.getMarket());
            fleet.setLocation(a1.getLocation().x, a1.getLocation().y);
            fleet.setFacing((float) Math.random() * 360f);
            fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, a1, (float) Math.random() * 90000f, null);
        }

        MarketAPI market = null;
        SectorEntityToken a2 = system.getEntityById("volturn");
        market = a2.getMarket();
        a2.getMarket().addCondition(Conditions.DISSIDENT);
        a2.getMarket().addCondition(Conditions.VICE_DEMAND);
        a2.getMarket().removeIndustry(Industries.GROUNDDEFENSES, MarketAPI.MarketInteractionMode.LOCAL, true);
        a2.getMarket().addIndustry(Industries.HEAVYBATTERIES);
        if (!DEenablelitemode) {
            a2.getMarket().getIndustry(Industries.LIGHTINDUSTRY).setSpecialItem(new SpecialItemData(Items.BIOFACTORY_EMBRYO, null));
        }
        if (!DEenablelitemode) {
            if (!isPAGSM) {
                a2.getMarket().addCondition("DE_Megafauna");
            } else {
                a2.getMarket().addCondition("DE_Megafauna_PAGSM");
            }
        }

        SectorEntityToken a3 = system.getEntityById("cruor");
        if (!DEenablelitemode) {
            a3.getMarket().getIndustry(Industries.MINING).setSpecialItem(new SpecialItemData(Items.MANTLE_BORE, null));
        }

        SectorEntityToken a4 = system.getEntityById("opis_mining_plat");
        if (!DEenablelitemode) {
            if (!isPAGSM) {
                a4.getMarket().addCondition("DE_Patrioticfervor");
            } else {
                a4.getMarket().addCondition("DE_Patrioticfervor_PAGSM");
            }
        }

        // Andrada contact info so he actually gives missions
        PersonAPI andrada = Global.getSector().getImportantPeople().getPerson("andrada");
        andrada.addTag(Tags.CONTACT_MILITARY);
        andrada.addTag(Tags.CONTACT_TRADE);
    }

    // Hi im Tri-Tach and I abuse the hell out of colony items
    private static void initHybrasilTTMarketChanges() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Hybrasil");
        SectorEntityToken h1 = system.getEntityById("culann");
        h1.getMarket().getIndustry(Industries.MILITARYBASE).setSpecialItem(new SpecialItemData(Items.CRYOARITHMETIC_ENGINE, null));
        h1.getMarket().getIndustry(Industries.REFINING).setSpecialItem(new SpecialItemData(Items.CATALYTIC_CORE, null));

        SectorEntityToken h2 = system.getEntityById("eochu_bres");
        h2.getMarket().addIndustry(Industries.HEAVYBATTERIES);
        h2.getMarket().getIndustry(Industries.LIGHTINDUSTRY).setSpecialItem(new SpecialItemData(Items.BIOFACTORY_EMBRYO, null));
        h2.getMarket().getIndustry(Industries.FARMING).setSpecialItem(new SpecialItemData(Items.SOIL_NANITES, null));
    }

    private static void initAndorMarketChanges() {
        if (!DEenablelitemode || !DEenablefortressmode) {
            SectorAPI sector = Global.getSector();
            StarSystemAPI system = sector.getStarSystem("Andor");
            SectorEntityToken a1 = system.getEntityById("ryzan_supercomplex");
            if (!isPAGSM) {
                a1.getMarket().addCondition("DE_Patrioticfervor");
            } else {
                a1.getMarket().addCondition("DE_Patrioticfervor_PAGSM");
            }

            SectorEntityToken a2 = system.getEntityById("andor_viewport");
            if (!isPAGSM) {
                a2.getMarket().addCondition("DE_Patrioticfervor");
            } else {
                a2.getMarket().addCondition("DE_Patrioticfervor_PAGSM");
            }
        }
    }

    // oopsie.
    /*private static void addBifrost() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Valhalla");
        SectorEntityToken midgard = system.getEntityById("midgard");
        // Midgard superfleet
        FleetParamsV3 params = new FleetParamsV3(
                midgard.getMarket(), // add a source(has to be from a MarketAPI)
                null, // loc in hyper; don't need if have market
                "independent",
                2f, // quality override route.getQualityOverride()
                FleetTypes.TASK_FORCE,
                600f, // combatPts(minimal so special ships can be added)
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f// qualityMod
        );
        params.officerNumberMult = 2f;
        params.officerLevelBonus = 4;
        params.officerNumberBonus = 4;
        params.officerLevelLimit = Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
        params.modeOverride = FactionAPI.ShipPickMode.ALL;
        params.averageSMods = 1;
        params.flagshipVariantId = "paragon_Raider";
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) return;
        fleet.setFaction("independent", true);
        fleet.getFlagship().setId("paragon_Raider");
        fleet.setNoFactionInName(true);
        fleet.setName("Bifrost Armada");
        midgard.getContainingLocation().addEntity(fleet);
        fleet.setAI(Global.getFactory().createFleetAI(fleet));
        fleet.setMarket(midgard.getMarket());
        fleet.setLocation(midgard.getLocation().x, midgard.getLocation().y);
        fleet.setFacing((float) Math.random() * 360f);
        fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, midgard, (float) Math.random() * 90000f, null);
    }*/

    private static void initFortressModeChanges() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Askonia");
        // Lion's Teeth - IndEvo mine belt - unused for now due to issues with faction ownership
        /*if (Global.getSettings().getModManager().isModEnabled("IndEvo")) {
            SectorEntityToken jumpPoint2 = system.getEntityById("salus_jump");
            CampaignTerrainAPI mineBelt = ((BaseLocation) jumpPoint2.getContainingLocation()).addTerrain("IndEvo_mine_belt", new com.fs.starfarer.api.impl.campaign.terrain.IndEvo_MineBeltTerrainPlugin.MineBeltParams(
                    (int) Math.round(300 / 3f), //this specifies the mine amount
                    300,
                    200,
                    300,
                    300,
                    2f,
                    8f,
                    "The Lion's Teeth"));
            mineBelt.setCircularOrbitPointingDown(jumpPoint2, 0, 0, 300);
            system.addRingBand(jumpPoint2, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 300f, 300f);
            if (!isPAGSM) {
                mineBelt.setMarket(system.getEntityById("phrygian_fortress").getMarket());
                //mineBelt.getOrbitFocus().setId("phrygian_fortress");
            } else {
                mineBelt.setMarket(system.getEntityById("phrygian_fortress_PAGSM").getMarket());
                //mineBelt.getOrbitFocus().setId("phrygian_fortress_PAGSM");
                //system.getEntityById("phrygian_fortress_PAGSM").getMarket().addCondition("IndEvo_mineFieldCondition");
            }

            SectorEntityToken jumpPoint3 = system.getEntityById("askonia_jump_point_alpha");
            CampaignTerrainAPI mineBelt2 = ((BaseLocation) jumpPoint3.getContainingLocation()).addTerrain("IndEvo_mine_belt", new com.fs.starfarer.api.impl.campaign.terrain.IndEvo_MineBeltTerrainPlugin.MineBeltParams(
                    (int) Math.round(300 / 3f), //this specifies the mine amount
                    300,
                    200,
                    300,
                    300,
                    2f,
                    8f,
                    "The Lion's Teeth"));
            mineBelt2.setCircularOrbitPointingDown(jumpPoint3, 0, 0, 300);
            system.addRingBand(jumpPoint3, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 300f, 300f);
            if (!isPAGSM) {
                mineBelt.setMarket(system.getEntityById("phrygian_fortress").getMarket());
                //mineBelt.getOrbitFocus().setId("phrygian_fortress");
            } else {
                mineBelt.setMarket(system.getEntityById("phrygian_fortress_PAGSM").getMarket());
                //mineBelt.getOrbitFocus().setId("phrygian_fortress_PAGSM");
                //system.getEntityById("phrygian_fortress_PAGSM").getMarket().addCondition("IndEvo_mineFieldCondition");
            }
        }*/

        // Replacing Drakon with Ravaryea analogue
        LocationAPI hyper = Global.getSector().getHyperspace();
        String amazing = "mamamia"; // 11000, -7500(Naraka), -4400,-4500(Askonia) +- 200
        for (SectorEntityToken e : hyper.getAllEntities()) {
            if (e.getLocation().x > -4200 && e.getLocation().x < -4600 && e.getLocation().y > -4300 && e.getLocation().y < -4700) {
                amazing = e.getId();
            }
        }
        Global.getSector().getHyperspace().removeEntity(Global.getSector().getEntityById(amazing));
        system.removeEntity(Global.getSector().getEntityById("drakon"));
        PlanetAPI drakon = system.addPlanet("drakon_fortress", system.getStar(), "Drakon", "terran-eccentric", 280, 150, 12000, 1000); //formerly 450 and 650
        if (!isPAGSM) {
            drakon.setCustomDescriptionId("planet_drakon_fortress");
        } else {
            drakon.setCustomDescriptionId("planet_drakon_fortress_PAGSM");
        }
        MarketAPI drakon_fortress_market = addMarketplace(
                "sindrian_diktat",
                drakon,
                null,
                "Drakon",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.HABITABLE,
                                Conditions.VERY_COLD,
                                Conditions.DARK,
                                Conditions.FARMLAND_ADEQUATE,
                                Conditions.ORGANICS_PLENTIFUL,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.OUTPOST
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
                                Industries.PATROLHQ,
                                Industries.MINING,
                                Industries.FARMING,
                                "commerce",
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
        drakon.setMarket(drakon_fortress_market);
        drakon.getMarket().getCondition(Conditions.FARMLAND_ADEQUATE).setSurveyed(true);
        drakon.getMarket().getCondition(Conditions.VERY_COLD).setSurveyed(true);
        drakon.getMarket().getCondition(Conditions.DARK).setSurveyed(true);
        drakon.getMarket().getCondition(Conditions.POPULATION_5).setSurveyed(true);
        drakon.getMarket().getCondition(Conditions.ORGANICS_PLENTIFUL).setSurveyed(true);
        drakon.getMarket().getCondition(Conditions.VOLATILES_DIFFUSE).setSurveyed(true);
        drakon.getMarket().getCondition(Conditions.OUTPOST).setSurveyed(true);
        drakon.getMarket().addCondition("DE_Unstablelamp");
        drakon.getMarket().getCondition("DE_Unstablelamp").setSurveyed(true);
        drakon.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        //drakon.getMarket().addIndustry(Industries.ORBITALSTATION);
        //drakon.getMarket().addIndustry(Industries.PATROLHQ);
        //drakon.getMarket().addIndustry("commerce");
        drakon.getMarket().getIndustry(Industries.POPULATION).setSpecialItem(new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null));
        NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(drakon, 50f);
        well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(drakon, 470f);
        hyper.addEntity(well);

        // Volturn stuff
        SectorEntityToken a2 = system.getEntityById("volturn");
        if (!isPAGSM) {
            a2.getMarket().addCondition("DE_Lobsters");
        } else {
            a2.getMarket().addCondition("DE_Lobsters_PAGSM");
        }
        a2.getMarket().removeIndustry(Industries.ORBITALSTATION_MID, MarketAPI.MarketInteractionMode.LOCAL, true);
        a2.getMarket().addIndustry(Industries.BATTLESTATION_MID);
        a2.getMarket().addIndustry(Industries.PATROLHQ);
        a2.getMarket().getIndustry(Industries.HEAVYBATTERIES).setSpecialItem(new SpecialItemData(Items.DRONE_REPLICATOR, null));

        // Sanguis Monitor Platform - Cruor stuff
        SectorEntityToken a3 = system.getEntityById("cruor");
        a3.getMarket().addIndustry(Industries.ORBITALSTATION);
        SectorEntityToken sanguis = system.addCustomEntity("de_sanguis", Global.getSettings().getString("diktat_enhancement", "de_sanguis"), "station_mining00", "sindrian_diktat");
        sanguis.setInteractionImage("illustrations", "orbital");
        sanguis.setCircularOrbitPointingDown(a3, 45 + 180, 80 + 60, 30);
        sanguis.setMarket(a3.getMarket());
        //sanguis.getMarket().setPrimaryEntity(system.getEntityById("cruor"));
        a3.getMarket().getConnectedEntities().add(sanguis);

        // Umbra gets annexed during 2nd AI war - deprecated due to Usurpers interference and idgaf rewrite Usurpers for this
        /*String amazing2 = "mamamia"; // 11000, -7500(Naraka), -4400,-4500(Askonia) +- 200
        for (SectorEntityToken e : hyper.getAllEntities()) {
            if (e.getLocation().x > -4200 && e.getLocation().x < -4600 && e.getLocation().y > -4300 && e.getLocation().y < -4700) {
                amazing2 = e.getId();
            }
        }
        Global.getSector().getHyperspace().removeEntity(Global.getSector().getEntityById(amazing2));
        Global.getSector().getEconomy().removeMarket(Global.getSector().getEntityById("umbra").getMarket());
        system.removeEntity(Global.getSector().getEntityById("umbra"));
        PlanetAPI umbra = system.addPlanet("umbra_fortress", system.getStar(), "Umbra", "rocky_ice", 280, 150, 11000, 600); //formerly 450 and 650
        if (!isPAGSM) {
            umbra.setCustomDescriptionId("planet_umbra_fortress");
        } else {
            umbra.setCustomDescriptionId("planet_umbra_fortress_PAGSM");
        }
        MarketAPI umbra_fortress_market = addMarketplace(
                "sindrian_diktat",
                umbra,
                null,
                "Umbra",
                5,

                new ArrayList<>(
                        Arrays.asList(
                                Conditions.POPULATION_5,
                                Conditions.THIN_ATMOSPHERE,
                                Conditions.COLD,
                                Conditions.POOR_LIGHT,
                                Conditions.ORE_SPARSE,
                                Conditions.VOLATILES_DIFFUSE,
                                Conditions.OUTPOST
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
                                Industries.MILITARYBASE,
                                Industries.MINING,
                                Industries.HEAVYBATTERIES,
                                Industries.FUELPROD,
                                Industries.ORBITALSTATION
                        )
                ),
                //tariffs
                0.3f,
                //freeport
                false,
                //junk and chatter
                true);
        umbra.setMarket(umbra_fortress_market);
        umbra.getMarket().getCondition(Conditions.THIN_ATMOSPHERE).setSurveyed(true);
        umbra.getMarket().getCondition(Conditions.COLD).setSurveyed(true);
        umbra.getMarket().getCondition(Conditions.POOR_LIGHT).setSurveyed(true);
        umbra.getMarket().getCondition(Conditions.POPULATION_5).setSurveyed(true);
        umbra.getMarket().getCondition(Conditions.ORE_SPARSE).setSurveyed(true);
        umbra.getMarket().getCondition(Conditions.VOLATILES_DIFFUSE).setSurveyed(true);
        umbra.getMarket().getCondition(Conditions.OUTPOST).setSurveyed(true);
        umbra.getMarket().setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        umbra.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
        umbra.getSpec().setGlowColor(new Color(255, 255, 255, 255));
        umbra.getSpec().setUseReverseLightForGlow(true);
        umbra.applySpecChanges();
        umbra.setInteractionImage("illustrations", "pirate_station");
        NascentGravityWellAPI well2 = Global.getSector().createNascentGravityWell(umbra, 50f);
        well2.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(umbra, 470f);
        hyper.addEntity(well2);*/

        // Nortia is sad :(
        PlanetAPI nortia = (PlanetAPI) system.getEntityById("nortia"); //formerly 450 and 650
        if (!isPAGSM) {
            nortia.setCustomDescriptionId("planet_nortia_fortress");
        } else {
            nortia.setCustomDescriptionId("planet_nortia_fortress_PAGSM");
        }

        // Lion's Cubs revival
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
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

            // Should add Lion's Cubs to Phrygian
            if (!isPAGSM) {
                FleetParamsV3 params3 = new FleetParamsV3(
                        system.getEntityById("phrygian_fortress").getMarket(), // add a source(has to be from a MarketAPI)
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
                system.getEntityById("phrygian_fortress").getContainingLocation().addEntity(fleet);
                fleet.setAI(Global.getFactory().createFleetAI(fleet));
                //fleet.setMarket(system.getEntityById("phrygian_fortress").getMarket());
                fleet.setLocation(system.getEntityById("phrygian_fortress").getLocation().x, system.getEntityById("phrygian_fortress").getLocation().y);
                fleet.setFacing((float) Math.random() * 360f);
                fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, system.getEntityById("phrygian_fortress"), (float) Math.random() * 90000f, null);
            } else {
                // The Manager's Interns(PAGSM variant)
                FleetParamsV3 params3 = new FleetParamsV3(
                        system.getEntityById("phrygian_fortress_PAGSM").getMarket(), // add a source(has to be from a MarketAPI)
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
                fleet.getFleetData().addFleetMember("hammerhead_LMane");
                fleet.getFleetData().addFleetMember("hammerhead_LMane");
                fleet.getFleetData().addFleetMember("hammerhead_LMane");
                fleet.getFleetData().addFleetMember("hammerhead_LMane");
                fleet.getFleetData().addFleetMember("hammerhead_LMane");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("monitor_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LFortitude");
                fleet.getFleetData().addFleetMember("centurion_LFortitude");
                fleet.setNoFactionInName(true);
                fleet.setName("The Manager's Interns");
                // a1.getMarket().getContainingLocation().addEntity(fleet);
                system.getEntityById("phrygian_fortress_PAGSM").getContainingLocation().addEntity(fleet);
                fleet.setAI(Global.getFactory().createFleetAI(fleet));
                //fleet.setMarket(system.getEntityById("phrygian_fortress_PAGSM").getMarket());
                fleet.setLocation(system.getEntityById("phrygian_fortress_PAGSM").getLocation().x, system.getEntityById("phrygian_fortress_PAGSM").getLocation().y);
                fleet.setFacing((float) Math.random() * 360f);
                fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, system.getEntityById("phrygian_fortress_PAGSM"), (float) Math.random() * 90000f, null);
            }

            // 3rd fleet because fortress askonia wasn't buffed enough
        FleetParamsV3 params4 = new FleetParamsV3(
                drakon.getMarket(), // add a source(has to be from a MarketAPI)
                null, // loc in hyper; don't need if have market
                "sindrian_diktat",
                2f, // quality override route.getQualityOverride()
                FleetTypes.TASK_FORCE,
                400f, // combatPts(minimal so special ships can be added)(500f otherwise)
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f// qualityMod
        );
        params4.officerNumberMult = 2f;
        params4.officerLevelBonus = 2;
        params4.officerNumberBonus = 2;
        params4.officerLevelLimit = Global.getSettings().getInt("officerMaxLevel") + (int) OfficerTraining.MAX_LEVEL_BONUS;
        params4.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
        params4.averageSMods = 0;
        params4.commanderLevelLimit = 8;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params4);
        if (fleet == null || fleet.isEmpty()) return;
        fleet.setFaction("sindrian_diktat", true);
        fleet.setNoFactionInName(true);
        fleet.setName("Fringe Defense Force");
        // a1.getMarket().getContainingLocation().addEntity(fleet);
        drakon.getContainingLocation().addEntity(fleet);
        fleet.setAI(Global.getFactory().createFleetAI(fleet));
        //fleet.setMarket(drakon.getMarket());
        fleet.setLocation(drakon.getLocation().x, drakon.getLocation().y);
        fleet.setFacing((float) Math.random() * 360f);
        fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, drakon, (float) Math.random() * 90000f, null);
        }

    private static void initStationNames() {
        SectorAPI sector = Global.getSector();
        StarSystemAPI system = sector.getStarSystem("Askonia");

        // Juturnus Control Garrison
        SectorEntityToken a2 = system.getEntityById("volturn");
        SectorEntityToken juturnus = system.addCustomEntity("de_juturnus", Global.getSettings().getString("diktat_enhancement", "de_juturnus"), "station_side04", "sindrian_diktat");
        juturnus.setInteractionImage("illustrations", "orbital");
        juturnus.setCustomDescriptionId("de_volturn_juturnus");
        juturnus.setCircularOrbitPointingDown(a2, 45 + 180, 120 + 60, 30);
        juturnus.setMarket(a2.getMarket());
        //juturnus.getMarket().setPrimaryEntity(system.getEntityById("volturn"));
        a2.getMarket().getConnectedEntities().add(juturnus);
    }

    private static void initShadowyardsChanges() {
        if (isShadowyards) {
            SectorAPI sector = Global.getSector();
            StarSystemAPI system = sector.getStarSystem("Askonia");
            SectorEntityToken tiger = system.getEntityById("tiger_post");
            if (!isPAGSM) {
                if (!DEenablefortressmode) {
                    tiger.setCustomDescriptionId("outpost_tiger_DE");
                } else {
                    tiger.setCustomDescriptionId("outpost_tiger_DE_fortress");
                }
            } else {
                if (DEenablefortressmode) {
                    tiger.setCustomDescriptionId("outpost_tiger_DE_fortress_PAGSM");
                }
            }
        }
    }

    private static void initIndEvoChanges() {
        if (isIndEvo && !DEenablelitemode) {
            SectorAPI sector = Global.getSector();
            StarSystemAPI system = sector.getStarSystem("Askonia");
            StarSystemAPI system2 = sector.getStarSystem("Andor");
            // Normals
            SectorEntityToken a1 = system.getEntityById("sindria");
            SectorEntityToken a2 = system.getEntityById("volturn");
            SectorEntityToken a3 = system.getEntityById("cruor");
            SectorEntityToken a8 = system.getEntityById("opis_mining_plat");
            SectorEntityToken a9 = system.getEntityById("salus_siphon_plat");
            if (!isPAGSM && !DEenablefortressmode) {
            SectorEntityToken a4 = system.getEntityById("phrygian");
            a4.getMarket().addIndustry("IndEvo_ComArray");
            } else if (!DEenablefortressmode) {
            SectorEntityToken a4 = system.getEntityById("phrygian_PAGSM");
            a4.getMarket().addIndustry("IndEvo_ComArray");
            }
            // Fortress Mode markets and Andoran equivalents
            if (!DEenablefortressmode) {
                SectorEntityToken a5 = system2.getEntityById("ryzan_supercomplex");
                SectorEntityToken a6 = system2.getEntityById("andor_viewport");
                a5.getMarket().addIndustry("IndEvo_IntArray");
                a6.getMarket().addIndustry("IndEvo_ComArray");
                a6.getMarket().addIndustry("IndEvo_Academy");
            } else {
                a3.getMarket().addIndustry("IndEvo_ComArray");
                a3.getMarket().addIndustry("IndEvo_ScrapYard");
                a3.getMarket().addIndustry(Industries.HEAVYINDUSTRY);
                a9.getMarket().addIndustry("IndEvo_ComArray");
                if (!isPAGSM) {
                    SectorEntityToken a5 = system.getEntityById("abiectis_hq");
                    SectorEntityToken a6 = system.getEntityById("phrygian_fortress");
                    a2.getMarket().addIndustry("IndEvo_AdInfra");
                    a5.getMarket().addIndustry("IndEvo_IntArray");
                    a6.getMarket().addIndustry("IndEvo_ComArray");
                    a6.getMarket().addIndustry("IndEvo_Academy");
                } else {
                    SectorEntityToken a5 = system.getEntityById("abiectis_hq_PAGSM");
                    SectorEntityToken a6 = system.getEntityById("phrygian_fortress_PAGSM");
                    a2.getMarket().addIndustry("IndEvo_AdInfra");
                    a5.getMarket().addIndustry("IndEvo_IntArray");
                    a6.getMarket().addIndustry("IndEvo_ComArray");
                    a6.getMarket().addIndustry("IndEvo_Academy");
                }
            }
            a1.getMarket().addIndustry("IndEvo_IntArray");
            a1.getMarket().addIndustry("IndEvo_ReqCenter");
            a2.getMarket().addIndustry("IndEvo_dryDock");
            a8.getMarket().addIndustry("IndEvo_ComArray");
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || exerelin.campaign.SectorManager.getManager().isCorvusMode()) {
            initAskonia();
            initStationNames();
            if (isShadowyards) {
                initShadowyardsChanges();
            }
            if (isIndEvo) {
                initIndEvoChanges();
            }
            if (!DEenablelitemode) {
                if (!DEenablefortressmode) {
                    initAndorMarketChanges();
                }

                if (!DEremovenondiktatfeatures) {
                    //addBifrost();
                    initHybrasilTTMarketChanges();
                }
            }
            if (DEenablefortressmode) {
                initFortressModeChanges();
            }
            MarketAPI market = null;
            MarketAPI market2 = null;

            //adding npcs
            if (!DEenablelitemode) {
                if (!DEenablefortressmode) {
                    //market = Global.getSector().getEconomy().getMarket("ryzan_supercomplex");
                    market = Global.getSector().getStarSystem("Andor").getEntityById("ryzan_supercomplex").getMarket();
                    ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
                    if (market != null) {
                        // timid/tim
                        PersonAPI TTambassador = Global.getFactory().createPerson();
                        TTambassador.setId(TTAmbassador);
                        TTambassador.setGender(FullName.Gender.MALE);
                        TTambassador.setFaction(Factions.TRITACHYON);
                        TTambassador.getName().setFirst("Timothy");
                        TTambassador.getName().setLast("Ironheart");
                        TTambassador.setPostId("TTambassadorpost");
                        TTambassador.setRankId("TTambassadorrank");
                        TTambassador.setImportance(PersonImportance.HIGH);
                        TTambassador.setPortraitSprite("graphics/portraits/portrait_corporate08.png");
                        TTambassador.setVoice(Voices.OFFICIAL);
                        TTambassador.addTag(Tags.CONTACT_UNDERWORLD);
                        TTambassador.addTag(Tags.CONTACT_TRADE);
                        ip.addPerson(TTambassador);
                        market.getCommDirectory().addPerson(TTambassador, 2);
                        market.addPerson(TTambassador);

                        // wmgreywind
                        PersonAPI PLambassador = Global.getFactory().createPerson();
                        PLambassador.setId(PLAmbassador);
                        PLambassador.setGender(FullName.Gender.MALE);
                        PLambassador.setFaction(Factions.PERSEAN);
                        PLambassador.getName().setFirst("Bill");
                        PLambassador.getName().setLast("Silvergale");
                        PLambassador.setPostId("PLambassadorpost");
                        PLambassador.setRankId("PLambassadorrank");
                        PLambassador.setImportance(PersonImportance.HIGH);
                        PLambassador.setPortraitSprite(Global.getSettings().getSpriteName("characters", PLambassador.getId()));
                        //PLambassador.setPortraitSprite("graphics/portraits/portrait_league06.png");
                        PLambassador.setVoice(Voices.OFFICIAL);
                        PLambassador.addTag(Tags.CONTACT_MILITARY);
                        PLambassador.addTag(Tags.CONTACT_TRADE);
                        ip.addPerson(PLambassador);
                        market.getCommDirectory().addPerson(PLambassador, 3);
                        market.addPerson(PLambassador);

                    }
                } else {
                    if (!isPAGSM) {
                        //market2 = Global.getSector().getEconomy().getMarket("abiectis_hq");
                        market2 = Global.getSector().getStarSystem("Askonia").getEntityById("abiectis_hq").getMarket();
                    } else {
                        //market2 = Global.getSector().getEconomy().getMarket("abiectis_hq_PAGSM");
                        market2 = Global.getSector().getStarSystem("Askonia").getEntityById("abiectis_hq_PAGSM").getMarket();
                    }
                    ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
                    if (market2 != null) {
                        // timid/tim
                        PersonAPI TTambassador = Global.getFactory().createPerson();
                        TTambassador.setId(TTAmbassador);
                        TTambassador.setGender(FullName.Gender.MALE);
                        TTambassador.setFaction(Factions.TRITACHYON);
                        TTambassador.getName().setFirst("Timothy");
                        TTambassador.getName().setLast("Ironheart");
                        TTambassador.setPostId("TTambassadorpost");
                        TTambassador.setRankId("TTambassadorrank");
                        TTambassador.setImportance(PersonImportance.HIGH);
                        TTambassador.setPortraitSprite("graphics/portraits/portrait_corporate08.png");
                        TTambassador.setVoice(Voices.OFFICIAL);
                        TTambassador.addTag(Tags.CONTACT_UNDERWORLD);
                        TTambassador.addTag(Tags.CONTACT_TRADE);
                        ip.addPerson(TTambassador);
                        market2.getCommDirectory().addPerson(TTambassador, 2);
                        market2.addPerson(TTambassador);

                        // wmgreywind
                        PersonAPI PLambassador = Global.getFactory().createPerson();
                        PLambassador.setId(PLAmbassador);
                        PLambassador.setGender(FullName.Gender.MALE);
                        PLambassador.setFaction(Factions.PERSEAN);
                        PLambassador.getName().setFirst("Bill");
                        PLambassador.getName().setLast("Silvergale");
                        PLambassador.setPostId("PLambassadorpost");
                        PLambassador.setRankId("PLambassadorrank");
                        PLambassador.setImportance(PersonImportance.HIGH);
                        PLambassador.setPortraitSprite(Global.getSettings().getSpriteName("characters", PLambassador.getId()));
                        //PLambassador.setPortraitSprite("graphics/portraits/portrait_league06.png");
                        PLambassador.setVoice(Voices.OFFICIAL);
                        PLambassador.addTag(Tags.CONTACT_MILITARY);
                        PLambassador.addTag(Tags.CONTACT_TRADE);
                        ip.addPerson(PLambassador);
                        market2.getCommDirectory().addPerson(PLambassador, 3);
                        market2.addPerson(PLambassador);

                    }
                }
                ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
                MarketAPI market3 = null;
                //market3 = Global.getSector().getEconomy().getMarket("midgard");
                if (!DEremovenondiktatfeatures) {
                    market3 = Global.getSector().getStarSystem("Valhalla").getEntityById("midgard").getMarket();
                    if (market3 != null) {
                        PersonAPI person = Global.getSector().getFaction("independent").createRandomPerson(FullName.Gender.ANY);
                        person.setId(MIDGARDADMIN);
                        person.setRankId(Ranks.FACTION_LEADER);
                        person.setPostId(Ranks.POST_ADMINISTRATOR);
                        person.setImportance(PersonImportance.VERY_HIGH);
                        person.addTag(Tags.CONTACT_TRADE);
                        person.addTag(Tags.CONTACT_MILITARY);
                        person.setVoice(Voices.OFFICIAL);
                        person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
                        ip.addPerson(person);

                        market3.setAdmin(person);
                        market3.getCommDirectory().addPerson(person, 0);
                        market3.addPerson(person);
                    }
                }
            }
        }
    }
    private static void initDrakonConditionCheck() {
        if (DEenablefortressmode && !DEdisabledrakoncheck) {
            SectorAPI sector = Global.getSector();
            StarSystemAPI system = sector.getStarSystem("Askonia");
            SectorEntityToken drakon = system.getEntityById("drakon_fortress");
            SpecialItemData drakonHasLamp = drakon.getMarket().getIndustry(Industries.POPULATION).getSpecialItem();
            //!drakonHasLamp.equals(new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null))
            if (drakonHasLamp == null || !drakonHasLamp.equals(new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null))) {
                drakon.getMarket().removeCondition("DE_Unstablelamp");
            }
        }
    }

    /*private static void initLobsterProliferation() {
            SectorAPI sector = Global.getSector();
            StarSystemAPI system = sector.getStarSystem("Askonia");
            SectorEntityToken waterplanets = (SectorEntityToken) sector.getEntitiesWithTag(Conditions.WATER_SURFACE);
            //!drakonHasLamp.equals(new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null))
            if (waterplanets.getFaction().equals("sindrian_diktat")) {
                waterplanets.getMarket().addCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
            }
    }*/

    @Override
    public void onGameLoad (boolean newGame){
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        //initLobsterProliferation();
        if ((!haveNexerelin || SectorManager.getManager().isCorvusMode()) && !DEdisabledrakoncheck) {
            initDrakonConditionCheck();
        }
        if (!Global.getSector().getListenerManager().hasListenerOfClass(DE_OmegaListenerFleet.class))
            Global.getSector().getListenerManager().addListener(new DE_OmegaListenerFleet());
    }
}