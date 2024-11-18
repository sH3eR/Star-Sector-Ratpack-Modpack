package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Originally by Nia Tahl in Tahlan Shipworks as the Daemon Core, used with permission by King Alfonzo

public class HMI_MaxwellBoost extends BaseHullMod {

    private static final Map<HullSize, Integer> MAG = new HashMap<>();
    static {
        MAG.put(HullSize.FRIGATE, 0);
        MAG.put(HullSize.DESTROYER, 1);
        MAG.put(HullSize.CRUISER, 2);
        MAG.put(HullSize.CAPITAL_SHIP, 3);
    }


    public static float BOOST = 10f;
    public static float MALUS = 33f;
    public static float MALUS2 = 66f;

    public static float BONUS_CHARGES = 1f;
    public static float COOLDOWN_PERCENT = 15f;
    public static float REGEN_PERCENT = 25f;
    public static float RANGE_PERCENT = 50f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getTimeMult().modifyMult(id, 1f + (0.01f * BOOST));
        stats.getEnergyWeaponDamageMult().modifyMult(id, 1f + (0.01f * BOOST));
        stats.getPeakCRDuration().modifyMult(id, 1f - (0.01f * MALUS));

        stats.getSystemUsesBonus().modifyFlat(id, BONUS_CHARGES);
        stats.getSystemRegenBonus().modifyPercent(id, REGEN_PERCENT);
        stats.getSystemRangeBonus().modifyPercent(id, RANGE_PERCENT);
        stats.getSystemCooldownBonus().modifyMult(id, 1f - (COOLDOWN_PERCENT/100f));
    }


    public void advanceInCombat(ShipAPI ship, float amount) {

        String id = "HMI_MaxwellDie";
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
            if (ship.isPhased()) {
                ship.getMutableStats().getPeakCRDuration().modifyMult(id, 1f - (0.01f * MALUS2));
            }

            if (!ship.isPhased()) {
                ship.getMutableStats().getPeakCRDuration().unmodify(id);
            }
        }


    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {

        // Don't do this if we're in player fleet
        if (member.getFleetCommander().isPlayer() || member.getFleetCommander().isDefault()) {
            return;
        }

        // Another check, I guess
        if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null) {
            for (FleetMemberAPI mem : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                if (mem.getId().equals(member.getId())) {
                    return;
                }
            }
        }

        // and another
        if (!member.getFleetCommander().getFaction().getId().contains("hmi_maxwell")) {
            return;
        }

        // Now we make a new captain if we don't have an AI captain already
        if (member.getCaptain() != null) {
            if (member.getCaptain().isAICore()) {
                return;
            }
        }

        // Apparently this can be the case
        if (Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE) == null) {
            return;
        }

        int die = MathUtils.getRandomNumberInRange(1, 5) - MAG.get(member.getHullSpec().getHullSize());

        PersonAPI person; // yes, a "person"
            if (die <= 1) {
                person = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE).createPerson(Commodities.ALPHA_CORE, "hmi_maxwell", Misc.random);
            } else if (die <= 3) {
                person = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE).createPerson(Commodities.BETA_CORE, "hmi_maxwell", Misc.random);
            } else {
                person = Misc.getAICoreOfficerPlugin(Commodities.GAMMA_CORE).createPerson(Commodities.GAMMA_CORE, "hmi_maxwell", Misc.random);
            }
            member.setCaptain(person);
        }


    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BOOST + "%";
        if (index == 1) return "" + (int) BOOST + "%";
        if (index == 2) return "an extra charge";
        if (index == 3) return "" + (int) REGEN_PERCENT + "%";
        if (index == 4) return "" + (int) COOLDOWN_PERCENT + "%";
        if (index == 5) return "" + (int) MALUS + "%";
        if (index == 6) return "" + (int) MALUS2 + "%";
        return null;
    }
}
