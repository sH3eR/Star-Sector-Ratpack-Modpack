package data.campaign.fleets;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HMIObsidianLowtechFleetAssignmentAI extends RouteFleetAssignmentAI {

	protected boolean pirate;
	protected IntervalUtil piracyCheck = new IntervalUtil(0.2f, 0.4f);
	public HMIObsidianLowtechFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route, boolean pirate) {
		super(fleet, route);
		this.pirate = pirate;
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (pirate) {
			float days = Global.getSector().getClock().convertToDays(amount);
			piracyCheck.advance(days);
			if (piracyCheck.intervalElapsed()) {
				doPiracyCheck();
			}
		}
	}
	
	protected void doPiracyCheck() {
		if (fleet.getBattle() != null) return;
		
		
		boolean isCurrentlyPirate = fleet.getFaction().getId().equals(Factions.PIRATES);
		
		if (fleet.isTransponderOn() && !isCurrentlyPirate) {
			return;
		}
		
		if (isCurrentlyPirate) {
			List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
			if (visible.isEmpty()) {
				fleet.setFaction(Factions.PIRATES, true);
				Misc.clearTarget(fleet, true);
			}
			return;
		}
		
		List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
		if (visible.size() == 1) {
			int weakerCount = 0;
			for (CampaignFleetAPI other : visible) {
				if (fleet.getAI() != null && 
						Global.getSector().getFaction(Factions.HEGEMONY).isHostileTo(other.getFaction()) && !other.getTags().equals("ObsidianLowTech")) {
					EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
					if (option == EncounterOption.ENGAGE || option == EncounterOption.HOLD) {
						float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
						VisibilityLevel level = other.getVisibilityLevelTo(fleet);
						boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
										   level == VisibilityLevel.COMPOSITION_DETAILS;
						if (dist < 1000f && seesComp) {
							weakerCount++;
						}
					}
				}
			}
		
			if (weakerCount == 1) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, false);
				fleet.setNoFactionInName(true);
				fleet.setFaction(Factions.HEGEMONY, true);
			}
		}
		
	}

}










