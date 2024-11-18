package DE.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class VectorTriangulator extends BaseHullMod {

//	public static final float BONUS = 100f;
//	
//	public String getDescriptionParam(int index, HullSize hullSize) {
//		if (index == 0) return "" + (int)BONUS + "%";
//		return null;
//	}
//	
//	
//	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//		stats.getBallisticWeaponRangeBonus().modifyPercent(id, BONUS);
//		stats.getEnergyWeaponRangeBonus().modifyPercent(id, BONUS);
//	}


	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return !(ship.getVariant().getHullMods().contains(HullMods.INTEGRATED_TARGETING_UNIT) || ship.getVariant().getHullMods().contains(HullMods.ADVANCED_TARGETING_CORE));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Incompatible with Integrated Targeting Unit and Advanced Targeting Core";
		// DTC is not included; maybe include if balance issues?
	}
	
	public static float RANGE_BONUS = 75f;
	public static float PD_MINUS = 75f;
	public static float BALLISTIC_ROF_PENALTY = 10f;
	public static float ENERGY_DAMAGE_PENALTY = 10f;
	
	/*public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(RANGE_BONUS) + "%";
		if (index == 1) return "" + (int)Math.round(RANGE_BONUS - PD_MINUS) + "%";
		//if (index == 0) return "" + (int)RANGE_THRESHOLD;
		//if (index == 1) return "" + (int)((RANGE_MULT - 1f) * 100f);
		//if (index == 1) return "" + new Float(VISION_BONUS).intValue();
		return null;
	}*/
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -PD_MINUS);
		stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -PD_MINUS);
		stats.getBallisticRoFMult().modifyPercent(id, -BALLISTIC_ROF_PENALTY);
		stats.getEnergyWeaponDamageMult().modifyPercent(id, -ENERGY_DAMAGE_PENALTY);
	}

	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();

		LabelAPI label = tooltip.addPara("increases the base range of medium weapons by %s, but does not affect PD weapons.", opad, h,
				"" + (int)RANGE_BONUS);
//		label.setHighlight("base", "Ballistic", "" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
//		label.setHighlightColors(h, Misc.MOUNT_BALLISTIC, h, h);
		label.setHighlight("" + (int)RANGE_BONUS);
		label.setHighlightColors(h, h);

		label = tooltip.addPara("decreases ballistic weapon firerate by %s and energy weapon damage by %s.", opad, h,
				"" + (int)BALLISTIC_ROF_PENALTY,"" + (int)ENERGY_DAMAGE_PENALTY);
		label.setHighlight("" + (int)BALLISTIC_ROF_PENALTY,"" + (int)ENERGY_DAMAGE_PENALTY);
		label.setHighlightColors(h, h);
	}
}
