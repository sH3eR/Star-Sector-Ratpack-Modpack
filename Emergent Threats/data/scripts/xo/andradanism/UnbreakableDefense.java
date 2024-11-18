package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class UnbreakableDefense extends SCBaseSkillPlugin {
    
	//reactive combat shields 10% damage reduction bonus handled by hullmod class
	private static float ARMOR_BONUS = 5f;
	private static float ARMOR_BONUS_LG = 10f;
	
	private static float FLUX_BONUS = 5f;
	private static float FLUX_BONUS_LG = 10f;	
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }
    
	@Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("We shield ourselves from the sun, for we are not fools. But we do not hide from it, for we are not cowards. So to with our enemies.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("+10%% flux capacity and armor for Lion's Guard ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("+5%% flux capacity and armor for all other ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
	}

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        String manufacturer = variant.getHullSpec().getManufacturer();
		if (manufacturer.equals("Lion's Guard")) {
			stats.getArmorBonus().modifyPercent(id, ARMOR_BONUS_LG);
			stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS_LG);
		}
		else {
			stats.getArmorBonus().modifyPercent(id, ARMOR_BONUS);
			stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS);
		}
    }
}