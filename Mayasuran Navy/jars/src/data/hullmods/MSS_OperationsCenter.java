package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.magiclib.util.MagicIncompatibleHullmods;

import java.util.HashSet;
import java.util.Set;

//import data.scripts.util.MagicIncompatibleHullmods;

public class MSS_OperationsCenter extends BaseHullMod {


	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
	static
	{
		// These hullmods will automatically be removed
		// This prevents unexplained hullmod blocking
		BLOCKED_HULLMODS.add("safetyoverrides");
		BLOCKED_HULLMODS.add("operations_center");
	}


	public static final float RECOVERY_BONUS = 250f;
	public static final String MOD_ID = "operations_center_mod";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		for (String tmp : BLOCKED_HULLMODS) {
			if(stats.getVariant().getHullMods().contains(tmp)){
				MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), tmp, "MSS_OperationsCenter");
			}
		}
	}


	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) RECOVERY_BONUS + "%";
		return null;
	}




	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine == null) return;
		
		CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOriginalOwner());
		if (manager == null) return;
		
		DeployedFleetMemberAPI member = manager.getDeployedFleetMember(ship);
		if (member == null) return; // happens in refit screen etc
		
		boolean apply = ship == engine.getPlayerShip();
		PersonAPI commander = null;
		if (member.getMember() != null) {
			member.getMember().getFleetCommander();
		}
		apply |= commander != null && ship.getCaptain() == commander;
		
		if (apply) {
			ship.getMutableStats().getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).modifyFlat(MOD_ID, RECOVERY_BONUS * 0.01f);
		} else {
			ship.getMutableStats().getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).unmodify(MOD_ID);
		}
	}

}








