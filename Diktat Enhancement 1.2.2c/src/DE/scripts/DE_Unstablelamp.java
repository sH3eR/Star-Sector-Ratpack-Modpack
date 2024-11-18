package DE.scripts;

import java.util.Map;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;



public class DE_Unstablelamp extends BaseHazardCondition {

	private float HAZARD_MALUS = 0.75f;

	public void apply(String id) {
				market.getHazard().modifyFlat(id, HAZARD_MALUS, condition.getName());
	}

	public void unapply(String id) {
		market.getHazard().unmodifyFlat(id);
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
	}

	@Override
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		String pct = "" + (int)(HAZARD_MALUS * 100f) + "%";
		if (HAZARD_MALUS > 0) pct = "+" + pct;
		tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), pct);
	}
}




