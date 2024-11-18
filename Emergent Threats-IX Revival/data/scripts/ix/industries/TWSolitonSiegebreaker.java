package data.scripts.ix.industries;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.campaign.CampaignUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionFleetDamage;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionParams;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class TWSolitonSiegebreaker extends BaseIndustry {

	private float daysToFire = 10f;
	private IntervalUtil tracker = new IntervalUtil(daysToFire, daysToFire);
	private boolean isFired = false;
	
	@Override
	public boolean isHidden() {
		return false;
	}	
	
	@Override
	public boolean isFunctional() {
		return true;
	}
	
	@Override
	public void apply() {
		super.apply(true);
		applyIncomeAndUpkeep(3);
		
		demand(Commodities.FUEL, 7);
		demand(Commodities.HEAVY_MACHINERY, 7);
		
		if (!isFunctional()) {
			unapply();
		}
	}

	@Override
	public void unapply() {
		super.unapply();
	}
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	public String getNameForModifier() {
		return Misc.ucFirst(getSpec().getName().toLowerCase());
	}
	
	@Override
	public boolean isAvailableToBuild() {
		return false;
	}
	
	@Override
	public String getUnavailableReason() {
		return "";
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
	public void advance(float amount) {
		super.advance(amount);
		if (Global.getSector().getEconomy().isSimMode()) return;
		if (!isFunctional()) return;
				
		if (market.hasCondition("blockaded")) {
			float days = Global.getSector().getClock().convertToDays(amount);
			if (isFired) {
				tracker = new IntervalUtil(5f, 5f);
				isFired = false;
			}
			else tracker.advance(days);
		}

		if (tracker.intervalElapsed() && !isFired) {
			SectorEntityToken explosion = null;
			StarSystemAPI system = market.getStarSystem();
			SectorEntityToken point = (SectorEntityToken) system.getJumpPoints().get(1);
			SectorEntityToken origin = market.getStarSystem().getEntityById("tw_danu_culmen_station");
			LocationAPI cl = point.getContainingLocation();
			Vector2f loc = point.getLocation();
			Vector2f vel = point.getVelocity();
			float size = point.getRadius() + 1000f; //in system explosion size
			
			SectorEntityToken hPoint = Global.getSector().getHyperspace().getEntityById("ix_danu_beacon").getOrbitFocus();
			float hyperspaceSize = hPoint.getRadius() + 100f; //in hyperspace explosion size
			
			int hostileCount = checkForHostileFleets(point, origin, size);
			int hostileCountHyperspace = checkForHostileFleets(hPoint, origin, hyperspaceSize);
			
			if (hostileCount + hostileCountHyperspace == 0) return;
			
			Color color = new Color(50, 255, 50, 255);
			
			ExplosionParams params = new ExplosionParams(color, cl, loc, size, 3f);
			params.damage = ExplosionFleetDamage.EXTREME;
			explosion = cl.addCustomEntity(Misc.genUID(), "Hyperwave Inversion Event", 
											Entities.EXPLOSION, Factions.NEUTRAL, params);
			explosion.setLocation(loc.x, loc.y);
			deleteShips(point, size);
			
			cl = hPoint.getContainingLocation();
			loc = hPoint.getLocation();
			vel = hPoint.getVelocity();
			hyperspaceSize = hPoint.getRadius() + 100f; //in hyperspace explosion size
			params = new ExplosionParams(color, cl, loc, hyperspaceSize, 3f);
			params.damage = ExplosionFleetDamage.EXTREME;
			explosion = cl.addCustomEntity(Misc.genUID(), "Hyperwave Inversion Event", 
											Entities.EXPLOSION, Factions.NEUTRAL, params);
			explosion.setLocation(loc.x, loc.y);
			deleteShips(hPoint, hyperspaceSize);
			
			isFired = true;
		}
	}
	
	private int checkForHostileFleets(SectorEntityToken token, SectorEntityToken source, float size) {
		int fleetCount = 0;
		List<CampaignFleetAPI> fleets = CampaignUtils.getNearbyFleets(token, size);
		for (CampaignFleetAPI fleet : fleets) {
			if (fleet.isHostileTo(source) && !fleet.isPlayerFleet()) fleetCount++;
		}
		return fleetCount;
	}
	
	private void deleteShips(SectorEntityToken token, float size) {
		List<CampaignFleetAPI> fleetsToDelete = CampaignUtils.getNearbyFleets(token, size);
		for (CampaignFleetAPI fleet : fleetsToDelete) {
			if (!fleet.isPlayerFleet()) {
				List<FleetMemberAPI> members = fleet.getMembersWithFightersCopy();
				for (FleetMemberAPI member : members) {
					fleet.removeFleetMemberWithDestructionFlash(member);
				}
			}
		}
	}
	
	@Override
	public boolean canInstallAICores() {
		return false;
	}
}