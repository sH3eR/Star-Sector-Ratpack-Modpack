package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.fleets.MN_personalFleetPyotr;
import data.scripts.fleets.MN_personalFleetVice;
import data.scripts.weapons.ai.MSS_grenadestickyAI;
import data.scripts.weapons.ai.MSS_plasmabombAI;
import data.scripts.world.ChaseMairaathSwitcher;
import data.scripts.world.MN_gen;
import data.scripts.world.MN_people;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.SectorManager;
import exerelin.campaign.intel.BuyColonyIntel;
import org.dark.shaders.util.ShaderLib;


public class MN_modPlugin extends BaseModPlugin {

    //public static final String antiMissile_ID = "SCY_antiS";
    //public static final String smartgun_ID = "MSS_smartgun_shot";
    //public static final String coasting_ID = "SCY_coastingS";
    //public static final String laser_ID = "SCY_laserS";
    //public static final String bomberTorpedo_ID = "SCY_bomberTorpedo";
    //public static final String cluster_ID = "SCY_clusterS";    
    //public static final String rocket_ID = "SCY_rocketS";      
    //public static final String arc_ID = "SCY_arcS";

    ////////////////////////////////////////
    //                                    //
    //      MISSILES AI OVERRIDES         //
    //                                    //
    ////////////////////////////////////////

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case "MSS_thumper_grenade_sticky":
                return new PluginPick<MissileAIPlugin>(new MSS_grenadestickyAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case "MSS_bomb_plasma":
                return new PluginPick<MissileAIPlugin>(new MSS_plasmabombAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }



    public static boolean isExerelin = false;

    private static void initMN() {
        if (isExerelin && !SectorManager.getCorvusMode()) {
            return;
        }
        new MN_gen().generate(Global.getSector());
    }

    private static void setExerelin(boolean bool)
    {
        isExerelin = bool;
    }
    ////////////////////////////////////////
    //                                    //
    //       ON APPLICATION LOAD          //
    //                                    //
    ////////////////////////////////////////

    @Override
    public void onApplicationLoad() throws ClassNotFoundException {
        setExerelin(Global.getSettings().getModManager().isModEnabled("nexerelin"));
        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.lazywizard.lazylib.ModUtils");
        } catch (ClassNotFoundException ex) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "LazyLib is required to run at least one of the mods you have installed."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download LazyLib at http://fractalsoftworks.com/forum/index.php?topic=5444"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }


        //Check ShaderLib for lights
        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");
            ShaderLib.init();
            //LightData.readLightDataCSV("data/lights/MSS_light_data.csv");
            //TextureData.readTextureDataCSV("data/lights/MSS_texture_data.csv");
        } catch (ClassNotFoundException ex) { }
    }

    ////////////////////////////////////////
    //                                    //
    //        ON NEW GAME CREATION        //
    //                                    //
    ////////////////////////////////////////

    @Override
    public void onNewGame() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()){
            new MN_gen().generate(Global.getSector());
            Global.getSector().addScript(new ChaseMairaathSwitcher());
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
            MarketAPI mairaath = Global.getSector().getEconomy().getMarket("mairaath");
        if (mairaath != null) {
            MN_people.create();
        }
    }

    @Override
    public void onNewGameAfterTimePass() {
        // handle Nex's governorship intel event
        // so governorship is regained properly if market is lost and retaken
        SectorAPI sector = Global.getSector();
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (haveNexerelin && SectorManager.getCorvusMode() && ChaseMairaathSwitcher.MAYASURA.equals(PlayerFactionStore.getPlayerFactionIdNGC()))
        {
            SectorEntityToken mairaath = Global.getSector().getEntityById(ChaseMairaathSwitcher.MAIRAATH);
            if (mairaath == null) return;
            MarketAPI market = mairaath.getMarket();
            if (market != null) {
                BuyColonyIntel intel = new BuyColonyIntel(market.getFactionId(), market);
                intel.init();
            }
        }
        if (!sector.hasScript(MN_personalFleetPyotr.class)) {
            sector.addScript(new MN_personalFleetPyotr());
        }
        if (!sector.hasScript(MN_personalFleetVice.class)) {
            sector.addScript(new MN_personalFleetVice());
        }
    }
}