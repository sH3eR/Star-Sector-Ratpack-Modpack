package shmoneelse.sicdustkeeper.hullmods;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;


public class AIPlayground extends BaseHullMod {
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        if(stats == null) return;
        if(stats.getFleetMember() == null) return;
        float mult = 2f;

        if(stats.getFleetMember().getCaptain() != null) {
            if (stats.getFleetMember().getCaptain().isAICore()) {
                mult += stats.getFleetMember().getCaptain().getMemoryWithoutUpdate().getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT);
            }
        }

        if(HullSize.DESTROYER.equals(hullSize))
            mult *= 1.25f;
        if(HullSize.CRUISER.equals(hullSize))
            mult *= 1.5f;
        if(HullSize.CAPITAL_SHIP.equals(hullSize))
            mult *= 2f;

        stats.getSuppliesPerMonth().modifyMult("AIPlayground", mult);
        stats.getVariant().addTag(Tags.TAG_AUTOMATED_NO_PENALTY);
    }

    private int getInterfaceCount(MutableShipStatsAPI stats) { // implementation lifted from Emergent Threats Abomination Interface
        int interfaceCount = 0;
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return interfaceCount;


        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();

        List<MarketAPI> marketList = Global.getSector().getEconomy().getMarketsCopy();
        for (MarketAPI market : marketList) {
            if (market.getSubmarket(Submarkets.SUBMARKET_STORAGE) != null) {
                CargoAPI storage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
                List<FleetMemberAPI> storageList = storage.getMothballedShips().getMembersListCopy();
                if (!storageList.isEmpty()) {
                    for (FleetMemberAPI ship : storageList) fleetList.add(ship);
                }
            }
        }

        for (FleetMemberAPI member : fleetList) {
            if (member.getVariant().hasHullMod("AIPlayground")) interfaceCount++;
        }
        return interfaceCount;
    }

    public boolean isModuleCheck(ShipAPI ship) { // implementation lifted from Emergent Threats Remnant Subsystem Util
        if (ship.isStationModule() || ship.getParentStation() != null) return true;
        ShipHullSpecAPI spec = ship.getVariant().getHullSpec();
        if (spec.getSuppliesPerMonth() == 0 && spec.getEngineSpec().getMaxSpeed() == 0) return true;
        return false;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        if(!ship.getVariant().hasHullMod("automated")) return false;
        if(getInterfaceCount(ship.getMutableStats()) >= Global.getSector().getPlayerStats().getOfficerNumber().getModifiedInt()) return false;
        if(isModuleCheck(ship)) return false;
        return true;
    }


    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if(!ship.getVariant().hasHullMod("automated")) return "Ship is not automated";
        if(getInterfaceCount(ship.getMutableStats()) >= Global.getSector().getPlayerStats().getOfficerNumber().getModifiedInt()) return "Too many integration centers installed in the fleet";
        if(isModuleCheck(ship)) return "Integration centers are installed on the central hub";
        return null; // Shouldn't ever happen but if it does then it will crash the game. Yay.
    }
}
