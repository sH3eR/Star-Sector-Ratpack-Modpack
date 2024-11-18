package data.scripts.ix.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

public class TWArmsBazaarMarket extends BaseSubmarketPlugin {
	
	@Override
	public void init(SubmarketAPI submarket) {
		super.init(submarket);
	}
	
	@Override
	public float getTariff() {
		return 0.09f;
	}
	
	@Override
	public String getName() {
		return "Arms Bazaar";
	}
	
	@Override
	public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
		return false;
	}
	
	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (stack.isCommodityStack()) {
			if (stack.getCommodityId().equals("fuel")) return false;
			else return true;
		} 
		return !stack.isWeaponStack();
	}
	
	@Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		return (!commodityId.equals("fuel"));
	}
	
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "The arms bazaar only trades in weapons and fuel";
	}
	
	@Override 
	public boolean isParticipatesInEconomy() {
		return false;
	}
	
	@Override
	public boolean isEnabled(CoreUIAPI ui) {
		return true;
	}
	
	@Override
	public boolean showInFleetScreen() {
		return false;
	}
	
	@Override
	public void updateCargoPrePlayerInteraction() {
		float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
		addAndRemoveStockpiledResources(seconds, false, true, true);
		sinceLastCargoUpdate = 0f;
		
		if (okToUpdateShipsAndWeapons()) {
			sinceSWUpdate = 0f;
			pruneWeapons(0f);
			
			int weapons = 7 + Math.max(0, market.getSize() - 1) * 2;
			addWeapons(weapons, weapons + 2, 3, "ix_trinity");
			
			if (getCargo().getCommodityQuantity("fuel") < 1000f) {
				getCargo().addCommodity("fuel", 2000f + (float) Math.random() * 1000f);
			}
			else if (getCargo().getCommodityQuantity("fuel") < 2000f) {
				getCargo().addCommodity("fuel", 1000f + (float) Math.random() * 2000f);
			}
		}
		getCargo().sort();
	}
}