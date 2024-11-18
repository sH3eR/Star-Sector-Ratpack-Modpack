package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class CHM_mayasura extends BaseHullMod {
    public static final float MAX_CR_BONUS = 10f;
	public static final float MIN_CR_BONUS = 5f;
	public static final String Manufacturer = "Mayasuran";
    
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {   
		if (Manufacturer.equals(stats.getVariant().getHullSpec().getManufacturer()) || stats.getVariant().hasHullMod("MSS_Prime") || stats.getVariant().hasHullMod("MSS_PrimeC") || stats.getVariant().hasHullMod("MSS_Militarized")) {
            stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f, "Mayasuran Prime Readiness");
        } else {stats.getMaxCombatReadiness().modifyFlat(id, MIN_CR_BONUS * 0.01f, "Mayasuran Readiness");}
	}
	
    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(235,231,230,255);
    }
	
    @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + "+" + (int) MIN_CR_BONUS + "%";
		return null;
	}
	
	@Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        tooltip.addPara("%s", 6f, Misc.getGrayColor(), "\"Mayasuran captains knows what it means to lose everything and can be trusted to carry out orders against overwhelming odds.\"").italicize();
    }
	
}
