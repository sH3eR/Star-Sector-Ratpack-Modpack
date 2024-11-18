package data.campaign.fleets;

import java.util.Random;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.misc.MiscFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerFleetAssignmentAI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import static com.fs.starfarer.api.impl.campaign.fleets.MercFleetManagerV2.*;

public class HMMaxwellResupplyRouteManager extends BaseRouteFleetManager {

	protected StarSystemAPI system;
	
	public HMMaxwellResupplyRouteManager(StarSystemAPI system) {
		super(1f, 14f);
		this.system = system;
	}

	protected String getRouteSourceId() {
		return "salvage_" + system.getId();
	}
	
	protected int getMaxFleets() {
		//if (true) return 0;
		int numRemnantSystems = 1;

		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.getTags().equals(Tags.THEME_REMNANT_SECONDARY)|| system.getTags().equals(Tags.THEME_REMNANT_MAIN) || system.getTags().equals(Tags.THEME_REMNANT_RESURGENT)|| system.getTags().equals(Tags.THEME_REMNANT_MAIN))
			numRemnantSystems++;
		}
		float salvage = getVeryApproximateSalvageValue(system);
		return (int) (1 + Math.min(salvage*numRemnantSystems * 2 , 6));
	}
	
	protected void addRouteFleetIfPossible() {

		Random random = new Random();
		if (TutorialMissionIntel.isTutorialInProgress()) {
			return;
		}

		MarketAPI market = pickSourceMarket();
		if (market == null) return;

		SectorEntityToken to = getMaxwellToken(random);
		if (to == null) return;

		Long seed = new Random().nextLong();
		String id = getRouteSourceId();

		OptionalFleetData extra = new OptionalFleetData(market);
		
		RouteData route = RouteManager.getInstance().addRoute(id, market, seed, extra, this);


		float orbitDays = 4f + (float) Math.random() * 2f;
		float deorbitDays = 3f + (float) Math.random() * 2f;
		float patrolDays = 21f + (float) Math.random() * 9f;

		SectorEntityToken target = to;
		if ((float) Math.random() > 0.15f && to.getStarSystem() != null) {
			if ((float) Math.random() > 0.25f) {
				target = to.getStarSystem().getCenter();
			} else {
				target = to.getStarSystem().getHyperspaceAnchor();
			}
		}

		if (market.getContainingLocation() == to.getContainingLocation() && !market.getContainingLocation().isHyperspace()) {
			route.addSegment(new RouteManager.RouteSegment(ROUTE_PREPARE, orbitDays, market.getPrimaryEntity()));
			route.addSegment(new RouteManager.RouteSegment(ROUTE_PATROL, patrolDays, target));
			route.addSegment(new RouteManager.RouteSegment(ROUTE_STAND_DOWN, deorbitDays, market.getPrimaryEntity()));
		} else {
			route.addSegment(new RouteManager.RouteSegment(ROUTE_PREPARE, orbitDays, market.getPrimaryEntity()));
			route.addSegment(new RouteManager.RouteSegment(ROUTE_TRAVEL, market.getPrimaryEntity(), to));
			route.addSegment(new RouteManager.RouteSegment(ROUTE_PATROL, patrolDays, target));
			route.addSegment(new RouteManager.RouteSegment(ROUTE_RETURN, to, market.getPrimaryEntity()));
			route.addSegment(new RouteManager.RouteSegment(ROUTE_STAND_DOWN, deorbitDays, market.getPrimaryEntity()));
		}
	}
	
	
	public static float getVeryApproximateSalvageValue(StarSystemAPI system) {
		return system.getEntitiesWithTag(Tags.SALVAGEABLE).size();
	}
	
	public MarketAPI pickSourceMarket() {
//		if (true) {
//			return Global.getSector().getEconomy().getMarket("jangala");
//		}

//TODO - Try and figure out a way for these fleets to spawn from Maxwell Stations; annoyingly the RouteManage only works with markets
//This will do for the time being

		WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>();

		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.getContainingLocation() == null) continue;
			if (market.getContainingLocation().isHyperspace()) continue;
			if (market.getFaction().isHostileTo("hmi_maxwell")) continue;
			if (market.isHidden()) continue;

			float distLY = Misc.getDistanceLY(system.getLocation(), market.getLocationInHyperspace());
			float weight = market.getSize();

			float f = Math.max(0.1f, 1f - Math.min(1f, distLY / 20f));
			f *= f;
			weight *= f;

			markets.add(market, weight);
		}

		return markets.pick();
	}

	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = route.getRandom();
		
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		picker.add(FleetTypes.SCAVENGER_SMALL, 10f);
		picker.add(FleetTypes.SCAVENGER_MEDIUM, 15f);
		picker.add(FleetTypes.SCAVENGER_LARGE, 5f);
		
		String type = picker.pick();
		
		boolean pirate = random.nextBoolean();
		CampaignFleetAPI fleet = createMaxwellScavenger(type, system.getLocation(), route, route.getMarket(), pirate, random);
		if (fleet == null) return null;;
		
		fleet.addScript(new HMMaxwellFleetAssignmentAI(fleet, route, pirate));
		
		return fleet;
	}
	
	public static CampaignFleetAPI createMaxwellScavenger(String type, Vector2f locInHyper, MarketAPI source, boolean pirate, Random random) {
		return createMaxwellScavenger(type, locInHyper, null, source, pirate, random);
	}
	public static CampaignFleetAPI createMaxwellScavenger(String type, Vector2f locInHyper, RouteData route, MarketAPI source, boolean pirate, Random random) {
		if (random == null) random = new Random();

		
		if (type == null) {
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			picker.add(FleetTypes.SCAVENGER_SMALL, 10f);
			picker.add(FleetTypes.SCAVENGER_MEDIUM, 15f);
			picker.add(FleetTypes.SCAVENGER_LARGE, 5f);
			type = picker.pick();
		}
		
		
		int combat = 0;
		int freighter = 0;
		int tanker = 0;
		int transport = 0;
		int utility = 0;
		
		
		if (type.equals(FleetTypes.SCAVENGER_SMALL)) {
			combat = random.nextInt(2) + 1;
			tanker = random.nextInt(2) + 1;
			utility = random.nextInt(2) + 1;
		} else if (type.equals(FleetTypes.SCAVENGER_MEDIUM)) {
			combat = 4 + random.nextInt(5);
			freighter = 4 + random.nextInt(5);
			tanker = 3 + random.nextInt(4);
			transport = random.nextInt(2);
			utility = 2 + random.nextInt(3);
		} else if (type.equals(FleetTypes.SCAVENGER_LARGE)) {
			combat = 7 + random.nextInt(8);
			freighter = 6 + random.nextInt(7);
			tanker = 5 + random.nextInt(6);
			transport = 3 + random.nextInt(8);
			utility = 4 + random.nextInt(5);
		}
		
		if (pirate) {
//			combat += transport;
//			combat += utility;
			transport = utility = 0;
		}
		
		combat *= 5f;
		freighter *= 3f;
		tanker *= 3f;
		transport *= 1.5f;
		
		FleetParamsV3 params = new FleetParamsV3(
				route != null ? route.getMarket() : source,
				locInHyper,
				"hmi_maxwell", // quality will always be reduced by non-market-faction penalty, which is what we want
				route == null ? null : route.getQualityOverride(),
				type,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				transport, // transportPts
				0f, // linerPts
				utility, // utilityPts
				0f // qualityMod
				);
		if (route != null) {
			params.timestamp = route.getTimestamp();
		}
		params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;

//		fleet.setFaction(Factions.INDEPENDENT, true);
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SCAVENGER, true);
		
		if (pirate || true) {
			Misc.makeLowRepImpact(fleet, "scav");
		}
		
		return fleet;
	}


	public String getStartingActionText(CampaignFleetAPI fleet, RouteManager.RouteSegment segment, MiscFleetRouteManager.MiscRouteData data) {
		String type = fleet.getMemoryWithoutUpdate().getString(FleetTypes.TASK_FORCE);
		return "preparing for a resupply contract";
	}

	public String getEndingActionText(CampaignFleetAPI fleet, RouteManager.RouteSegment segment, MiscFleetRouteManager.MiscRouteData data) {
		//return "orbiting " + data.from.getName();
		return "returned from a resupply contract";
	}

	public String getTravelToDestActionText(CampaignFleetAPI fleet, RouteManager.RouteSegment segment, MiscFleetRouteManager.MiscRouteData data) {
		return "carrying out a resupply contract";
	}

	public String getTravelReturnActionText(CampaignFleetAPI fleet, RouteManager.RouteSegment segment, MiscFleetRouteManager.MiscRouteData data) {
		return "returning home";
	}


	public boolean shouldCancelRouteAfterDelayCheck(RouteData data) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}
	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		
	}

	public SectorEntityToken getMaxwellToken(Random random) {
		WeightedRandomPicker<SectorEntityToken> pickerTo = new WeightedRandomPicker<SectorEntityToken>(random);
		WeightedRandomPicker<String> pickerS = new WeightedRandomPicker<String>(random);

		pickerS.add(getTargetMaxwell(random), 2);

		StarSystemAPI pickSys = Global.getSector().getStarSystem(pickerS.pick());
		if (pickSys == null) {return null; }
		for(SectorEntityToken jp : pickSys.getEntitiesWithTag("HMI_STATION_MAXWELL_PICK")) {
			pickerTo.add(jp, 100000/ MathUtils.getDistance(pickSys.getCenter(),jp.getLocation()));
		}
		SectorEntityToken to = pickerTo.pick();

		if (to == null || to.getContainingLocation().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) { return null; }
		return to;
	}

	public static String getTargetMaxwell(Random random) {
		String result = null;
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);

		for(StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag("HMI_MAXWELL_PICK")) {
						picker.add(system.getId(), 11);
				}
			}
		result = picker.pick();

		return result;
	}
}







