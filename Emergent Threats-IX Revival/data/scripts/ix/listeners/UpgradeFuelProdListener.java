package data.scripts.ix.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;

//upgrades fuel industry after player uses antimatter stabilizer, done away from market to avoid concurrency bug
public class UpgradeFuelProdListener extends BaseCampaignEventListener {
	
	private static String FUEL = "fuelprod";
	private static String FUEL_IX = "ix_fuel_production";
	
	public UpgradeFuelProdListener() {
		super(true);
	}	
	
	@Override
	public void reportPlayerClosedMarket(MarketAPI market) {
		//removes duplicate building and gives full refund if player tries to build basic fuel industry
		if (Global.getSector().getMemoryWithoutUpdate().is("$colony_upgradeFuelProd", false)) { 
			if (market.hasIndustry(FUEL) && market.hasIndustry(FUEL_IX)) {
				if (market.getIndustry(FUEL).isBuilding()) {	
					Global.getSector().getPlayerFleet().getCargo().getCredits().add(450000); 
				}
				market.removeIndustry(FUEL, null, false);
			} 
		}
		
		if (Global.getSector().getMemoryWithoutUpdate().is("$colony_upgradeFuelProd", true)) {
			//determine old industry modifiers
			Industry fuel = market.getIndustry(FUEL);
			String coreId = fuel.getAICoreId();
			String itemId = null;
			boolean isImproved = fuel.isImproved();
			if (fuel.getSpecialItem() != null) itemId = fuel.getSpecialItem().getId();
			
			//add new industry with modifiers and hide old industry
			if (itemId == null) market.addIndustry(FUEL_IX);
			else market.addIndustry(FUEL_IX, new ArrayList<String>(Arrays.asList(itemId)));
			if (coreId != null) market.getIndustry(FUEL_IX).setAICoreId(coreId);
			market.getIndustry(FUEL_IX).setImproved(isImproved);
			fuel.setAICoreId(null);
			fuel.setSpecialItem(null);
			market.removeIndustry(FUEL, null, false);
			
			//reset memkey so event can be repeated if player loots another stabilizer
			Global.getSector().getMemoryWithoutUpdate().set("$colony_upgradeFuelProd", false);
		}
	}
}