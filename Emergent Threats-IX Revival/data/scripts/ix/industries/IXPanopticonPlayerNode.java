package data.scripts.ix.industries;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.util.PanopticonStructureUtil;

public class IXPanopticonPlayerNode extends BaseIndustry {

	private static float DEFAULT_PATHER_INTEREST = 4f;
	private static float DEFENSE_BONUS_NODE = 0.5f;
	private static int STABILITY_BONUS = 5;
	private static String CORE = "ix_panopticon";
	private static String NODE = "ix_panopticon_node";
	private static String PLAYER_CORE = "ix_panopticon_player_core";	//structure id
	private static String PLAYER_NODE = "ix_panopticon_player_node";	//structure id
	private static String IX_PLAYER_CORE_ID = "ix_panopticon_instance";	//core id
	private static String MONITORED_VERTEX = "ix_monitored";			//condition id
	private static String MONITORED_PLAYER = "ix_monitored_player";		//condition id
	
	@Override
	public void apply() {
		super.apply(false);
		if (market.hasCondition(MONITORED_VERTEX)) return;
		if (isFunctional()) {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + DEFENSE_BONUS_NODE, getNameForModifier());
			market.suppressCondition(Conditions.PIRATE_ACTIVITY);
			if (!market.hasCondition(MONITORED_VERTEX) && !market.hasCondition(MONITORED_PLAYER)) {
				market.addCondition(MONITORED_PLAYER);
				market.getStability().modifyFlat(id, STABILITY_BONUS, "Panopticon monitoring");
			}
			PanopticonStructureUtil.applyBlackMarketChange(market, "apply");
		}
		else unapply();
	}
	
	@Override
	public void unapply() {
		super.unapply();
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		market.unsuppressCondition(Conditions.PIRATE_ACTIVITY);
		if (market.hasIndustry(PLAYER_CORE) && !market.getIndustry(PLAYER_CORE).isHidden()) return;
		market.removeCondition(MONITORED_PLAYER);
		market.getStability().unmodify(id);
		PanopticonStructureUtil.applyBlackMarketChange(market, "unapply");
	}
	
	@Override
	public boolean isDisrupted() {
		if (isHidden()) return true;
		String key = getDisruptedKey();
		return market.getMemoryWithoutUpdate().is(key, true);
	}
	
	//classic panopticon overrides player version when both are present
	@Override
	public boolean isHidden() {
		boolean hidden = false;
		if (market.hasIndustry(CORE) && !market.getIndustry(CORE).isHidden()) hidden = true;
		else if (market.hasIndustry(NODE) && !market.getIndustry(NODE).isHidden()) hidden = true;
		else if (market.hasIndustry(PLAYER_CORE) && !market.getIndustry(PLAYER_CORE).isHidden()) hidden = true;
		else if (!isFunctional()) hidden = true;
		if (hidden) unapply();
		return hidden;
	}
	
	@Override
	public boolean isFunctional() {
		return PanopticonStructureUtil.panopticonIsActiveCheck(market, false);
	}

	@Override
	public float getPatherInterest() {
		float interest = DEFAULT_PATHER_INTEREST;
		boolean hasIndEvo = Global.getSettings().getModManager().isModEnabled("IndEvo");
		if (market.hasIndustry("BOGGLED_CHAMELEON") 
					&& (Commodities.ALPHA_CORE).equals(market.getIndustry("BOGGLED_CHAMELEON").getAICoreId())) return 0f;
		if (isFunctional()) {
			for (Industry industry : market.getIndustries()) {
				if (hasIndEvo && industry.getCurrentName().equals("Monastic Order")) return 0f;
				if (industry.getId().equals(PLAYER_NODE)) interest -= (int) DEFAULT_PATHER_INTEREST;
				else if (industry.getId().equals(CORE)
							|| industry.getId().equals(NODE)
							|| industry.getId().equals(PLAYER_CORE)) interest += 0;
				else interest -= (int) industry.getPatherInterest();
			}
		}
		return interest;
	}
	
	@Override
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
