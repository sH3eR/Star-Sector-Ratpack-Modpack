package data.scripts.ix.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Pair;

public class IXFuelProduction extends BaseIndustry {

	public void apply() {
		super.apply(true);
		
		//market.removeIndustry("fuelprod", null, false); //handled by listener so player gets refund
		
		supplyBonus.modifyFlat(getModId(2), market.getAdmin().getStats().getDynamic().getValue(Stats.FUEL_SUPPLY_BONUS_MOD, 0), "Administrator");
		
		int size = market.getSize();
		
		demand(Commodities.VOLATILES, size + 1);
		demand(Commodities.HEAVY_MACHINERY, size - 1);
		
		supply(Commodities.FUEL, size - 1);
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.VOLATILES);
		
		applyDeficitToProduction(1, deficit, Commodities.FUEL);
		
		if (!isFunctional()) {
			supply.clear();
		}
	}
	
	@Override
	public void unapply() {
		super.unapply();
	}
	
	@Override
	//For player embassy, can build only after quest complete, other variants cannot be built
	public boolean isAvailableToBuild() {
		return false;
	}
	
	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}

	@Override
	protected boolean canImproveToIncreaseProduction() {
		return true;
	}
}
