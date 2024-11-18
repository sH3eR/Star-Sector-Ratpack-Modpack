package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.econ.HMI_items;

public class HMI_MaxwellSupport extends BaseIndustry {
	protected transient SubmarketAPI saved = null;
	public void apply() {
		super.apply(true);

		int size = market.getSize();

		supply(Commodities.FOOD, size + 1);
		supply(Commodities.ORGANICS, size + 1);
		supply(Commodities.SUPPLIES, size + 1);
		supply(Commodities.DOMESTIC_GOODS, size);
		supply(Commodities.DRUGS, size);
		supply(Commodities.FUEL, size + 1);
		supply(Commodities.SUPPLIES, size + 2);
		supply(Commodities.METALS, size + 1);
		supply(Commodities.RARE_METALS, size);
		supply(Commodities.VOLATILES, size -1);
		supply(Commodities.CREW, size + 2);
		supply(Commodities.SHIPS, size);

	}

	@Override
	public void unapply() {
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

