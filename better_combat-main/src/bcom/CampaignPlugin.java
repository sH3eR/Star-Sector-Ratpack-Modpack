package bcom;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;


public final class CampaignPlugin extends BaseCampaignPlugin {
    @Override
    public PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin> pickBattleCreationPlugin(
            SectorEntityToken set
    ) {
        return new PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin>(
                new BcomBattleCreationPlugin(), PickPriority.MOD_SET //higher than general, will not override custom SOTF content
        );
    }
    ////    @Override
    ////    public PluginPick<BattleCreationPlugin> pickBattleCreationPlugin(SectorEntityToken opponent) {
    ////        boolean haveSotf = Global.getSettings().getModManager().isModEnabled("secretsofthefrontier");
    ////        if(Settings.isEnableMap()){
    ////            return new PluginPick<BattleCreationPlugin>(new BattleCreationPlugin(), CampaignPlugin.PickPriority.MOD_GENERAL);
    ////        }
    ////        return null;
    ////    }
}
