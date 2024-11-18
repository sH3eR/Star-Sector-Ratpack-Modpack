package data.hullmods.vice;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class FleetOverride extends BaseHullMod {

	private static float FIGHTER_DAMAGE_BONUS = 15f;
	private static float FIGHTER_DAMAGE_BONUS_XO = 20f;
	private static float PILOT_SURVIVAL_PENALTY = 50f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 1f + PILOT_SURVIVAL_PENALTY * 0.01f);
	}
	
	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		MutableShipStatsAPI stats = fighter.getMutableStats();
		float bonus = isDoctrinalPurityActive() ? FIGHTER_DAMAGE_BONUS_XO : FIGHTER_DAMAGE_BONUS;
		stats.getBallisticWeaponDamageMult().modifyMult(id, 1f + bonus * 0.01f);
		stats.getEnergyWeaponDamageMult().modifyMult(id, 1f + bonus * 0.01f);
		stats.getMissileWeaponDamageMult().modifyMult(id, 1f + bonus * 0.01f);
	}
	
	private boolean isDoctrinalPurityActive() {
		return (Global.getSector().getMemoryWithoutUpdate().is("$xo_doctrinal_purity_is_active", true));
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (isForModSpec || ship == null) return;
		if (isDoctrinalPurityActive()) {
			tooltip.addPara("Damage bonus is being %s by Executive Officer", 10f, Misc.getHighlightColor(), "enhanced");
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		float bonus = isDoctrinalPurityActive() ? FIGHTER_DAMAGE_BONUS_XO : FIGHTER_DAMAGE_BONUS;
		if (index == 0) return "" + (int) bonus + "%";
		if (index == 1) return "" + (int) PILOT_SURVIVAL_PENALTY + "%";
		return null;
	}
}
