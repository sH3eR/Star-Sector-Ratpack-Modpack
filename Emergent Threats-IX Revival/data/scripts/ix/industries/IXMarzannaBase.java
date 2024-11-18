package data.scripts.ix.industries;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import data.hullmods.ix.DModHandler;
import lunalib.lunaSettings.LunaSettings;

//code is used for Marzanna Cartel HQ, Tributary Port, Fleet Command (Vertex Station)
public class IXMarzannaBase extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

	private static float OFFICER_PROB = 0.2f;
	private static float DEFENSE_BONUS = 0.2f;
	private static int STABILITY_BONUS = 2; //no bonus for Tributary Port
	private static String CARTEL_BASE_ID = "ix_marzanna_base";
	private static String CARTEL_CONDITION = "ix_cartel_activity";
	private static String MARZANNA_ID = "ix_marzanna";
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String IX_HONOR_ID = "ix_core";
	
	//patrol code reused for Tributary Port
	private static String TRIBUTE_ID = "tw_tributary_port";			//structure id
	private static String TW_FAC_ID = "ix_trinity";					//faction id
	private static String ALPHA_ID = "alpha_core";
	private static String PANOP_ID = "ix_panopticon_core";
	private static String INSTANCE_ID = "ix_panopticon_instance";
	private static float BASIC_TRIBUTE_BONUS = 0.05f;
	private static float HIGH_TRIBUTE_BONUS = 0.10f;
	
	//partial code reuse for Fleet Command (Vertex Station)
	private static String FCOMM_ID = "ix_fleet_command";
	private static String IX_CORE = "ix_panopticon";
	private static String IX_NODE = "ix_panopticon_node";
	private static String PLAYER_CORE = "ix_panopticon_player_core";
	private static String PLAYER_NODE = "ix_panopticon_player_node";
	private static float INSTANCE_FLEET_BONUS = 0.25f;
	private static float OFFICER_PROB_MOD_HIGH_COMMAND = 0.3f;
	private static float DEFENSE_BONUS_COMMAND = 0.3f;
	private static int IMPROVE_NUM_PATROLS_BONUS = 1;
	private static String CORONAL_CONDITION = "aotd_coronal_market_cond";
	
	@Override
	public boolean isHidden() {
		if (this.id.equals(FCOMM_ID) && (market.hasIndustry(Industries.PATROLHQ) 
				|| market.hasIndustry(Industries.MILITARYBASE) 
				|| market.hasIndustry(Industries.HIGHCOMMAND))) return true;
		if (this.id.equals(TRIBUTE_ID)) {
			if (TW_FAC_ID.equals(market.getFactionId())) return true;			
			else return (market.getFaction().getRelationship(TW_FAC_ID) < 0.25f);
		}
		else return (market.hasIndustry(Industries.MILITARYBASE) 
				|| market.hasIndustry(Industries.HIGHCOMMAND));
	}	
	
	@Override
	public boolean isFunctional() {
		if (isDisrupted()) return false;
		if (isBuilding() || isUpgrading()) return false;
		if (this.id.equals(FCOMM_ID) && (market.hasIndustry(Industries.PATROLHQ) 
				|| market.hasIndustry(Industries.MILITARYBASE) 
				|| market.hasIndustry(Industries.HIGHCOMMAND))) return false;
		if (this.id.equals(TRIBUTE_ID)) {
			if (TW_FAC_ID.equals(market.getFactionId())) return false;
			else return (market.getFaction().getRelationship(TW_FAC_ID) >= 0.25f);
		}
		else return (!market.hasIndustry(Industries.MILITARYBASE) 
				&& !market.hasIndustry(Industries.HIGHCOMMAND));
	}
	
	public void apply() {
		super.apply(true);
		applyIncomeAndUpkeep(3);
		
		int size = market.getSize();
		int light = 1;
		int medium = 0;
		int heavy = 0;
		
		DynamicStatsAPI dynamic = market.getStats().getDynamic();
		
		//Fleet Command changes done entirely within this if block
		if (this.id.equals(FCOMM_ID)) {
			int extraDemand = 3;
			
			if (size <= 5) {
				light = 2;
				medium = 2;
				heavy = 1;
			}
			else if (size <= 7) {
				light = 3;
				medium = 3;
				heavy = 2;
			}
			else if (size <= 10) {
				light = 4;
				medium = 4;
				heavy = 3;
			}
			
			dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), light);
			dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), medium);
			dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), heavy);
			
			demand(Commodities.SUPPLIES, size - 1 + extraDemand);
			demand(Commodities.FUEL, size - 1 + extraDemand);
			demand(Commodities.SHIPS, size - 1 + extraDemand);
		
			supply(Commodities.CREW, size);
			supply(Commodities.MARINES, size);
			
			market.getStability().modifyFlat(getModId(), STABILITY_BONUS, "Fleet command HQ");
			
			float mult = getDeficitMult(Commodities.SUPPLIES);
			String extra = "";
			if (mult != 1) {
				String com = "" + getMaxDeficit(Commodities.SUPPLIES).one;
				extra = " (" + getDeficitText(com).toLowerCase() + ")";
			}
			float bonus = DEFENSE_BONUS_COMMAND;
			
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);

			float officerProb = OFFICER_PROB_MOD_HIGH_COMMAND;
			market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).modifyFlat(getModId(0), officerProb);
			
			//core structure is installed by system creator to avoid concurrency error
			if (isFunctional() && isInstanceInstalled()) {
				List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
				markets.remove(market);
				for (MarketAPI m : markets) {
					if (m.getFactionId().equals(IX_FAC_ID) 
								&& (!m.hasCondition(CARTEL_CONDITION))
								&& (!m.hasCondition(CORONAL_CONDITION))) {
						if (!m.hasIndustry(IX_NODE) && !m.hasIndustry(IX_CORE)) m.addIndustry(IX_NODE);
					}
				}
				market.addCondition("ix_monitored");
			}
			
			MemoryAPI memory = market.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
			Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
			
			if (!isFunctional()) {
				supply.clear();
				unapply();
			}
			return;
		}
		
		if (this.id.equals(TRIBUTE_ID)) {
			light = 0;
			float bonus = 0;
			if (market.getFaction().getRelationship(TW_FAC_ID) >= 0.75f) {
				heavy = 1;
				bonus = 0.15f;
			}
			else if (market.getFaction().getRelationship(TW_FAC_ID) >= 0.50f) {
				medium = 1;
				bonus = 0.10f;
			}
			else if (market.getFaction().getRelationship(TW_FAC_ID) >= 0.25f) {
				light = 1;
				bonus = 0.05f;
			}
			
			if (ALPHA_ID.equals(market.getIndustry(id).getAICoreId())) {
				bonus += BASIC_TRIBUTE_BONUS;
				medium++;
			}
			
			else if (isInstanceInstalled()) {
				bonus += HIGH_TRIBUTE_BONUS;
				heavy++;
			}
			
			else if (isPanopInstalled()) {
				bonus += BASIC_TRIBUTE_BONUS;
				light++;
			}
			
			dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), light);
			dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), medium);
			dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), heavy);
			
			demand(Commodities.SUPPLIES, size);
			demand(Commodities.FUEL, size - 1);
			
			market.getIncomeMult().modifyMult(getModId(0), 1 + bonus, getNameForModifier());
			market.getAccessibilityMod().modifyFlat(getModId(0), bonus, getNameForModifier());
			
			MemoryAPI memory = market.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
			Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
			if (!isFunctional()) {
				supply.clear();
				unapply();
			}
			
			return;
		}
		
		if (size <= 3) {
			light = 1;
			medium = 0;
			heavy = 0;
		}
		else if (size == 4) {
			light = 2;
			medium = 0;
			heavy = 0;
		}
		else if (size == 5) {
			light = 3;
			medium = 0;
			heavy = 0;
		}
		else if (size == 6) {
			light = 3;
			medium = 1;
			heavy = 0;
		}
		else if (size == 7) {
			light = 3;
			medium = 2;
			heavy = 0;
		}
		else if (size >= 8) {
			light = 3;
			medium = 3;
			heavy = 0;
		}
		
		if (MARZANNA_ID.equals(market.getPlanetEntity().getFaction())) heavy = 1; 
		
		dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), light);
		dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), medium);
		dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), heavy);
		
		demand(Commodities.SUPPLIES, size);
		demand(Commodities.FUEL, size);
		demand(Commodities.SHIPS, size);
		
		supply(Commodities.CREW, size - 1);
		supply(Commodities.MARINES, size - 1);
		supply(Commodities.DRUGS, size - 3);
		supply(Commodities.ORGANS, size - 4);
		
		market.getStability().modifyFlat(getModId(), STABILITY_BONUS, "Marzanna cartel HQ");
		
		float mult = getDeficitMult(Commodities.SUPPLIES);
		String extra = "";
		if (mult != 1) {
			String com = (String) getMaxDeficit(Commodities.SUPPLIES).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}

		dynamic.getMod(Stats.GROUND_DEFENSES_MOD)
				.modifyMult(getModId(), 1f + DEFENSE_BONUS * mult, getNameForModifier() + extra);
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
		dynamic.getMod(Stats.OFFICER_PROB_MOD).modifyFlat(getModId(0), OFFICER_PROB);		
		
		if (market.getSubmarket(Submarkets.SUBMARKET_BLACK) != null) {
			market.getSubmarket(Submarkets.SUBMARKET_BLACK).setFaction(Global.getSector().getFaction("ix_marzanna"));
		}
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}

	@Override
	public void unapply() {
		super.unapply();
		
		DynamicStatsAPI dynamic = market.getStats().getDynamic();
		
		//Fleet Command changes done entirely within if block
		if (this.id.equals(FCOMM_ID)) {
			MemoryAPI memory = market.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
			Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
			market.getStability().unmodifyFlat(getModId());
			dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(getModId());
			dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(getModId());
			dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(getModId());
			dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
			dynamic.getMod(Stats.OFFICER_PROB_MOD).unmodifyFlat(getModId(0));
			market.removeCondition("ix_monitored");
			return;
		}
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
		
		//unmodifyStabilityWithBaseMod();
		
		dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(getModId());
		dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(getModId());
		dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(getModId());
		
		if (this.id.equals(TRIBUTE_ID)) {
			market.getIncomeMult().unmodifyMult(getModId(0));
			market.getAccessibilityMod().unmodifyFlat(getModId(0));
			return;
		}
		
		dynamic.getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		dynamic.getMod(Stats.OFFICER_PROB_MOD).unmodifyFlat(getModId(0));
		
		//market.removeCondition(CARTEL_CONDITION);
		
		if (market.getSubmarket(Submarkets.SUBMARKET_BLACK) != null) {
			market.getSubmarket(Submarkets.SUBMARKET_BLACK).setFaction(Global.getSector().getFaction("pirates"));
		}
	}
	
	@Override
	//For player embassy, can build only after quest complete, other variants cannot be built
	public boolean isAvailableToBuild() {
		if (!this.id.equals(TRIBUTE_ID)) return false;
		if (TW_FAC_ID.equals(market.getFactionId())) return false;
		return (market.getFaction().getRelationship(TW_FAC_ID) >= 0.25f);
	}
	
	@Override
	public boolean showWhenUnavailable() {
		if (TRIBUTE_ID.equals(id)) return true;
		else return false;
	}
	
	@Override
	public String getUnavailableReason() {
		if (TW_FAC_ID.equals(market.getFactionId())) return "Cannot be built on Trinity Worlds colony.";
		return "Trinity Worlds attitude must be at or above Welcoming.";
	}
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		if (TRIBUTE_ID.equals(id)) return false;
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (TRIBUTE_ID.equals(id)) {
			float opad = 10f;
			tooltip.addPara("Upon reaching %s relations with the %s, a fleet will arrive to guard this colony. The size of this fleet will increase at %s and %s relations. Also gain a cumulative %s bonus to colony income and accessibility for each reputation rank that has been obtained.", opad, Misc.getHighlightColor(), 
			"welcoming", 
			"Trinity Worlds", 
			"friendly", 
			"cooperative",
			"5%");
			return;
		}
		
		else if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			addStabilityPostDemandSection(tooltip, hasDemand, mode);
			addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS, Commodities.SUPPLIES);
		}
	}
	
	@Override
	public String getNameForModifier() {
		if (getSpec().getName().contains("HQ")) return getSpec().getName();
		return Misc.ucFirst(getSpec().getName().toLowerCase());
	}
	
	@Override
	public String getCurrentImage() {
		return super.getCurrentImage();
	}

	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}
	
	@Override
	public boolean canImprove() {
		if (this.id.equals(FCOMM_ID)) return true;
		return false;
	}
	
	protected void applyImproveModifiers() {
		String key = "fleet_command_improve";
		if (isImproved()) {
			market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(key, IMPROVE_NUM_PATROLS_BONUS);
		}
		else market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(key);
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String str = "" + (int) IMPROVE_NUM_PATROLS_BONUS;
		String type = "heavy patrols";
		
		if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) info.addPara("Number of " + type + " launched increased by %s.", 0f, highlight, str);
		else info.addPara("Increases the number of " + type + " launched by %s.", 0f, highlight, str);

		info.addSpacer(opad);
		super.addImproveDesc(info, mode);
	}
	
	private float patrolSpawnInterval = Global.getSettings().getFloat("averagePatrolSpawnInterval");
	protected IntervalUtil tracker = new IntervalUtil(patrolSpawnInterval * 0.7f, patrolSpawnInterval * 1.3f);
	protected float returningPatrolValue = 0f;
	
	@Override
	protected void buildingFinished() {
		super.buildingFinished();
		tracker.forceIntervalElapsed();
	}
	
	@Override
	protected void upgradeFinished(Industry previous) {
		super.upgradeFinished(previous);
		tracker.forceIntervalElapsed();
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		if (Global.getSector().getEconomy().isSimMode()) return;
		if (!isFunctional() || isHidden()) return;
		float days = Global.getSector().getClock().convertToDays(amount);
		
		float spawnRate = 1f;
		float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
		spawnRate *= rateMult;
		
		if (Global.getSector().isInNewGameAdvance()) spawnRate *= 3f;
		
		float extraTime = 0f;
		if (returningPatrolValue > 0) {
			float interval = tracker.getIntervalDuration();
			extraTime = interval * days;
			returningPatrolValue -= days;
			if (returningPatrolValue < 0) returningPatrolValue = 0;
		}
		tracker.advance(days * spawnRate + extraTime);

		if (DebugFlags.FAST_PATROL_SPAWN) tracker.advance(days * spawnRate * 100f);
		
		if (tracker.intervalElapsed()) {
			String sid = getRouteSourceId();
			
			int light = getCount(PatrolType.FAST);
			int medium = getCount(PatrolType.COMBAT);
			int heavy = getCount(PatrolType.HEAVY);

			int maxLight = getMaxPatrols(PatrolType.FAST);
			int maxMedium = getMaxPatrols(PatrolType.COMBAT);
			int maxHeavy = getMaxPatrols(PatrolType.HEAVY);
			
			if (isHidden()) {
				maxLight = 0;
				maxMedium = 0;
				maxHeavy = 0;
			}
			
			WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
			picker.add(PatrolType.HEAVY, maxHeavy - heavy); 
			picker.add(PatrolType.COMBAT, maxMedium - medium); 
			picker.add(PatrolType.FAST, maxLight - light); 
			
			if (picker.isEmpty()) return;
			
			PatrolType type = (PatrolType) picker.pick();
			PatrolFleetData custom = new PatrolFleetData(type);
			
			OptionalFleetData extra = new OptionalFleetData(market);
			extra.fleetType = type.getFleetType();
			
			RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
			extra.strength = (float) getPatrolCombatFP(type, route.getRandom());
			extra.strength = Misc.getAdjustedStrength(extra.strength, market);
			
			float patrolDays = 35f + (float) Math.random() * 10f;
			route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
		}
	}
	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {

	}
	
	public boolean shouldRepeat(RouteData route) {
		return false;
	}
	
	public int getCount(PatrolType ... types) {
		int count = 0;
		for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
			if (data.getCustom() instanceof PatrolFleetData) {
				PatrolFleetData custom = (PatrolFleetData) data.getCustom();
				for (PatrolType type : types) {
					if (type == custom.type) {
						count++;
						break;
					}
				}
			}
		}
		return count;
	}

	public int getMaxPatrols(PatrolType type) {
		DynamicStatsAPI dynamic = market.getStats().getDynamic();
		if (type == PatrolType.FAST) return (int) dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
		if (type == PatrolType.COMBAT) return (int) dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
		if (type == PatrolType.HEAVY) return (int) dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
		return 0;
	}
	
	public String getRouteSourceId() {
		return getMarket().getId() + "_" + "military";
	}
	
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (!isFunctional()) return;
		
		if (reason == FleetDespawnReason.REACHED_DESTINATION) {
			RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
			if (route.getCustom() instanceof PatrolFleetData) {
				PatrolFleetData custom = (PatrolFleetData) route.getCustom();
				if (custom.spawnFP > 0) {
					float fraction  = fleet.getFleetPoints() / custom.spawnFP;
					returningPatrolValue += fraction;
				}
			}
		}
	}
	
	public static int getPatrolCombatFP(PatrolType type, Random random) {
		float combat = 0;
		
		if (type == PatrolType.FAST) combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
		else if (type == PatrolType.COMBAT) combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
		else if (type == PatrolType.HEAVY) combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
		return (int) Math.round(combat);
	}
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		PatrolFleetData custom = (PatrolFleetData) route.getCustom();
		PatrolType type = custom.type;
		Random random = route.getRandom();
		String factionId = MARZANNA_ID;
		
		//spawn different faction ships based on structure id
		if (this.id.equals(TRIBUTE_ID)) factionId = TW_FAC_ID;
		else if (this.id.equals(FCOMM_ID)) {
			PatrolType honorType = ("Challenging").equals(LunaSettings.getString("EmergentThreats_IX_Revival", "ix_difficulty_setting")) ? PatrolType.HEAVY : PatrolType.COMBAT;
			if (market.getFactionId().equals(IX_FAC_ID) && type == honorType) factionId = IX_HONOR_ID;
			else factionId = market.getFactionId();
		}
		else if (this.id.equals(CARTEL_BASE_ID) && type == PatrolType.HEAVY) factionId = IX_FAC_ID;
		
		CampaignFleetAPI fleet = createPatrol(type, factionId, route, market, null, random);
		if (fleet == null || fleet.isEmpty()) return null;
		fleet.addEventListener(this);
		
		market.getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
	
		fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);	
		fleet.addScript(new PatrolAssignmentAIV4(fleet, route));
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);
		
		if (custom.spawnFP <= 0) custom.spawnFP = fleet.getFleetPoints();
		
		return fleet;
	}
	
	public static CampaignFleetAPI createPatrol(PatrolType type, String factionId, RouteData route, MarketAPI market, Vector2f locInHyper, Random random) {
		if (random == null) random = new Random();	
		float combat = getPatrolCombatFP(type, random);
		float tanker = 0f;
		float freighter = 0f;
		String fleetType = type.getFleetType();
		
		FleetParamsV3 params = new FleetParamsV3(
				market, 
				locInHyper,
				factionId,
				route == null ? null : route.getQualityOverride(),
				fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		if (route != null) params.timestamp = route.getTimestamp();
		params.random = random;
		
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet.getFaction().getId().equals(IX_HONOR_ID)) {
			fleet.setFaction(market.getFactionId());
			fleet.setName("Honor Guard");
			for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
				DModHandler.clearDModsFromFleetMember(member);
			}
		}
		
		else if (fleet.getFaction().getId().equals(TW_FAC_ID)) {
			fleet.setFaction(market.getFactionId());
			fleet.setNoFactionInName(true);
			fleet.setName("Trinity Worlds Auxiliaries");
			for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
				DModHandler.clearDModsFromFleetMember(member);
			}
		}
		
		//Marzanna patrols gain behavior of local market faction but keep the fleet name
		else if (fleet.getFaction().getId().equals(MARZANNA_ID)) {
			fleet.setFaction(market.getFactionId());
			fleet.setNoFactionInName(true);
			fleet.setName("Marzanna Cartel Enforcers");
		}
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		if (!fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PATROLS_HAVE_NO_PATROL_MEMORY_KEY)) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
		}
		
		String postId = Ranks.POST_PATROL_COMMANDER;
		String rankId = Ranks.SPACE_COMMANDER;
		if (type == PatrolType.FAST) rankId = Ranks.SPACE_LIEUTENANT;
		else if (type == PatrolType.COMBAT) rankId = Ranks.SPACE_COMMANDER;
		else if (type == PatrolType.HEAVY) rankId = Ranks.SPACE_CAPTAIN;
		
		fleet.getCommander().setPostId(postId);
		fleet.getCommander().setRankId(rankId);
		
		return fleet;
	}

	@Override
	protected int getBaseStabilityMod() {
		if (this.id.equals(TRIBUTE_ID)) return 0;
		else return STABILITY_BONUS;
	}

	@Override
	public boolean canInstallAICores() {
		return true;
	}
	
	public static float ALPHA_CORE_BONUS = 0.25f;
	
	@Override
	protected void applyAICoreModifiers() {
		if (aiCoreId == null) {
			applyNoAICoreModifiers();
			return;
		}
		boolean isAlpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean isBeta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean isGamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		boolean isPanop = aiCoreId.equals(PANOP_ID);
		boolean isInstance = aiCoreId.equals(INSTANCE_ID);

		if (isAlpha || isInstance) applyAlphaCoreModifiers();
		else if (isBeta || isPanop) applyBetaCoreModifiers();
		else if (isGamma) applyGammaCoreModifiers();
		else applyNoAICoreModifiers();
	}
	
	@Override
	protected void applyAlphaCoreModifiers() {
		//tributary port effect done in apply
		if (!this.id.equals(FCOMM_ID)) return;
		String coreName = isInstanceInstalled() ? "Panopticon instance" : "Alpha core";
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(
				getModId(), 1f + ALPHA_CORE_BONUS, coreName + " (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyBetaCoreModifiers() {
		applyNoAICoreModifiers();
	}
	
	@Override
	protected void applyGammaCoreModifiers() {
		applyNoAICoreModifiers();
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		if (!this.id.equals(FCOMM_ID)) return;
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId());
	}
	
	private boolean isInstanceInstalled() {
		return INSTANCE_ID.equals(market.getIndustry(this.id).getAICoreId());
	}
	
	private boolean isPanopInstalled() {
		return PANOP_ID.equals(market.getIndustry(this.id).getAICoreId());
	}
	
	@Override
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		String coreName = "Alpha core";
		if (this.id.equals(FCOMM_ID) && isInstanceInstalled()) coreName = "Panopticon instance";
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, coreName);
	}
	
	@Override
	protected void updateAICoreToSupplyAndDemandModifiers() {
		if (aiCoreId == null) return;
		
		boolean isAlpha = aiCoreId.equals(Commodities.ALPHA_CORE); 
		boolean isBeta = aiCoreId.equals(Commodities.BETA_CORE); 
		boolean isGamma = aiCoreId.equals(Commodities.GAMMA_CORE);
		boolean isInstance = aiCoreId.equals(INSTANCE_ID);
		boolean isPanop = aiCoreId.equals(PANOP_ID);
		
		if (isAlpha || isInstance) applyAlphaCoreSupplyAndDemandModifiers();
		else if (isBeta || isPanop) applyBetaCoreSupplyAndDemandModifiers();
		else if (isGamma) applyGammaCoreSupplyAndDemandModifiers();
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
		boolean instance = coreId.equals(INSTANCE_ID);
		boolean panop = coreId.equals(PANOP_ID);
		
		if (alpha) addAlphaCoreDescription(tooltip, mode);
		else if (beta) addBetaCoreDescription(tooltip, mode);
		else if (gamma)	addGammaCoreDescription(tooltip, mode);
		else if (instance) {
			if (this.id.equals(FCOMM_ID)) addInstanceDescription(tooltip, mode);
			else if (this.id.equals(TRIBUTE_ID)) addAlphaCoreDescription(tooltip, mode, "instance");
			else addUnknownCoreDescription(coreId, tooltip, mode);
		}
		else if (panop) {
			if (this.id.equals(TRIBUTE_ID)) addAlphaCoreDescription(tooltip, mode, "panop");
			else addBetaCoreDescription(tooltip, mode);
		}
		else addUnknownCoreDescription(coreId, tooltip, mode);
	}
	
	//only applies to fleet command
	protected void addInstanceDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Panopticon Instance currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Panopticon Instance. ";
		}
		
		String str = Strings.X + (1f + INSTANCE_FLEET_BONUS);
		String monitor = "Panopticon Monitoring";
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP && market.getFactionId().equals(IX_FAC_ID)) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " 
					+ "Increases fleet size by %s. " 
					+ "Enacts %s on every colony of the IX Battlegroup.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", 
					"" + DEMAND_REDUCTION,
					str,
					monitor);
			tooltip.addImageWithText(opad);
			return;
		}
		
		else if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " 
					+ "Increases fleet size by %s. " 
					+ "Enhanced monitoring %s due to foreign occupation.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", 
					"" + DEMAND_REDUCTION,
					str,
					"disabled");
			tooltip.addImageWithText(opad);
			return;
		}
		
		else if (market.getFactionId().equals(IX_FAC_ID)) {
			tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " 
					+ "Increases fleet size by %s. " 
					+ "Enacts %s on every colony of the IX Battlegroup.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", 
					"" + DEMAND_REDUCTION,
					str,
					monitor);
		}
		
		else tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " 
					+ "Increases fleet size by %s. " 
					+ "Enhanced monitoring %s due to foreign occupation.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", 
					"" + DEMAND_REDUCTION,
					str,
					"disabled");
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		addAlphaCoreDescription(tooltip, mode, "");
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode, String coreType) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Alpha-level AI core currently assigned. ";
		if (coreType.equals("instance") && this.id.equals(TRIBUTE_ID)) pre = "Panopticon Instance currently assigned. ";
		else if (coreType.equals("panop") && this.id.equals(TRIBUTE_ID)) pre = "Panopticon Core currently assigned. ";
		
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Alpha-level AI core. ";
			if (coreType.equals("instance") && this.id.equals(TRIBUTE_ID)) pre = "Panopticon Instance. ";
			else if (coreType.equals("panop") && this.id.equals(TRIBUTE_ID)) pre = "Panopticon Core. ";
		}
		
		//tributary port bonus
		if (this.id.equals(TRIBUTE_ID)) {
			float bonus = coreType.equals("instance") ? HIGH_TRIBUTE_BONUS : BASIC_TRIBUTE_BONUS;
			String fleetSize = "medium";
			if (coreType.equals("instance")) fleetSize = "large";
			else if (coreType.equals("panop")) fleetSize = "small";
			
			if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
				CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
				if (isInstanceInstalled()) coreSpec = Global.getSettings().getCommoditySpec(INSTANCE_ID);
				else if (isPanopInstalled()) coreSpec = Global.getSettings().getCommoditySpec(PANOP_ID);
				TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
				text.addPara(pre + "Reduces demand by %s unit. Further increase income and accessibility by %s. Gain additional %s sized %s auxiliary patrol.", 0f, highlight,
				"" + DEMAND_REDUCTION,
				"" + (int) (bonus * 100) + "%", 
				fleetSize,
				"Trinity Worlds");
				tooltip.addImageWithText(opad);
				return;
			}
			
			tooltip.addPara(pre + "Reduces demand by %s unit. Further increase income and accessibility by %s. Gain additional %s sized %s auxiliary patrol.", 0f, highlight, 
			"" + DEMAND_REDUCTION,
			"" + (int) (bonus * 100) + "%",
			fleetSize,
			"Trinity Worlds");
			return;
		}
		
		float a = ALPHA_CORE_BONUS;
		String str = Strings.X + (1f + a);
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases fleet size by %s.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases fleet size by %s.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				str);
	}
}