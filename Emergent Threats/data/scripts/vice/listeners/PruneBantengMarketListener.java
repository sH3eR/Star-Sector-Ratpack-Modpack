package data.scripts.vice.listeners;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

//reduces Banteng (Standard/TT/SD)
public class PruneBantengMarketListener extends BaseCampaignEventListener {
	
	private static String BANTENG = "vice_banteng";
	//private static String BANTENG_SD = "vice_banteng_sd";
	//private static String BANTENG_TT = "vice_banteng_ix";
	private static int MAX_SHIP_COUNT = 4;
	
	public PruneBantengMarketListener() {
		super(true);
	}	
	
	public static void pruneMarket(MarketAPI market) {
		if (market == null || market.getSubmarketsCopy() == null) return;
		List<SubmarketAPI> submarkets = market.getSubmarketsCopy();
		for (SubmarketAPI s : submarkets) {
			int shipCountBant = 0;
			if (s.getSpecId().equals(Submarkets.SUBMARKET_STORAGE)) return;
			List<FleetMemberAPI> shipsToDelete = new ArrayList<FleetMemberAPI>();
			List<FleetMemberAPI> ships = s.getCargo().getMothballedShips().getMembersListCopy();
			for (FleetMemberAPI ship : ships) {
				//prune extra haulers
				if (ship.getHullSpec().getHullId().startsWith(BANTENG)) {
					if (shipCountBant < MAX_SHIP_COUNT) shipCountBant++;
					else shipsToDelete.add(ship);
				}
			}
			if (!shipsToDelete.isEmpty()) {
				for (FleetMemberAPI ship : shipsToDelete) {
					s.getCargo().getMothballedShips().removeFleetMember(ship);
				}
			}
			s.getPlugin().updateCargoPrePlayerInteraction();
		}
	}

	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		pruneMarket(market);
	}
}