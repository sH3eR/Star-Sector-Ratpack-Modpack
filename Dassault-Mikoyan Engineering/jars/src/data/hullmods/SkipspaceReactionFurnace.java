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
import java.util.HashSet;
import java.util.Set;

public class SkipspaceReactionFurnace extends BaseHullMod {
    private String getString(String key) {
    return Global.getSettings().getString("HullMod", "istl_" + key);}

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(7);
    public static final float FLUX_RESISTANCE = 50f;
    private static final float PROFILE_DECREASE = 25f;
    public static final float VENT_RATE_BONUS = 25f;
    public static final float ZERO_FLUX_BONUS = 50;
    public static final float ZERO_FLUX_MULT = 5;
    public static final float CORONA_EFFECT_REDUCTION = 0.5f;
    //Engine damage float

    static
    {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("safetyoverrides"); // It would be bad
        BLOCKED_HULLMODS.add("eis_aquila"); // We mean it!
        BLOCKED_HULLMODS.add("converted_hangar"); // No converted hangar, either.
        BLOCKED_HULLMODS.add("roider_fighterClamps"); // We mean it!        
        BLOCKED_HULLMODS.add("fluxbreakers"); // No immunity
        BLOCKED_HULLMODS.add("istl_bbassault"); // Sneaky little bastards
        BLOCKED_HULLMODS.add("istl_bbdefense"); // Every loophole you find, I'll stop up
        BLOCKED_HULLMODS.add("istl_bbsupport"); // So prepare for an arms race if you wanna use this stuff.
    }
    
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - FLUX_RESISTANCE * 0.01f);
        stats.getSensorProfile().modifyPercent(id, -PROFILE_DECREASE);
        stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BONUS);
	stats.getZeroFluxMinimumFluxLevel().modifyMult(id, ZERO_FLUX_MULT * 0.01f); // Assumes the base game is setting it to 1% at most with Helm 3
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_REDUCTION);
        //Might do some bonus engine damage later
    }
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float padS = 2f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);
	tooltip.addPara("- " + getString("ReactionFurnaceDesc1"), pad, Misc.getHighlightColor(), "100%");
	tooltip.addPara("- " + getString("ReactionFurnaceDesc2"), padS, Misc.getHighlightColor(), "25%");
        tooltip.addPara("- " + getString("ReactionFurnaceDesc3"), padS, Misc.getHighlightColor(), "25%");
        tooltip.addPara("- " + getString("ReactionFurnaceDesc4"), padS, Misc.getHighlightColor(), "50su", "5%");
	tooltip.addPara("- " + getString("ReactionFurnaceDesc5"), padS, Misc.getHighlightColor(), "50%");
	tooltip.addSectionHeading("Incompatibilities", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/hullmod_incompatible.png", 40);
            text.addPara(getString("DMEAllIncomp"), padS);
            text.addPara("- Safety Overrides", Misc.getNegativeHighlightColor(), padS);
            if (Global.getSettings().getModManager().isModEnabled("timid_xiv")) {
                text.addPara("- Aquilla Reactor Protocol", Misc.getNegativeHighlightColor(), 0f);
            }
            text.addPara("- Converted Hangar", Misc.getNegativeHighlightColor(), 0f);
            if (Global.getSettings().getModManager().isModEnabled("roider")) {
                text.addPara("- Fighter Clamps", Misc.getNegativeHighlightColor(), 0f);
            }      
            text.addPara("- Resistant Flux Conduits", Misc.getNegativeHighlightColor(), 0f);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        for (String tmp : BLOCKED_HULLMODS)
        {
            if (ship.getVariant().getHullMods().contains(tmp))
            {
                ship.getVariant().removeMod(tmp);
                DMEBlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }
    
//    public String getDescriptionParam(int index, HullSize hullSize)
//    {
//        if (index == 0)
//        {
//            return "" + (int) FLUX_RESISTANCE;
//        }
//        if (index == 1)
//        {
//            return "" + (int) VENT_RATE_BONUS;
//        }
//        if (index == 2)
//        {
//            return "" + (int) PROFILE_DECREASE;
//        }
//        if (index == 3)
//        {
//            return "" + (int) ZERO_FLUX_BONUS + "";
//        }
//        if (index == 4)
//        {
//            return "" + (int) ZERO_FLUX_MULT + "";
//        }
//        if (index == 5)
//        {    
//            return "" + (int) Math.round((1f - CORONA_EFFECT_REDUCTION) * 100f) + "%";
//        }
//        return null;
//    }

}
