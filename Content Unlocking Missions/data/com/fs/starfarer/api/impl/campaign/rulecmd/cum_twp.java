package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class cum_twp extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		MemoryAPI memory = dialog.getInteractionTarget().getMemoryWithoutUpdate();
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 100);
                BaseSalvageSpecial.clearExtraSalvage(dialog.getInteractionTarget());
                BaseSalvageSpecial.clearExtraSalvage(memoryMap);
                CargoAPI extraSalvage = Global.getFactory().createCargo(true);
                extraSalvage.addFuel(Math.round((Math.max(40f, 40f+random.nextFloat()*60f))));
		extraSalvage.addCommodity(Commodities.VOLATILES, Math.round((Math.max(200f, 200f+random.nextFloat()*200f))));
                
                dialog.getVisualPanel().showLoot("Salvaged", extraSalvage, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
			}
		});
                ShipVariantAPI variant = Global.getSettings().getVariant("buffalo_hegemony_Standard").clone();
                variant.addPermaMod(HullMods.EXPANDED_CARGO_HOLDS, true);
                ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                data.addShip(new ShipRecoverySpecial.PerShipData(variant, ShipRecoverySpecial.ShipCondition.PRISTINE, Global.getSector().getFaction(Factions.HEGEMONY).pickRandomShipName(), Factions.NEUTRAL, 0f));
                data.addShip(new ShipRecoverySpecial.PerShipData(variant, ShipRecoverySpecial.ShipCondition.PRISTINE, Global.getSector().getFaction(Factions.HEGEMONY).pickRandomShipName(),Factions.NEUTRAL, 0f));
                data.addShip(new ShipRecoverySpecial.PerShipData(variant, ShipRecoverySpecial.ShipCondition.PRISTINE, Global.getSector().getFaction(Factions.HEGEMONY).pickRandomShipName(), Factions.NEUTRAL, 0f));
                data.addShip(new ShipRecoverySpecial.PerShipData(variant, ShipRecoverySpecial.ShipCondition.PRISTINE, Global.getSector().getFaction(Factions.HEGEMONY).pickRandomShipName(), Factions.NEUTRAL, 0f));
                Misc.setSalvageSpecial(dialog.getInteractionTarget(), data);
                return true;
	}
        
}

