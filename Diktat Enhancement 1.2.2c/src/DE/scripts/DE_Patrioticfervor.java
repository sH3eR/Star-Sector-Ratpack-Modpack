package DE.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;



public class DE_Patrioticfervor extends BaseMarketConditionPlugin {

    public static final float STAB_DEF = -5f;
    public static final float STAB_BONUS = 3f;
    boolean isPAGSM = Global.getSettings().getModManager().isModEnabled("PAGSM");

    @Override
    public void apply(String id) {
        super.apply(id);
        if (market.getFaction() != null) {
            if (market.getFaction().getId().contains("sindrian_diktat")) {
                int marketMult = Misc.getFactionMarkets(market.getFactionId()).size();
                if (!isPAGSM) {
                    market.getStability().modifyFlat(id, STAB_BONUS, "Patriotic Fervor");
                }
                else{
                    market.getStability().modifyFlat(id, STAB_BONUS, "Company Loyalty");
                }
            }
            else {
                if (!isPAGSM) {
                    market.getStability().modifyFlat(id, STAB_DEF, "Patriotic Fervor");
                }
                else{
                    market.getStability().modifyFlat(id, STAB_DEF, "Company Loyalty");
                }
                }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getStability().modifyFlat(id, STAB_DEF);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        if (market == null) {
            return;
        }

        int marketMult = Misc.getFactionMarkets(market.getFactionId()).size();
        float pad = 10f;
        float padS = 2f;
        tooltip.addPara(
            "%s stability if controlled by the Sindrian Diktat/Sindrian Fuel Company;",
                pad,
                Misc.getHighlightColor(),
                "+" + (int) STAB_BONUS
        );
        tooltip.addPara(
                "%s reduction in stability otherwise.",
                padS,
                Misc.getHighlightColor(),
                "" + (int) STAB_DEF
        );
        //tooltip.addPara(txt("patriotic fervor"), 10f);
        //tooltip.addPara(txt("patriotic fervor"), 10f, Misc.getHighlightColor(), txt("patriotic fervor"));

        //if (market.getFaction() == null)?
        if (market.getFaction() == null) {
            //if (market.getFaction().getId().contains("neutral")) {
                /*Global.getSector().getClock().convertToMonths(2160);
                try {
                    wait(2160);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                market.setFactionId("sindrian_diktat");
                market.setSize(3);
                market.removeCondition("DE_Patrioticfervor");
           // }
        }
    }
}
