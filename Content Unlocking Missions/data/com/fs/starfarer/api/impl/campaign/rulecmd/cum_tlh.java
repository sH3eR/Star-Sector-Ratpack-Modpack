package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;


public class cum_tlh extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                //BaseSalvageSpecial.clearExtraSalvage(dialog.getInteractionTarget());
                //BaseSalvageSpecial.clearExtraSalvage(memoryMap);
                CargoAPI extraSalvage = Global.getFactory().createCargo(true);
		extraSalvage.addSpecial(Global.getSector().getSeedString().hashCode() % 2 > 0 ? new SpecialItemData("cryoarithmetic_engine", null) : new SpecialItemData("soil_nanites", null), 1);
                dialog.getVisualPanel().showLoot("Salvaged", extraSalvage, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
			}
		});
                return true;
	}
        
}

