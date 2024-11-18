package data.scripts.xo.synthesis;

import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class DroneTactics extends SCBaseSkillPlugin {
	
	private static String ADB_MOD_ID = "vice_adaptive_drone_bay";
	private static String AFC_MOD_ID = "vice_adaptive_flight_command";
	private static float DAM_BONUS = 10f;
	private static float AFC_BONUS = 20f;
	private static float DRONE_REPLACEMENT_BONUS = 25f;

	@Override
    public String getAffectsString() {
        return "all drone fighters";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
        tooltip.addPara("+10%% damage dealt, or +20%% when Adaptive Flight Command is enabled", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("25%% reduction to drone replacement time when Adaptive Drone Bay is enabled", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("Bonuses apply to all wings on ships with Autonomous Bays or Drone Conversion hullmod", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
		if(stats.getVariant().hasHullMod(ADB_MOD_ID)) {
			stats.getFighterRefitTimeMult().modifyMult(id, 1f - DRONE_REPLACEMENT_BONUS * 0.01f);
		}
    }

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		float bonus = DAM_BONUS;
		boolean isAlwaysValid = ship.getVariant().hasHullMod("SKR_remote") || ship.getVariant().hasHullMod("rat_autonomous_bays");
		if (ship.getVariant().hasHullMod(AFC_MOD_ID)) bonus = AFC_BONUS;
		if (isAlwaysValid || fighter.getHullSpec().getMinCrew() == 0) {
			MutableShipStatsAPI stats = fighter.getMutableStats();
			stats.getBallisticWeaponDamageMult().modifyPercent(id, bonus);
			stats.getEnergyWeaponDamageMult().modifyPercent(id, bonus);
			stats.getMissileWeaponDamageMult().modifyPercent(id, bonus);
		}
	}
}
