package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class istl_ExperiencedDamageControl extends BaseHullMod {
        private String getString(String key) {
        return Global.getSettings().getString("HullMod", "istl_" + key);}

	public static final float RATE_DECREASE_MODIFIER = 15f;
	public static final float CREW_LOSS_MULT = 0.15f;
	public static final float REPAIR_BONUS = 15f;
        
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - RATE_DECREASE_MODIFIER / 100f);
		stats.getCrewLossMult().modifyMult(id, 1f - CREW_LOSS_MULT);
		stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, CREW_LOSS_MULT);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
        }
	
        @Override
        public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
            float pad = 10f;
            float padS = 2f;
            tooltip.addSectionHeading("Details", Alignment.MID, pad);
            tooltip.addPara(getString("ComCrewDesc1"), pad, Misc.getHighlightColor(), "15%", "15%");
            tooltip.addPara(getString("ComCrewDesc2"), padS, Misc.getHighlightColor(), "15%");
        }
        
//	public String getDescriptionParam(int index, HullSize hullSize) {
//		if (index == 0) return "" + (int) (CREW_LOSS_MULT * 100f) + "%";
//		if (index == 1) return "" + (int) REPAIR_BONUS + "%";
//		if (index == 2) return "" + (int) RATE_DECREASE_MODIFIER + "%";
//		return null;
//	}
        
        @Override
        public Color getBorderColor() {
            return new Color(147, 102, 50, 0);
        }

        @Override
        public Color getNameColor() {
            return new Color(76, 113, 175, 255);
        }
	
	public boolean isApplicableToShip(ShipAPI ship) {
		int bays = (int) ship.getMutableStats().getNumFighterBays().getBaseValue();
//		if (ship != null && ship.getVariant().getHullSpec().getBuiltInWings().size() >= bays) {
//			return false;
//		}
		return ship != null && bays > 0; 
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship does not have standard fighter bays";
	}
}




