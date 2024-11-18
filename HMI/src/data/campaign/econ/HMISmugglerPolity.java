package data.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HMISmugglerPolity extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

    public static final float ACCESS_BONUS = 25f;

    @Override
    public void apply(String id) {
        market.addTransientImmigrationModifier(this);

        market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS / 100f, "Smuggler Network");
    }

    @Override
    public void unapply(String id) {
        market.removeTransientImmigrationModifier(this);

        market.getAccessibilityMod().unmodifyFlat(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("%s accessibility.",
                10f, Misc.getHighlightColor(),
                "+" + (int) ACCESS_BONUS + "%");
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add("HMI", 10f);
    }
}
