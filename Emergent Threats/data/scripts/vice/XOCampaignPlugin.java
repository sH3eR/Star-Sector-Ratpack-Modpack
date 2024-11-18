package data.scripts.vice;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;

import data.scripts.vice.SynthesisCorePlugin;

public class XOCampaignPlugin extends BaseCampaignPlugin {

	private static String SYNTHESIS_CORE_ID = "xo_synthesis_core";

	public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
		if (SYNTHESIS_CORE_ID.equals(commodityId)) {
			return new PluginPick<AICoreOfficerPlugin>(new SynthesisCorePlugin(), PickPriority.MOD_SET);
		}
		else return null;
	}
}