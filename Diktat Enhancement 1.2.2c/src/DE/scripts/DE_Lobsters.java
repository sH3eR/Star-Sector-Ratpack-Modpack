package DE.scripts;// Thanks for the lobster girls Alfonzo

//import com.fs.starfarer.api.Global;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
//import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class DE_Lobsters extends BaseHazardCondition implements MarketImmigrationModifier {

    private float DEFENSE_BONUS = 3f;

    @Override
    public void apply(String id) {

        super.apply(id);
//        Object test = Global.getSettings().getSpec(ConditionGenDataSpec.class, condition.getId(), true);
//        if (test instanceof ConditionGenDataSpec) {
//            ConditionGenDataSpec spec = (ConditionGenDataSpec) test;
//            float hazard = spec.getHazard();
//            if (hazard != 0) {
//                market.getHazard().modifyFlat(id, hazard, condition.getName());
//            }
//        }

        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DEFENSE_BONUS, "Volturnian Lobster Brutes");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
//        market.getHazard().unmodify(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.removeTransientImmigrationModifier(this);
    }

    private float getThisImmigrationBonus() {
        return -1 * market.getSize();
    } // Probably redundant but also necessary for the below code

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.POOR, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
    }


    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara(
                "+%s defense rating.",
                10f,
                Misc.getHighlightColor(),
                "" + (int) ((DEFENSE_BONUS - 1) * 100) + "%"
        );
    }
}




