package bcom;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.FleetMemberDeploymentListener;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.coreui.V;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BcomFleetDeploymentListener implements FleetMemberDeploymentListener {
    List<Vector2f> locationsDeployedAtFrame = new ArrayList<>();
    List<Float> collisionsDeployedAtFrame = new ArrayList<>();
    float time;
    @Override
    public void reportFleetMemberDeployed(DeployedFleetMemberAPI member) {
        //does not run in simulation! Or in an escape!

        ShipAPI ship = member.getShip();
        if(Settings.isEnableModifyDeployment()) {
            //change the deployment location.
            CombatEngineAPI engine = Global.getCombatEngine();
            float newTime = engine.getTotalElapsedTime(false);
            if (time != newTime) {
                locationsDeployedAtFrame = new ArrayList<>();
                collisionsDeployedAtFrame = new ArrayList<>();
            }
            time = newTime;
            float height = engine.getMapHeight();
            float width = engine.getMapWidth();
            boolean onTop;
//        int xDist = Settings.getDeployedXDist();
//            engine.getContext().getStandoffRange();
//        int yDist = Settings.getDeployedYDist();
            float xDist = Settings.getDeployedXDist();//1k
            float yDist = Settings.getDeployedYDist();//1k
            float spaceUsed = Settings.getDeploymentSpaceUsed();//0.5f
            //positive is in front of the line, negitave is behind
            float distFromEdge = (height/2) - Math.abs(ship.getLocation().getY());

            if ((engine.getTotalElapsedTime(false)<1.0 && Math.abs(ship.getLocation().getX()) < 6000)||distFromEdge<0) { // 5000 is the limit outwards from deployment
                    onTop = ship.getLocation().getY() > 0;
                int usableWidth = (int) (engine.getMapWidth());
                List<ShipAPI> ships = engine.getShips();
                for (int i = 0; i < 100; i++) {
                    //row loop
                    for (int j = 1; j < usableWidth / xDist / spaceUsed; j++) {
                        int position = j / 2;
                        if (j % 2 == 1) {
                            position = position * -1;
                        }
                        position = (int) (position * xDist);
                        boolean collision = false;
                        float deployYPos = 0f;//positive is backwards, negitave is forawrds
                        if(engine.getTotalElapsedTime(false)<1.0){
                            deployYPos = -Settings.getInitialDeploymentYOffset();
                        }else{
                            deployYPos = -Settings.getDeploymentYOffset();
                        }
                        if (onTop) {
                            deployYPos = engine.getMapHeight() / 2 + i * yDist + deployYPos;
                        } else {
                            deployYPos = (engine.getMapHeight() / 2) * -1 - i * yDist - deployYPos;
                        }
                        Vector2f atteptedSpawnLocation = new Vector2f(position, deployYPos);
                        for (ShipAPI indexShip : ships) {
                            if (Misc.getDistance(indexShip.getLocation(), atteptedSpawnLocation) - indexShip.getCollisionRadius() - ship.getCollisionRadius() < 0) {
                                //location is bad
                                collision = true;
                            }
                        }
                        for (int k = 0; k < locationsDeployedAtFrame.size(); k++) {
                            if (Misc.getDistance(locationsDeployedAtFrame.get(k), atteptedSpawnLocation) - collisionsDeployedAtFrame.get(k) - ship.getCollisionRadius() < 0) {
                                //location is bad
                                collision = true;
                            }
                        }
                        if (!collision) {
                            //location is good!

                            ship.getLocation().set(position, deployYPos);
                            i = 1000000;
                            j = 1000000;
                            break;
                        }
                    }
                }
            }

            locationsDeployedAtFrame.add(ship.getLocation());
            collisionsDeployedAtFrame.add(ship.getCollisionRadius());
        }
        //asteroids in ships
//        if(Settings.isAsteroidFragFix()){
            if(true){
                Iterator<Object> asteroids = Global.getCombatEngine().getAsteroidGrid().getCheckIterator(ship.getLocation(),ship.getCollisionRadius(),ship.getCollisionRadius());
            while(asteroids.hasNext()){
                Object asteroid = asteroids.next();
//                Global.getCombatEngine().removeEntity((CombatEntityAPI) asteroid);
                ExtraAsteroidPlugin.removeAsteroidNoSplit((CombatEntityAPI)asteroid,Global.getCombatEngine());
            }
        }
    }
}
