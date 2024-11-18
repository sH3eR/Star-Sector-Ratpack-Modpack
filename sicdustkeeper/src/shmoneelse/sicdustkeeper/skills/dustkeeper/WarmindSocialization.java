package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.util.Misc;

public class WarmindSocialization extends DustkeeperBaseAutoPointsSkillPlugin {
    @Override
    public int getProvidedPoints() {
        if(Global.getSector() == null) return 30;
        if(Global.getSector().getPlayerFleet() == null) return 30;
        if(Global.getSector().getPlayerFleet().getCargo() == null) return 30;
        if(Global.getSector().getPlayerFleet().getFleetData() == null) return 30; // Everyone loves null checks

        float crewSurplus = Global.getSector().getPlayerFleet().getCargo().getCrew() - Global.getSector().getPlayerFleet().getFleetData().getMinCrew();
        float retval = 30;

        if(crewSurplus <= 0)
            ; // Do nothing, prevents negative adjustment
        else if(crewSurplus <= 3000)
            retval += (crewSurplus) / 60f; // +50 points at 3000 crew, scales linearly.
        else
            retval += 50;

        return (int) retval; // Cast to int and return
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
        tooltip.addPara("Provides automated ship points that scale with surplus crew", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("Starts at 30 with an additional 50 at 3000 crew, scaling linearly (maximum of 80)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());

        super.addTooltip(data, tooltip);
    }
}