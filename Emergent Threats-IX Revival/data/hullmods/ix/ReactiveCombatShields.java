package data.hullmods.ix;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ReactiveCombatShields extends BaseHullMod {

	private static float DAMAGE_REDUCTION = 40f;
	private static float DAMAGE_REDUCTION_SYNTHESIS = 50f;
	private static float EMP_RESIST = 50f;
	private static float FLUX_THRESHOLD = 60f;
	private static float FLUX_THRESHOLD_SMOD = 50f;
	private static float SHIELD_EFFICIENCY_THRESHOLD = 0.6f;
	private static Color SHIELD_INNER_COLOR_ACTIVE = new Color(100,225,100,75);
	private static Color SHIELD_INNER_LOW_TECH = new Color(255,125,125,75);
	private static Color SHIELD_INNER_HIGH_TECH = new Color(125,125,255,75);
	
	private static String EQUALIZER_MOD = "ix_entropy_arrestor";
	private static String CONFLICT_MOD = "hardenedshieldemitter";
	private static String THIS_MOD = "ix_reactive_combat_shields";
	private static String SYNTHESIS_CHECKER_MOD = "ix_reactive_checker";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getVariant().getHullMods().remove(CONFLICT_MOD);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship.getOwner() == 0) ship.getVariant().getHullMods().remove(SYNTHESIS_CHECKER_MOD);
	}
	
	@Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive() || ship.getShield() == null) return;
		float threshold = isSMod(ship.getMutableStats()) ? FLUX_THRESHOLD_SMOD : FLUX_THRESHOLD;
		boolean isActive = ship.getMutableStats().getShieldDamageTakenMult().getMultStatMod(THIS_MOD) != null; 
		if (!isActive && (getShieldEfficiency(ship) <= SHIELD_EFFICIENCY_THRESHOLD)) return;
		else if (ship.getFluxLevel() >= threshold * 0.01f) {
			float damageReduction = isSynthesisActive(ship) ? DAMAGE_REDUCTION_SYNTHESIS : DAMAGE_REDUCTION;
			ship.getShield().setInnerColor(SHIELD_INNER_COLOR_ACTIVE);
			ship.getMutableStats().getShieldDamageTakenMult().modifyMult(THIS_MOD, 1f - damageReduction * 0.01f);
			ship.getMutableStats().getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(THIS_MOD, EMP_RESIST * 0.01f);
		}
		else {
			String style = ship.getHullStyleId();
			if (style.equals("LOW_TECH")) ship.getShield().setInnerColor(SHIELD_INNER_LOW_TECH);
			else ship.getShield().setInnerColor(SHIELD_INNER_HIGH_TECH);
			ship.getMutableStats().getShieldDamageTakenMult().unmodify(THIS_MOD);
			ship.getMutableStats().getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).unmodify(THIS_MOD);
		}
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null || ship.getShield() == null) return;
		float e = getShieldEfficiency(ship);
		if (e <= SHIELD_EFFICIENCY_THRESHOLD) {
			//if second decimal onward of shieldEfficiency is 0 (ie 0.6000...), display only 1 decimal
			String shieldEfficiency = (e * 10) % 1 == 0 ? String.format("%.1f", e) : String.format("%.2f", e);
			String s = "Warning: " + shieldEfficiency + " shield efficiency exceeds activation limit.";
			tooltip.addPara("%s", 10f, Misc.getNegativeHighlightColor(), s);
		}
	}
	
	//player fleet uses memflag checker, NPC fleets use hullmod checker which is always removed in player fleets
	private boolean isSynthesisActive(ShipAPI ship) {
		if (Global.getSector().getMemoryWithoutUpdate().is("$xo_dynamic_shields_is_active", true) && ship.getOwner() == 0) {
			return true;
		}
		else if (ship.getVariant().hasHullMod(SYNTHESIS_CHECKER_MOD) && ship.getOwner() != 0) return true;
		return false;
		
	}
	
	//for text display only
	private boolean isSynthesisActive() {
		return Global.getSector().getMemoryWithoutUpdate().is("$xo_dynamic_shields_is_active", true);
	}
	
	private float getShieldEfficiency(ShipAPI ship) {
		float e = ship.getShield().getFluxPerPointOfDamage() * ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
		return e;
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getShield() == null) return false;
		if (ship.getVariant().getHullMods().contains(EQUALIZER_MOD)) return false;
		return (!ship.getVariant().getHullMods().contains(CONFLICT_MOD));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getShield() == null) return "Ship has no shields";
		if (ship.getVariant().getHullMods().contains(EQUALIZER_MOD)) return "Incompatible with Immanence Engine";
		if (ship.getVariant().getHullMods().contains(CONFLICT_MOD)) return "Incompatible shield modification present";
		return null;
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		String s = "" + (int) DAMAGE_REDUCTION + "%";
		if (isSynthesisActive()) s = "" + (int) DAMAGE_REDUCTION_SYNTHESIS + "% (Dynamic Shields)";
		
		if (index == 0) return s;
		if (index == 1) return "" + (int) FLUX_THRESHOLD + "%";
		if (index == 2) return "" + SHIELD_EFFICIENCY_THRESHOLD;
		return null;
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FLUX_THRESHOLD_SMOD + "%";
		return null;
	}
}