package DE.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;


/**
 *	RemoveShip <fleet member reference>
 */
// A variation of RemoveShip that should work for radiants only

public class DE_RemoveShip extends BaseCommandPlugin {

	/*                for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
		if (member.getVariant().getHullId().equals("radiant")) {
			Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(member);
			addShipLossText(member, dialog.getTextPanel());
			return true;
		}
	}*/

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;

		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
			if (member.getHullId().equals("radiant")) {
				Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(member);
				addShipLossText(member, dialog.getTextPanel());
			}
		}
		return true;
	}

		/*Misc.VarAndMemory var = params.get(0).getVarNameAndMemory(memoryMap);
//		ShipVariantAPI variant = (ShipVariantAPI) var.memory.get(var.name);
//		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);

		FleetMemberAPI member = (FleetMemberAPI) Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
				//(FleetMemberAPI) var.memory.get(var.name);
		if (member.getHullId().equals("radiant")) {
			Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(member);
			addShipLossText(member, dialog.getTextPanel());
		}
		return true;*/


	public static void addShipLossText(FleetMemberAPI member, TextPanelAPI text) {
		text.setFontSmallInsignia();
		text.addParagraph("Lost " + member.getVariant().getFullDesignationWithHullNameForShip(), Misc.getNegativeHighlightColor());
		text.highlightInLastPara(Misc.getHighlightColor(), member.getVariant().getFullDesignationWithHullNameForShip());
		text.setFontInsignia();
	}
}
