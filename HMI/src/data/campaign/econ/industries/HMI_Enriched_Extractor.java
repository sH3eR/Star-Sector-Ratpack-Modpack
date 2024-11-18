package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;

public class HMI_Enriched_Extractor extends BaseIndustry {

    @Override
    public void apply() {

        super.apply(true);
        int size = market.getSize();

        demand(Commodities.HEAVY_MACHINERY, size + 2);
        demand(Commodities.SUPPLIES, size + 2);
        demand(Commodities.CREW, size );

        supply(Commodities.VOLATILES, size + 3);
        supply(Commodities.FUEL, size + 3);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY, Commodities.SUPPLIES, Commodities.CREW);

        applyDeficitToProduction(1, deficit, Commodities.VOLATILES, Commodities.FUEL);


    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }
}
