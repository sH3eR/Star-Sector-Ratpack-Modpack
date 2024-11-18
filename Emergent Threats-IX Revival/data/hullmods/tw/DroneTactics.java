package data.hullmods.tw;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class DroneTactics extends BaseHullMod {

    private static float RANGE_BONUS = 1000f;
	private static String NODE_NAME = "Drone Control Node";
	private static String DRONE_NAME = "Nimbus Combat Drones";
	private static String TW_MOD = "tw_drone_control_node";
	
	private static String NIMBUS_PREF = "nimbus_tw_";
	private static String NIMBUS_REM = "vice_nimbus_wing";
	
	private static boolean IS_ACTIVE = false;
	
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean isAllValidDrones = true;
		if (!stats.getVariant().hasHullMod(TW_MOD)) isAllValidDrones = false;
		else {
			int fighterBays = stats.getNumFighterBays().getModifiedInt();
			for (int i = 0; i < fighterBays; i++) {
				if (stats.getVariant() == null || stats.getVariant().getWingId(i) == null) continue;
				else if (isValidDrone(stats.getVariant().getWingId(i)) == false) isAllValidDrones = false;
			}
		}
		if (isAllValidDrones) stats.getFighterWingRange().modifyFlat(id, RANGE_BONUS);
		IS_ACTIVE = isAllValidDrones;
    }
	
	private boolean isValidDrone(String id) {
		return (id.equals(NIMBUS_REM) || id.startsWith(NIMBUS_PREF));
	}
	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) RANGE_BONUS;
		if (index == 1) return NODE_NAME;
		if (index == 2) return DRONE_NAME;
        return null;
    }
	
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		String bonusIsActive = IS_ACTIVE ? "active" : "inactive";
		tooltip.addPara("Range bonus is %s", 10f, Misc.getHighlightColor(), bonusIsActive);
		
		String s = "\"Strike first, strike last, and you shall be ever victorious.\"";
        tooltip.addPara("%s", 6f, Misc.getGrayColor(), s);
    }
	
    @Override
    public Color getNameColor() {
        return new Color(190,255,150,255);
    }
}