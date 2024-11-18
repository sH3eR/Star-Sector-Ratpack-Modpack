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

public class BladeBreakerDefense extends BaseHullMod {
    private String getString(String key) {
    return Global.getSettings().getString("HullMod", "istl_" + key);}

    //private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);
    public static final float HEALTH_BONUS = 100f;
    public static final float SHIELD_BONUS = 20f;
    public static final float PIERCE_MULT = 0.5f;
    public static final float DAMAGE_REDUCTION = 0.8f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
	stats.getDynamic().getStat(Stats.SHIELD_PIERCED_MULT).modifyMult(id, PIERCE_MULT);
        stats.getHighExplosiveDamageTakenMult().modifyMult(id, DAMAGE_REDUCTION);
        stats.getEnergyDamageTakenMult().modifyMult(id, DAMAGE_REDUCTION);
    }
    
    //Add effects.
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id)
    {
	MutableShipStatsAPI stats = fighter.getMutableStats();

	stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_REDUCTION);
	stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_REDUCTION);
    }    
 
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec)
    {
        float pad = 10f;
        float padS = 2f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/istl_hullmod_defense.png", 40);
            text.addPara("- " + getString("BBDefenseDesc1"), pad, Misc.getHighlightColor(), "20%");
            text.addPara("- " + getString("BBDefenseDesc2"), padS, Misc.getHighlightColor(), "20%");
            text.addPara("- " + getString("BBDefenseDesc3"), padS, Misc.getHighlightColor(), "100%");
        TooltipMakerAPI text2 = tooltip.beginImageWithText("graphics/ISTL/icons/tooltip/istl_hullmod_fighter.png", 40);
            text2.addPara("- " + getString("BBDefenseDescFtr"), padS, Misc.getHighlightColor(), "20%");
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