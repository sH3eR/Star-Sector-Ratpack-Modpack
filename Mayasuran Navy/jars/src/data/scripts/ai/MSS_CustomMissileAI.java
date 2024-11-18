//By Tartiflette, fast and highly customizable Missile AI.
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class MSS_CustomMissileAI implements MissileAIPlugin, GuidedMissileAI {
          
    
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    //Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
    //  Set to a negative value to disable
    private final float OVERSHOT_ANGLE=60;
    
    //Time to complete a wave in seconds.
    private final float WAVE_TIME=2;
    
    //Angle of the waving in degree (divided by 3 with ECCM). Set to a negative value to avoid all waving.
    private final float WAVE_AMPLITUDE=15;
    
    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;
    
    //Does the missile try to correct it's velocity vector as fast as possible or just point to the desired direction and drift a bit?
    //  Can create strange results with large waving
    //  Require a projectile with a decent turn rate and around twice that in turn acceleration compared to their top speed
    //  Usefull for slow torpedoes with low forward acceleration, or ultra precise anti-fighter missiles.     
    private final boolean OVERSTEER=false;  //REQUIRE NO OVERSHOOT ANGLE!
    
    //Does the missile switch its target if it has been destroyed?
    private final boolean TARGET_SWITCH=true;
    
    //Does the missile find a random target or aways tries to hit the ship's one?     
    //   0: No random target seeking, 
    //       If the launching ship has a valid target, the missile will pursue that one.
    //       If there is no target selected, it will check for an unselected cursor target.
    //       If there is none, it will pursue its closest valid threat.    
    //   1: Local random target, 
    //       If the ship has a target selected, the missile will pick a random valid threat around that one. 
    //       If the ship has none, the missile will pursue a random valid threat around the cursor, or itself in AI control.    
    //   2: Full random, 
    //       The missile will always seek a random valid threat around itself.
    private final Integer RANDOM_TARGET=0;
    
    //Both targeting behavior can be false for a missile that always get the ship's target or the closest one
    //Prioritize hitting fighters and drones (if false, the missile will still be able to target fighters but not drones) 
    private final boolean ANTI_FIGHTER=false;    //INCOMPATIBLE WITH ASSAULT
    
    //Target the biggest threats first
    private final boolean ASSAULT=true;  //INCOMPATIBLE WITH ANTI-FIGHTER
    
    //range in which the missile seek a target in game units.
    private final float MAX_SEARCH_RANGE = 1500;
    
    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE=500;
    
    //Is the missile lead the target or tailchase it?
    private final boolean LEADING=true;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=3;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
    
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    //Max range of the missile after modifiers.
    private final float MAX_RANGE;
    //Random starting offset for the waving.
    private final float OFFSET;
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public MSS_CustomMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        MAX_RANGE = missile.getWeapon().getRange();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
        OFFSET=(float)(Math.random()*Math.PI*2);
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {return;}
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || (TARGET_SWITCH && (target instanceof ShipAPI && ((ShipAPI) target).isHulk())
                                  || !engine.isEntityInPlay(target)
                   )
                ){
            setTarget(assignTarget(MISSILE));
            //forced acceleration by default
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
        
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            //set the next check time
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.03f,
                            MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation())/PRECISION_RANGE)
            );
            if(LEADING){
                //best intercepting point
                lead = AIUtils.getBestInterceptPoint(
                        MISSILE.getLocation(),
                        MAX_SPEED*ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
                        target.getLocation(),
                        target.getVelocity()
                );                
                //null pointer protection
                if (lead == null) {
                    lead = target.getLocation(); 
                }
            } else {
                lead = target.getLocation();
            }
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        MISSILE.getLocation(),
                        lead
                );
        
        if (OVERSTEER){
            //velocity angle correction
            float offCourseAngle = MathUtils.getShortestRotation(
                    VectorUtils.getFacing(MISSILE.getVelocity()),
                    correctAngle
                    );

            float correction = MathUtils.getShortestRotation(                
                    correctAngle,
                    VectorUtils.getFacing(MISSILE.getVelocity())+180
                    ) 
                    * 0.5f * //oversteer
                    (float)((FastTrig.sin(Math.PI/90*(Math.min(Math.abs(offCourseAngle),45))))); //damping when the correction isn't important

            //modified optimal facing to correct the velocity vector angle as soon as possible
            correctAngle = correctAngle+correction;
        }
        
        if(WAVE_AMPLITUDE>0){            
            //waving
            float multiplier=1;
            if(ECCM<=1){
                multiplier=0.3f;
            }
            correctAngle+=multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+MISSILE.getElapsed()*(2*Math.PI/WAVE_TIME));
        }
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
        
        if(OVERSHOT_ANGLE<=0 || Math.abs(aimAngle)<OVERSHOT_ANGLE){
            MISSILE.giveCommand(ShipCommand.ACCELERATE);  
        }
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    //////////////////////
    //    TARGETING     //
    //////////////////////
    
    public CombatEntityAPI assignTarget(MissileAPI missile){
        
        ShipAPI theTarget=null;        
        ShipAPI source = missile.getSource();        
        ShipAPI currentTarget;
        
        //check for a target from its source
        if(source != null
                && source.getShipTarget() != null
                && source.getShipTarget() instanceof ShipAPI
                && source.getShipTarget().getOwner() != missile.getOwner()
                ){
            currentTarget=source.getShipTarget();
        } else {
            currentTarget=null;
        }
        
        //random target selection
        if (RANDOM_TARGET>0){  
            //random mode 1: the missile will look for a target around itsef
            Vector2f location = missile.getLocation();   
            //random mode 2: if its source has a target selected, it will look for random one around that point
            if( RANDOM_TARGET<2){                                     
                if(currentTarget != null 
                        && currentTarget.isAlive()
                        && MathUtils.isWithinRange(missile, currentTarget, MAX_RANGE)
                        ){
                    location = currentTarget.getLocation();
                } else if (source != null
                        && source.getMouseTarget()!=null){
                    location=source.getMouseTarget();
                }
            }
            //fetch the right kind of target
            if(ANTI_FIGHTER){
                theTarget = getRandomFighterTarget(location);
            } else if(ASSAULT){
                theTarget = getRandomLargeTarget(location);
            } else {
                theTarget = getAnyTarget(location);
            }    
        //non random targeting    
        } else {
            if(source!=null){
                //ship target first
                if(currentTarget!=null
                        && currentTarget.isAlive()
                        && currentTarget.getOwner()!=missile.getOwner()
                       // && !(ANTI_FIGHTER && !(currentTarget.isDrone() && currentTarget.isFighter()))
                        && !(ASSAULT && (currentTarget.isDrone() || currentTarget.isFighter()))
                        ){
                    theTarget=currentTarget;                
                } else {
                    //or cursor target if there isn't one
                    List<ShipAPI> mouseTargets = CombatUtils.getShipsWithinRange(source.getMouseTarget(), 100f);
                    if (!mouseTargets.isEmpty()) {
                        Collections.sort(mouseTargets, new CollectionUtils.SortEntitiesByDistance(source.getMouseTarget()));
                        for (ShipAPI tmp : mouseTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    //&& !(ANTI_FIGHTER && !(tmp.isDrone() && tmp.isFighter()))
                                    && !(ASSAULT && (tmp.isDrone() || tmp.isFighter() || tmp.isFrigate()))
                                    ) {
                                theTarget=tmp;
                                break;
                            }
                        }
                    }                
                }
            }
            //still no valid target? lets try the closest one
            //most of the time a ship will have a target so that doesn't need to be perfect.
            if(theTarget==null){
                List<ShipAPI> closeTargets = AIUtils.getNearbyEnemies(missile, MAX_SEARCH_RANGE);
                if (!closeTargets.isEmpty()) {
                    Collections.sort(closeTargets, new CollectionUtils.SortEntitiesByDistance(missile.getLocation()));
                    if (ASSAULT){   //assault missiles will somewhat prioritize toward bigger threats even if there is a closer small one, and avoid fighters and drones.
                        for (ShipAPI tmp : closeTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    ) {
                                if (tmp.isCapital() || tmp.isCruiser()){
                                    theTarget=tmp;
                                    break;
                                } else if (tmp.isDestroyer() && Math.random()>0.5){
                                    theTarget=tmp;
                                    break;
                                } else if (tmp.isDestroyer() && Math.random()>0.75){
                                    theTarget=tmp;
                                    break;
                                } else if (!tmp.isDrone() && !tmp.isFighter() && Math.random()>0.95){
                                    theTarget=tmp;
                                    break;
                                }
                            }
                        }
                    }else if(ANTI_FIGHTER){    //anti-fighter missile will target the closest drone or fighter
                        for (ShipAPI tmp : closeTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    && (tmp.isDrone() || tmp.isFighter())
                                    ) {
                                theTarget=tmp;
                                break;
                            }
                        }
                    }else{  //non assault, non anti-fighter missile target the closest non-drone ship
                        for (ShipAPI tmp : closeTargets) {
                            if (tmp.isAlive() 
                                    && tmp.getOwner() != missile.getOwner()
                                    && !tmp.isDrone()
                                    ) {  
                                theTarget=tmp;
                                break;
                            }
                        }
                    }
                } 
            }
        }        
        return theTarget;
    }
    
    //Random picker for fighters and drones
    public ShipAPI getRandomFighterTarget(Vector2f location){
        ShipAPI select=null;
        Map<Integer, ShipAPI> PRIORITYLIST = new HashMap<>();
        Map<Integer, ShipAPI> OTHERSLIST = new HashMap<>();
        int i=1, u=1;
        List<ShipAPI> potentialTargets = CombatUtils.getShipsWithinRange(location, MAX_RANGE);
        if (!potentialTargets.isEmpty()) {
            for (ShipAPI tmp : potentialTargets) {
                if (tmp.isAlive() 
                        && tmp.getOwner() != MISSILE.getOwner()
                        ) {
                    if (tmp.isFighter() || tmp.isDrone()){
                        PRIORITYLIST.put(i, tmp);
                        i++;
                    } else {                            
                        OTHERSLIST.put(u, tmp);
                        u++;
                    }
                }
            }
            if (!PRIORITYLIST.isEmpty()){
                int chooser=Math.round((float)Math.random()*(i-1)+0.5f);
                select=PRIORITYLIST.get(chooser);
            } else if (!OTHERSLIST.isEmpty()){                    
                int chooser=Math.round((float)Math.random()*(u-1)+0.5f);
                select=OTHERSLIST.get(chooser);
            }
        }
        return select;
    }
    
    //Random target selection strongly weighted toward bigger threats in range
    public ShipAPI getRandomLargeTarget(Vector2f location){
        ShipAPI select=null;
        Map<Integer, ShipAPI> PRIORITY1 = new HashMap<>();
        Map<Integer, ShipAPI> PRIORITY2 = new HashMap<>();
        Map<Integer, ShipAPI> PRIORITY3 = new HashMap<>();
        Map<Integer, ShipAPI> PRIORITY4 = new HashMap<>();
        Map<Integer, ShipAPI> OTHERSLIST = new HashMap<>();
        int i=1, u=1, v=1, x=1, y=1;
        List<ShipAPI> potentialTargets = CombatUtils.getShipsWithinRange(location, MAX_RANGE);
        if (!potentialTargets.isEmpty()) {
            for (ShipAPI tmp : potentialTargets) {
                if (tmp.isAlive() 
                        && tmp.getOwner() != MISSILE.getOwner()
                        && !tmp.isDrone()
                        ) {
                    if (tmp.isCapital()){
                        PRIORITY1.put(i, tmp);
                        i++;
                        PRIORITY2.put(u, tmp);
                        u++;
                        PRIORITY3.put(x, tmp);
                        x++;
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else if (tmp.isCruiser()){
                        PRIORITY2.put(u, tmp);
                        u++;
                        PRIORITY3.put(x, tmp);
                        x++;
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else if (tmp.isDestroyer()){
                        PRIORITY3.put(x, tmp);
                        x++;
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else if (tmp.isFrigate()){
                        PRIORITY4.put(v, tmp);
                        v++;
                        OTHERSLIST.put(y, tmp);
                        y++;
                    } else {
                        OTHERSLIST.put(y, tmp);
                        y++;
                    }
                }
            }
            if (!PRIORITY1.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(i-1)+0.5f);
                select=PRIORITY1.get(chooser);
            } else if (!PRIORITY2.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(u-1)+0.5f);
                select=PRIORITY2.get(chooser);
            } else if (!PRIORITY3.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(x-1)+0.5f);
                select=PRIORITY3.get(chooser);
            } else if (!PRIORITY4.isEmpty() && Math.random()>0.8f){
                int chooser=Math.round((float)Math.random()*(v-1)+0.5f);
                select=PRIORITY4.get(chooser);
            } else if (!OTHERSLIST.isEmpty()){                    
                int chooser=Math.round((float)Math.random()*(y-1)+0.5f);
                select=OTHERSLIST.get(chooser);
            }
        }
        return select;
    }

    //Pure random target picker
    public ShipAPI getAnyTarget(Vector2f location){
        ShipAPI select=null;
        Map<Integer, ShipAPI> TARGETLIST = new HashMap<>();
        int i=1;
        List<ShipAPI> potentialTargets = CombatUtils.getShipsWithinRange(location, MAX_RANGE);
        if (!potentialTargets.isEmpty()) {
            for (ShipAPI tmp : potentialTargets) {
                if (tmp.isAlive() 
                        && tmp.getOwner() != MISSILE.getOwner()
                        && !tmp.isDrone()
                        ){
                    TARGETLIST.put(i, tmp);
                    i++;                        
                }
            }
            if (!TARGETLIST.isEmpty()){
                int chooser=Math.round((float)Math.random()*(i-1)+0.5f);
                select=TARGETLIST.get(chooser);
            }
        }
        return select;
    }
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}