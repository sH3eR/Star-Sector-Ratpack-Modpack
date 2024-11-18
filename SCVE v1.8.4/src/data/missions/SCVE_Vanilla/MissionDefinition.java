package data.missions.SCVE_Vanilla;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.TreeSet;

import static data.scripts.SCVE_ComparatorUtils.memberComparator;
import static data.scripts.SCVE_FilterUtils.blacklistedShips;
import static data.scripts.SCVE_ModPlugin.MOD_PREFIX;
import static data.scripts.SCVE_Utils.*;

public class MissionDefinition implements MissionDefinitionPlugin {

    private final Logger log = Global.getLogger(MissionDefinition.class);

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // initialize
        initializeMission(api, getString("vanillaTagline"), null);

        boolean flagship = true;
        for (FleetMemberAPI member : getVanillaFleetMembers(blacklistedShips)) {
            // don't use api.addFleetMember() because then the ships start at 0 CR
            String variantId = member.getVariant().getHullVariantId();
            FleetMemberAPI ship = api.addToFleet(FleetSide.PLAYER, variantId, FleetMemberType.SHIP,
                    MOD_PREFIX + " " + member.getHullId(), flagship);
            if (flagship) {
                flagship = false;
            }
        }
    }

    // this method is simpler for grabbing vanilla ships than relying on the listMap
    public static Set<FleetMemberAPI> getVanillaFleetMembers(Set<String> blacklist) {
        Set<FleetMemberAPI> fleetMemberSet = new TreeSet<>(memberComparator);
        for (ShipHullSpecAPI shipHullSpec : Global.getSettings().getAllShipHullSpecs()) {
            if (!shipHullSpec.getShipFilePath().startsWith("data") && validateHullSpec(shipHullSpec, blacklist)) {
                String hullVariantId = shipHullSpec.getHullId() + HULL_SUFFIX;
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, hullVariantId);
                fleetMemberSet.add(member);
            }
        }
        return fleetMemberSet;
    }
}