package org.niatahl.tahlan.plugins

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.AICoreAdminPlugin
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import org.niatahl.tahlan.utils.TahlanIDs.CORE_ARCHDAEMON
import org.niatahl.tahlan.utils.TahlanIDs.CORE_DAEMON
import org.niatahl.tahlan.utils.TahlanIDs.NEURALLINK_COMM

class CampaignPluginImpl: BaseCampaignPlugin() {

    override fun pickAICoreOfficerPlugin(commodityId: String): PluginPick<AICoreOfficerPlugin>? {
        return when (commodityId) {
            CORE_DAEMON, CORE_ARCHDAEMON -> PluginPick<AICoreOfficerPlugin>(DaemonOfficerPlugin(), CampaignPlugin.PickPriority.MOD_SET)
            NEURALLINK_COMM -> PluginPick<AICoreOfficerPlugin>(NeuralLinkOfficerPlugin(), CampaignPlugin.PickPriority.MOD_SET)
            else -> null
        }
    }

    override fun pickAICoreAdminPlugin(commodityId: String?): PluginPick<AICoreAdminPlugin>? {
        return when (commodityId) {
            CORE_ARCHDAEMON -> PluginPick<AICoreAdminPlugin>(DaemonOfficerPlugin(), CampaignPlugin.PickPriority.MOD_SET)
            else -> null
        }
    }

    override fun getId(): String {
        return "TahlanCampaignPluginImpl"
    }
}