package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;


public class cum_np extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                /*We do this instead*/if (params.isEmpty()) {Global.getSoundPlayer().playSound("cr_allied_warning", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f()); return false;}
                if (params.size() > 2) {
                    CargoAPI extraSalvage = Global.getFactory().createCargo(true);
                    if (params.get(2).getInt(memoryMap) == 2) {
                        extraSalvage.addWeapons(Global.getSector().getSeedString().hashCode() % 4 > 0 ? Global.getSector().getSeedString().hashCode() % 4 > 1 ? Global.getSector().getSeedString().hashCode() % 4 > 2 ? "amsrm" : "shockrepeater" : "riftlance" : "minipulser", 2);
                        Global.getSector().getPlayerFleet().getCargo().addWeapons(Global.getSector().getSeedString().hashCode() % 4 > 0 ? Global.getSector().getSeedString().hashCode() % 4 > 1 ? Global.getSector().getSeedString().hashCode() % 4 > 2 ? "amsrm" : "shockrepeater" : "riftlance" : "minipulser", 2);
                        AddRemoveCommodity.addStackGainText(extraSalvage.getStacksCopy().get(0), dialog.getTextPanel());
                        return true;
                    }
                    if (params.get(2).getInt(memoryMap) == 1) {
                        extraSalvage.addWeapons(Global.getSector().getSeedString().hashCode() % 5 > 0 ? Global.getSector().getSeedString().hashCode() % 5 > 1 ? Global.getSector().getSeedString().hashCode() % 5 > 2 ? Global.getSector().getSeedString().hashCode() % 5 > 3 ? "resonatormrm" : "riftbeam" : "disintegrator" : "cryoflux" : "cryoblaster", 1);
                        Global.getSector().getPlayerFleet().getCargo().addWeapons(Global.getSector().getSeedString().hashCode() % 5 > 0 ? Global.getSector().getSeedString().hashCode() % 5 > 1 ? Global.getSector().getSeedString().hashCode() % 5 > 2 ? Global.getSector().getSeedString().hashCode() % 5 > 3 ? "resonatormrm" : "riftbeam" : "disintegrator" : "cryoflux" : "cryoblaster", 1);
                        AddRemoveCommodity.addStackGainText(extraSalvage.getStacksCopy().get(0), dialog.getTextPanel());
                        return true;
                    }
                }
                if (Global.getSector().getPlayerStats() != null && Global.getSector().getPlayerStats().getSkillsCopy() != null) {
                    int techlevel = 0;
                    for (SkillLevelAPI skill : Global.getSector().getPlayerStats().getSkillsCopy()) {
                        if ((skill.getLevel() > 0) && "technology".equals(skill.getSkill().getGoverningAptitudeId())) {
                            techlevel++;
                        }
                    }
                    if (techlevel >= params.get(0).getInt(memoryMap)) {Global.getSoundPlayer().playSound(params.get(1).getString(memoryMap), 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());
                    } /* We don't do that here else {Global.getSoundPlayer().playSound("cr_allied_warning", 1, 1, Global.getSoundPlayer().getListenerPos(), new Vector2f());}*/
                    return techlevel >= params.get(0).getInt(memoryMap);
                }
                return false;
	}
}

