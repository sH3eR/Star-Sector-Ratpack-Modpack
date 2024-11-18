package bcom;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.Ship;
import org.lwjgl.util.vector.Vector2f;

public class ZfbPlugin extends BaseEveryFrameCombatPlugin {
    String bcomKey = "bcomModifiedBounds";
    String bcomKeyHulk = "bcomModifiedSpeedForHulk";
    String hulkMovementKey = "bcomDoneWithMovement";

    String deployedNormallyKey = "bcomAfterDeploymentSlowdown";

    public ZfbPlugin() {
    }

    @Override
    public void advance(float amount, java.util.List<InputEventAPI> events) {

        if (Global.getCombatEngine() != null && !Global.getCombatEngine().isPaused()) {

            for (ShipAPI ship : Global.getCombatEngine().getShips()) {

                afterDeployment(ship, amount);

                //modifies zero flux speed boost
                zfsb(ship);

                retreatAtLowCr(ship);

                shipFragmentMovement(ship);

                modifyStats(ship);
            }
        }
    }
    public void zfsb(ShipAPI ship){
        ShipHullSpecAPI hullspec = ship.getHullSpec();
        if (!Settings.getShipExclusions().contains(ship.getId()) && Settings.isEnableZeroFluxBoost()&&(!Settings.isEnableZeroFluxBoostFixDeploy()||ship.getFullTimeDeployed()>4.5f)) {
            float value = Settings.getZeroFluxBoostLimit(hullspec) - (ship.getMaxSpeedWithoutBoost() + ship.getMutableStats().getZeroFluxSpeedBoost().base);
            if(value<Settings.getMinimumZeroFluxSpeedBoost(hullspec) - ship.getMutableStats().getZeroFluxSpeedBoost().base){
                value = Settings.getMinimumZeroFluxSpeedBoost(hullspec)- ship.getMutableStats().getZeroFluxSpeedBoost().base;
            }
            if (value > 0 - ship.getMutableStats().getZeroFluxSpeedBoost().base) {
                ship.getMutableStats().getZeroFluxSpeedBoost().modifyFlat("bcom", value);
            } else {
                ship.getMutableStats().getZeroFluxSpeedBoost().modifyFlat("bcom", 0 - ship.getMutableStats().getZeroFluxSpeedBoost().base);
            }
        }
    }

