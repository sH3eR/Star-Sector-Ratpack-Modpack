package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.thoughtworks.xstream.XStream;
import data.hullmods.DMEBlockedHullmodDisplayScript;
import data.scripts.campaign.intel.istl_DiscoverEntityListener;
import data.scripts.weapons.ai.istl_Itano_AMM_AI;
import data.scripts.weapons.ai.istl_GradMissileAI;
import exerelin.campaign.SectorManager;

import java.io.IOException;
import org.apache.log4j.Level;
import org.json.JSONException;

import data.scripts.world.ISTLGen;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.json.JSONObject;
//import data.scripts.world.systems.Nikolaev;
//import data.scripts.world.systems.Martinique;


public class DMEModPlugin extends BaseModPlugin
{
    
    private static final String DME_SETTINGS = "DMEsettings.ini";
    public static boolean RANDOMBREAKERS;
    //public static boolean HANDMADEBREAKERS;
    //public static boolean NEXBREAKERS;

    // Missiles and weapons that use custom AI
    public static final String ITANO_MISSILE_ID = "istl_AMM";
    public static final String GRAD_MISSILE_ID = "istl_TBM";
    //public static final String MARTEAU_MISSILE_ID = "istl_cluster_bomb";

    //Loads in if we have Nexerelin or not; this is important for some later script checking
    public static final boolean isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        
//    @Override
//    public void configureXStream(XStream x)
//    {
//        // Xtreme overkill edition
//        // Singletons could just be commented out, along with anything tagged "Short-lived" but... Overkill Never Fails™
// 
//        x.alias("DME6emeFleetManager", SixthBureauFleetManager.class);  // Singleton
//        x.alias("BladeBreakerAssignmentAI", BladeBreakerAssignmentAI.class);  // Singleton
//        x.alias("BladeBreakerSeededFleetManager", BladeBreakerSeededFleetManager.class);  // Singleton
//        x.alias("BladeBreakerStationFleetManager", BladeBreakerStationFleetManager.class);  // Singleton
//        x.alias("BladeBreakerBladeBreakerThemeGenerator", BladeBreakerThemeGenerator.class);  // Singleton
//
//        
////        // Retrofit submarket
////        x.alias("THIRetroSubmkt", tiandong_RetrofitSubmarketPlugin.class);  // Singleton
////        x.alias("THIConvScrpt", ConversionScript.class);  // *Not* a singleton, although fairly short-lived
////        x.aliasAttribute(ConversionScript.class, "cargo", "c");
////        x.aliasAttribute(ConversionScript.class, "hullIdTo", "to");
////        x.aliasAttribute(ConversionScript.class, "hullTypeFrom", "hF");
////        x.aliasAttribute(ConversionScript.class, "hullTypeTo", "hT");
////        x.aliasAttribute(ConversionScript.class, "shipName", "sn");
////        x.aliasAttribute(ConversionScript.class, "started", "s");
////        x.aliasAttribute(ConversionScript.class, "time", "t");
//    }
    
    private static void initDME()
    {
        //new Nikolaev().generate(Global.getSector());
        //new Martinique().generate(Global.getSector());
        //new Kostroma().generate(Global.getSector());
        //new Besson().generate(Global.getSector());
        //new Aleph().generate(Global.getSector());
        
        new ISTLGen().generate(Global.getSector());
    }
    
//    private static void initHandmadeBreakers()
//    {
//        new ISTLGen().handmadebreakers(Global.getSector());
//    }
    
    private static void initBreakers()
    {
        new ISTLGen().randombreakers(Global.getSector());
    }

    @Override
    public void onNewGame()
    {    
//    if (isExerelin && !SectorManager.getCorvusMode())
//        {
//            return;
//        }
//        initDME();
        
      if (!isExerelin || SectorManager.getCorvusMode())
          {
          initDME();
          }
      
      //Whatever the hell I add to get my handmade Breakers setting to work.
      
      if (RANDOMBREAKERS)
          {
          return;
          }
          initBreakers();
    }
    
    
    @Override
    public void onGameLoad(boolean newGame)
    {
        Global.getSector().addTransientScript(new DMEBlockedHullmodDisplayScript());
        addBreakerBeaconListener();
    }
    
    private static void loadDMESettings() throws IOException, JSONException {
        JSONObject setting = Global.getSettings().loadJSON(DME_SETTINGS);
        RANDOMBREAKERS = setting.getBoolean("noRandomBreakers");
        //HANDMADEBREAKERS = setting.getBoolean("noHandmadeBreakers");
        //NEXBREAKERS = setting.getBoolean("noNexBreakers");
    }
        
    protected void addBreakerBeaconListener() {
    
        SectorAPI sector = Global.getSector();
        GenericPluginManagerAPI plugins = sector.getGenericPlugins();
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
    
        if (!listeners.hasListenerOfClass(istl_DiscoverEntityListener.class)) {
            listeners.addListener(new istl_DiscoverEntityListener());
        }    
    }
    
    @Override
    public void onApplicationLoad()
    {
//        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
//        if (!hasLazyLib) {
//            throw new RuntimeException("Dassault-Mikoyan Engineering requires LazyLib!" +
//            "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");
//        }
        //neccessary to make lighting stuff work 
        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            ShaderLib.init();
            LightData.readLightDataCSV("data/lights/istl_light_data.csv");
            TextureData.readTextureDataCSV("data/lights/istl_texture_data.csv");
        }
//        if (!hasGraphicsLib) {
//            throw new RuntimeException("Dassault-Mikoyan Engineering requires GraphicsLib!" +
//            "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=10982");
//        }
//        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
//        if (!hasMagicLib) {
//            throw new RuntimeException("Dassault-Mikoyan Engineering requires MagicLib! Where is the magic?" +
//            "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718.0");
//        }
        try {
            loadDMESettings();
        } catch (IOException | JSONException e) {
            Global.getLogger(DMEModPlugin.class).log(Level.ERROR, "DMEsettings.ini loading failed!" + e.getMessage());
        }
    }
    
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip)
    {
        switch (missile.getProjectileSpecId())
        {
            case ITANO_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new istl_Itano_AMM_AI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case GRAD_MISSILE_ID: //Currently very, very broken. Stupid people (I'm looking at you, glupi lopov) who steal this script without reading this comment will get what they deserve.
                return new PluginPick<MissileAIPlugin>(new istl_GradMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
//            case MARTEAU_MISSILE_ID:
//                return new PluginPick<MissileAIPlugin>(new istl_MarteauMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            default:
                return null;
        }
    }
}
