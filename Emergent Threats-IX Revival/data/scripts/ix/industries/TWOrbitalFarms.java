package data.scripts.ix.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;

public class TWOrbitalFarms extends BaseIndustry {
	
	public void apply() {
		super.apply(true);
		applyIncomeAndUpkeep(3);
		int size = market.getSize();
		demand(Commodities.HEAVY_MACHINERY, size - 3);
		demand(Commodities.ORE, size - 1);
		demand(Commodities.ORGANICS, size - 1);
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORE);
		applyDeficitToProduction(0, deficit, Commodities.FOOD);
		supply(Commodities.FOOD, size - 1);
		
		if (!isFunctional()) {
			supply.clear();
		}
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

	@Override
	public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltip(mode, tooltip, expanded);
	}	
	
	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
	
	@Override
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level.prev();
	}

	@Override
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level.prev();
	}
}
