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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BladeBreakerEngineering extends BaseHullMod {
    private String getString(String key) {
    return Global.getSettings().getString("HullMod", "istl_" + key);}
    
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(6);
    
    public static final float COST_REDUCTION_LG = 8f;
    public static final float COST_REDUCTION_MED = 4f;
    public static final float COST_REDUCTION_SM = 2f;
    private static final float SUPPLY_USE_MULT = 1.5f;
    private static final float OVERLOAD_DUR_MULT = 1.25f;
    public static final float CORONA_EFFECT_REDUCTION = 0.05f;
    public static final float DAMAGE_MULT = 1.5f;
    public static final float RADIUS_MULT = 0.75f;

    static
    {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("safetyoverrides"); // It would be bad
        BLOCKED_HULLMODS.add("eis_aquila"); // Also bad - no biscuit for you, selkie!
        BLOCKED_HULLMODS.add("converted_hangar"); // No super fighter spam for you!
        BLOCKED_HULLMODS.add("roider_fighterClamps"); // We mean it!
        BLOCKED_HULLMODS.add("targetingunit"); // No general range boost for you!
        BLOCKED_HULLMODS.add("dedicated_targeting_core"); // No slightly crappier range boost for you, either!
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_LG); // Overpriced weapons get a reduction,
        stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_LG); // Mainly to push the player to use Blade Breaker tech,
        stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_MED); // And to keep things feeling different and scary.
        stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_MED); // The more you feel like you're getting away with something,
        stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_SM); // The more fun their weapons are to use,
        stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_SM); // Well, that and the sweet-ass FX.
	stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT); // Got to pay the piper.
        stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_DUR_MULT); // And it comes at such a cost.
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_REDUCTION); // But with consolation prizes, too.
        stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DAMAGE_MULT); // Half again the bang...
        stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, RADIUS_MULT); // ...at a quarter less distance.
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec)
    {
        float pad = 10f;
        float padS = 2f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);
	tooltip.addPara("- " + getString("BBEngDesc1"), pad, Misc.getHighlightColor(), "2", "4", "8 OP");
        tooltip.addPara("- " + getString("BBEngDesc2"), padS, Misc.getHighlightColor(), "50%");
        tooltip.addPara("- " + getString("BBEngDesc3"), padS, Misc.getHighlightColor(), "25%");
	tooltip.addPara("- " + getString("BBEngDesc4"), padS, Misc.getHighlightColor(), "95%");
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
            text.addPara("- Integrated Targeting Unit", Misc.getNegativeHighlightColor(), 0f);
            text.addPara("- Dedicated Targeting Core", Misc.getNegativeHighlightColor(), 0f);
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
//            return "" + (int) COST_REDUCTION_SM + "";
//        }
//        if (index == 1)
//        {
//            return "" + (int) COST_REDUCTION_MED + "";
//        }
//        if (index == 2)
//        {
//            return "" + (int) COST_REDUCTION_LG + "";
//        }
//        if (index == 3)
//        {
//            return "" + (int) HARD_FLUX_DISSIPATION_PERCENT;
//        }
//        if (index == 4)
//        {
//            return "" + (int)((SUPPLY_USE_MULT - 1f) * 100f) + "%";
//        }
//        if (index == 5)
//        {    
//            return "" + (int) Math.round((OVERLOAD_DUR_MULT - 1f) * 100f) + "%";
//        }            
//        if (index == 6)
//        {    
//            return "" + (int) Math.round((1f - CORONA_EFFECT_REDUCTION) * 100f) + "%";
//        }
//        return null;
//    }

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}


