package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.hmi_Tags;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.awt.*;
import java.util.*;

import static com.fs.starfarer.api.campaign.AICoreOfficerPlugin.AUTOMATED_POINTS_MULT;

// Originally by Nia Tahl in Tahlan Shipworks as the Daemon Core, used with permission by King Alfonzo

public class HMI_RemMessCore extends BaseHullMod {

    private static final Map<HullSize, Integer> MAG = new HashMap<>();
    static {
        MAG.put(HullSize.FRIGATE, 0);
        MAG.put(HullSize.DESTROYER, 1);
        MAG.put(HullSize.CRUISER, 2);
        MAG.put(HullSize.CAPITAL_SHIP, 3);
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
        if (!member.getFleetCommander().getFaction().getId().contains("mess_remnant")) {
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

        int die = MathUtils.getRandomNumberInRange(1, 8) - MAG.get(member.getHullSpec().getHullSize());


        PersonAPI person; // yes, a "person"
            if (die <= 1) {
                person = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE).createPerson(Commodities.ALPHA_CORE, "mess_remnant", Misc.random);
                member.setCaptain(person);
            }
            if (die > 1 && die <= 3) {
                person = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE).createPerson(Commodities.BETA_CORE, "mess_remnant", Misc.random);
                member.setCaptain(person);
            }
            if (die > 3 && die < 5) {
                person = Misc.getAICoreOfficerPlugin(Commodities.GAMMA_CORE).createPerson(Commodities.GAMMA_CORE, "mess_remnant", Misc.random);
                member.setCaptain(person);
            }
            if (die >= 5) {
                person = createPerson(hmi_Tags.MESSCORE, "mess_remnant", Misc.random);
                member.setCaptain(person);
            }

    }
    PersonAPI createPerson(String aiCoreId, String factionId, Random random) {
        if (random == null) random = new Random();

        PersonAPI person = Global.getFactory().createPerson();
        person.setFaction(factionId);
        person.setAICoreId(aiCoreId);

        CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(aiCoreId);
        boolean mess = hmi_Tags.MESSCORE.equals(aiCoreId);

        person.getStats().setSkipRefresh(true);

        person.setName(new FullName(spec.getName(), "", FullName.Gender.ANY));
        int points = 0;
        float mult = 1f;
        if (mess) {
            person.setPortraitSprite("graphics/portraits/portrait_static.png");
            person.getStats().setLevel(3);
            person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
            person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
            person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
        }
        person.getMemoryWithoutUpdate().set(AUTOMATED_POINTS_MULT, mult);

        person.setPersonality(Personalities.RECKLESS);
        person.setRankId(Ranks.SPACE_CAPTAIN);
        person.setPostId(null);

        person.getStats().setSkipRefresh(false);

        return person;
    }
}
