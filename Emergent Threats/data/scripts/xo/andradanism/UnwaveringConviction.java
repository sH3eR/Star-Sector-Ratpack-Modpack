package data.scripts.xo.andradanism;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class UnwaveringConviction extends SCBaseSkillPlugin {
    
	private static float FLUX_THRESHOLD_LOW = 50f;
	private static float FLUX_THRESHOLD_HIGH = 75f;
	private static float ENERGY_ROF_BONUS_LOW = 15f;
	private static float ENERGY_ROF_BONUS_HIGH = 20f;
	private static String ID = "xo_andrada_unwavering_conviction";
	
	@Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }
    
	@Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
		tooltip.addPara("Victory always comes at a cost, and the heroic dead have paid it willingly.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addPara("  -Quotations from the Supreme Executor", 0f, Misc.getTextColor(), Misc.getHighlightColor());
		tooltip.addSpacer(10f);
		tooltip.addPara("+15%% energy weapon rate of fire when flux is over 50%%", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
		tooltip.addPara("+25%% energy weapon rate of fire when flux is over 75%%", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
	}
	
	@Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {
		if (!ship.isAlive()) return;
		if (ship.getFluxLevel() >= FLUX_THRESHOLD_HIGH * 0.01f) {
			ship.getMutableStats().getEnergyRoFMult().modifyPercent(ID, ENERGY_ROF_BONUS_HIGH);
		}
		else if (ship.getFluxLevel() >= FLUX_THRESHOLD_LOW * 0.01f) {
			ship.getMutableStats().getEnergyRoFMult().modifyPercent(ID, ENERGY_ROF_BONUS_LOW);
		}
		else ship.getMutableStats().getEnergyRoFMult().unmodify(ID);
	}
}