package data.scripts.ix.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

//import lunalib.lunaSettings.LunaSettings;

public class IXMonitoredPlayer extends BaseHazardCondition {
	
    private static int STABILITY_BONUS = 5;
	
    @Override
    public void apply(String id) {
		//market.getStability().modifyFlat(id, STABILITY_BONUS, "Panopticon monitoring");
	}
    
    @Override
    public void unapply(String id) {
		//market.getStability().unmodify(id);
	}
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        float opad = 10f;
		//tooltip.addPara("%s stability", opad, Misc.getHighlightColor(), "+" + STABILITY_BONUS);
    }
}