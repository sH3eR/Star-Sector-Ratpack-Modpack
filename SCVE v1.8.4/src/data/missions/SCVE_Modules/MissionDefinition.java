package data.missions.SCVE_Modules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.util.Pair;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.*;

import static data.scripts.SCVE_ComparatorUtils.memberComparator;
import static data.scripts.SCVE_FilterUtils.blacklistedShips;
import static data.scripts.SCVE_ModPlugin.MOD_PREFIX;
import static data.scripts.SCVE_ModPlugin.modToHull;
import static data.scripts.SCVE_Utils.*;

public class MissionDefinition implements MissionDefinitionPlugin {

    private final Logger log = Global.getLogger(data.missions.SCVE_Mods.MissionDefinition.class);

    private static boolean firstLoad = true;
    private static int currentSelection;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // initialize
        if (modToHull.size() == 0) {
            initializeMission(api, getString("modNoMods"), null);
            api.addToFleet(FleetSide.PLAYER, Global.getSettings().getString("errorShipVariant"), FleetMemberType.SHIP,
                    getString("modNoMods"), false);
        } else {
            String currentModId = getCurrentMod();
            String currentModName = Global.getSettings().getModManager().getModSpec(currentModId).getName();
            createModListBriefing(api);
            initializeMission(api, String.format(getString("modTagline"), currentModName), currentModId);

            List<String> shipList = new ArrayList<>(modToHull.getList(currentModId)); // make new ship list so removing doesn't affect the original list
            shipList.removeAll(blacklistedShips);

            // don't use api.addFleetMember() because then the ships start at 0 CR
            if (shipList.isEmpty()) {
                api.addToFleet(FleetSide.PLAYER, Global.getSettings().getString("errorShipVariant"), FleetMemberType.SHIP,
                        getString("modNoShips"), true);
            } else {
                boolean flagship = true;
                for (FleetMemberAPI member : getModularModFleetMembers(shipList)) {
                    int numModules = member.getVariant().getStationModules().size();
                    String variantId = member.getVariant().getHullVariantId();
                    FleetMemberAPI ship = api.addToFleet(FleetSide.PLAYER, variantId, FleetMemberType.SHIP,
                            MOD_PREFIX + " " + member.getHullId(), flagship);
                    if (flagship) {
                        flagship = false;
                    }
                    for (int i = 1; i < numModules; i++) {
                        api.addToFleet(FleetSide.PLAYER, Global.getSettings().getString("errorShipVariant"), FleetMemberType.SHIP,
                                "PLACEHOLDER", false);
                    }
                }
            }
        }
    }

    public String getCurrentMod() {
        String currentMod = null;
        if (firstLoad) {
            currentSelection = 0;
            firstLoad = false;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            currentSelection++;
            if (currentSelection >= modToHull.size()) {
                currentSelection = 0;
            }
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            currentSelection--;
            if (currentSelection < 0) {
                currentSelection = Math.max(0, modToHull.size() - 1);
            }
        }
        if (!modToHull.isEmpty()) {
            ArrayList<Pair<String, String>> modIdNamePairList = new ArrayList<>();
            for (String modId : modToHull.keySet()) {
                modIdNamePairList.add(new Pair<>(modId, Global.getSettings().getModManager().getModSpec(modId).getName()));
            }
            Collections.sort(modIdNamePairList, modComparator);
            currentMod = modIdNamePairList.get(currentSelection).one;
        }
        return currentMod;
    }

    public void createModListBriefing(MissionDefinitionAPI api) {
        ArrayList<Pair<String, String>> modIdNamePairList = new ArrayList<>();
        for (String modId : modToHull.keySet()) {
            modIdNamePairList.add(new Pair<>(modId, Global.getSettings().getModManager().getModSpec(modId).getName()));
        }
        Collections.sort(modIdNamePairList, modComparator);
        String currentMod = modIdNamePairList.get(currentSelection).one;
        ArrayList<String> modList = new ArrayList<>();
        // use names if < 15
        if (modIdNamePairList.size() <= 15) {
            for (Pair<String, String> modIdNamePair : modIdNamePairList) {
                if (currentMod.equals(modIdNamePair.one)) {
                    modList.add(getString("modBriefingHL")
                            + modIdNamePair.two
                            + getString("modBriefingHL"));
                } else {
                    modList.add(modIdNamePair.two);
                }
            }
            // else use ids
        } else {
            for (Pair<String, String> modIdNamePair : modIdNamePairList) {
                if (currentMod.equals(modIdNamePair.one)) {
                    modList.add(getString("modBriefingHL")
                            + modIdNamePair.one
                            + getString("modBriefingHL"));
                } else {
                    modList.add(modIdNamePair.one);
                }
            }
        }
        api.addBriefingItem("");
        api.addBriefingItem("");
        api.addBriefingItem(getString("modBriefing") + modList);
    }

    Comparator<Pair<String, String>> modComparator = new Comparator<Pair<String, String>>() {
        @Override
        public int compare(Pair<String, String> o1, Pair<String, String> o2) {
            return o1.two.compareToIgnoreCase(o2.two); //sort by names
        }
    };

    public static Set<FleetMemberAPI> getModularModFleetMembers(List<String> modShipIds) {
        Set<FleetMemberAPI> fleetMemberSet = new TreeSet<>(memberComparator);
        for (String hullId : modShipIds) {
            String hullVariantId = hullId + HULL_SUFFIX;
            ShipVariantAPI variant = Global.getSettings().getVariant(hullVariantId);
            int numModules = variant.getStationModules().size();
            if (numModules == 0) {
                continue;
            }
            FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, hullVariantId);
            fleetMemberSet.add(member);
        }
        return fleetMemberSet;
    }
}