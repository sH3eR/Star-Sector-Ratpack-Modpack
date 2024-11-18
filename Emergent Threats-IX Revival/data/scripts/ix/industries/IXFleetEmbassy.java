package data.scripts.ix.industries;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
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

//code is used for Fleet Embassy, Fleet Embassy (Emerald Station), Couldburst Academy
public class IXFleetEmbassy extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

	private static float OFFICER_PROB = 0.2f;
	private static float DEFENSE_BONUS = 0.2f;
	private static float OFFICER_PROB_MOD = 0.3f;
	private static int STABILITY_BONUS = 2;
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String HG_FAC_ID = "ix_core";
	private static String TW_FAC_ID = "ix_trinity";
	private static String MZ_FAC_ID = "ix_marzanna";
	private static String IX_PLAYER_CORE_ID = "ix_panopticon_instance";
	private static String MONITORED_VERTEX = "ix_monitored";
	private static String MONITORED_PLAYER = "ix_monitored_player";
	private static String ACADEMY_ID = "tw_cloudburst_academy";
	private static String EMBASSY_ID = "tw_fleet_embassy";
	private static String EMBASSY_PLAYER_ID = "ix_embassy_player";
	private static String SPY_CENTER_ID = "ix_surveillance_center";

	private boolean isInstanceInstalled() {
		return IX_PLAYER_CORE_ID.equals(market.getIndustry(id).getAICoreId());
	}

	@Override
	public boolean isHidden() {
		//for handling TW structures only
		FactionAPI ix = Global.getSector().getFaction(IX_FAC_ID);
		if (this.id.equals(ACADEMY_ID) || this.id.equals(EMBASSY_ID)) {
			if (!TW_FAC_ID.equals(market.getFactionId())) return true; //appears only under Trinity Worlds rule
			if (ix.getRelationship(TW_FAC_ID) <= -0.25) return true; //appears only when IX is friendly to TW
		}
		return false;
	}
	
	@Override
	public boolean isFunctional() {
		if (isDisrupted()) return false;
		if (isBuilding() || isUpgrading()) return false;
		return (!isHidden());
	}
	
	@Override
	//For player embassy, can build only with background or after quest complete, other variants cannot be built
	public boolean isAvailableToBuild() {
		if (!this.id.equals(EMBASSY_PLAYER_ID)) return false;
		if (TW_FAC_ID.equals(market.getFactionId()) 
					|| MZ_FAC_ID.equals(market.getFactionId())
					|| IX_FAC_ID.equals(market.getFactionId())) return false;
		boolean canBuild = Global.getSector().getMemoryWithoutUpdate().is("$can_build_embassy", true);
		if (!canBuild) return false;
		
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
		for (MarketAPI mkt : markets) {
			if (mkt.hasIndustry(EMBASSY_PLAYER_ID)) {
				canBuild = false;
				break;
			}
		}
		
		if (!canBuild) return false;
		
		if (market.hasIndustry(EMBASSY_ID)	
					|| market.hasIndustry(ACADEMY_ID) 
					|| market.hasIndustry(SPY_CENTER_ID)) canBuild = false;
		return canBuild;
	}
	
	@Override
	public boolean showWhenUnavailable() {
		if (EMBASSY_PLAYER_ID.equals(id)) return true;
		else return false;
	}
	
	@Override
	public String getUnavailableReason() {
		if (!isAvailableToBuild()) return "Requires IX Battlegroup permission. Only 1 can be built.";
		return "";
	}
	
	public void apply() {
		super.apply(true);
		applyIncomeAndUpkeep(3);
		
		int size = market.getSize();
		int light = 1;
		int medium = 1;
		int heavy = 1;
		
		if (size <= 3) {
			light = 1;
			medium = 1;
			heavy = 1;
		}
		else if (size == 4) {
			light = 2;
			medium = 2;
			heavy = 1;
		}
		else if (size == 5) {
			light = 2;
			medium = 2;
			heavy = 2;
		}
		else if (size == 6) {
			light = 3;
			medium = 3;
			heavy = 2;
		}
		else if (size == 7) {
			light = 3;
			medium = 3;
			heavy = 3;
		}
		else if (size >= 8) {
			light = 3;
			medium = 4;
			heavy = 3;
		}
		
		if (this.id.equals(EMBASSY_PLAYER_ID)) {
			light = 0;
			medium = 0;
			heavy = 0;
			
			FactionAPI ix = Global.getSector().getFaction(IX_FAC_ID);
			String thisFactionId = market.getFactionId();
			if ((ix.getRelationship(thisFactionId) >= 0.75f)) heavy = 1;
			if ((ix.getRelationship(thisFactionId) >= 0.50f)) medium = 1;
			if ((ix.getRelationship(thisFactionId) >= 0.25f)) light = 1;
		}
		
		else if (this.id.equals(EMBASSY_ID)) {
			if (market.hasIndustry(Industries.MILITARYBASE) || market.hasIndustry(Industries.HIGHCOMMAND)) {
				light = 0;
				medium = 1;
				heavy = 1;
			}
		}
		
		if (isHidden()) {
				light = 0;
				medium = 0;
				heavy = 0;
		}
		
		DynamicStatsAPI dynamic = market.getStats().getDynamic();
		
		dynamic.getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), light);
		dynamic.getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), medium);
		dynamic.getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), heavy);
		
		if (!id.equals(EMBASSY_PLAYER_ID)) {
			demand(Commodities.SUPPLIES, size + 1);
			demand(Commodities.CREW, size + 1);
			demand(Commodities.FUEL, size + 1);
			demand(Commodities.SHIPS, size + 1);
		}
		else {
			demand(Commodities.SUPPLIES, size);
			demand(Commodities.FUEL, size - 1);
		}
		
		if (id.equals(ACADEMY_ID)) {
			supply(Commodities.MARINES, size);
		}
		
		modifyStabilityWithBaseMod();
		
		float mult = getDeficitMult(Commodities.SUPPLIES);
		String extra = "";
		if (mult != 1) {
			String com = (String) getMaxDeficit(Commodities.SUPPLIES).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}
		
		if (this.id.equals(ACADEMY_ID)) {
			dynamic.getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), 1f + DEFENSE_BONUS * mult, getNameForModifier() + extra);
		}
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
		
		dynamic.getMod(Stats.OFFICER_PROB_MOD).modifyFlat(getModId(0), OFFICER_PROB);		
		
		//if (isInstanceInstalled() && isFunctional()) PanopticStructureUtil.addGreenNodesToSystem(market);
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
	}

	@Override
	public void unapply() {
		super.unapply();
		
		MemoryAPI memory = market.getMemoryWithoutUpdate();
		Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
		Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
		unmodifyStabilityWithBaseMod();
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(getModId());
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).unmodifyFlat(getModId(0));
		
	}
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			float opad = 10f;
			if (!this.id.equals(EMBASSY_PLAYER_ID)) {
				String allies = this.id.equals(EMBASSY_ID) ? "IX Battlegroup Honor Guard" : "IX Battlegroup";
				tooltip.addPara("Deploys allied %s patrols", opad, Misc.getHighlightColor(), allies);
				if (this.id.equals(ACADEMY_ID))	addGroundDefensesImpactSection(tooltip, DEFENSE_BONUS, Commodities.SUPPLIES);
			}
			else {
				String allies = isInstanceInstalled() ? "Honor Guard fleet" : "IX Battlegroup fleet";	
				tooltip.addPara("The faction that owns this colony can raise its reputation above %s to the %s, and will gain %s reputation each month when below %s. On reaching %s relations, an %s will arrive to guard this colony. At %s and %s relations, receive another fleet of larger size.", opad, Misc.getHighlightColor(), 
				"hostile",
				"IX Battlegroup",
				"10",
				"35",
				"welcoming",
				allies,
				"friendly",
				"cooperative");
				tooltip.addPara("If built on a player owner colony while %s, reputation effects will apply to the commissioned faction.", opad, Misc.getHighlightColor(), "commissioned");
			}
			if (isInstanceInstalled()) tooltip.addPara("Projecting %s across system", opad, Misc.getHighlightColor(), "Panopticon Monitoring");
			else tooltip.addPara("Can install a %s", opad, Misc.getHighlightColor(), "Panopticon Instance");

		}
	}
	
	@Override
	protected int getBaseStabilityMod() {
		if (!market.hasCondition(MONITORED_PLAYER) && !market.hasCondition(MONITORED_VERTEX)) return STABILITY_BONUS;
		else return 0;
	}
	
	public String getNameForModifier() {
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
		return false;
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
		if (!isFunctional()) return;
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
		SectorAPI sector = Global.getSector();
		String factionId = TW_FAC_ID;
		
		//honor guard medium spawns for TW embassy, need to change faction after spawn due to naming issue
		if (id.equals(EMBASSY_ID) && type == PatrolType.COMBAT) factionId = HG_FAC_ID;
		
		//honor guard medium/heavy spawns for player embassy with core, otherwise normal ix fleets
		if (id.equals(EMBASSY_PLAYER_ID)) {
			if (isInstanceInstalled()) factionId = HG_FAC_ID;
			else factionId = IX_FAC_ID;
		}
		
		//mixed fleets for cloudburst academy, honor guard done later due to naming issues
		else if (id.equals(ACADEMY_ID)) {
			if (type == PatrolType.HEAVY && Math.random() < 0.5f) factionId = IX_FAC_ID;
			else if (type == PatrolType.COMBAT && Math.random() < 0.3f) factionId = IX_FAC_ID;
			else if (type == PatrolType.FAST && Math.random() < 0.2f) factionId = IX_FAC_ID;
		}
		
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
		
		//change faction to avoid "Honor Guard Honor Guard" and attacking fleets neutral to market
		if (fleet.getFaction().getId().equals(HG_FAC_ID)) {
			fleet.setFaction(IX_FAC_ID, true);
			if (!TW_FAC_ID.equals(market.getFactionId())) fleet.setFaction(market.getFactionId(), false);
			fleet.setNoFactionInName(true);
			fleet.setName("Honor Guard Delegation");
			for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
				DModHandler.clearDModsFromFleetMember(member);
			}
		}
		else if (fleet.getFaction().getId().equals(IX_FAC_ID)) {
			if (!TW_FAC_ID.equals(market.getFactionId())) fleet.setFaction(market.getFactionId());
			fleet.setNoFactionInName(true);
			fleet.setName("IX Battlegroup Delegation");
			for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
				DModHandler.clearDModsFromFleetMember(member);
			}
		}
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		if (!fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PATROLS_HAVE_NO_PATROL_MEMORY_KEY)) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
			if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
			}
		}
		
		else if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
			if (market != null && market.isHidden()) fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);
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
	public boolean canInstallAICores() {
		return true;
	}
	
	public static float ALPHA_CORE_BONUS = 0.25f;
	@Override
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(
				getModId(), 1f + ALPHA_CORE_BONUS, getNameForModifier());
	}
	
	protected void applyInstanceCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(
				getModId(), 1f + ALPHA_CORE_BONUS, getNameForModifier());
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
		if (alpha) applyAlphaCoreModifiers();
		else if (beta) applyBetaCoreModifiers();
		else if (gamma) applyGammaCoreModifiers();
		else if (instance) applyInstanceCoreModifiers();
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId());
	}
	
	@Override
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
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

		FactionAPI faction = market.getFaction();
		Color color = faction.getBaseUIColor();
		Color dark = faction.getDarkUIColor();
		
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
		else if (instance && id.equals(EMBASSY_PLAYER_ID)) addPlayerEmbassyInstanceDescription(tooltip, mode);
		else if (instance) addInstanceDescription(tooltip, mode);
		else addUnknownCoreDescription(coreId, tooltip, mode);
	}
		
	protected void addPlayerEmbassyInstanceDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Panopticon Instance currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Panopticon Instance. ";
		}
		
		String monitor = "Panopticon Monitoring";
		String honorGuard = "Honor Guard";
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s, demand by %s unit. " +
					"Improve allied patrols to %s ships. Extends %s to colonies in system.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%",
					"" + DEMAND_REDUCTION,
					honorGuard,
					monitor);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s, demand by %s unit. " +
				"Improve allied patrols to %s ships. Extends %s to colonies in system.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%",
				"" + DEMAND_REDUCTION,
				honorGuard,
				monitor);
	}
	
	protected void addInstanceDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Panopticon Instance currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Panopticon Instance. ";
		}
		
		float a = ALPHA_CORE_BONUS;
		String str = Strings.X + (1f + a);
		String monitor = "Panopticon Monitoring";
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s, demand by %s unit. " +
					"Increases fleet size by %s. Extends %s to every in-faction colony in this system.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					str,
					monitor);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s, demand by %s unit. " +
				"Increases fleet size by %s. Extends %s to every in-faction colony in this system.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				str,
				monitor);
	}
}