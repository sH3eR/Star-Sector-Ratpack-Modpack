package org.niatahl.tahlan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.HashSet;
import java.util.Set;

import static org.niatahl.tahlan.utils.Utils.txt;

public class SilberherzMinor extends BaseHullMod {

    private static final String SILBER_ID = "Silberherz_minor_ID";
    private static final float DEBUFF_FACTOR = 0.7f;
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("safetyoverrides");
    }
	
	private static final float PD_PERCENT = 50f;
	private static final float WEAPON_HP = 50f;

    private boolean runOnce =  false;
    private String origPersonality = "steady";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

	    stats.getDamageToFighters().modifyPercent(id,PD_PERCENT);
	    stats.getDamageToMissiles().modifyPercent(id,PD_PERCENT);
	    stats.getWeaponHealthBonus().modifyPercent(id,WEAPON_HP);

	}

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {

	    boolean hasGantry = false;

        for (FleetMemberAPI ship: member.getFleetData().getMembersListCopy()) {
            if (ship.getVariant().hasHullMod("tahlan_regaliagantry")) {
                hasGantry = true;
            }
        }

        if (!hasGantry) {
            member.getStats().getSuppliesPerMonth().modifyMult(SILBER_ID,2f);
            member.getStats().getBaseCRRecoveryRatePercentPerDay().modifyMult(SILBER_ID,0.5f);
        } else {
            member.getStats().getSuppliesPerMonth().unmodify(SILBER_ID);
            member.getStats().getBaseCRRecoveryRatePercentPerDay().unmodify(SILBER_ID);
        }

    }

    public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)PD_PERCENT + txt("%");
		if (index == 1) return "" + (int)WEAPON_HP + txt("%");
        if (index == 2) return txt("hmd_silberherz4");
        if (index == 3) return txt("hmd_silberherz6");
        if (index == 4) return txt("hmd_silberherz7");
		return null;
	}
	

}
