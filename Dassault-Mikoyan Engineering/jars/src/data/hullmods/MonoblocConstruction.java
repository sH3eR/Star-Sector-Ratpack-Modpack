package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MonoblocConstruction extends BaseHullMod {
    private String getString(String key) {
    return Global.getSettings().getString("HullMod", "istl_" + key);}
        
//    private static Map mag = new HashMap(); //Old range threshold crap; Depreciated.
//    static {
//	mag.put(HullSize.FIGHTER, 600f);
//	mag.put(HullSize.FRIGATE, 600f);
//	mag.put(HullSize.DESTROYER, 700f);
//	mag.put(HullSize.CRUISER, 800f);
//	mag.put(HullSize.CAPITAL_SHIP, 1000f);
//    }
    
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(8);
    public static final float FLUX_RESISTANCE = 25f; // Stack this with Resistant Flux Conduits and you're EMP-proof
    private static final float OVERLOAD_DUR_MULT = 0.5f; // Overload duration cut by 50%.
    //private static final float BALLISTIC_RANGE_MULT = 0.85f; // Range reduction multiplier for ballistics. Depreciated.
    //private static final float RANGE_THRESHOLD = 600f; // Threshold for range reduction; no effect under 600; Depreciated.
    //private static final float RANGE_MULT = 0.5f; // Range penalty above threshold.; Depreciated.
    public static final float ZERO_FLUX_BONUS = 25;
    
    static
    {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("heavyarmor"); // No heavy armor on DME ships.
        BLOCKED_HULLMODS.add("apex_armor"); // We mean it!
        BLOCKED_HULLMODS.add("apex_cryo_armor");
        BLOCKED_HULLMODS.add("converted_hangar"); // No converted hangar, either.
        BLOCKED_HULLMODS.add("roider_fighterClamps"); // We mean it!
        BLOCKED_HULLMODS.add("istl_bbassault"); // Sneaky little bastards
        BLOCKED_HULLMODS.add("istl_bbdefense"); // Every loophole you find, I'll stop up
        BLOCKED_HULLMODS.add("istl_bbsupport"); // So prepare for an arms race if you wanna use this stuff.
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - FLUX_RESISTANCE * 0.01f); // GET RECKLESS WITH THOSE DROPPED SHIELDS SON.
        stats.getOverloadTimeMod().modifyMult(id, 1f - OVERLOAD_DUR_MULT); // Whee what are consequences.
        //stats.getBallisticWeaponRangeBonus().modifyMult(id, BALLISTIC_RANGE_MULT); // range cut to ballistics.
        //stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD); // old fixed range threshold.
        //stats.getWeaponRangeThreshold().modifyFlat(id, (Float) mag.get(hullSize)); // new range threshold by hull size
	//stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT); // range multiplier beyond threshold.
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BONUS);
    }
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        float padS = 2f;
	tooltip.addSectionHeading("Details", Alignment.MID, pad);
	tooltip.addPara("%s " + getString("MonoblocDesc1"), pad, Misc.getHighlightColor(), "-", "25%");
	tooltip.addPara("%s " + getString("MonoblocDesc2"), padS, Misc.getHighlightColor(), "-", "50%");
        //tooltip.addPara("%s " + getString("MonoblocDesc3"), padS, Misc.getHighlightColor(), "-", "50%", "600", "700", "800", "1000");
        tooltip.addPara("%s " + getString("MonoblocDesc3"), padS, Misc.getHighlightColor(), "-", "25su");
        tooltip.addSectionHeading("Incompatibilities", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/hullmod_incompatible.png", 40);
            text.addPara(getString("DMEAllIncomp"), padS);
            text.addPara("- Heavy Armor", Misc.getNegativeHighlightColor(), padS);
            if (Global.getSettings().getModManager().isModEnabled("apex_design")) {
                text.addPara("- Nanolaminate Plating", Misc.getNegativeHighlightColor(), 0f);
                text.addPara("- Cryocooled Armor Lattice", Misc.getNegativeHighlightColor(), 0f);
            }
            text.addPara("- Converted Hangar", Misc.getNegativeHighlightColor(), 0f);
            if (Global.getSettings().getModManager().isModEnabled("roider")) {
                text.addPara("- Fighter Clamps", Misc.getNegativeHighlightColor(), 0f);
            }
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
//            return "" + (int) Math.round((1f - OVERLOAD_DUR_MULT) * 100f);
//        }
//        if (index == 2)
//        {
//            //return "" + (int) Math.round((1f - BALLISTIC_RANGE_MULT) * 100f);
//            return "" + (int) Math.round((1f - RANGE_MULT) * 100f);
//        }
//        if (index == 3) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
//	if (index == 4) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
//	if (index == 5) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
//	if (index == 6) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
//        if (index == 7)
//        {
//            return "" + (int) ZERO_FLUX_BONUS + "";
//        }
//        return null;
//    }
}
