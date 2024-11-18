package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;


public class cum_fh extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                if (params.size() > 0) {Misc.fadeAndExpire(dialog.getInteractionTarget());} else {UhohStinky(dialog);}
                return true;
	}
        
    public void UhohStinky(InteractionDialogAPI dialog) {
        
		FleetParamsV3 params = new FleetParamsV3(
				null, 
				null, // loc in hyper; don't need if have market
				Factions.HEGEMONY,
				2f, // quality override route.getQualityOverride()
				FleetTypes.TASK_FORCE,
				100f, // combatPts
				0f, // freighterPts 
				0f, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
                params.onlyRetainFlagship = true;
                params.maxShipSize = 3;
            CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
            if (fleet == null || fleet.isEmpty()) return;
            fleet.setNoFactionInName(true);
            fleet.setName("Off-duty Patrol");
            fleet.getMemoryWithoutUpdate().set("$cum_fh_interaction", true);
            fleet.getMemoryWithoutUpdate().set("$cum_fh_sibling", Math.random() > 0.5f ? "sister" :"brother");
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
            dialog.getInteractionTarget().getContainingLocation().addEntity(fleet);
            fleet.setLocation(Global.getSector().getPlayerFleet().getLocation().x, Global.getSector().getPlayerFleet().getLocation().y);
            fleet.addAbility(Abilities.EMERGENCY_BURN);
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (fleet.getAI() == null) {
                fleet.setAI(Global.getFactory().createFleetAI(fleet));
                fleet.setLocation(fleet.getLocation().x, fleet.getLocation().y);
            }
            float expire = fleet.getMemoryWithoutUpdate().getExpire(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
            fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true, Math.max(expire, 30f));
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
            //fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true, 30f);
            fleet.getMemoryWithoutUpdate().set(FleetAIFlags.PLACE_TO_LOOK_FOR_TARGET, new Vector2f(playerFleet.getLocation()), 30f);
            if (fleet.getAI() instanceof ModularFleetAIAPI) {
                ((ModularFleetAIAPI)fleet.getAI()).getTacticalModule().setTarget(playerFleet);
            }
            fleet.addAssignmentAtStart(FleetAssignment.INTERCEPT, playerFleet, 30f, null);
            fleet.setFacing((float) Math.random() * 360f);
            Misc.giveStandardReturnToSourceAssignments(fleet, false);
    }    
}

