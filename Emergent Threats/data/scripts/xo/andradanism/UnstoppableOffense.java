package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class UnstoppableOffense extends SCBaseSkillPlugin {
    
	private static float ENERGY_DAMAGE = 0.05f;
	private static float ENERGY_DAMAGE_LG = 0.10f;
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("The weak fear the spiritual potency of the killing machine. The strong instinctually recognize its beauty.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("+10%% energy weapon damage for Lion's Guard ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("+5%% energy weapon damage for all other ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        String manufacturer = variant.getHullSpec().getManufacturer();
		if (manufacturer.equals("Lion's Guard")) {
			stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_DAMAGE_LG);
		}
		else {
			stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_DAMAGE);
		}
    }
}
