package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.istl_HullMods;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class BladeBreakerAssault extends BaseHullMod {
    private String getString(String key) {
    return Global.getSettings().getString("HullMod", "istl_" + key);}
    
    private static Map speed = new HashMap();
    static {
	speed.put(HullSize.FRIGATE, 50f);
	speed.put(HullSize.DESTROYER, 40f);
	speed.put(HullSize.CRUISER, 25f);
	speed.put(HullSize.CAPITAL_SHIP, 15f);
    }
	
    public static final float FLUX_REDUCTION = 25f;
    private static final float FLUX_DISSIPATION_MULT = 2f;
    private static final float PEAK_MULT = 0.75f;

    private static final float RANGE_THRESHOLD = 600f;
    private static final float RANGE_MULT = 0.5f; // Range penalty above threshold.
    
    private static final float FTR_SPEED_BOOST = 0.25f;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getMaxSpeed().modifyFlat(id, (Float) speed.get(hullSize));
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
        stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
	stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);

        stats.getVentRateMult().modifyMult(id, 0f);

        stats.getWeaponRangeThreshold().modifyFlat(id, RANGE_THRESHOLD);
	stats.getWeaponRangeMultPastThreshold().modifyMult(id, RANGE_MULT); // range multiplier beyond threshold.
    }
    
    //Add effects.
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id)
    {
	MutableShipStatsAPI stats = fighter.getMutableStats();

        stats.getMaxSpeed().modifyMult(id, 1f + FTR_SPEED_BOOST);
    }
 
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec)
    {
        float pad = 10f;
        float padS = 2f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/istl_hullmod_movement.png", 40);
            text.addPara("- " + getString("BBAssaultDesc1"), pad, Misc.getHighlightColor(), "50", "40", "25", "15");
            text.addPara("- " + getString("BBAssaultDesc2"), padS, Misc.getHighlightColor(), "25%");
            text.addPara("- " + getString("BBAssaultDesc3"), padS, Misc.getHighlightColor(), "2");
            text.addPara("- " + getString("BBAssaultDesc4"), padS, Misc.getHighlightColor(), "600");
            text.addPara("- " + getString("BBAssaultDesc5"), padS, Misc.getHighlightColor(), "25%");
        TooltipMakerAPI text2 = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/istl_hullmod_fighter.png", 40);
            text2.addPara("- " + getString("BBAssaultDescFtr"), padS, Misc.getHighlightColor(), "25%");
    }
    
    private Color color = new Color(0,255,0,255);
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        HullSize hullSize = ship.getHullSize();
        MutableShipStatsAPI stats = ship.getMutableStats();
        String id = "BladeBreakerAssault";
        FluxTrackerAPI flux = ship.getFluxTracker();
        final float fluxLevel = flux.getCurrFlux() / flux.getMaxFlux();
        float speedBonus = 1f;
        // Doubled bonus if overloaded or venting.
        if (flux.isOverloadedOrVenting()) {
            speedBonus = 2f;
        }            
        //Improves top speed/accel based on flux level
        stats.getMaxSpeed().modifyFlat(id, (Float) speed.get(hullSize) * speedBonus * fluxLevel);
        stats.getAcceleration().modifyPercent(id, 60f * (Float) speed.get(hullSize) * speedBonus * fluxLevel);
        stats.getDeceleration().modifyPercent(id, 40f * (Float) speed.get(hullSize) * speedBonus * fluxLevel);
        //Improves turning/turn accel based on flux level
        stats.getMaxTurnRate().modifyFlat(id, 5f * (speedBonus/2f) * fluxLevel);
        stats.getMaxTurnRate().modifyPercent(id, 100f * (Float) speed.get(hullSize) * fluxLevel);
        stats.getTurnAcceleration().modifyFlat(id, 10f * (speedBonus/1.5f) * fluxLevel);
        stats.getTurnAcceleration().modifyPercent(id, 50f * (speedBonus/1.5f) * fluxLevel);
        //Also improves your chances of getting your shit wrecked based on flux level
        stats.getEngineDamageTakenMult().modifyPercent(id, 100f * fluxLevel);
        //Sweet green visuals based on flux level
        ship.getEngineController().fadeToOtherColor(this, color, null, 1f * fluxLevel, 0.5f);
        ship.getEngineController().extendFlame(this, 0.3f * fluxLevel, 0.3f * fluxLevel, 0.6f * fluxLevel);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (shipHasOtherModInCategory(ship, spec.getId(), istl_HullMods.TAG_BREAKER_PACKAGE)) return false;
        return ship.getVariant().hasHullMod(istl_HullMods.BLADEBREAKER_BASE) && super.isApplicableToShip(ship);
    }
    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (shipHasOtherModInCategory(ship, spec.getId(), istl_HullMods.TAG_BREAKER_PACKAGE)) {
                return "Can only install one combat focus on a Blade Breaker hull";
        }
        if (!ship.getVariant().hasHullMod(istl_HullMods.BLADEBREAKER_BASE)) {
                return "Must be installed on a Blade Breaker ship";
        }
        return super.getUnapplicableReason(ship);
    }
}