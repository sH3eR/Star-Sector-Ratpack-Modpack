package data.scripts.ix.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.util.PanopticonStructureUtil;
import lunalib.lunaSettings.LunaSettings;

public class IXPanopticonCore extends BaseIndustry {

	private static float DEFAULT_PATHER_INTEREST = 10f;
	private static float DEFENSE_BONUS_NODE = 1f;
	private static int STABILITY_BONUS = 5;
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String CORE = "ix_panopticon";
	private static String NODE = "ix_panopticon_node";
	private static String PLAYER_CORE = "ix_panopticon_player_core";
	private static String PLAYER_NODE = "ix_panopticon_player_node";
	private static String IX_PLAYER_CORE_ID = "ix_panopticon_instance";
	private static String MONITORED_VERTEX = "ix_monitored";
	private static String MONITORED_PLAYER = "ix_monitored_player";
	private static String FLEET_COMMAND_STATION = "ix_zorya_vertex";
	private static String FCOMM = "ix_fleet_command";
	private static String THIS_MOD_ID = "EmergentThreats_IX_Revival";
	private static String HONOR_GUARD_SUBMARKET = "ix_honor_guard_market";
	
	//also handles propagation of instance core and nodes via PanopticonStructureUtil.activatePlayerCores()
	@Override
	public void apply() {
		super.apply(false);
		
		PanopticonStructureUtil.activatePlayerCores();
		if (isFunctional()) {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + DEFENSE_BONUS_NODE, getNameForModifier());
			market.suppressCondition(Conditions.PIRATE_ACTIVITY);
			market.addCondition(MONITORED_VERTEX);
			market.getStability().modifyFlat(id, STABILITY_BONUS, "Panopticon monitoring");
			if (market.getFactionId().equals(IX_FAC_ID) && !market.hasSubmarket(HONOR_GUARD_SUBMARKET)) {
				market.addSubmarket(HONOR_GUARD_SUBMARKET);
			}
			else market.removeSubmarket(HONOR_GUARD_SUBMARKET);
			PanopticonStructureUtil.applyBlackMarketChange(market, "apply");
		}
		else unapply();
	}
	
	@Override
	public void unapply() {
		super.unapply();
		PanopticonStructureUtil.activatePlayerCores();
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		market.unsuppressCondition(Conditions.PIRATE_ACTIVITY);
		market.removeCondition(MONITORED_VERTEX);
		market.removeSubmarket(HONOR_GUARD_SUBMARKET);
		market.getStability().unmodify(id);
		PanopticonStructureUtil.applyBlackMarketChange(market, "unapply");
	}
	
	@Override
	public boolean isDisrupted() {
		if (isHidden()) return true;
		String key = getDisruptedKey();
		return market.getMemoryWithoutUpdate().is(key, true);
	}
	
	@Override
	public boolean isHidden() {
		boolean hidden = false;
		if (!market.getFactionId().equals(IX_FAC_ID) || !isFunctional()) hidden = true;
		if (hidden) unapply();
		return hidden;
	}
	
	@Override
	public boolean isFunctional() {
		return panopticonIsActiveCheck();
	}

	@Override
	public float getPatherInterest() {
		float interest = DEFAULT_PATHER_INTEREST;
		boolean hasIndEvo = Global.getSettings().getModManager().isModEnabled("IndEvo");
		if (market.hasIndustry("BOGGLED_CHAMELEON") 
					&& (Commodities.ALPHA_CORE).equals(market.getIndustry("BOGGLED_CHAMELEON").getAICoreId())) return 0f;
		if (panopticonIsActiveCheck()) {
			for (Industry industry : market.getIndustries()) {
				if (hasIndEvo && industry.getCurrentName().equals("Monastic Order")) return 0f;
				if (industry.getId().equals(CORE)) interest -= (int) DEFAULT_PATHER_INTEREST;
				else if (industry.getId().equals(NODE)
							|| industry.getId().equals(PLAYER_CORE)
							|| industry.getId().equals(PLAYER_NODE)) interest += 0;
				else interest -= (int) industry.getPatherInterest();
			}
		}
		return interest;
	}
	
	private boolean panopticonIsActiveCheck () {
		MarketAPI m = market;
		boolean isMonitored = true;
		if (m == null || m.getFactionId() == null || !m.getFactionId().equals(IX_FAC_ID) || !m.hasIndustry(FCOMM)) isMonitored = false;
		else isMonitored = m.getIndustry(FCOMM).isFunctional() && IX_PLAYER_CORE_ID.equals(m.getIndustry(FCOMM).getAICoreId());
		return isMonitored;
	}
	
	public boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	public void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			float opad = 10f;
			tooltip.addPara("Eliminates Pather cells", opad);
			tooltip.addPara("Stability bonus: %s", opad, Misc.getHighlightColor(), "+" + STABILITY_BONUS);
			addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS_NODE, (String[])null);
		}
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
	public boolean canImprove() {
		return false;
	}
	
	@Override
	public boolean canInstallAICores() {
		return false;
	}
	
	@Override
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level.next();
	}

	@Override
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level.next();
	}
}
