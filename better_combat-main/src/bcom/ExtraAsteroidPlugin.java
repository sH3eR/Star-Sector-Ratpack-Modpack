package bcom;

import com.fs.graphics.Sprite;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ExtraAsteroidPlugin extends BaseEveryFrameCombatPlugin {

    //ran resets between battles. I think because it is a new instance every battle.
    private boolean ran = false;
//    private static String memKeyHeath = "$BcomAsteroidHealth";
    private static String memKeyModified = "$BcomAsteroidModified";
//    private static String memKeyId = "$BcomAsteroidId";
    static int rollingId = 0;
    private IntervalUtil startupTimer = new IntervalUtil(2,2);
    private IntervalUtil cullInterval = new IntervalUtil(1,2);
    public static List<CombatEntityAPI> storedAsteroids = new ArrayList<>();

    boolean addedScript = false;
//    private boolean internalDone = false;

    private float asteroidAmount;
    private float asteroidAngle;

    public ExtraAsteroidPlugin() {
            float numAsteroidsWithinRange = 0;
            if (Global.getSettings().getCurrentState() == GameState.CAMPAIGN &&
                    Global.getSector().getPlayerFleet() != null) {
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                LocationAPI loc = playerFleet.getContainingLocation();
                if (loc instanceof StarSystemAPI) {
                    StarSystemAPI system = (StarSystemAPI) loc;
                    List<SectorEntityToken> asteroids = system.getAsteroids();
                    for (SectorEntityToken asteroid : asteroids) {
                        float range = Vector2f.sub(playerFleet.getLocation(),
                                asteroid.getLocation(),
                                new Vector2f()).length();
                        if (range < 500) numAsteroidsWithinRange++;
                    }
                }
                //asteroid rings
                for (CampaignTerrainAPI terrain : playerFleet.getContainingLocation().getTerrainCopy()) {
                    if (terrain.getType().equals(Terrain.ASTEROID_BELT) || terrain.getType().equals(Terrain.ASTEROID_FIELD)) {
                        if (terrain.getPlugin().containsEntity(playerFleet)) {
                            numAsteroidsWithinRange = numAsteroidsWithinRange + 30;
                        }
                    }
                }
            } else if (Global.getSettings().getCurrentState() == GameState.TITLE) {
                numAsteroidsWithinRange = 20f;
            }
            if (Settings.isForceAsteroids()) {
                if (numAsteroidsWithinRange < 10f) {
                    numAsteroidsWithinRange = 10f;
                }
            }
            if (numAsteroidsWithinRange > Settings.getAsteroidsForMaxSpawn()) {
                asteroidAmount = 1f;
            } else {
                asteroidAmount = numAsteroidsWithinRange / Settings.getAsteroidsForMaxSpawn();
            }
            asteroidAngle = (float) (Math.random() * 365);
        }
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        super.advance(amount, events);

        CombatEngineAPI combatEngine = Global.getCombatEngine();
        float height = combatEngine.getMapHeight();
        float width = combatEngine.getMapWidth();

        //TODO move somewhere else?
        if(!addedScript){
            addedScript=true;
            if(!combatEngine.getListenerManager().hasListenerOfClass(BcomFleetDeploymentListener.class)){
                combatEngine.getListenerManager().addListener(new BcomFleetDeploymentListener());
            }
        }


        if(!Settings.isEnableAsteroids()){
            ran = true;
        }

        if (!ran) {
            //number of asteroids will be scaled off of combatEngine.getAsteroids() and settings
            int numAsteroids = (int) (Settings.getAsteroidDensity()*height*width*asteroidAmount);
            if(numAsteroids>Settings.getMaxAsteroids()){
                numAsteroids = Settings.getMaxAsteroids();
            }

            for(int i=0;i<numAsteroids;i++){
                float dx = 0f;
                float dy = 0f;
                if(Settings.isMoveStartingAsteroidsWithFlyinAsteroids()){
                    double asteroidRadians = (float) Math.toRadians(asteroidAngle);
                    dx = (float) (Math.sin(asteroidRadians)*Settings.getFlyinAsteroidSpeed());
                    dy = (float) (Math.cos(asteroidRadians)*Settings.getFlyinAsteroidSpeed());
                }

                CombatEntityAPI a = addAsteroid(combatEngine,(float) (Math.random()*width)-width/2,(float) (Math.random()*height)-width/2,dx,dy);

                a.setHitpoints((float)(100*Math.random())); //so the asteroids frag instead of the ships at first
//                a.setCustomData(memKeyId,rollingId);
//                rollingId++;
            }
            ran=true;
        }
//        if(!internalDone) {
//            if (!combatEngine.isPaused()) {
//                startupTimer.advance(amount);
//            }
//            if (startupTimer.intervalElapsed()) {
//                for(CombatEntityAPI d : combatEngine.getAsteroids()){
//                    if(d.getCustomData().containsKey(memKeyHeath)) {
//                        d.setHitpoints((Float) d.getCustomData().get(memKeyHeath));
//                    }
//                }
//                internalDone=true;
//            }
//
//        }

        //calculate flyin asteroids.
        //check if asteroid is due to spawn this frame.

        if(!(combatEngine.getAsteroids().size()>Settings.getMaxAsteroids())&&Math.random()<Settings.getFlyinAsteroidNumber()*amount * asteroidAmount * ((combatEngine.getMapHeight() + combatEngine.getMapWidth())/10000) && !combatEngine.isPaused()&&Settings.isEnableFlyinAsteroids()){
            double asteroidRadians = (float) Math.toRadians(asteroidAngle);
            double x = Math.sin(asteroidRadians);
            double y = Math.cos(asteroidRadians);
            float mapHeight = combatEngine.getMapHeight();
            float mapWidth = combatEngine.getMapWidth();
            Vector2f spawningLocation = new Vector2f();
            if(Math.random()<Math.abs(x)){
                //asteroid spawning on the left/right wall
                if(x>0){
                    //left wall
                    spawningLocation.setX(-((mapWidth/2)+1000));
                }else{
                    //right wall
                    spawningLocation.setX((mapWidth/2)+1000);
                }
                spawningLocation.setY((float) ((mapHeight*Math.random())-mapHeight/2));
            }else{
                //asteroid spawning on the top/bottom wall
                if(y>0){
                    //bottom wall
                    spawningLocation.setY(-((mapHeight/2)+1000));
                }else{
                    //top wall
                    spawningLocation.setY((mapHeight/2)+1000);
                }
                spawningLocation.setX((float) ((mapWidth*Math.random())-mapWidth/2));
            }
            addAsteroid(combatEngine,spawningLocation.getX(),spawningLocation.getY(),
                    (float)x*Settings.getFlyinAsteroidSpeed(),(float)y*Settings.getFlyinAsteroidSpeed());
        }

        //remove asteroids 1k units outside the map
        if(!combatEngine.isPaused()) {
            cullInterval.advance(amount);
            if(cullInterval.intervalElapsed()){
                for(CombatEntityAPI c :combatEngine.getAsteroids()){
                    if(Math.abs(c.getLocation().getY())> combatEngine.getMapHeight()/2+1500||
                            Math.abs(c.getLocation().getX())> combatEngine.getMapWidth()/2+1500){
                        combatEngine.removeEntity(c);
                    }
                }
            }
        }
        if(Settings.isEnableSplitAsteriods()) {
                List<CombatEntityAPI> updatedAsteroids = combatEngine.getAsteroids();
                int j=0;
//                if(storedAsteroids.size()!=updatedAsteroids.size()){
//                    Logger.getLogger(this.getClass().getName()).info("sizeDiff!!!");
//                }
                List<CombatEntityAPI> asteroidsToExplode = new ArrayList<>();
                for(int i=0;i<storedAsteroids.size();i++) {
                    try {
                        if (!storedAsteroids.get(i).toString().equals(updatedAsteroids.get(j).toString()) && Math.abs(storedAsteroids.get(i).getLocation().getY()) < combatEngine.getMapHeight() / 2 + 1000 && // 0 and 0 used to be 1500, was causing issue with slow moving vanilla asteroids
                                Math.abs(storedAsteroids.get(i).getLocation().getX()) < combatEngine.getMapWidth() / 2 + 1000) {
                            //ids is gone and asteroid is in bounds, split asteroid
//                            Logger.getLogger(this.getClass().getName()).info("explode!!!");
                            //addAsteroidFromDestroyed()
                            CombatEntityAPI a = storedAsteroids.get(i);
                            Vector2f loc1 = a.getLocation();

                            //it is possible for this to be triggered when it shouldn't be somehow.
                            boolean skip = false;
                            for (int i2 = 0; i2 < updatedAsteroids.size(); i2++) {
                                CombatEntityAPI a2 = updatedAsteroids.get(i2);
                                Vector2f loc2 = a2.getLocation();
                                float minDist = a.getCollisionRadius() / 2 + a2.getCollisionRadius() / 2;
                                if (Math.abs(loc1.getX() - loc2.getX()) < minDist && Math.abs(loc1.getY() - loc2.getY()) < minDist) {
                                    skip = true;
                                    break;
                                }
                            }
                            if (skip) {
//                                Logger.getLogger(this.getClass().getName()).info("Asteroid attempted to create sub-asteroids, but was inside an asteroid!");
                                continue;
                            }
                            asteroidsToExplode.add(a);
                        }else {
                            j++;
                        }
                    }catch(IndexOutOfBoundsException ignored){
//                        Logger.getLogger(this.getClass().getName()).info("Index out of bounds! This should not be possible");
//                        Logger.getLogger(this.getClass().getName()).info("i="+i);
//                        Logger.getLogger(this.getClass().getName()).info("j="+j);
                        }
                }
                if(asteroidsToExplode.size()>10 && combatEngine.getTotalElapsedTime(false)<0.1) {
                    Logger.getLogger(this.getClass().getName()).info("over 10 asteroids set to explode in one frame at the start of combat. Likely an error");
                    storedAsteroids = new ArrayList<>(updatedAsteroids);
                    return;
                }
                for(CombatEntityAPI a : asteroidsToExplode) {
                float scale = a.getCollisionRadius();
                int subcount = (int) (scale * (Settings.getSplitAsteroidMinNumber() + (Settings.getSplitAsteroidMaxNumber() - Settings.getSplitAsteroidMinNumber())));
//                            if(subcount>Settings.getSplitAsteroidMax()){
//                                subcount = Settings.getSplitAsteroidMax();
//                            }
                if (Settings.getSplitAsteroidsAbsoluteMax() < subcount) {
                    subcount = Settings.getSplitAsteroidsAbsoluteMax();
                }
                for (int k = 0; k < subcount; k++) {
                    float asteroidScale = (float) (scale * (Settings.getSplitAsteroidsMinSize() + (Settings.getSplitAsteroidsMaxSize() - Settings.getSplitAsteroidsMinSize()) * Math.random()));
                    float angle = (float) (Math.random() * 365);
                    Vector2f vectorAngle = Misc.getUnitVectorAtDegreeAngle(angle);
                    float toModify = (float) (a.getCollisionRadius() * 0.8f);
                    Vector2f modified = new Vector2f((float) (vectorAngle.getX() * toModify * Math.random()), (float) (vectorAngle.getY() * toModify * Math.random()));
                    Vector2f location = new Vector2f();
                    Vector2f velocity = new Vector2f((float) (vectorAngle.getX() * (Settings.getSplitAsteroidMinSpeed() + (Settings.getSplitAsteroidMaxSpeed() - Settings.getSplitAsteroidMinSpeed()) * Math.random())), (float) (vectorAngle.getY() * (Settings.getSplitAsteroidMinSpeed() + (Settings.getSplitAsteroidMaxSpeed() - Settings.getSplitAsteroidMinSpeed()) * Math.random())));
                    Vector2f.add(velocity, a.getVelocity(), velocity);
                    Vector2f.add(a.getLocation(), modified, location);
                    addAsteroidFromDestroyed(combatEngine, location.x, location.y, velocity.getX(), velocity.getY(), asteroidScale);
                }
            }
            storedAsteroids = new ArrayList<>(updatedAsteroids);
                }
        }


