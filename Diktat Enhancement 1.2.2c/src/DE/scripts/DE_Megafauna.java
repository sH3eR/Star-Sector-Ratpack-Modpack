package DE.scripts;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class DE_Megafauna extends BaseHazardCondition {

	public static float STABILITY_PENALTY_SOFT = -1f;
	public static float STABILITY_PENALTY = -4f;
	
	public void apply(String id) {
		super.apply(id);
		if (market.getFaction() != null) {
			if (market.getFaction().getId().contains("sindrian_diktat")) {
				int marketMult = Misc.getFactionMarkets(market.getFactionId()).size();
				market.getStability().modifyFlat(id, STABILITY_PENALTY_SOFT, "Megafauna Attacks");
			}
			else {
				market.getStability().modifyFlat(id, STABILITY_PENALTY, "Megafauna Attacks");
				}
			}
		}

	public void unapply(String id) {
		super.unapply(id);
		market.getStability().unmodify(id);
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		float pad = 10f;
		float padS = 2f;
		tooltip.addPara(
				"%s stability if controlled by the Sindrian Diktat/Sindrian Fuel Company;",
				pad,
				Misc.getHighlightColor(),
				"" + (int) STABILITY_PENALTY_SOFT
		);
		tooltip.addPara(
				"%s reduction in stability otherwise.",
				padS,
				Misc.getHighlightColor(),
				"" + (int) STABILITY_PENALTY
		);
	}
}


