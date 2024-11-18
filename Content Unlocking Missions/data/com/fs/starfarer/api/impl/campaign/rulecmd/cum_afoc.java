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
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class cum_afoc extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                 if (params.size() > 0) {Misc.fadeAndExpire(dialog.getInteractionTarget());return true;}
		MemoryAPI memory = dialog.getInteractionTarget().getMemoryWithoutUpdate();
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 100);
                BaseSalvageSpecial.clearExtraSalvage(dialog.getInteractionTarget());
                BaseSalvageSpecial.clearExtraSalvage(memoryMap);
                CargoAPI extraSalvage = Global.getFactory().createCargo(true);
		extraSalvage.addCommodity(Commodities.ORE, 12f);
                extraSalvage.addCommodity(Commodities.HAND_WEAPONS, Math.round((Math.max(100f, 100f+random.nextFloat()*40f))));
                extraSalvage.addCommodity(Commodities.ORGANS, Math.round(random.nextFloat()*1f));
                dialog.getVisualPanel().showLoot("Salvaged", extraSalvage, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
			}
		});
                ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                ShipVariantAPI variant = Global.getSettings().getVariant("mule_d_pirates_Smuggler").clone();
                variant.setSource(VariantSource.REFIT);
                variant.addPermaMod(HullMods.EXPANDED_CARGO_HOLDS, true);
                variant.addPermaMod(HullMods.INSULATEDENGINE, true);
                data.addShip(new ShipRecoverySpecial.PerShipData(variant, ShipRecoverySpecial.ShipCondition.PRISTINE, "Cherenkov Bloom", Factions.NEUTRAL, 0f));
                Misc.setSalvageSpecial(dialog.getInteractionTarget(), data);
                return true;
	}
        
}

