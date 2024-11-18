package data.scripts.ix.util;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

import lunalib.lunaSettings.LunaSettings;

public class PanopticonStructureUtil {
	
	private static String MONITORED_VERTEX = "ix_monitored";
	private static String MONITORED_PLAYER = "ix_monitored_player";
	private static String CORE_ID = "ix_panopticon_instance";
	private static String CORE = "ix_panopticon";
	private static String NODE = "ix_panopticon_node";
	private static String P_CORE = "ix_panopticon_player_core";
	private static String P_NODE = "ix_panopticon_player_node";
	private static String CORONAL_CONDITION = "aotd_coronal_market_cond";

	private static List<String> STRUCTURE_LIST = new ArrayList<String>();
	static {
		STRUCTURE_LIST.add("tw_cloudburst_academy");
		STRUCTURE_LIST.add("tw_fleet_embassy");
		STRUCTURE_LIST.add("ix_embassy_player");
		STRUCTURE_LIST.add("ix_surveillance_center");
	}
	
	public static void applyBlackMarketChange(MarketAPI market, String command) {
		if (command.equals("apply")) {
			boolean isEnabled = LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_monitor_enabled");
			if (isEnabled) market.removeSubmarket(Submarkets.SUBMARKET_BLACK);
			else market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		}
		else if (command.equals("unapply")) {
			market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		}
		else return;
	}
	
	public static boolean panopticonIsActiveCheck(MarketAPI m, boolean isCoreWorld) {
		boolean isActive = false;
		if (isCoreWorld) {
			for (String s: STRUCTURE_LIST) {
				if (m.hasIndustry(s) && !m.getIndustry(s).isHidden() && CORE_ID.equals(m.getIndustry(s).getAICoreId())) isActive = true;
			}
		}
		else {
			//old nodes are not active on worlds where a core would be present
			for (String s: STRUCTURE_LIST) {
				if (m.hasIndustry(s) && !m.getIndustry(s).isHidden() && CORE_ID.equals(m.getIndustry(s).getAICoreId())) return false;
			}
			
			if (m.getStarSystem() == null || m.getStarSystem().getId() == null) return false;
			if (m.getFactionId() == null) return false;
			String systemId = m.getStarSystem().getId();
			String facId = m.getFactionId();
			List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
			for (MarketAPI mk: markets) {
				if (mk.getStarSystem() == null || mk.getStarSystem().getId() == null) continue;
				if (mk.getFactionId() == null) continue;
				if (mk.getStarSystem().getId().equals(systemId) && mk.getFactionId().equals(facId)) {
					for (String s : STRUCTURE_LIST) {
						if (mk.hasIndustry(s) && !mk.getIndustry(s).isHidden() && CORE_ID.equals(mk.getIndustry(s).getAICoreId())) {
							isActive = true;
							break;
						}
					}
				}
			}
		}
		return isActive;
	}
	
	public static void activatePlayerCores() {
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
		List<String> systemList = new ArrayList<String>();
		List<String> factionList = new ArrayList<String>();
		
		//add core and record core systems
		for (MarketAPI m : markets) {
			for (String s: STRUCTURE_LIST) {
				if (m.hasIndustry(s) && !m.getIndustry(s).isHidden() && CORE_ID.equals(m.getIndustry(s).getAICoreId())) {
					if (!m.hasIndustry(CORE) && !m.hasIndustry(NODE) 
						&& !m.hasIndustry(P_CORE) && !m.hasIndustry(P_NODE)) {
						m.addIndustry(P_CORE);
					}
					else if (m.hasIndustry(P_NODE) && m.getIndustry(P_NODE).isHidden()) m.addIndustry(P_CORE);
					if (m.getStarSystem() != null && m.getStarSystem().getId() != null) {
						if (m.getFactionId() == null) continue;
						systemList.add(m.getStarSystem().getId());
						factionList.add(m.getFactionId());
					}
				}
			}
		}
		
		//if market matches a core system on list, faction matches core market faction, but has no core, add node
		for (MarketAPI m : markets) {
			if (m.getStarSystem() == null || m.getStarSystem().getId() == null || m.getFactionId() == null) continue;
			String systemId = m.getStarSystem().getId();
			for (int i = 0; i < systemList.size(); i++) {
				if (systemId.equals(systemList.get(i))) {
					if (m.getFactionId() == null || factionList.get(i) == null) continue;
					if (m.getFactionId().equals(factionList.get(i))) {
						if (!m.hasIndustry(CORE) && !m.hasIndustry(NODE) 
									&& !m.hasIndustry(P_CORE) && !m.hasIndustry(P_NODE)
									&& !m.hasCondition(CORONAL_CONDITION)) {
							m.addIndustry(P_NODE);
						}
						else if (m.hasCondition(CORONAL_CONDITION)) {
							m.removeIndustry(NODE, null, false);
							m.removeIndustry(P_NODE, null, false);
						}
						else if (m.hasIndustry(P_CORE) && m.getIndustry(P_CORE).isHidden()) m.addIndustry(P_NODE);
					}
				}
			}
		}
	}
}