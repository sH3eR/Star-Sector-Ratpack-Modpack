package data.scripts.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetScript;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import org.lwjgl.util.vector.Vector2f;

public class MN_personalFleetPyotr extends PersonalFleetScript {

    public MN_personalFleetPyotr() {
        super("msspyotr");
        setMinRespawnDelayDays(10f);
        setMaxRespawnDelayDays(20f);
    }

    @Override
    public CampaignFleetAPI spawnFleet() {

        MarketAPI mairaath = Global.getSector().getEconomy().getMarket("mairaath");

        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();

        Vector2f loc = mairaath.getLocationInHyperspace();

        m.triggerCreateFleet(HubMissionWithTriggers.FleetSize.MAXIMUM, HubMissionWithTriggers.FleetQuality.SMOD_2, "mayasuran_guard", FleetTypes.TASK_FORCE, loc);
        m.triggerSetFleetOfficers( HubMissionWithTriggers.OfficerNum.MORE, HubMissionWithTriggers.OfficerQuality.UNUSUALLY_HIGH);
        m.triggerSetFleetCommander(getPerson());
        m.triggerSetFleetFaction("mayasura");
        m.triggerSetPatrol();
        m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, mairaath);
        m.triggerFleetSetNoFactionInName();
        m.triggerFleetSetName("The Vengeance of Mairaath");
        m.triggerPatrolAllowTransponderOff();
        m.triggerOrderFleetPatrol(mairaath.getStarSystem());

        CampaignFleetAPI fleet = m.createFleet();
        FleetMemberAPI oldFlagship = fleet.getFlagship();
        FleetMemberAPI newFlagship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "MSS_Victory_Elite");
        fleet.getFleetData().addFleetMember(newFlagship);
        if (newFlagship != null && oldFlagship != null) {
            newFlagship.setCaptain(oldFlagship.getCaptain());
            oldFlagship.setFlagship(false);
            newFlagship.setFlagship(true);
            fleet.getFleetData().setFlagship(newFlagship);
            fleet.getFleetData().removeFleetMember(oldFlagship);}
        fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
        mairaath.getContainingLocation().addEntity(fleet);
        fleet.setLocation(mairaath.getPlanetEntity().getLocation().x, mairaath.getPlanetEntity().getLocation().y);
        fleet.setFacing((float) random.nextFloat() * 360f);
        fleet.getFlagship().setShipName("MSS Mairaath Arisen");
        fleet.getFleetData().sort();
        return fleet;
    }

    @Override
    public boolean canSpawnFleetNow() {
        MarketAPI mairaath = Global.getSector().getEconomy().getMarket("mairaath");
        if (mairaath == null || mairaath.hasCondition(Conditions.DECIVILIZED)) return false;
        if (!mairaath.hasIndustry("elitemayasuranguard")) return false;
        return true;
    }

    @Override
    public boolean shouldScriptBeRemoved() {
        return false;
    }
}
