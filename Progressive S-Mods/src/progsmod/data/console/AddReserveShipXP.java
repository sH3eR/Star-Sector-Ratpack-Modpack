package progsmod.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import util.SModUtils;

public class AddReserveShipXP implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        InteractionDialogAPI currentDialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if (currentDialog == null) {
            return wrongContext();
        }

        SectorEntityToken interactionTarget = currentDialog.getInteractionTarget();
        if (interactionTarget == null) {
            return wrongContext();
        }

        FleetMemberAPI fleetMember = (FleetMemberAPI) interactionTarget.getMemory().get("$selectedShip");
        if (fleetMember == null) {
            return wrongContext();
        }

        int amount;
        try {
            amount = Integer.parseInt(args);
        } catch (NumberFormatException e) {
            Console.showMessage("Specified XP amount must be an integer.");
            return CommandResult.BAD_SYNTAX;
        }

        SModUtils.addReserveXP(fleetMember.getHullSpec().getBaseHullId(), amount);
        SModUtils.displayXP(currentDialog, fleetMember);
        if (SModUtils.forceUpdater != null) {
            SModUtils.forceUpdater.addReserveXP(amount);
        }
        return CommandResult.SUCCESS;
    }

    public CommandResult wrongContext() {
        Console.showMessage("This command only works while a ship is selected for S-Mod modifications.");
        return CommandResult.WRONG_CONTEXT;
    }
}
