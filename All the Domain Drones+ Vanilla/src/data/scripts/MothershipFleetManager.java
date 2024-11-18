package data.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.enc.EncounterManager;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPoint;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPointProvider;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;

import static javafx.beans.binding.Bindings.and;
import static javafx.beans.binding.Bindings.floatValueAt;


//Just a copy of the Script that spawns fleets for Remnant bases.
public class MothershipFleetManager extends SourceBasedFleetManager {

    public class MothershipSystemEPGenerator implements EncounterPointProvider {
        public List<EncounterPoint> generateEncounterPoints(LocationAPI where) {
            if (!where.isHyperspace()) return null;
            if (totalLost > 0 && source != null) {
                String id = "ep_" + source.getId();
                EncounterPoint ep = new EncounterPoint(id, where, source.getLocationInHyperspace(), EncounterManager.EP_TYPE_OUTSIDE_SYSTEM);
                ep.custom = this;
                List<EncounterPoint> result = new ArrayList<EncounterPoint>();
                result.add(ep);
                return result;//source.getContainingLocation().getName()
            }
            return null;
        }
    }

    protected int minPts;
    protected int maxPts;
    protected int totalLost;
    protected transient MothershipSystemEPGenerator epGen;

    public MothershipFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay,
                                  int minPts, int maxPts) {
        super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
        this.minPts = minPts;
        this.maxPts = maxPts;
    }

    protected Object readResolve() {
        return this;
    }

    protected transient boolean addedListener = false;
    @Override
    public void advance(float amount) {
        if (!addedListener) {
            epGen = new MothershipSystemEPGenerator();
            Global.getSector().getListenerManager().addListener(epGen, true);
            addedListener = true;
        }
        super.advance(amount);
    }


    @Override
    protected CampaignFleetAPI spawnFleet() {
        if (source == null) return null;

        Random random = new Random();

        int combatPoints = minPts + random.nextInt(maxPts - minPts + 1);

        float civilianPoints = 0f;
        float civilianPointsFuel = 0f;
        float civilianPointsUtil = 0f;

            // This is made by AtlanticAccent on Discord after having seen my previous spaghetti code.
        if (combatPoints <= 16) {
            // If less than 16, divide by X and round up
            civilianPoints = (float) Math.ceil((double)combatPoints / 2);
            civilianPointsUtil = (float) Math.ceil((double)combatPoints / 3);
            civilianPointsFuel = (float) Math.ceil((double)combatPoints / 2);
        } else if (combatPoints > 30) {
            // if greater than 30
            civilianPoints = 30f; civilianPointsUtil = 18f; civilianPointsFuel = 30f;
        } else if (combatPoints > 20) {
            // if greater than 20 (and less than 30 implied by not taking above case)
            civilianPoints = 22f; civilianPointsUtil = 16f; civilianPointsFuel = 22f;
        } else {
            // otherwise must be greater than 16 and less than equal 20
            civilianPoints = 18f; civilianPointsUtil = 12f; civilianPointsFuel = 18f;
        }


        int bonus = totalLost * 4;
        if (bonus > maxPts) bonus = maxPts;

        combatPoints += bonus;

        String type = FleetTypes.PATROL_SMALL;
        if (combatPoints > 8) type = FleetTypes.PATROL_MEDIUM;
        if (combatPoints > 16) type = FleetTypes.PATROL_LARGE;

        combatPoints *= 8f;

        FleetParamsV3 params = new FleetParamsV3(
                source.getMarket(),
                source.getLocationInHyperspace(),
                "derelict",
                1f,
                type,
                combatPoints, // combatPts
                civilianPoints, // freighterPts
                civilianPointsFuel, // tankerPts
                0f, // transportPts
                0f, // linerPts
                civilianPointsUtil, // utilityPts
                0f // qualityMod
        );
        params.random = random;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null) return null;;

        fleet.addAbility(Abilities.TRANSPONDER);
        fleet.getAbility(Abilities.TRANSPONDER).activate();
        LocationAPI location = source.getContainingLocation();
        location.addEntity(fleet);

        RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, false);

        fleet.setLocation(source.getLocation().x, source.getLocation().y);
        fleet.setFacing(random.nextFloat() * 360f);

        fleet.addScript(new RemnantAssignmentAI(fleet, (StarSystemAPI) source.getContainingLocation(), source));
        fleet.getMemoryWithoutUpdate().set("$sourceId", source.getId());

        return fleet;
    }


    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        super.reportFleetDespawnedToListener(fleet, reason, param);
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            String sid = fleet.getMemoryWithoutUpdate().getString("$sourceId");
            if (sid != null && source != null && sid.equals(source.getId())) {
                //if (sid != null && sid.equals(source.getId())) {
                totalLost++;
            }
        }
    }

    public int getTotalLost() {
        return totalLost;
    }
}