//OLD CODE. Use if I end up using the vanilla map
//        //modify asteroids flying in. also modifies asteroids currently on the map.
//        if(Settings.isEnableAsteroids()&&combatEngine.getAsteroids().size()>30*Settings.getMapSizeFactor()) {
//            for (CombatEntityAPI a : combatEngine.getAsteroids()) {
//                if (!a.getCustomData().containsKey(memKeyModified)) {
//                    float scale0 = Settings.getAsteroidScaleFactor() - Settings.getAsteroidMinimumScaleFactor();
//                    scale0 = (float) (scale0 * Math.random());
//                    if (scale0 < 0) {
//                        scale0 = 0;
//                    }
//                    float size = (float) (1f + (1 * .07)); //1 for type
//                    float scale = size * scale0 + size * Settings.getAsteroidMinimumScaleFactor();
//                    try {
//                        updateAsteroidSize(a, scale, combatEngine);
//                    } catch (Throwable e) {
//                        throw new RuntimeException(e);
//                    }
//                    a.setHitpoints(a.getHitpoints() * Settings.getAsteroidHp());
//                    a.setCustomData(memKeyModified, true);
//                }
//            }
//        }

        //
    public static void removeAsteroidNoSplit(CombatEntityAPI asteroid, CombatEngineAPI combatEngine){
        float dist = 500f;
        CombatEntityAPI toRemove = null;
        for(CombatEntityAPI a : storedAsteroids){
            if(Misc.getDistance(a.getLocation(),asteroid.getLocation())<dist) {
                dist = Misc.getDistance(a.getLocation(), asteroid.getLocation());
                toRemove = a;
            }
        }
        combatEngine.removeObject(asteroid);
        if(toRemove!=null){
            storedAsteroids.remove(toRemove);
        }
    }

    public static CombatEntityAPI addAsteroid(CombatEngineAPI combatEngine, float x, float y, float dx, float dy){
//        int typeNumber = Settings.getActualNumberOfAsteroidTextures();//set from code, not from settings
        int typeNumber = Settings.getAsteroidIndex();//set from code, not from settings
        int type = ((int)(Math.random()*100000))%typeNumber;
        float scale0 = Settings.getAsteroidScaleFactor()-Settings.getAsteroidMinimumScaleFactor();
        scale0 = (float) (scale0*Math.random());
        if(scale0<0){
            scale0=0;
        }
        float size = (float) (1f + (type*.07)); //"base" size
        float scale = size*scale0 +size*Settings.getAsteroidMinimumScaleFactor();

        //movement
        dx = (float) (dx + dx*(Math.random()-0.5)*(Settings.getAsteroidRandomMovementFactor()));
        dx = dx * 1/(1+ size * Settings.getAsteroidSizeSlowdownFactor());

        dy = (float) (dy + (Math.random()-0.5)*(Settings.getAsteroidRandomMovementFactor()));
        dy = dy * 1/(1+ size * Settings.getAsteroidSizeSlowdownFactor());

        CombatEntityAPI a = combatEngine.spawnAsteroid(type,x,y,dx,dy);
        try {
            updateAsteroidSize(a,scale,combatEngine);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        a.setHitpoints((float) ((Math.pow(a.getCollisionRadius(),Settings.getAsteroidHpExponent()))*Settings.getAsteroidHp()));
//        a.setCustomData(memKeyId,rollingId);
//        rollingId++;
        return a;
    }

    public static CombatEntityAPI addAsteroidFromDestroyed(CombatEngineAPI combatEngine, float x, float y, float dx, float dy, float scale){

//        int type = ((int)(Math.random()*100000))%5;
        float scale0 = Settings.getAsteroidScaleFactor()-Settings.getAsteroidMinimumScaleFactor();
        scale0 = (float) (scale0*Math.random());
        if(scale0<0){
            scale0=0;
        }
//        float scale = size*scale0 +size*Settings.getAsteroidMinimumScaleFactor();
        float size = (scale0+Settings.getAsteroidMinimumScaleFactor())*scale;
        int type = (int) ((int)(size-1)/0.7f);
        if(type>3){
            type = 3;
        }
        //movement
        dx = (float) (dx + dx*(Math.random()-0.5)*(Settings.getAsteroidRandomMovementFactor()));
        dx = dx * 1/(1+ size * Settings.getAsteroidSizeSlowdownFactor());

        dy = (float) (dy + (Math.random()-0.5)*(Settings.getAsteroidRandomMovementFactor()));
        dy = dy * 1/(1+ size * Settings.getAsteroidSizeSlowdownFactor());

        CombatEntityAPI a = combatEngine.spawnAsteroid(type,x,y,dx,dy);
        try {
            updateAsteroidSize(a,scale,combatEngine);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        a.setHitpoints((float) ((Math.pow(a.getCollisionRadius(),Settings.getAsteroidHpExponent()))*Settings.getAsteroidHp()));

//        a.setCustomData(memKeyId,rollingId);
//        rollingId++;
        return a;
    }


    public static CombatEntityAPI updateAsteroidSize(CombatEntityAPI a, float scale, CombatEngineAPI combatEngine) throws Throwable {
        a.setCollisionRadius(a.getCollisionRadius()*scale);//WAS *Settings.getAsteroidCollisionRadiusMult(). Changed the actual collistion
        Sprite s = new Sprite();
        try {
//            Sprite s = (Sprite) ReflectionUtils.getPrivateVariable("sprite",a);
//            public static Object getPrivateVariable(String fieldName, Object instanceToGetFrom) throws Throwable {
            String fieldName = "sprite";
            Object instanceToGetFrom = a;
            Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
            MethodHandle getMethod = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
            MethodHandle getNameMethod = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
            MethodHandle setAccessMethod = MethodHandles.lookup().findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));

            Object[] instancesOfFields = instanceToGetFrom.getClass().getDeclaredFields();
            for (Object obj : instancesOfFields) {
                setAccessMethod.invoke(obj, true);
                String name = (String) getNameMethod.invoke(obj);
                if (name.equals(fieldName)) {
                    s = (Sprite) getMethod.invoke(obj, instanceToGetFrom);
                }
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

        s.setWidth(s.getWidth()*scale);
        s.setHeight(s.getHeight()*scale);
//            ReflectionUtils.setPrivateVariable("sprite",a,s);
        //String fieldName, Object instanceToModify, Object newValue
        String fieldName = "sprite";
        Object instanceToModify = a;
        Object newValue = s;
        Class<?> fieldClass = Class.forName("java.lang.reflect.Field",false, Class.class.getClassLoader());
        MethodHandle setMethod = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Object.class, Object.class));
        MethodHandle getNameMethod = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
        MethodHandle setAccessMethod = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, boolean.class));
        Object[] instancesOfFields = instanceToModify.getClass().getDeclaredFields();
        for(Object obj : instancesOfFields){
            setAccessMethod.invoke(obj, true);
            Object name = getNameMethod.invoke(obj);
            if(name.toString() == fieldName) {
                setMethod.invoke(obj, instanceToModify, newValue);
            }
        }
        //END COPYPASTA
