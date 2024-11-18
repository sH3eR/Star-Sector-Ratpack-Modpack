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
import data.campaign.ids.istl_HullMods;
import java.util.HashMap;
import java.util.Map;

public class BladeBreakerSupport extends BaseHullMod {
    private String getString(String key) {
    return Global.getSettings().getString("HullMod", "istl_" + key);}
    
	private static final Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 0f);
		mag.put(HullSize.FRIGATE, 100f);
		mag.put(HullSize.DESTROYER, 200f);
		mag.put(HullSize.CRUISER, 300f);
		mag.put(HullSize.CAPITAL_SHIP, 400f);
	}
        
        public static final float TURRET_SPEED_BONUS = 50f;
        public static final float SIGHT_BONUS = 125f;
        public static final float FTR_RANGE_BONUS = 100f;
        //public static final float DAMAGE_REDUCTION = 0.2f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) 
        {
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, (Float) mag.get(hullSize));
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, (Float) mag.get(hullSize));
                stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
                stats.getBeamWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		stats.getSightRadiusMod().modifyPercent(id, SIGHT_BONUS);
                //stats.getFragmentationDamageTakenMult().modifyMult(id, DAMAGE_REDUCTION);
	}
        
        //Add effects.
        public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id)
        {
		MutableShipStatsAPI stats = fighter.getMutableStats();

		stats.getBallisticWeaponRangeBonus().modifyFlat(id, FTR_RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, FTR_RANGE_BONUS);
	}
        
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec)
    {
        float pad = 10f;
        float padS = 2f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/istl_hullmod_sensor.png", 40);
            text.addPara("- " + getString("BBSupportDesc1"), pad, Misc.getHighlightColor(), "100", "200", "300", "400");
            text.addPara("- " + getString("BBSupportDesc2"), padS, Misc.getHighlightColor(), "50%");
            text.addPara("- " + getString("BBSupportDesc3"), padS, Misc.getHighlightColor(), "25%");
        TooltipMakerAPI text2 = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/istl_hullmod_fighter.png", 40);
            text2.addPara("- " + getString("BBSupportDescFtr"), padS, Misc.getHighlightColor(), "100");
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
        
//        @Override
//        public String getUnapplicableReason(ShipAPI ship) {
//            return "Must be installed on a Blade Breaker ship.";
//        }
//        @Override
//        public boolean isApplicableToShip(ShipAPI ship) {
//            // Allows any ship with a DME hull id, except all the ones that are blocked, mwahaha.
//            return ship.getHullSpec().getHullId().startsWith("istl_");
//        }
	
}