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

public class IXKresnikMechanism extends BaseIndustry {

	private float daysToFire = 10f;
	private IntervalUtil tracker = new IntervalUtil(daysToFire, daysToFire);
	private boolean isFired = false;
	
	@Override
	public boolean isHidden() {
		//return (market.hasIndustry(Industries.MILITARYBASE));
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
		
		//visuals
		float minSize = 10f;
		float maxSize = 15f;
		float sizeMult = 20f;
		minSize *= sizeMult;
		maxSize *= sizeMult;
			
		PlanetAPI star = market.getStarSystem().getStar();
		SectorEntityToken tap = market.getStarSystem().getEntityById("ix_zorya_vertex");
		
		float dirToTap = Misc.getAngleInDegrees(star.getLocation(), tap.getLocation());
		Vector2f unitToTap = Misc.getUnitVectorAtDegreeAngle(dirToTap);
		Vector2f focusLoc = new Vector2f(unitToTap);
		float widthMod = -40f;
		focusLoc.scale(star.getRadius() + tap.getRadius() + widthMod);
		Vector2f.add(star.getLocation(), focusLoc, focusLoc);
		
		float ctcDist = Misc.getDistance(focusLoc, star.getLocation());
		float dist = ctcDist - star.getRadius() - tap.getRadius();
		
		Color color = star.getSpec().getCoronaColor();
		float colorScale = 0.2f;
		color = Misc.scaleColor(color, colorScale);
				
		float angleMod = -70f;
		float dirToStar = Misc.getAngleInDegrees(focusLoc, star.getLocation()) + angleMod;
		float arc = star.getRadius() / ((float) Math.PI * (ctcDist - tap.getRadius())) * 360f; 
		
		float angle = dirToStar - arc / 2f;
		Vector2f unit = Misc.getUnitVectorAtDegreeAngle(angle);
		float x = unit.x * dist + focusLoc.x;
		float y = unit.y * dist + focusLoc.y;
		Vector2f loc = new Vector2f(x, y);
		
		float size = minSize + (float) Math.random() * (maxSize - minSize);
		float bright = 0.2f;
		float rampUp = 0f;
		float dur = 1f;
		float mult = 1f;
		
		Vector2f vel = focusLoc;
		
		//plume
		mult = 0.5f; //speed mult
		rampUp = 0.1f;
		x = vel.x * mult;
		y = vel.y * mult;
		vel.set(x, y);
		tap.getContainingLocation().addParticle(loc, vel, size, bright, rampUp, dur, color);
		
		//base
		dur = 0.2f;
		bright = 0.1f;
		x = 0;
		y = 0;
		vel.set(x, y);
		color = new Color(200, 150, 100, 50);
		tap.getContainingLocation().addParticle(loc, vel, size, bright, rampUp, dur, color);
		//end visuals
		
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
			SectorEntityToken point = (SectorEntityToken) system.getJumpPoints().get(0);
			LocationAPI cl = point.getContainingLocation();
			loc = point.getLocation();
			vel = point.getVelocity();
			size = point.getRadius() + 2000f; //in system explosion size
			
			SectorEntityToken hPoint = Global.getSector().getHyperspace().getEntityById("ix_zorya_beacon").getOrbitFocus();
			float hyperspaceSize = hPoint.getRadius() + 200f; //in hyperspace explosion size
			
			int hostileCount = checkForHostileFleets(point, tap, size);
			int hostileCountHyperspace = checkForHostileFleets(hPoint, tap, hyperspaceSize);
			
			if (hostileCount + hostileCountHyperspace == 0) return;
			
			color = new Color(255, 50, 0, 255);
			
			ExplosionParams params = new ExplosionParams(color, cl, loc, size, 5f);
			params.damage = ExplosionFleetDamage.EXTREME;
			explosion = cl.addCustomEntity(Misc.genUID(), "Hyperwave Inversion Event", 
											Entities.EXPLOSION, Factions.NEUTRAL, params);
			explosion.setLocation(loc.x, loc.y);
			deleteShips(point, size);
			
			cl = hPoint.getContainingLocation();
			loc = hPoint.getLocation();
			vel = hPoint.getVelocity();
			hyperspaceSize = hPoint.getRadius() + 200f; //in hyperspace explosion size
			params = new ExplosionParams(color, cl, loc, hyperspaceSize, 5f);
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