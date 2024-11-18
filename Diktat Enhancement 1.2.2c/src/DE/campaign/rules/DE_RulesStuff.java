package DE.campaign.rules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.CoreCampaignPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class DE_RulesStuff extends CoreCampaignPluginImpl {
    Random rand = new Random();

    public void updateEntityFacts(SectorEntityToken entity, MemoryAPI memory) {
        memory.set("$Hegemonyweaponamount",rand.nextInt(25),0);
        memory.set("$PLweaponamount",rand.nextInt(25),0);
        memory.set("$TTweaponamount",rand.nextInt(25),0);
        memory.set("$omegaweaponamount",5,0);
        memory.set("$de_FoughtOmega",false);
    }
}