    public void shipFragmentMovement(ShipAPI ship){
        ShipHullSpecAPI hullspec = ship.getHullSpec();
        if(Settings.isEnableShipFragments() && !ship.isAlive() && !ship.getCustomData().containsKey(bcomKeyHulk)){//parts of the hulk have the same ID as the parent.
            ship.setCustomData(bcomKeyHulk,new Vector2f(ship.getLocation()));
            ship.setCustomData("bcomPreviousId",ship.getId());
            ship.setCustomData("bcomCountdown",Global.getCombatEngine().getTotalElapsedTime(false));
        }

        //this does the explosion logic
        if(ship.getVisualBounds()!=null && ship.getCustomData().containsKey(bcomKeyHulk) && !ship.getCustomData().containsKey(hulkMovementKey)){
            if((float)ship.getCustomData().get("bcomCountdown")+1<Global.getCombatEngine().getTotalElapsedTime(false)){
                ship.setCustomData(bcomKey,true);
            }
            boolean run = false;
            Vector2f prevLocation = (Vector2f) ship.getCustomData().get(bcomKeyHulk);
//                      List<Float> angleList= new ArrayList<>();
//                      List<Float> massList = new ArrayList<>();
            Vector2f currentLocation = ship.getLocation();
            for(ShipAPI s : Global.getCombatEngine().getShips()){
                if(s.getId().equals(ship.getId()) && Misc.getAngleInDegrees(prevLocation,currentLocation)!=0){
                    run = true;
//                              angleList.add(Misc.getAngleInDegrees(currentLocation,s.getLocation()));
//                              massList.add(s.getMass());
                }
            }
            if(!run){
                return;
            }
            //this logic did not work
            //average the other parts locations weighted by mass
//                      float value=0;
//                      float weight=0;
//                      for(int i = 0 ; i< angleList.size() ; i++){
//                          value += angleList.get(i)*massList.get(i);
//                          weight += massList.get(i);
//                      }
//                      value = value/weight;

            float mass = ship.getMass();
            float radians = (float) Math.toRadians(Misc.getAngleInDegrees(prevLocation,currentLocation));
//                      float radians = (float) Math.toRadians(value);
            float collisionRadius = ship.getCollisionRadius();
            float distance = Misc.getDistance(prevLocation,currentLocation);
            Vector2f currentVelocity = ship.getVelocity();
//        float rad = (float) Math.toRadians(angle);
//        return new Vector2f((float) Math.cos(rad), (float) Math.sin(rad));
            Vector2f additionalVelocity = new Vector2f((float) Math.cos(radians), (float) Math.sin(radians));
            //min speed, max speed, shipSizeFactor, massSlowdownFactor
            float maxFactor = Settings.getShipFragmentMaxFactor(hullspec);//600
            float minFactor = Settings.getShipFragmentMinFactor(hullspec);//200
            float massSpeedFactor = Settings.getShipFragmentMassSlowdownFactor(hullspec);//0.001
            float value = (float) ((Math.random()*(maxFactor-minFactor)+minFactor)* 1/(1+ mass * massSpeedFactor));
            additionalVelocity.scale(value);
//                      additionalVelocity.scale((100/mass)*collisionRadius*Settings.getShipFragmentSpeedMult());
            additionalVelocity.setX(additionalVelocity.getX()+currentVelocity.getX());
            additionalVelocity.setY(additionalVelocity.getY()+currentVelocity.getY());
            ship.getVelocity().set(additionalVelocity.getX(),additionalVelocity.getY());
            ship.setCustomData(hulkMovementKey,true);
        }
    }
    public void retreatAtLowCr(ShipAPI ship){
        ShipHullSpecAPI hullspec = ship.getHullSpec();
        if(Settings.isEnableRetreatAtLowCr() && ship instanceof Ship && ship.isRetreating() && ship.getOwner()!=100 && (ship.getOwner()!= FleetSide.PLAYER.ordinal()||ship.isAlly())){
            CombatFleetManagerAPI cfm = Global.getCombatEngine().getFleetManager(ship.getOwner());
            CombatTaskManagerAPI ctm;
            if(ship.isAlly()){
                ctm = cfm.getTaskManager(true);
            }else{
                ctm = cfm.getTaskManager(false);
            }
            ship.getTags();
            boolean run = false;
            float cr = ship.getCurrentCR();
            if(cr==ship.getCRAtDeployment()){
                return;
            }
            switch (((Ship) ship).getPersonality()){
                case Personalities.RECKLESS:
                    run = cr<=Settings.getRetreatCrReckless(hullspec);
                    break;
                case Personalities.AGGRESSIVE:
                    run = cr<=Settings.getRetreatCrAggressive(hullspec);
                    break;
                case Personalities.STEADY:
                    run = cr<=Settings.getRetreatCrSteady(hullspec);
                    break;
                case Personalities.CAUTIOUS:
                    run = cr<=Settings.getRetreatCrCautious(hullspec);
                    break;
                case Personalities.TIMID:
                    run = cr<=Settings.getRetreatCrTimid(hullspec);
                    break;
            }
            if(!run){
                return;
            }
            DeployedFleetMemberAPI deployedMember = cfm.getDeployedFleetMember(ship);
            ctm.orderRetreat(deployedMember, false, false);
        }
    }
    public void modifyStats(ShipAPI ship){
        ShipHullSpecAPI hullspec = ship.getHullSpec();
        if(!ship.getCustomData().containsKey(bcomKey)){
            if(ship.getHullSize() != ShipAPI.HullSize.FIGHTER && Settings.isEnableBounds()) {
                ship.setCollisionRadius(ship.getCollisionRadius() * Settings.getBoundsMult(hullspec));
            }
            if(ship.getHullSize() != ShipAPI.HullSize.FIGHTER && Settings.isEnableCrReductionOnHullDamage()) {
                ship.addListener(new BcomDamageListener());
            }
            if(Settings.isEnableZeroFluxBoost()){//TODO new feature?
                ship.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat("bcom",Settings.getZeroFluxSpeedBoostMinFluxLevel(hullspec));
            }
            if(Settings.isEnableRangeLimitIncrease())
                ship.getMutableStats().getWeaponRangeThreshold().modifyMult("bcom",Settings.getRangeThresholdMult(hullspec));
            if(Settings.isEnableSightMult())
                ship.getMutableStats().getSightRadiusMod().modifyMult("bcom",Settings.getSightMult(hullspec));
            if(Settings.isLeadingTargetAccuracy())
                ship.getMutableStats().getAutofireAimAccuracy().modifyMult("bcom", Settings.getAutoFireAimAccuracy(hullspec));
            if(Settings.isBeamWeaponDamageDropoff())
                ship.addListener(new BeamDamageDealtListener());
            if(Settings.isEnableShipExplosion() && (ship.getVariant()==null || ( !Settings.isExplosionNegatedByReducedExplosionHullmod() || !ship.getVariant().hasHullMod("reduced_explosion")))) {
                ship.getMutableStats().getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult("bcom", Settings.getExplosionRadiusMult(hullspec));
                ship.getMutableStats().getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult("bcom", Settings.getExplosionDamageMult(hullspec));
            }
            ship.setCustomData(bcomKey,true);
        }
    }
    public void afterDeployment(ShipAPI ship, float v){
        float slowdownAmount = Settings.getAfterDeploymentSlowdownAmount(ship.getHullSpec());
        float slowdownTime = Settings.getAfterDeploymentSlowdownTime(ship.getHullSpec());
        float immuneTime = Settings.getDeploymentImmuneTime(ship.getHullSpec());
        if(Settings.isEnableAfterDeployment()) {
            if(ship.getTravelDrive()!=null && ship.getTravelDrive().isActive()){
                ship.setCustomData(deployedNormallyKey,true);
                if(immuneTime>0f){
                    ship.getMutableStats().getHullDamageTakenMult().modifyMult("bcomDeploymentImmunity",0f);
                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult("bcomDeploymentImmunity",0f);
                }
            }else if(ship.getCustomData().containsKey(deployedNormallyKey)){
                    if (!ship.getCustomData().containsKey("bcomDeploymentTime")) {
                        ship.setCustomData("bcomDeploymentTime", 0f);
                    }
                    ship.setCustomData("bcomDeploymentTime", (float) ship.getCustomData().get("bcomDeploymentTime") + v);
                    if(slowdownTime>0f) {
                        if ((float) ship.getCustomData().get("bcomDeploymentTime") < slowdownTime) {
                            float slowdown = 1 - slowdownAmount * v;
                            Vector2f velocity = ship.getVelocity();
                            velocity.setY(velocity.getY() * slowdown);
                            velocity.setX(velocity.getX() * slowdown);
                        }
                    }
                    if(immuneTime>0f){
                        if ((float) ship.getCustomData().get("bcomDeploymentTime") > immuneTime) {
                            ship.getMutableStats().getHullDamageTakenMult().unmodify("bcomDeploymentImmunity");
                            ship.getMutableStats().getArmorDamageTakenMult().unmodify("bcomDeploymentImmunity");

                        }
                    }
                }else{
                ship.getMutableStats().getHullDamageTakenMult().unmodify("bcomDeploymentImmunity");
                ship.getMutableStats().getArmorDamageTakenMult().unmodify("bcomDeploymentImmunity");
                }
        }
    }
}
