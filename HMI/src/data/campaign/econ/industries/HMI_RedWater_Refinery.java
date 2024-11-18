package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.HMI_items;

public class HMI_RedWater_Refinery extends BaseIndustry {

	public void apply() {
		super.apply(true);

		int size = market.getSize();

		demand(Commodities.SUPPLIES, size );
		demand(Commodities.CREW, size + 2);
		demand(Commodities.HEAVY_MACHINERY, size - 1);

		supply(HMI_items.REDWATER, size );
		supply(Commodities.ORGANICS, size + 3);
		supply(Commodities.DRUGS, size -1);

		Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY, Commodities.SUPPLIES, Commodities.CREW);

		applyDeficitToProduction(1, deficit, Commodities.ORGANICS, Commodities.DRUGS, HMI_items.REDWATER);
    }
    

	@Override
	public void unapply() {
		super.unapply();
	}

	@Override
	public boolean isAvailableToBuild() {
		return false;
	}

	@Override
	public boolean showWhenUnavailable() {
		return false;
	}
}

