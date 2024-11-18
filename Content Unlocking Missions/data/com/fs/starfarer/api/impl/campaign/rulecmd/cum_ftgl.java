package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.ArrayList;
import java.util.Random;


public class cum_ftgl extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                if (params.size() == 0) {dialog.getVisualPanel().showPersonInfo(Global.getSector().getFaction(Factions.LUDDIC_PATH).createRandomPerson());}
                if (params.size() == 1) {List<SalvageEntityGenDataSpec.DropData> dropRandom = new ArrayList<SalvageEntityGenDataSpec.DropData>();
		List<SalvageEntityGenDataSpec.DropData> dropValue = new ArrayList<SalvageEntityGenDataSpec.DropData>();
		SalvageEntityGenDataSpec.DropData d = new SalvageEntityGenDataSpec.DropData();	
		d.chances = 10;	
		d.group = "high_weapons2";
		dropRandom.add(d);	
               
		CargoAPI result = SalvageEntity.generateSalvage(Misc.getRandom(new Random().nextLong(), 11), 1f, 1f, params.get(0).getFloat(memoryMap), 1f, dropValue, dropRandom);
                
                    for (CargoStackAPI weapon : result.getStacksCopy()) {
                        Global.getSector().getPlayerFleet().getCargo().addFromStack(weapon);
                        AddRemoveCommodity.addStackGainText(weapon, dialog.getTextPanel());
                    }
                }
                if (params.size() == 2) {
                    if (dialog.getInteractionTarget() == null) return false;
                    if (dialog.getInteractionTarget().getStarSystem() == null) return false;
                    return (params.get(1).getString(memoryMap).equals(dialog.getInteractionTarget().getStarSystem().getBaseName()));
                }
		return true;
	}
}