//        a.setHitpoints(a.getHitpoints()*scale*100);
        a.setMass(a.getMass()*scale*Settings.getAsteroidMass());

        if(a.getExactBounds()!=null) {
            a.getExactBounds().update(a.getLocation(), a.getFacing());
//            a.getExactBounds() = (a.getExactBounds().clone());
            try {
//                ReflectionUtils.setPrivateVariableFromSuperClass("bounds",a,a.getExactBounds());//TODO without .clone()
//                public static void setPrivateVariableFromSuperClass(String fieldName, Object instanceToModify, Object newValue) throws Throwable {
                String fieldName2="bounds";
                Object instanceToModify2 = a;
                Object newValue2 = a.getExactBounds();
                Class<?> fieldClass2 = Class.forName("java.lang.reflect.Field",false, Class.class.getClassLoader());
                MethodHandle setMethod2 = MethodHandles.lookup().findVirtual(fieldClass2, "set", MethodType.methodType(Void.TYPE, Object.class, Object.class));
                MethodHandle getNameMethod2 = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
                MethodHandle setAccessMethod2 = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, boolean.class));
                Object[] instancesOfFields2 = instanceToModify2.getClass().getSuperclass().getDeclaredFields();
                for(Object obj : instancesOfFields2){
                    setAccessMethod2.invoke(obj, true);
                    Object name = getNameMethod2.invoke(obj);
                    if(name.toString() == fieldName2) {
                        setMethod2.invoke(obj, instanceToModify2, newValue2);
                    }
                }
//                }
                //END COPYPASTA

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        combatEngine.getAsteroidGrid().addObject(a,a.getLocation(),a.getCollisionRadius()*2f,a.getCollisionRadius()*2f);
        combatEngine.getAiGridAsteroids().addObject(a,a.getLocation(),a.getCollisionRadius()*2f,a.getCollisionRadius()*2f);
        return a;
    }
}
