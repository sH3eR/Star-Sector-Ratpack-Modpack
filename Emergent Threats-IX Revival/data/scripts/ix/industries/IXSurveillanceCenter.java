package data.scripts.ix.industries;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class IXSurveillanceCenter extends BaseIndustry {

	private static int STABILITY_BONUS = 2;
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String HG_FAC_ID = "ix_core";
	private static String TW_FAC_ID = "ix_trinity";
	private static String IX_PLAYER_CORE_ID = "ix_panopticon_instance";
	private static String MONITORED_VERTEX = "ix_monitored";
	private static String MONITORED_PLAYER = "ix_monitored_player";
	private static String ACADEMY_ID = "tw_cloudburst_academy";
	private static String EMBASSY_ID = "tw_fleet_embassy";
	private static String EMBASSY_PLAYER_ID = "ix_embassy_player";
	private static String SPY_PLAYER_ID = "ix_surveillance_center";
	
	private static String VERTEX_CORE = "ix_panopticon";
	private static String VERTEX_NODE = "ix_panopticon_node";
	private static String PLAYER_CORE = "ix_panopticon_player_core";
	private static String PLAYER_NODE = "ix_panopticon_player_node";
	
	@Override
	public void apply() {
		super.apply(true);
		applyIncomeAndUpkeep(3);
		
		modifyStabilityWithBaseMod();
		
		int size = market.getSize();
		demand(Commodities.SUPPLIES, size);
		demand(Commodities.CREW, size);
		demand(Commodities.MARINES, size);
		
		//if (isInstanceInstalled() && isFunctional()) PanopticStructureUtil.addGreenNodesToSystem(market);
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}
	
	@Override
	public void unapply() {
		super.unapply();
		unmodifyStabilityWithBaseMod();
	}
	
	private boolean isInstanceInstalled() {
		return IX_PLAYER_CORE_ID.equals(market.getIndustry(id).getAICoreId());
	}
	
	@Override
	public boolean isAvailableToBuild() {
		if (!market.hasIndustry(EMBASSY_ID)
				&& !market.hasIndustry(ACADEMY_ID)
				&& !market.hasIndustry(EMBASSY_PLAYER_ID)) {
			return true;
		}
		else return false;
	}

	@Override
	public float getPatherInterest() {
		if (market.hasIndustry("BOGGLED_CHAMELEON")	
					&& (Commodities.ALPHA_CORE).equals(market.getIndustry("BOGGLED_CHAMELEON").getAICoreId())) return 0f;
				
		if (market.hasIndustry(PLAYER_CORE)
					|| market.hasIndustry(PLAYER_NODE)
					|| market.hasIndustry(VERTEX_CORE)
					|| market.hasIndustry(VERTEX_NODE)) return 0f;
					
		float interest = 0f;
		for (Industry industry : market.getIndustries()) {
			if (industry.getId().equals(SPY_PLAYER_ID)) interest += 0;
			else interest -= (int) industry.getPatherInterest();
		}
		return interest;
	}
	
	public boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	public void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			float opad = 10f;
			if (isInstanceInstalled()) tooltip.addPara("Projecting %s across system", opad, Misc.getHighlightColor(), "Panopticon Monitoring");
			else {
				tooltip.addPara("Eliminates Pather cells", opad);
				tooltip.addPara("Stability bonus: %s", opad, Misc.getHighlightColor(), "+" + STABILITY_BONUS);
				tooltip.addPara("Can install a %s", opad, Misc.getHighlightColor(), "Panopticon Instance");
			}
		}
	}
	
	@Override
	protected int getBaseStabilityMod() {
		if (!market.hasCondition(MONITORED_PLAYER) && !market.hasCondition(MONITORED_VERTEX)) return STABILITY_BONUS;
		else return 0;
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
		return true;
	}
	
	@Override
	protected void applyAICoreModifiers() {
		if (aiCoreId == null) {
			applyNoAICoreModifiers();
			return;
		}
		boolean alpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean gamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		boolean instance = aiCoreId.equals(IX_PLAYER_CORE_ID); 
		if (alpha || instance) applyAlphaCoreModifiers();
		else if (beta) applyBetaCoreModifiers();
		else if (gamma) applyGammaCoreModifiers();
	}
	
	@Override
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		String coreId = isInstanceInstalled() ? "Panopticon instance" : "Alpha core";
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, coreId);
	}
	
	@Override
	protected void updateAICoreToSupplyAndDemandModifiers() {
		if (aiCoreId == null) return;
		
		boolean alpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean gamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		boolean instance = aiCoreId.equals(IX_PLAYER_CORE_ID);
		
		if (alpha || instance) applyAlphaCoreSupplyAndDemandModifiers();
		else if (beta) applyBetaCoreSupplyAndDemandModifiers();
		else if (gamma) applyGammaCoreSupplyAndDemandModifiers();
	}
	
	@Override
	public void addAICoreSection(TooltipMakerAPI tooltip, String coreId, AICoreDescriptionMode mode) {
		float opad = 10f;

		Color color = market.getFaction().getBaseUIColor();
		Color dark = market.getFaction().getDarkUIColor();
		
		if (mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
			if (coreId == null) {
				tooltip.addPara("No AI core currently assigned. Click to assign an AI core from your cargo.", opad);
				return;
			}
		}
		
		boolean alpha = coreId.equals(Commodities.ALPHA_CORE); 
		boolean beta = coreId.equals(Commodities.BETA_CORE); 
		boolean gamma = coreId.equals(Commodities.GAMMA_CORE);
		boolean instance = coreId.equals(IX_PLAYER_CORE_ID);
		
		if (alpha) addAlphaCoreDescription(tooltip, mode);
		else if (beta) addBetaCoreDescription(tooltip, mode);
		else if (gamma)	addGammaCoreDescription(tooltip, mode);
		else if (instance) addInstanceDescription(tooltip, mode);
		else addUnknownCoreDescription(coreId, tooltip, mode);
	}
	
	protected void addInstanceDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Panopticon Instance currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Panopticon Instance. ";
		}
		
		String monitor = "Panopticon Monitoring";
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s, demand by %s unit. " +
					"Extends %s to every in-faction colony in this system.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%",
					"" + DEMAND_REDUCTION,
					monitor);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s, demand by %s unit. " +
				"Extends %s to every in-faction colony in this system.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%",
				"" + DEMAND_REDUCTION,
				monitor);
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