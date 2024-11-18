package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.HashSet;
import java.util.Set;

public class RefurbishedFrame extends BaseHullMod {
    private String getString(String key) {
    return Global.getSettings().getString("HullMod", "istl_" + key);}
    
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(6);
    public static final float REPAIR_RATE_BONUS = 25f;
    public static final float CR_RECOVERY_BONUS = 25f;
    public static final float REPAIR_BONUS = 25f;
    
    static
    {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("heavyarmor"); // No heavy armor on DME ships.
        BLOCKED_HULLMODS.add("apex_armor"); // We mean it!
        BLOCKED_HULLMODS.add("apex_cryo_armor");
        BLOCKED_HULLMODS.add("istl_bbassault"); // Sneaky little bastards
        BLOCKED_HULLMODS.add("istl_bbdefense"); // Every loophole you find, I'll stop up
        BLOCKED_HULLMODS.add("istl_bbsupport"); // So prepare for an arms race if you wanna use this stuff.
    }
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float padS = 2f;
	tooltip.addSectionHeading("Details", Alignment.MID, pad);
	tooltip.addPara("- " + getString("RefurbDesc1"), pad, Misc.getHighlightColor(), "25%");
	tooltip.addPara("- " + getString("RefurbDesc2"), padS, Misc.getHighlightColor(), "25%");
        tooltip.addSectionHeading("Incompatibilities", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/hullmod_incompatible.png", 40);
            text.addPara(getString("DMEAllIncomp"), padS);
            text.addPara("- Heavy Armor", Misc.getNegativeHighlightColor(), padS);
            if (Global.getSettings().getModManager().isModEnabled("apex_design")) {
                text.addPara("- Nanolaminate Plating", Misc.getNegativeHighlightColor(), 0f);
                text.addPara("- Cryocooled Armor Lattice", Misc.getNegativeHighlightColor(), 0f);
            }
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);

        stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, CR_RECOVERY_BONUS);
        stats.getRepairRatePercentPerDay().modifyPercent(id, REPAIR_RATE_BONUS);
    }

    public String getDescriptionParam(int index, HullSize hullSize)
    {
        if (index == 0)
        {
            return "" + (int) REPAIR_BONUS;
        }
        if (index == 1)
        {
            return "" + (int) CR_RECOVERY_BONUS;
        }
        return null;
    }

}
