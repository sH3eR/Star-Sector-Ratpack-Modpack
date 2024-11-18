package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.HMI_items;

public class HMI_Quantum_Synthesis extends BaseIndustry {

	public void apply() {
		super.apply(true);

		int size = market.getSize();

		demand(Commodities.SUPPLIES, size );
		demand(Commodities.CREW, size );
		demand(Commodities.METALS, size - 1);
		demand(Commodities.RARE_METALS, size - 3);
		demand(Commodities.HEAVY_MACHINERY, size );

		supply(HMI_items.HMICRYSTAL, size );
		supply(Commodities.ORE, size + 4);
		supply(Commodities.RARE_ORE, size + 2);


		Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY, Commodities.SUPPLIES, Commodities.CREW);
		applyDeficitToProduction(1, deficit, Commodities.ORE, Commodities.RARE_ORE);

		Pair<String, Integer> deficit2 = getMaxDeficit(Commodities.RARE_METALS, Commodities.METALS, Commodities.HEAVY_MACHINERY);
		applyDeficitToProduction(1, deficit2, HMI_items.HMICRYSTAL);

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

