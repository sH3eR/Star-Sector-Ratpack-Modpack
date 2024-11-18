package bcom;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.combat.entities.Ship;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Settings {
    //this will handle map size and exclusions for missile and gun changes

    private static final String SETTINGS_FILE_PATH= "data/config/bcomSettings.json";
    private static final String SHIP_EXCLUSIONS_FILE_PATH="data/config/bcom/shipExclusions.csv";
    private static final String MISSILE_TARGETING_EXCLUSIONS_FILE_PATH="data/config/bcom/missileTargetExclusion.csv";
    private static final String MISSILE_TIMER_EXCLUSIONS_FILE_PATH="data/config/bcom/missileTimerExclusion.csv";
    private static final String WEAPON_EXCLUSIONS_FILE_PATH="data/config/bcom/weaponExclusion.csv";
    private static final String SHIP_SYSTEMS="data/config/bcom/shipSystems.csv";

    private static int asteroidIndex;

    private static float
            MAP_SIZE_FACTOR,
            STANDOFF_FACTOR_WITH_OBJECTIVES,
            STANDOFF_FACTOR_WITHOUT_OBJECTIVES, MISSILE_RANGE_MULT,PROJECTILE_COAST_MULT,FIGHTER_RANGE_MULT,
            FLYIN_ASTEROID_NUMBER, FLYIN_ASTEROID_SPEED, ASTEROID_SIZE_SLOWDOWN_FACTOR,
            RING_ASTTEROID_NUMBER, RING_ASTEROID_SPEED,
            ASTEROID_RANDOM_MOVEMENT_FACTOR,
            ASTEROID_HP, ASTEROID_MASS, ASTEROID_HP_EXPONENT,
            FIGHTER_SPEED_BOOST_FACTOR,ASTEROID_SCALE_FACTOR,ASTEROID_MINIMUM_SCALE_FACTOR, ASTEROID_DENSITY,
            WEAPON_RANGE_MULT, WEAPON_RANGE_ADDITON, SHIP_SPEED, SHIP_ACCELERATION_MULT, ASTEROID_COLLISION_RADIUS_MULT,SHIP_DECELERATION_MULT,BOUNDS_MULT,MINIMUM_ZERO_FLUX_SPEED_BOOST_MULT,
            WEAPON_RANGE_THRESHOLD_MULT, SPLIT_ASTEROIDS_MIN_NUMBER, SPLIT_ASTEROIDS_MAX_NUMBER, SPLIT_ASTEROIDS_MIN_SIZE, SPLIT_ASTEROIDS_MAX_SIZE,
            SPLIT_ASTEROIDS_MIN_SPEED, SPLIT_ASTEROIDS_MAX_SPEED,
            PROJECTILE_SPEED_MULT, SPREAD_BUILDUP_MULT, SPREAD_DECAY_MULT, MAX_SPREAD_MULT,
            MISSILE_SPEED_MULT, MISSILE_ACCELERATION_MULT, MISSILE_DECELERATION_MULT, MISSILE_TURN_ACCELERATION_MULT, MISSILE_MAX_TURN_RATE_MULT,
            SIGHT_MULT, DEPLOY_X_DIST, DEPLOY_Y_DIST,DEPLOY_SPACE_USED, SHIP_ACCELERATION_ADDITION, SHIP_DECELERATION_ADDITION, CR_REDUCTION_HULL_OFFSET, CR_REDUCTION,AUTOFIRE_ACCURACY, COMBAT_CAMERA_MAX_ZOOM,
            INITIAL_DEPLOYMENT_Y_OFFSET, DEPLOYMENT_Y_OFFSET, SUBMUNITION_SPLIT_RANGE, SUBMUNITION_SPLIT_RANGE_RANGE,
    SUBMUNITION_SPREAD_SPEED, SUBMUNITIN_SPREAD_SPEED_RANGE, SUBMUNITION_SPREAD_SPEED_RANGE, SUBMUNITION_PROJECTILE_RANGE, SUBMUNITION_ARC, WEAPON_RANGE_EXP, ARMOR_MULT, ARMOR_ADDITION, SHIP_HP_MULT, SHIP_HP_ADDITION, SHIELD_UPKEEP_MULT, SHIELD_UPKEEP_ADDITION,
    SHIELD_EFFICIENCY_MULT, SHIELD_EFFICIENCY_ADDITION, SHIELD_ARC_MULT, SHIELD_ARC_ADDITION, SHIELD_RADIUS_MULT, SHIELD_RADIUS_ADDITION, PHASE_COST_MULT, PHASE_COST_ADDITION, PHASE_UPKEEP_MULT, PHASE_UPKEEP_ADDITION, FLUX_DISSIPATION_MULT, FLUX_DISSIPATION_ADDITION, FLUX_CAPACITY_MULT, FLUX_CAPACITY_ADDITION,
    OP_MULT, BEAM_DROPOFF_OFFSET, BEAM_DROPOFF_EXP,BEAM_DROPOFF_MULT, PHASE_SLOWDOWN_MULT, PHASE_MINIMUM_SLOWDOWN ,PHASE_FLUX_LEVEL_FOR_MIN_SPEED , PHASE_TIME_MULT, EXPLOSION_RADIUS_MULT, EXPLOSION_DAMAGE_MULT, MAP_SIZE_MULT_NO_OBJECTIVES, FIGHTER_BASE_SPEED, SHIP_BASE_SPEED,RETREAT_CR_RECKLESS,RETREAT_CR_AGGRESSIVE,
            RETREAT_CR_STEADY, RETREAT_CR_CAUTIOUS, RETREAT_CR_TIMID,SHIP_FRAGMENT_MAX_FACTOR, SHIP_FRAGMENT_MIN_FACTOR, SHIP_FRAGMENT_SLOWDOWN_FACTOR , ZERO_FLUX_SPEED_BOOST_MINIMUM_FLUX, WEAPON_DAMAGE_MULT,PPT_MULT,PPT_ADDITION,CR_DECAY_MULT,
    TURN_ACCELERATION_MULT, TURN_ACCELERATION_ADDITION, MAX_TURN_RATE_MULT, MAX_TURN_RATE_ADDITION, PPT_PER_HULL_DAMAGE, DEPLOYMENT_IMMUNITY_TIME, DEPLOYMENT_SLOWDOWN_AMOUNT,DEPLOYMENT_SLOWDOWN_TIME, BREAK_PROB_MULT, MIN_PIECES_MULT, MAX_PIECES_MULT;
            ;
    private static int ZERO_FLUX_BOOST_LIMIT,MAX_ASTEROIDS, ASTEROIDS_FOR_MAX_SPAWN, SPLIT_ASTEROIDS_ABSOLUTE_MAX, STARTING_COMMAND_POINTS, SECONDS_PER_COMMAND_POINT_RECOVERY, OP_ADDITION, MIN_PIECES_ADDITION, MAX_PIECES_ADDITION, PIECES_LIMIT;
    private static boolean ENABLE_MISSILE_RANGE, ENABLE_PROJECTILE_COAST, ENABLE_FIGHTER_RANGE_BOOST, ENABLE_ASTEROIDS, FORCE_ASTEROIDS, ENABLE_FLYIN_ASTEROIDS,
            ENABLE_WEAPON_RANGE_THRESHOLD, ENABLE_SPLIT_ASTEROIDS,
            ENABLE_WEAPON_RANGE, ENABLE_SHIP_SPEED, ENABLE_SHIP_ACCELERATION,
            ENABLE_ZERO_FLUX_BOOST, ENABLE_FIGHTER_SPEED_BOOST, ENABLE_ZERO_FLUX_FIX_DEPLOY, ENABLE_BOUNDS, STARTING_ASTEROIDS_MOVE,
            ENABLE_PROJECTILE_SPEED, ENABLE_RECOIL,ENABLE_MISSILE_SPEED, ENABLE_INSTANT_BEAMS, ENABLE_SIGHT, ENABLE_MAP, ENABLE_DEPLOYMENT,
            ENABLE_CR_REDUCTION, ENABLE_LEADING_TARGET_ACCURACY, ENABLE_SUBMUNITION, ENABLE_COMBAT_ZOOM, ENABLE_COMMAND_POINTS, ENABLE_ARBITRARY_SETTING, ENABLE_SHIP_DEFENCE, ENABLE_SHIP_SHIELD_AND_PHASE, ENABLE_SHIP_FLUX,
    ENABLE_OP, ENABLE_BEAM_DROPOFF, ENABLE_PHASE_CONSTANTS, ENABLE_SHIP_EXPLOSION, EXCLUDE_REDUCED_EXPLOSION, USE_ADDITIONAL_ASTEROID_SPRITES, ENABLE_RETREAT_AT_LOW_CR, ENABLE_SHIP_FRAGMENTS,
            ENABLE_PPT,ENABLE_DAMAGE,ENABLE_CUSTOM_VALUES,ENABLE_AFTER_DEPLOYMENT, ENABLE_NO_FLUX_BURN_SYSTEMS, ENABLE_SHIP_BREAK;
    private static final ArrayList<String> SHIP_EXCLUSIONS = new ArrayList<>();
    private static final ArrayList<String> MISSILE_TARGETING_EXCLUSIONS= new ArrayList<>();
    private static final ArrayList<String> MISSILE_TIMER_EXCLUSIONS= new ArrayList<>();
    private static final ArrayList<String> WEAPON_EXCLUSIONS= new ArrayList<>();

    private static final ArrayList<CustomValue> customValues = new ArrayList<>();

    private static final ArrayList<String> SHIP_SYSTEM_TO_MODIFY = new ArrayList<>();
    private static JSONArray ARBITRARY_JSON;
    public static int getAsteroidIndex(){return asteroidIndex;}
    public static void setAsteroidIndex(int index){asteroidIndex=index;}

    public static List<String> getShipExclusions(){return SHIP_EXCLUSIONS;}
    public static List<String> getMissileTargetingExclusions(){return MISSILE_TARGETING_EXCLUSIONS;}
    public static List<String> getMissileTimerExclusions(){return MISSILE_TIMER_EXCLUSIONS;}
    public static List<String> getWeaponExclusions(){return WEAPON_EXCLUSIONS;}
    public static List<String> getShipSystemsToModify(){return SHIP_SYSTEM_TO_MODIFY;}

    public static float getMissileRangeMult(WeaponSpecAPI weapon) {
        return getFloatSetting("MISSILE_RANGE_MULT",MISSILE_RANGE_MULT) * cv(true,weapon,"missileRangeMultiplier");
    }

    public static float getProjectileCoastMult(WeaponSpecAPI weapon) {
        return getFloatSetting("PROJECTILE_COAST_MULT",PROJECTILE_COAST_MULT) * cv(true,weapon,"ballisticCoastMultiplier");
    }

    public static float getFighterRangeMult(ShipHullSpecAPI ship) {
        return getFloatSetting("FIGHTER_RANGE_MULT",FIGHTER_RANGE_MULT) *cv(true,ship,"fighterRangeMultiplier");
    }

    public static int getZeroFluxBoostLimit(ShipHullSpecAPI ship) {
        return (int) (getIntSetting("ZERO_FLUX_BOOST_LIMIT",ZERO_FLUX_BOOST_LIMIT) + cv(false,ship,"zeroFluxBoostSpeedLimit"));
    }

    public static float getFighterSpeedBoostFactor(ShipHullSpecAPI ship) {
        return getFloatSetting("FIGHTER_SPEED_BOOST_FACTOR",FIGHTER_SPEED_BOOST_FACTOR) *cv(true,ship,"fighterSpeedBoostFactor");
    }

    public static boolean isEnableMissileRange() {
        return getBooleanSetting("ENABLE_MISSILE_RANGE",ENABLE_MISSILE_RANGE) ;
    }

    public static boolean isEnableProjectileCoast() {
        return getBooleanSetting("ENABLE_PROJECTILE_COAST",ENABLE_PROJECTILE_COAST) ;
    }

    public static boolean isEnableFighterRangeBoost() {
        return getBooleanSetting("ENABLE_FIGHTER_RANGE_BOOST",ENABLE_FIGHTER_RANGE_BOOST) ;
    }

    public static boolean isEnableZeroFluxBoost() {
        return getBooleanSetting("ENABLE_ZERO_FLUX_BOOST",ENABLE_ZERO_FLUX_BOOST) ;
    }

    public static boolean isEnableFighterSpeedBoost() {
        return getBooleanSetting("ENABLE_FIGHTER_SPEED_BOOST",ENABLE_FIGHTER_SPEED_BOOST) ;
    }
    public static boolean isEnableZeroFluxBoostFixDeploy() {
        return getBooleanSetting("ENABLE_ZERO_FLUX_FIX_DEPLOY",ENABLE_ZERO_FLUX_FIX_DEPLOY) ;
    }
    public static boolean isEnableSplitAsteriods(){
        return getBooleanSetting("ENABLE_SPLIT_ASTEROIDS",ENABLE_SPLIT_ASTEROIDS) ;
    }
    public static boolean isEnableMap(){
        return getBooleanSetting("ENABLE_MAP",ENABLE_MAP);
    }
    public static float getSplitAsteroidMinNumber(){
        return getFloatSetting("SPLIT_ASTEROIDS_MIN_NUMBER",SPLIT_ASTEROIDS_MIN_NUMBER) ;
    }
    public static float getSplitAsteroidMaxNumber(){
        return getFloatSetting("SPLIT_ASTEROIDS_MAX_NUMBER",SPLIT_ASTEROIDS_MAX_NUMBER) ;
    }
    public static float getSplitAsteroidsMinSize(){
        return getFloatSetting("SPLIT_ASTEROIDS_MIN_SIZE",SPLIT_ASTEROIDS_MIN_SIZE);
    }
    public static float getSplitAsteroidsMaxSize(){
        return getFloatSetting("SPLIT_ASTEROIDS_MAX_SIZE",SPLIT_ASTEROIDS_MAX_SIZE) ;
    }
    public static boolean isEnableAsteroids(){
        return getBooleanSetting("ENABLE_ASTEROIDS",ENABLE_ASTEROIDS) ;
    }
    public static boolean isForceAsteroids(){
        return getBooleanSetting("FORCE_ASTEROIDS",FORCE_ASTEROIDS) ;
    }
    public static float getAsteroidScaleFactor(){
        return getFloatSetting("ASTEROID_SCALE_FACTOR",ASTEROID_SCALE_FACTOR) ;
    }
    public static float getAsteroidMinimumScaleFactor(){
        return getFloatSetting("ASTEROID_MINIMUM_SCALE_FACTOR",ASTEROID_MINIMUM_SCALE_FACTOR) ;
    }
    public static float getAsteroidDensity(){
        return getFloatSetting("ASTEROID_DENSITY",ASTEROID_DENSITY);
    }
    public static int getMaxAsteroids(){
        return getIntSetting("MAX_ASTEROIDS",MAX_ASTEROIDS) ;
    }

    //map options
    public static float getMapSizeFactor() {
        return getFloatSetting("MAP_SIZE_FACTOR",MAP_SIZE_FACTOR) ;
    }

    public static float getStandoffFactorWithObjectives() {
        return getFloatSetting("STANDOFF_FACTOR_WITH_OBJECTIVES",STANDOFF_FACTOR_WITH_OBJECTIVES) ;
    }

    public static float getStandoffFactorWithoutObjectives() {
        return getFloatSetting("STANDOFF_FACTOR_WITHOUT_OBJECTIVES",STANDOFF_FACTOR_WITHOUT_OBJECTIVES) ;
    }

    public static float getFlyinAsteroidNumber() {
        return getFloatSetting("FLYIN_ASTEROID_NUMBER",FLYIN_ASTEROID_NUMBER) ;
    }

    public static float getFlyinAsteroidSpeed() {
        return getFloatSetting("FLYIN_ASTEROID_SPEED",FLYIN_ASTEROID_SPEED) ;
    }

    public static boolean isEnableFlyinAsteroids() {
        return getBooleanSetting("ENABLE_FLYIN_ASTEROIDS",ENABLE_FLYIN_ASTEROIDS);
    }

    public static boolean isMoveStartingAsteroidsWithFlyinAsteroids(){
        return getBooleanSetting("STARTING_ASTEROIDS_MOVE",STARTING_ASTEROIDS_MOVE) ;
    }

    public static float getAsteroidHp() {
        return getFloatSetting("ASTEROID_HP",ASTEROID_HP) ;
    }
    public static float getAsteroidHpExponent(){
        return getFloatSetting("ASTEROID_HP_EXPONENT",ASTEROID_HP_EXPONENT) ;
    }

    public static float getAsteroidMass() {
        return getFloatSetting("ASTEROID_MASS",ASTEROID_MASS) ;
    }

    public static float getWeaponRangeMult(WeaponSpecAPI weapon) {
        return getFloatSetting("WEAPON_RANGE_MULT",WEAPON_RANGE_MULT) * cv(true,weapon,"weaponRangeMultiplier") ;
    }
    public static float getWeaponRangeAddition(WeaponSpecAPI weapon){
        return getFloatSetting("WEAPON_RANGE_ADDITON",WEAPON_RANGE_ADDITON) + cv(false,weapon,"weaponRangeAddition") ;
    }

    public static float getShipSpeed(ShipHullSpecAPI ship) {
        return getFloatSetting("SHIP_SPEED",SHIP_SPEED) *  cv(true,ship,"shipSpeedFactor");
    }

    public static float getShipAccelerationMult(ShipHullSpecAPI ship) {
        return getFloatSetting("SHIP_ACCELERATION_MULT",SHIP_ACCELERATION_MULT) *cv(true,ship,"shipAccelerationMultiplier");
    }
    public static float getShipDecelerationMult(ShipHullSpecAPI ship) {
        return getFloatSetting("SHIP_DECELERATION_MULT",SHIP_DECELERATION_MULT) *cv(true,ship,"shipDecelerationMultiplier");
    }
    public static boolean isEnableWeaponRange() {
        return getBooleanSetting("ENABLE_WEAPON_RANGE",ENABLE_WEAPON_RANGE) ;
    }

    public static boolean isEnableShipSpeed() {
        return getBooleanSetting("ENABLE_SHIP_SPEED",ENABLE_SHIP_SPEED) ;
    }

    public static boolean isEnableShipAcceleration() {
        return getBooleanSetting("ENABLE_SHIP_ACCELERATION",ENABLE_SHIP_ACCELERATION) ;
    }
    public static float getAsteroidCollisionRadiusMult(){
        return ASTEROID_COLLISION_RADIUS_MULT;
    }

    public static float getAsteroidsForMaxSpawn(){
        return getFloatSetting("ASTEROIDS_FOR_MAX_SPAWN",ASTEROIDS_FOR_MAX_SPAWN) ;
    }
    public static float getAsteroidRandomMovementFactor(){
        return getFloatSetting("ASTEROID_RANDOM_MOVEMENT_FACTOR",ASTEROID_RANDOM_MOVEMENT_FACTOR) ;
    }
    public static float getAsteroidSizeSlowdownFactor(){
        return getFloatSetting("ASTEROID_SIZE_SLOWDOWN_FACTOR",ASTEROID_SIZE_SLOWDOWN_FACTOR) ;
    }
    public static float getRingAsteroidNumber(){
        return RING_ASTTEROID_NUMBER;
    }
    public static float getRingAsteroidSpeed(){
        return RING_ASTEROID_SPEED;
    }
    public static boolean isEnableBounds(){
        return getBooleanSetting("ENABLE_BOUNDS",ENABLE_BOUNDS) ;
    }
    public static float getBoundsMult(ShipHullSpecAPI ship){
        return getFloatSetting("BOUNDS_MULT",BOUNDS_MULT) *cv(true,ship,"shipBoundsMultiplier");
    }
    public static float getMinimumZeroFluxSpeedBoost(ShipHullSpecAPI ship) {
        return getFloatSetting("MINIMUM_ZERO_FLUX_SPEED_BOOST_MULT",MINIMUM_ZERO_FLUX_SPEED_BOOST_MULT) + cv(false,ship,"zeroFluxMinimumSpeed") ;
    }

    public static boolean isEnableRangeLimitIncrease(){
        return getBooleanSetting("ENABLE_WEAPON_RANGE_THRESHOLD",ENABLE_WEAPON_RANGE_THRESHOLD);
    }
    public static float getRangeThresholdMult(ShipHullSpecAPI ship){
        return getFloatSetting("WEAPON_RANGE_THRESHOLD_MULT",WEAPON_RANGE_THRESHOLD_MULT) * cv(true, ship,"weaponRangeThresholdMult") ;
    }
    public static float getSplitAsteroidMinSpeed(){
        return getFloatSetting("SPLIT_ASTEROIDS_MIN_SPEED",SPLIT_ASTEROIDS_MIN_SPEED) ;
    }
    public static float getSplitAsteroidMaxSpeed(){
        return getFloatSetting("SPLIT_ASTEROIDS_MAX_SPEED",SPLIT_ASTEROIDS_MAX_SPEED) ;
    }
    public static int getSplitAsteroidsAbsoluteMax(){
        return getIntSetting("SPLIT_ASTEROIDS_ABSOLUTE_MAX",SPLIT_ASTEROIDS_ABSOLUTE_MAX) ;
    }

    public static boolean isEnableProjectileSpeed(){
        return getBooleanSetting("ENABLE_PROJECTILE_SPEED",ENABLE_PROJECTILE_SPEED) ;
    }
    public static float getProjectileSpeedMult(WeaponSpecAPI weapon){
        return getFloatSetting("PROJECTILE_SPEED_MULT",PROJECTILE_SPEED_MULT) * cv(true,weapon,"projectileSpeedMult");
    }
    public static boolean isEnableRecoil(){
        return getBooleanSetting("ENABLE_RECOIL",ENABLE_RECOIL);
    }
    public static float getSpreadBuildupMult(WeaponSpecAPI weapon){
        return getFloatSetting("SPREAD_BUILDUP_MULT",SPREAD_BUILDUP_MULT) * cv(true, weapon,"spreadBuildupMult");
    }
    public static float getSpreadDecayRateMult(WeaponSpecAPI weapon){
        return getFloatSetting("SPREAD_DECAY_MULT",SPREAD_DECAY_MULT) * cv(true,weapon,"spreadDecayMult");
    }
    public static float getMaxSpreadMult(WeaponSpecAPI weapon){
        return getFloatSetting("MAX_SPREAD_MULT",MAX_SPREAD_MULT) * cv(true,weapon,"maxSpreadMult");
    }

    public static boolean isEnableMissileSpeed(){
        return getBooleanSetting("ENABLE_MISSILE_SPEED",ENABLE_MISSILE_SPEED) ;
    }
    public static float getMissileSpeedMult(WeaponSpecAPI weapon){
        return getFloatSetting("MISSILE_SPEED_MULT",MISSILE_SPEED_MULT) * cv(true,weapon,"missileSpeedMult") ;
    }
    public static float getMissileAccelerationMult(WeaponSpecAPI weapon){
        return getFloatSetting("MISSILE_ACCELERATION_MULT",MISSILE_ACCELERATION_MULT)  * cv(true,weapon,"missileAccelerationMult");
    }
    public static float getMissileDecelerationMult(WeaponSpecAPI weapon){
        return getFloatSetting("MISSILE_DECELERATION_MULT",MISSILE_DECELERATION_MULT)  * cv(true,weapon,"missileDecelerationMult");
    }
    public static float getMissileTurnAccelerationMult(WeaponSpecAPI weapon){
        return getFloatSetting("MISSILE_TURN_ACCELERATION_MULT",MISSILE_TURN_ACCELERATION_MULT)  * cv(true,weapon,"missileTurnAccelerationMult");
    }
    public static float getMissileMaxTurnRateMult(WeaponSpecAPI weapon){
        return getFloatSetting("MISSILE_MAX_TURN_RATE_MULT",MISSILE_MAX_TURN_RATE_MULT)  * cv(true,weapon,"missileMaxTurnRateMult");
    }
    public static boolean isEnableInstantBeams(){
        return getBooleanSetting("ENABLE_INSTANT_BEAMS",ENABLE_INSTANT_BEAMS) ;
    }
    public static boolean isEnableSightMult(){
        return getBooleanSetting("ENABLE_SIGHT",ENABLE_SIGHT);
    }
    public static float getSightMult(ShipHullSpecAPI ship){
        return getFloatSetting("SIGHT_MULT",SIGHT_MULT) * cv(true,ship,"sightMult");
    }
    public static boolean isEnableModifyDeployment(){
        return  getBooleanSetting("ENABLE_DEPLOYMENT",ENABLE_DEPLOYMENT);
    }
    public static float getDeployedXDist(){
        return getFloatSetting("DEPLOY_X_DIST",DEPLOY_X_DIST);
    }
    public static float getDeployedYDist(){
        return getFloatSetting("DEPLOY_Y_DIST",DEPLOY_Y_DIST);
    }
    public static float getDeploymentSpaceUsed(){
        return getFloatSetting("DEPLOY_SPACE_USED",DEPLOY_SPACE_USED);
    }
    public static float getShipAccelerationAddition( ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_ACCELERATION_ADDITION",SHIP_ACCELERATION_ADDITION) + cv(false,ship,"shipAccelerationAddition");
    }
    public static float getShipDecelerationAddition(ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_DECELERATION_ADDITION",SHIP_DECELERATION_ADDITION) + cv(false,ship,"shipDecelerationAddition");
    }
    public static boolean isEnableCrReductionOnHullDamage(){
        return getBooleanSetting("ENABLE_CR_REDUCTION",ENABLE_CR_REDUCTION);
    }
    public static float getCrReductionHullOffset(ShipHullSpecAPI ship){
        return getFloatSetting("CR_REDUCTION_HULL_OFFSET",CR_REDUCTION_HULL_OFFSET) * cv(true,ship,"crOffset");
    }
    public static float getCrReduction(ShipHullSpecAPI ship){
        return getFloatSetting("CR_REDUCTION",CR_REDUCTION) * cv(true,ship,"crReduction");
    }

    public static boolean isLeadingTargetAccuracy(){
        return getBooleanSetting("ENABLE_LEADING_TARGET_ACCURACY",ENABLE_LEADING_TARGET_ACCURACY);
    }
    public static float getAutoFireAimAccuracy(ShipHullSpecAPI ship){
        return getFloatSetting("AUTOFIRE_ACCURACY",AUTOFIRE_ACCURACY) * cv(true,ship,"autofireAccuracy");
    }
    public static float getInitialDeploymentYOffset(){
        return getFloatSetting("INITIAL_DEPLOYMENT_Y_OFFSET",INITIAL_DEPLOYMENT_Y_OFFSET);
    }
    public static float getDeploymentYOffset(){
        return getFloatSetting("DEPLOYMENT_Y_OFFSET",DEPLOYMENT_Y_OFFSET);
    }
    public static boolean isEnableSubmunitionFeature(){
        return getBooleanSetting("ENABLE_SUBMUNITION",ENABLE_SUBMUNITION);
    }
    public static float getSubmunitionSplitRange(WeaponSpecAPI weapon){
        return getFloatSetting("SUBMUNITION_SPLIT_RANGE",SUBMUNITION_SPLIT_RANGE) * cv(true,weapon,"submunitionSplitRangeMult");
    }
    public static float getSubmunitionSplitRangeRange(WeaponSpecAPI weapon){
        return getFloatSetting("SUBMUNITION_SPLIT_RANGE_RANGE",SUBMUNITION_SPLIT_RANGE_RANGE) * cv(true,weapon,"submunitionSplitRangeRangeMult");
    }
    public static float getSubmunitionSpreadSpeed(WeaponSpecAPI weapon){
        return getFloatSetting("SUBMUNITION_SPREAD_SPEED",SUBMUNITION_SPREAD_SPEED) * cv(true,weapon,"submunitionSpeedMult");
    }
    public static float getSubmunitionSpreadSpeedRange(WeaponSpecAPI weapon){
        return getFloatSetting("SUBMUNITION_SPREAD_SPEED_RANGE",SUBMUNITION_SPREAD_SPEED_RANGE) * cv(true,weapon,"submunitionSpreadSpeedMult");
    }
    public static float getSubmunitionProjectileRange(WeaponSpecAPI weapon){
        return getFloatSetting("SUBMUNITION_PROJECTILE_RANGE",SUBMUNITION_PROJECTILE_RANGE) * cv(true,weapon,"submunitionProjectileRangeMult");
    }
    public static float getSubmunitionArc(WeaponSpecAPI weapon){
        return getFloatSetting("SUBMUNITION_ARC",SUBMUNITION_ARC) * cv(true,weapon,"submunitionArcMult");
    }
    public static boolean isEnableCombatZoom(){
        return getBooleanSetting("ENABLE_COMBAT_ZOOM",ENABLE_COMBAT_ZOOM);
    }
    public static float getMaxCombatCameraZoom(){
        return getFloatSetting("COMBAT_CAMERA_MAX_ZOOM",COMBAT_CAMERA_MAX_ZOOM);
    }
//    public static boolean isEnableCommandPoints(){
//        return getBooleanSetting("ENABLE_COMMAND_POINTS",ENABLE_COMMAND_POINTS);
//    }
//    public static int getStartingCommandPoints(){
//        return getIntSetting("STARTING_COMMAND_POINTS",STARTING_COMMAND_POINTS);
//    }
    public static int getBaseSecondsPerCommandPoint(){
        return getIntSetting("SECONDS_PER_COMMAND_POINT_RECOVERY",SECONDS_PER_COMMAND_POINT_RECOVERY);
    }
    public static boolean isEnableArbitrarySetting(){
        return getBooleanSetting("ENABLE_ARBITRARY_SETTING",ENABLE_ARBITRARY_SETTING);
    }
    public static JSONArray getArbitrarySettings() throws JSONException {
            //no lunalib integration for this one... yet
            return ARBITRARY_JSON;
    }
    public static boolean isEnableShipDefence(){
        return getBooleanSetting("ENABLE_SHIP_DEFENCE",ENABLE_SHIP_DEFENCE);
    }
    public static boolean isEnableShipShieldAndPhase(){
        return getBooleanSetting("ENABLE_SHIP_SHIELD_AND_PHASE",ENABLE_SHIP_SHIELD_AND_PHASE);
    }
    public static boolean isEnableShipFlux(){
        return getBooleanSetting("ENABLE_SHIP_FLUX",ENABLE_SHIP_FLUX);
    }
    public static float getWeaponRangeExp(WeaponSpecAPI weapon){
        return getFloatSetting("WEAPON_RANGE_EXP",WEAPON_RANGE_EXP) * cv(true,weapon,"weaponRangeExponent");
    }
    public static float getArmorMult(ShipHullSpecAPI ship){
        return getFloatSetting("ARMOR_MULT",ARMOR_MULT)* cv(true,ship,"armorMult");
    }
    public static float getArmorAddition(ShipHullSpecAPI ship){
        return getFloatSetting("ARMOR_ADDITION",ARMOR_ADDITION) + cv(false,ship,"armorAddition");
    }
    public static float getShipHpMult(ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_HP_MULT",SHIP_HP_MULT) *  cv(true,ship,"shipHpMult");
    }
    public static float getShipHpAddition(ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_HP_ADDITION",SHIP_HP_ADDITION) + cv(false,ship,"shipHpAddition");
    }
    public static float getShieldUpkeepCostMult(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_UPKEEP_MULT",SHIELD_UPKEEP_MULT)* cv(true,ship,"shieldUpkeepMult");
    }
    public static float getShieldUpkeepCostAddition(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_UPKEEP_ADDITION",SHIELD_UPKEEP_ADDITION)+ cv(false,ship,"shieldUpkeepAddition");
    }
    public static float getShieldEfficiencyMult(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_EFFICIENCY_MULT",SHIELD_EFFICIENCY_MULT)* cv(true,ship,"shieldEfficiencyMult");
    }
    public static float getShieldEfficiencyAddition(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_EFFICIENCY_ADDITION",SHIELD_EFFICIENCY_ADDITION) + cv(false,ship,"shieldEfficiencyAddition");
    }
    public static float getShieldArcMult(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_ARC_MULT",SHIELD_ARC_MULT) * cv(true,ship,"shieldArcMult");
    }
    public static float getShieldArcAddition(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_ARC_ADDITION",SHIELD_ARC_ADDITION) +  cv(false,ship,"shieldArcAddition");
    }
    public static float getShieldRadiusMult(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_RADIUS_MULT",SHIELD_RADIUS_MULT) * cv(true,ship,"shieldRadiusMult");
    }
    public static float getShieldRadiusAddition(ShipHullSpecAPI ship){
        return getFloatSetting("SHIELD_RADIUS_ADDITION",SHIELD_RADIUS_ADDITION) + cv(false,ship,"shieldRadiusAddition");
    }
    public static float getPhaseCostMult(ShipHullSpecAPI ship){
        return getFloatSetting("PHASE_COST_MULT",PHASE_COST_MULT) * cv(true,ship,"phaseCostMult");
    }
    public static float getPhaseCostAddition(ShipHullSpecAPI ship){
        return getFloatSetting("PHASE_COST_ADDITION",PHASE_COST_ADDITION) + cv(false,ship,"phaseCostAddition");
    }
    public static float getPhaseUpkeepMult(ShipHullSpecAPI ship){
        return getFloatSetting("PHASE_UPKEEP_MULT",PHASE_UPKEEP_MULT) * cv(true,ship,"phaseUpkeepMult");
    }
    public static float getPhaseUpkeepAddition(ShipHullSpecAPI ship){
        return getFloatSetting("PHASE_UPKEEP_ADDITION",PHASE_UPKEEP_ADDITION) + cv(false,ship,"phaseUpkeepAddition");
    }
    public static float getFluxDissipationMult(ShipHullSpecAPI ship){
        return getFloatSetting("FLUX_DISSIPATION_MULT",FLUX_DISSIPATION_MULT) * cv(true,ship,"fluxDissipationMult");
    }
    public static float getFluxDissipationAddition(ShipHullSpecAPI ship){
        return getFloatSetting("FLUX_DISSIPATION_ADDITION",FLUX_DISSIPATION_ADDITION) + cv(false,ship,"fluxDissipationAddition");
    }
    public static float getFluxCapacityMult(ShipHullSpecAPI ship){
        return getFloatSetting("FLUX_CAPACITY_MULT",FLUX_CAPACITY_MULT) * cv(true,ship,"fluxCapacityMult");
    }
    public static float getFluxCapacityAddition(ShipHullSpecAPI ship){
        return getFloatSetting("FLUX_CAPACITY_ADDITION",FLUX_CAPACITY_ADDITION) + cv(false,ship,"fluxCapacityAddition");
    }

    public static boolean isPhaseConstants(){
        return getBooleanSetting("ENABLE_PHASE_CONSTANTS", ENABLE_PHASE_CONSTANTS);
    }

    public static float getPhaseMinimumSlowdown(){
        return getFloatSetting("PHASE_MINIMUM_SLOWDOWN",PHASE_MINIMUM_SLOWDOWN);
    }
    public static float getFluxLevelForMinSpeed(){
        return getFloatSetting("PHASE_FLUX_LEVEL_FOR_MIN_SPEED",PHASE_FLUX_LEVEL_FOR_MIN_SPEED);
    }
    public static float getPhaseTimeMult(){
        return getFloatSetting("PHASE_TIME_MULT",PHASE_TIME_MULT);
    }

    public static boolean isBeamWeaponDamageDropoff(){
        return getBooleanSetting("ENABLE_BEAM_DROPOFF",ENABLE_BEAM_DROPOFF);
    }

    public static float getBeamDropoffOffset(WeaponSpecAPI weapon){
        return getFloatSetting("BEAM_DROPOFF_OFFSET",BEAM_DROPOFF_OFFSET) * cv(true,weapon,"beamDropoffOffset");
    }

    public static float getBeamDropoffExp(WeaponSpecAPI weapon){
        return getFloatSetting("BEAM_DROPOFF_EXP",BEAM_DROPOFF_EXP)* cv(true,weapon,"beamDropoffExp");
    }
    public static float getBeamDropoffOffMult(WeaponSpecAPI weapon){
        return getFloatSetting("BEAM_DROPOFF_MULT",BEAM_DROPOFF_MULT) * cv(true,weapon,"beamDropoffMult");
    }

    public static boolean isEnableOp(){
        return getBooleanSetting("ENABLE_OP",ENABLE_OP);
    }

    public static float getOpMult(ShipHullSpecAPI ship){
        return getFloatSetting("OP_MULT",OP_MULT) * cv(true,ship,"shipOpMult");
    }
    public static int getOpAddition(ShipHullSpecAPI ship){
        return (int) (getIntSetting("OP_ADDITION",OP_ADDITION) + cv(false,ship,"shipOpAddition"));
    }
    public static boolean isEnableShipExplosion(){
        return getBooleanSetting("ENABLE_SHIP_EXPLOSION",ENABLE_SHIP_EXPLOSION);
    }
    public static boolean isExplosionNegatedByReducedExplosionHullmod(){
        return getBooleanSetting("EXCLUDE_REDUCED_EXPLOSION",EXCLUDE_REDUCED_EXPLOSION);
    }

    public static float getExplosionRadiusMult(ShipHullSpecAPI ship){
        return getFloatSetting("EXPLOSION_RADIUS_MULT",EXPLOSION_RADIUS_MULT) * cv(true,ship, "explosionRadiusMult");
    }
    public static float getExplosionDamageMult(ShipHullSpecAPI ship){
        return getFloatSetting("EXPLOSION_DAMAGE_MULT",EXPLOSION_DAMAGE_MULT) * cv(true,ship,"explosionDamageMult");
    }

    public static float getMapSizeFactorNoObjectives(){
        return getFloatSetting("MAP_SIZE_MULT_NO_OBJECTIVES",MAP_SIZE_MULT_NO_OBJECTIVES);
    }
    public static boolean useAdditionalAsteroidSprites(){
        return getBooleanSetting("USE_ADDITIONAL_ASTEROID_SPRITES",USE_ADDITIONAL_ASTEROID_SPRITES);
    }

    public static boolean isEnableShipFragments(){
        return getBooleanSetting("ENABLE_SHIP_FRAGMENTS",ENABLE_SHIP_FRAGMENTS);
    }
    public static float getShipFragmentMaxFactor(ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_FRAGMENT_MAX_FACTOR",SHIP_FRAGMENT_MAX_FACTOR) * cv(true,ship, "shipFragmentMaxSpeedFactor");
    }
    public static float getShipFragmentMinFactor(ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_FRAGMENT_MIN_FACTOR",SHIP_FRAGMENT_MIN_FACTOR)* cv(true,ship, "shipFragmentMinSpeedFactor");
    }
    public static float getShipFragmentMassSlowdownFactor(ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_FRAGMENT_SLOWDOWN_FACTOR",SHIP_FRAGMENT_SLOWDOWN_FACTOR)* cv(true,ship, "shipFragmentSlowdownByMass");
    }

    public static boolean isEnableRetreatAtLowCr(){
        return getBooleanSetting("ENABLE_RETREAT_AT_LOW_CR",ENABLE_RETREAT_AT_LOW_CR);
    }
    public static float getRetreatCrReckless(ShipHullSpecAPI ship){
        return getFloatSetting("RETREAT_CR_RECKLESS",RETREAT_CR_RECKLESS) + cv(false,ship,"crRetreatReckless");
    }
    public static float getRetreatCrAggressive(ShipHullSpecAPI ship){
        return getFloatSetting("RETREAT_CR_AGGRESSIVE",RETREAT_CR_AGGRESSIVE)+ cv(false,ship,"crRetreatAggressive");
    }
    public static float getRetreatCrSteady(ShipHullSpecAPI ship){
        return getFloatSetting("RETREAT_CR_STEADY",RETREAT_CR_STEADY)+ cv(false,ship,"crRetreatSteady");
    }
    public static float getRetreatCrCautious(ShipHullSpecAPI ship){
        return getFloatSetting("RETREAT_CR_CAUTIOUS",RETREAT_CR_CAUTIOUS)+ cv(false,ship,"crRetreatCautious");
    }
    public static float getRetreatCrTimid(ShipHullSpecAPI ship){
        return getFloatSetting("RETREAT_CR_TIMID",RETREAT_CR_TIMID)+ cv(false,ship,"crRetreatTimid");
    }

    public static float getSetMaxSpeedForFighters(ShipHullSpecAPI ship){
        return getFloatSetting("FIGHTER_BASE_SPEED",FIGHTER_BASE_SPEED) + cv(false,ship,"fighterTargetSpeed");
    }
    public static float getSetMaxSpeedForShips(ShipHullSpecAPI ship){
        return getFloatSetting("SHIP_BASE_SPEED",SHIP_BASE_SPEED) + cv(false,ship,"shipTargetSpeed");
    }

    public static float getShipTurnAccelerationMult(ShipHullSpecAPI ship){
        return getFloatSetting("TURN_ACCELERATION_MULT",TURN_ACCELERATION_MULT) * cv(true,ship,"turnAccelerationMult");
    }
    public static float getShipTurnAccelerationAddition(ShipHullSpecAPI ship){
        return getFloatSetting("TURN_ACCELERATION_ADDITION",TURN_ACCELERATION_ADDITION) + cv(false,ship,"turnAccelerationAddition");
    }
    public static float getShipMaxTurnRateMult(ShipHullSpecAPI ship){
        return getFloatSetting("MAX_TURN_RATE_MULT",MAX_TURN_RATE_MULT) * cv(true,ship,"maxTurnRateMult");
    }
    public static float getShipMaxTurnRateAddition(ShipHullSpecAPI ship){
        return getFloatSetting("MAX_TURN_RATE_ADDITION",MAX_TURN_RATE_ADDITION) + cv(false,ship,"maxTurnRateAddition");
    }
    public static boolean isEnablePptValues(){
        return getBooleanSetting("ENABLE_PPT",ENABLE_PPT);
    }
    public static float getPptMult(ShipHullSpecAPI ship){
        return getFloatSetting("PPT_MULT",PPT_MULT) * cv(true,ship,"pptMult");
    }
    public static float getPptAddition(ShipHullSpecAPI ship){
        return getFloatSetting("PPT_ADDITION",PPT_ADDITION) + cv(false,ship,"pptAddition");
    }
    public static float getCrLossPerSecondMult(ShipHullSpecAPI ship){
        return getFloatSetting("CR_DECAY_MULT",CR_DECAY_MULT) * cv(true,ship,"crDecayMult");
    }

    public static boolean isEnableDamage(){
        return getBooleanSetting("ENABLE_DAMAGE",ENABLE_DAMAGE);
    }
    public static float getWeaponDamageMult(WeaponSpecAPI weapon){
        return getFloatSetting("WEAPON_DAMAGE_MULT",WEAPON_DAMAGE_MULT) * cv(true,weapon,"weaponDamageMult");
    }
    public static float getZeroFluxSpeedBoostMinFluxLevel(ShipHullSpecAPI ship){
        return getFloatSetting("ZERO_FLUX_SPEED_BOOST_MINIMUM_FLUX",ZERO_FLUX_SPEED_BOOST_MINIMUM_FLUX) + cv(false,ship,"zeroFluxSpeedBoostMinimumFlux");
    }
    public static float getPptPerHullDamagePercent(ShipHullSpecAPI ship){//PPT_PER_HULL_DAMAGE
        return getFloatSetting("PPT_PER_HULL_DAMAGE",PPT_PER_HULL_DAMAGE) * cv(true,ship,"pptPerHullDamage");
    }

    //        //customValuesFeature
    //        ENABLE_CUSTOM_VALUES = settings.getBoolean("customValuesFeature");
    //
    //        //after deployment feature
    //        ENABLE_AFTER_DEPLOYMENT = settings.getBoolean("afterDeploymentFeature");
    //        DEPLOYMENT_SLOWDOWN_AMOUNT = (float) settings.getDouble("slowdownAfterDeploymentAmount");
    //        DEPLOYMENT_SLOWDOWN_TIME = (float) settings.getDouble("slowdownAfterDeploymentTime");
    //        DEPLOYMENT_IMMUNITY_TIME = (float) settings.getDouble("deploymentImmunityTime");
    public static boolean isEnableZeroFluxBurnSystems(){
        return getBooleanSetting("ENABLE_NO_FLUX_BURN_SYSTEMS",ENABLE_NO_FLUX_BURN_SYSTEMS);
    }
    public static boolean isEnableAfterDeployment(){
        return getBooleanSetting("ENABLE_AFTER_DEPLOYMENT",ENABLE_AFTER_DEPLOYMENT);
    }
    public static float getAfterDeploymentSlowdownAmount(ShipHullSpecAPI ship){
        return getFloatSetting("DEPLOYMENT_SLOWDOWN_AMOUNT",DEPLOYMENT_SLOWDOWN_AMOUNT)* cv(true,ship,"slowdownAfterDeploymentAmount");
    }
    public static float getAfterDeploymentSlowdownTime(ShipHullSpecAPI ship){
        return getFloatSetting("DEPLOYMENT_SLOWDOWN_TIME",DEPLOYMENT_SLOWDOWN_TIME) + cv(false,ship,"slowdownAfterDeploymentAmount");
    }
    public static float getDeploymentImmuneTime(ShipHullSpecAPI ship){
        return getFloatSetting("DEPLOYMENT_IMMUNITY_TIME",DEPLOYMENT_IMMUNITY_TIME) + cv(false,ship,"deploymentImmunityTime");
    }

    public static boolean isEnableShipBreak(){
        return getBooleanSetting("ENABLE_SHIP_BREAK",ENABLE_SHIP_BREAK);
    }
    public static float getBreakProb(ShipHullSpecAPI ship){
        return getFloatSetting("BREAK_PROB_MULT",BREAK_PROB_MULT) * cv(true,ship,"breakProbabilityMult");
    }
    public static float getBreakMinPiecesMult(ShipHullSpecAPI ship){
        return getFloatSetting("MIN_PIECES_MULT",MIN_PIECES_MULT) * cv(true,ship,"minPiecesMult");
    }
    public static int getBreakMinPeicesAddition(ShipHullSpecAPI ship){
        return (int) (getIntSetting("MIN_PIECES_ADDITION",MIN_PIECES_ADDITION) + cv(false,ship, "minPiecesAddition"));
    }
    public static float getBreakMaxPiecesMult(ShipHullSpecAPI ship){
        return getFloatSetting("MAX_PIECES_MULT",MAX_PIECES_MULT) * cv(true,ship,"maxPiecesMult");
    }
    public static int getBreakMaxPeicesAddition(ShipHullSpecAPI ship){
        return (int) (getIntSetting("MAX_PIECES_ADDITION",MAX_PIECES_ADDITION) + cv(false,ship,"maxPiecesAddition"));
    }
    public static int getBreakCeiling(ShipHullSpecAPI ship){
        return (int) (getIntSetting("PIECES_LIMIT",PIECES_LIMIT) + cv(false,ship,"piecesLimit"));
    }
    private static boolean getBooleanSetting(String value, boolean otherValue){
        if(isLunalib()){
            return LunaSettings.getBoolean("bcom",value);
        }else{
            return otherValue;
        }
    }
    private static float getFloatSetting(String value, float otherValue){
        if(isLunalib()){
            return LunaSettings.getDouble("bcom",value).floatValue();
        }else{
            return otherValue;
        }
    }
    private static int getIntSetting(String value, int otherValue){
        if(isLunalib()){
            return LunaSettings.getInt("bcom",value);
        }else{
            return otherValue;
        }
    }
    private static boolean isLunalib(){
        return Global.getSettings().getModManager().isModEnabled("lunalib") && LunaSettings.getBoolean("bcom","LUNALIB_SETTINGS_ENABLED");
    }
    private static float cv(boolean multiplicative, ShipHullSpecAPI ship, String valueKey){
        float runningTotal;
        if(multiplicative)
            runningTotal = 1;
        else
            runningTotal =0;
        for(CustomValue value : customValues){
            if(isCustomSettingApplicableShip(value,ship,valueKey)){
                if(multiplicative)
                    runningTotal *= value.effects.get(valueKey);
                else
                    runningTotal += value.effects.get(valueKey);

            }
        }
        return runningTotal;
    }
    private static float cv(boolean multiplicative, WeaponSpecAPI weapon, String valueKey){
        float runningTotal;
        if(multiplicative)
            runningTotal = 1;
        else
            runningTotal =0;
        for(CustomValue value : customValues){
            if(isCustomSettingApplicableWeapon(value,weapon,valueKey)){
                if(multiplicative)
                    runningTotal *= value.effects.get(valueKey);
                else
                    runningTotal += value.effects.get(valueKey);

            }
        }
        return runningTotal;
    }
    private static boolean isCustomSettingApplicableWeapon(CustomValue value, WeaponSpecAPI weapon, String valueKey){
        boolean match = true;
        match = match && value.effects.containsKey(valueKey);
        match = match && (value.id.isEmpty()||value.id.contains(weapon.getWeaponId()));
        if(!value.projectileId.isEmpty()) {
            String id;
            if (weapon.getProjectileSpec() instanceof MissileSpecAPI ) {
                id = ((MissileSpecAPI) weapon.getProjectileSpec()).getHullSpec().getHullId();
            } else if (weapon.getProjectileSpec() instanceof ProjectileSpecAPI) {
                id = ((ProjectileSpecAPI) weapon.getProjectileSpec()).getId();
            } else {
                id = "bcom_none";
            }
            match = match & value.projectileId.contains(id);
        }
        match = match && (value.weaponSize.isEmpty() || value.weaponSize.contains(weapon.getSize()));
        match = match && (value.weaponType.isEmpty() || value.weaponType.contains(weapon.getType()));
        match = match && (value.techStyle.isEmpty() || value.techStyle.contains(weapon.getManufacturer()));
        boolean foundTag = true;
        if(!value.tags.isEmpty()){
            foundTag = false;
            for(String tag :weapon.getTags()){
                if(value.tags.contains(tag)){
                    foundTag = true;
                    break;
                }
            }
        }
        match = match && foundTag;

        boolean foundHint = true;
        if(!value.tags.isEmpty()){
            foundHint = false;
            for(WeaponAPI.AIHints hint : weapon.getAIHints()){
                if(value.tags.contains(hint.toString())){
                    foundHint = true;
                    break;
                }
            }
        }
        match = match && foundHint;

        return match;
    }
    private static boolean isCustomSettingApplicableShip(CustomValue value, ShipHullSpecAPI ship, String valueKey){
        boolean match = true;
        match = match && value.effects.containsKey(valueKey);
        match = match && (value.id.isEmpty()||value.id.contains(ship.getHullId()));
        match = match && (value.hullSize.isEmpty() || value.hullSize.contains(ship.getHullSize()));
        match = match && (value.techStyle.isEmpty() || value.techStyle.contains(ship.getManufacturer()));
        boolean foundTag = true;
        if(!value.tags.isEmpty()){
            foundTag = false;
            for(String tag :ship.getTags()){
                if(value.tags.contains(tag)){
                    foundTag = true;
                    break;
                }
            }
        }

        boolean foundHint = true;
        if(!value.tags.isEmpty()){
            foundHint = false;
            for(ShipHullSpecAPI.ShipTypeHints hint : ship.getHints()){
                if(value.tags.contains(hint.toString())){
                    foundHint = true;
                    break;
                }
            }
        }
        match = match && foundHint;

        match = match && foundTag;
        return match;
    }
    public static void load() throws JSONException, IOException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE_PATH);
        final JSONArray shipSettings = Global.getSettings().getMergedSpreadsheetDataForMod("id",SHIP_EXCLUSIONS_FILE_PATH, "bcom");
        final JSONArray missileTargetingSettings = Global.getSettings().getMergedSpreadsheetDataForMod("id",MISSILE_TARGETING_EXCLUSIONS_FILE_PATH, "bcom");
        final JSONArray missileTimerSettings = Global.getSettings().getMergedSpreadsheetDataForMod("id",MISSILE_TIMER_EXCLUSIONS_FILE_PATH, "bcom");
        final JSONArray weaponExclusions = Global.getSettings().getMergedSpreadsheetDataForMod("id",WEAPON_EXCLUSIONS_FILE_PATH, "bcom");
        final JSONArray shipSystems = Global.getSettings().getMergedSpreadsheetDataForMod("id",SHIP_SYSTEMS,"bcom");

        MISSILE_RANGE_MULT= (float) settings.getDouble("missileRangeMultiplier");
        PROJECTILE_COAST_MULT= (float) settings.getDouble("ballisticCoastMultiplier");
        FIGHTER_RANGE_MULT= (float) settings.getDouble("fighterRangeMultiplier");
        ZERO_FLUX_BOOST_LIMIT= settings.getInt("zeroFluxBoostSpeedLimit");
        FIGHTER_SPEED_BOOST_FACTOR = (float) settings.getDouble("fighterSpeedBoostFactor");

        SHIP_SPEED = (float) settings.getDouble("shipSpeedFactor");
        SHIP_ACCELERATION_MULT = (float) settings.getDouble("shipAccelerationMultiplier");
        SHIP_DECELERATION_MULT = (float) settings.getDouble("shipDecelerationMultiplier");

        SHIP_ACCELERATION_ADDITION = (float) settings.getDouble("shipAccelerationAddition");
        SHIP_DECELERATION_ADDITION = (float) settings.getDouble("shipDecelerationAddition");

        WEAPON_RANGE_MULT = (float) settings.getDouble("weaponRangeMultiplier");
        WEAPON_RANGE_ADDITON = (float) settings.getDouble("weaponRangeAddition");

        ENABLE_MISSILE_RANGE = settings.getBoolean("missileRangeFeature");
        ENABLE_PROJECTILE_COAST = settings.getBoolean("projectileCoastFeature");
        ENABLE_FIGHTER_RANGE_BOOST = settings.getBoolean("fighterRangeFeature");
        ENABLE_ZERO_FLUX_BOOST = settings.getBoolean("zeroFluxBoostFeature");
        ENABLE_FIGHTER_SPEED_BOOST = settings.getBoolean("fighterSpeedBoostFeature");
        ENABLE_ZERO_FLUX_FIX_DEPLOY = settings.getBoolean("zeroFluxBoostFeatureFixDeploy");
        ENABLE_ASTEROIDS = settings.getBoolean("asteroidFeature");
        FORCE_ASTEROIDS = settings.getBoolean("forceAsteroids");
        ENABLE_WEAPON_RANGE =  settings.getBoolean("weaponRangeFeature");
        ENABLE_SHIP_SPEED =  settings.getBoolean("shipSpeedFeature");
        ENABLE_SHIP_ACCELERATION = settings.getBoolean("shipAccelerationFeature");

        ASTEROID_SCALE_FACTOR  = (float) settings.getDouble("asteroidScaleFactor");
        ASTEROID_MINIMUM_SCALE_FACTOR = (float) settings.getDouble("asteroidMinimumSizeFactor");
        ASTEROID_DENSITY = (float) settings.getDouble("asteroidDensity");
        MAX_ASTEROIDS = settings.getInt("maxAsteroids");
        ASTEROID_MASS = (float) settings.getDouble("asteroidMassFactor");
        ASTEROID_HP = (float) settings.getDouble("asteroidHpFactor");
        ASTEROID_HP_EXPONENT = (float) settings.getDouble("asteroidHpExponentFactor");
        ASTEROID_COLLISION_RADIUS_MULT = (float) settings.getDouble("asteroidCollisionFactor");

        RING_ASTTEROID_NUMBER = (float) settings.getDouble("ringAsteroidNumber");
        RING_ASTEROID_SPEED = (float) settings.getDouble("ringAsteroidSpeed");
        ASTEROID_RANDOM_MOVEMENT_FACTOR = (float) settings.getDouble("asteroidRandomMovementFactor");
        ASTEROIDS_FOR_MAX_SPAWN = settings.getInt("numNearbyAsteroidsForMaxSpawn");
        ASTEROID_SIZE_SLOWDOWN_FACTOR = (float) settings.getDouble("asteroidSizeSlowdownFactor");
        STARTING_ASTEROIDS_MOVE = settings.getBoolean("asteroidsMoveWithFlyinAsteroids");
        //map feature
        ENABLE_MAP = settings.getBoolean("mapFeature");
        MAP_SIZE_FACTOR = (float) settings.getDouble("mapSizeFactor");
        STANDOFF_FACTOR_WITH_OBJECTIVES = (float) settings.getDouble(
                "standOffFactorWithObjectives");
        STANDOFF_FACTOR_WITHOUT_OBJECTIVES = (float) settings.getDouble(
                "standOffFactorWithoutObjectives");

        //map feature asteroids
        ENABLE_FLYIN_ASTEROIDS = settings.getBoolean("flyinAsteroidFeature");
        FLYIN_ASTEROID_NUMBER = (float) settings.getDouble("flyinAstoidNumberMultiplier");
        FLYIN_ASTEROID_SPEED = (float) settings.getDouble("flyinAsteroidSpeedMultiplier");

        //bounds feature
        ENABLE_BOUNDS = settings.getBoolean("shipBoundsFeature");
        BOUNDS_MULT = (float) settings.getDouble("shipBoundsMultiplier");

        //threshold feature
        ENABLE_WEAPON_RANGE_THRESHOLD = settings.getBoolean("weaponRangeThresholdFeature");
        WEAPON_RANGE_THRESHOLD_MULT = (float) settings.getDouble("weaponRangeThresholdMult");

        //asteroid splitting
        ENABLE_SPLIT_ASTEROIDS = settings.getBoolean("asteroidSplittingFeature");
        SPLIT_ASTEROIDS_MIN_SIZE = (float) settings.getDouble("asteroidSplittingMinSize");
        SPLIT_ASTEROIDS_MAX_SIZE = (float) settings.getDouble("asteroidSplittingMaxSize");
        SPLIT_ASTEROIDS_MIN_NUMBER = (float) settings.getDouble("asteroidSplittingMinNumber");
        SPLIT_ASTEROIDS_MAX_NUMBER = (float) settings.getDouble("asteroidSplittingMaxNumber");
        SPLIT_ASTEROIDS_ABSOLUTE_MAX = settings.getInt("asteroidSplittingMax");
        SPLIT_ASTEROIDS_MIN_SPEED = (float) settings.getDouble("asteroidSplittingMinSpeed");
        SPLIT_ASTEROIDS_MAX_SPEED = (float) settings.getDouble("asteroidSplittingMaxSpeed");

        //proj speed
        ENABLE_PROJECTILE_SPEED = settings.getBoolean("projectileSpeedFeature");
        PROJECTILE_SPEED_MULT = (float) settings.getDouble("projectileSpeedMult");
        //spread stats
        ENABLE_RECOIL = settings.getBoolean("recoilFeature");
        SPREAD_BUILDUP_MULT = (float) settings.getDouble("spreadBuildupMult");
        SPREAD_DECAY_MULT = (float) settings.getDouble("spreadDecayMult");
        MAX_SPREAD_MULT = (float) settings.getDouble("maxSpreadMult");
        //missile speed
        //            MISSILE_SPEED_MULT, MISSILE_ACCELERATION_MULT, MISSILE_DECELERATION_MULT, MISSILE_TURN_ACCELERATION_MULT, MISSILE_MAX_TURN_RATE_MULT;
        ENABLE_MISSILE_SPEED = settings.getBoolean("missileSpeedFeature");
        MISSILE_SPEED_MULT = (float) settings.getDouble("missileSpeedMult");
        MISSILE_ACCELERATION_MULT = (float) settings.getDouble("missileAccelerationMult");
        MISSILE_DECELERATION_MULT = (float) settings.getDouble("missileDecelerationMult");
        MISSILE_TURN_ACCELERATION_MULT = (float) settings.getDouble("missileTurnAccelerationMult");
        MISSILE_MAX_TURN_RATE_MULT = (float) settings.getDouble("missileMaxTurnRateMult");

        ENABLE_INSTANT_BEAMS = settings.getBoolean("instantBeamFeature");

        MINIMUM_ZERO_FLUX_SPEED_BOOST_MULT = (float) settings.getDouble("zeroFluxMinimumSpeed");

        //sight
        ENABLE_SIGHT = settings.getBoolean("sightFeature");
        SIGHT_MULT = (float) settings.getDouble("sightMult");

        //deployment
        ENABLE_DEPLOYMENT = settings.getBoolean("deploymentFeature");
        DEPLOY_X_DIST = (float) settings.getDouble("deploymentXDist");
        DEPLOY_Y_DIST = (float) settings.getDouble("deploymentYDist");
        DEPLOY_SPACE_USED = (float) settings.getDouble("deploymentSpaceUsed");
        INITIAL_DEPLOYMENT_Y_OFFSET = (float) settings.getDouble("initialDeploymentYOffset");
        DEPLOYMENT_Y_OFFSET = (float) settings.getDouble("deploymentYOffset");

        //CR reduction
        ENABLE_CR_REDUCTION = settings.getBoolean("crReductionOnDamageFeature");
        CR_REDUCTION_HULL_OFFSET = (float) settings.getDouble("crOffset");
        CR_REDUCTION = (float) settings.getDouble("crReduction");
        //leading target accuracy
        ENABLE_LEADING_TARGET_ACCURACY = settings.getBoolean("autofireAccuracyFeature");
        AUTOFIRE_ACCURACY = (float) settings.getDouble("autofireAccuracy");

        //submunition feature
        ENABLE_SUBMUNITION = settings.getBoolean("submunitionFeature");
        SUBMUNITION_SPLIT_RANGE = (float) settings.getDouble("submunitionSplitRangeMult");
        SUBMUNITION_SPLIT_RANGE_RANGE = (float) settings.getDouble("submunitionSplitRangeRangeMult");
        SUBMUNITION_SPREAD_SPEED = (float) settings.getDouble("submunitionSpeedMult");
        SUBMUNITION_SPREAD_SPEED_RANGE = (float) settings.getDouble("submunitionSpreadSpeedMult");
        SUBMUNITION_PROJECTILE_RANGE = (float) settings.getDouble("submunitionProjectileRangeMult");
        SUBMUNITION_ARC = (float) settings.getDouble("submunitionArcMult");

        //combat camera zoom
        ENABLE_COMBAT_ZOOM = settings.getBoolean("combatCameraZoomFeature");
        COMBAT_CAMERA_MAX_ZOOM = (float) settings.getDouble("combatCameraMaxZoom");
//        //command point
//        ENABLE_COMMAND_POINTS = settings.getBoolean("commandPointFeature");
//        STARTING_COMMAND_POINTS = settings.getInt("startingCommandPoints");
//        SECONDS_PER_COMMAND_POINT_RECOVERY = settings.getInt("secondsPerCommandPointRecovery");

        //arbitrary json
        ENABLE_ARBITRARY_SETTING = settings.getBoolean("arbitrarySettingFeature");
        ARBITRARY_JSON = settings.getJSONArray("arbitrarySetting");

//        WEAPON_RANGE_EXP, ARMOR_MULT, ARMOR_ADDITION, SHIP_HP_MULT, SHIP_HP_ADDITION, SHIELD_UPKEEP_MULT, SHIELD_UPKEEP_ADDITION,
//                SHIELD_EFFICIENCY_MULT, SHIELD_EFFICIENCY_ADDITION, SHIELD_ARC_MULT, SHIELD_ARC_ADDITION, SHIELD_RADIUS_MULT,
//                SHIELD_RADIUS_ADDITION, PHASE_COST_MULT, PHASE_COST_ADDITION, PHASE_UPKEEP_MULT, PHASE_UPKEEP_ADDITION ;
//FLUX_DISSIPATION_MULT, FLUX_DISSIPATION_ADDITION, FLUX_CAPACITY_MULT, FLUX_CAPACITY_ADDITION

        //ENABLE_SHIP_DEFENCE, ENABLE_SHIP_SHIELD_AND_PHASE, ENABLE_SHIP_FLUX
        ENABLE_SHIP_DEFENCE =  settings.getBoolean("shipDefenceFeature");
        ENABLE_SHIP_SHIELD_AND_PHASE = settings.getBoolean("shipShieldAndPhaseFeature");
        ENABLE_SHIP_FLUX = settings.getBoolean("shipFluxFeature");

        WEAPON_RANGE_EXP = (float) settings.getDouble("weaponRangeExponent");

        ARMOR_MULT = (float) settings.getDouble("armorMult");
        ARMOR_ADDITION = (float) settings.getDouble("armorAddition");
        SHIP_HP_MULT = (float) settings.getDouble("shipHpMult");
        SHIP_HP_ADDITION = (float) settings.getDouble("shipHpAddition");
        SHIELD_UPKEEP_MULT = (float) settings.getDouble("shieldUpkeepMult");
        SHIELD_UPKEEP_ADDITION = (float) settings.getDouble("shieldUpkeepAddition");
        SHIELD_EFFICIENCY_MULT = (float) settings.getDouble("shieldEfficiencyMult");
        SHIELD_EFFICIENCY_ADDITION = (float) settings.getDouble("shieldEfficiencyAddition");
        SHIELD_ARC_MULT = (float) settings.getDouble("shieldArcMult");
        SHIELD_ARC_ADDITION = (float) settings.getDouble("shieldArcAddition");
        SHIELD_RADIUS_MULT = (float) settings.getDouble("shieldRadiusMult");
        SHIELD_RADIUS_ADDITION = (float) settings.getDouble("shieldRadiusAddition");
        PHASE_COST_MULT = (float) settings.getDouble("phaseCostMult");
        PHASE_COST_ADDITION = (float) settings.getDouble("phaseCostAddition");
        PHASE_UPKEEP_MULT = (float) settings.getDouble("phaseUpkeepMult");
        PHASE_UPKEEP_ADDITION= (float) settings.getDouble("phaseUpkeepAddition");
        FLUX_DISSIPATION_MULT= (float) settings.getDouble("fluxDissipationMult");
        FLUX_DISSIPATION_ADDITION= (float) settings.getDouble("fluxDissipationAddition");
        FLUX_CAPACITY_MULT= (float) settings.getDouble("fluxCapacityMult");
        FLUX_CAPACITY_ADDITION= (float) settings.getDouble("fluxCapacityAddition");

        ENABLE_OP = settings.getBoolean("opFeature");
        OP_MULT = (float) settings.getDouble("shipOpMult");
        OP_ADDITION = settings.getInt("shipOpAddition");

        ENABLE_BEAM_DROPOFF = settings.getBoolean("beamDamageDropoffFeature");
        BEAM_DROPOFF_OFFSET = (float) settings.getDouble("beamDropoffOffset") ;
        BEAM_DROPOFF_EXP= (float) settings.getDouble("beamDropoffExp");
        BEAM_DROPOFF_MULT= (float) settings.getDouble("beamDropoffMult");

        ENABLE_PHASE_CONSTANTS = settings.getBoolean("phaseConstantsFeature");
        PHASE_MINIMUM_SLOWDOWN = (float) settings.getDouble("phaseMinimumSlowdown");
        PHASE_FLUX_LEVEL_FOR_MIN_SPEED = (float) settings.getDouble("phaseFluxLevelForMinSpeed");
        PHASE_TIME_MULT = (float) settings.getDouble("PhaseTimeMult");

        ENABLE_SHIP_EXPLOSION = settings.getBoolean("shipExplosionFeature");
        EXCLUDE_REDUCED_EXPLOSION = settings.getBoolean("explosionNegatedByReducedExplosionHullmod");
        EXPLOSION_RADIUS_MULT = (float)settings.getDouble("explosionRadiusMult");
        EXPLOSION_DAMAGE_MULT = (float)settings.getDouble("explosionDamageMult");

        //MAP_SIZE_MULT_NO_OBJECTIVES, FIGHTER_BASE_SPEED, SHIP_BASE_SPEED
        MAP_SIZE_MULT_NO_OBJECTIVES = (float)settings.getDouble("mapSizeFactorNoObjectives");
        FIGHTER_BASE_SPEED = (float)settings.getDouble("fighterTargetSpeed");
        SHIP_BASE_SPEED = (float)settings.getDouble("shipTargetSpeed");

        USE_ADDITIONAL_ASTEROID_SPRITES = settings.getBoolean("useAdditionalAsteroidSprites");

        ENABLE_RETREAT_AT_LOW_CR = settings.getBoolean("retreatAtLowCrFeature");
        RETREAT_CR_RECKLESS = (float)settings.getDouble("crRetreatReckless");
        RETREAT_CR_AGGRESSIVE = (float)settings.getDouble("crRetreatAggressive");
        RETREAT_CR_STEADY = (float)settings.getDouble("crRetreatSteady");
        RETREAT_CR_CAUTIOUS = (float)settings.getDouble("crRetreatCautious");
        RETREAT_CR_TIMID = (float)settings.getDouble("crRetreatTimid");

        ENABLE_SHIP_FRAGMENTS = settings.getBoolean("shipFragmentSpeedFeature");
        SHIP_FRAGMENT_MAX_FACTOR = (float)settings.getDouble("shipFragmentMaxSpeedFactor");
        SHIP_FRAGMENT_MIN_FACTOR = (float)settings.getDouble("shipFragmentMinSpeedFactor");
        SHIP_FRAGMENT_SLOWDOWN_FACTOR = (float)settings.getDouble("shipFragmentSlowdownByMass");

        //ENABLE_PPT,ENABLE_DAMAGE,ENABLE_CUSTOM_VALUES
        //ZERO_FLUX_SPEED_BOOST_MINIMUM_FLUX, WEAPON_DAMAGE_MULT,PPT_MULT,PPT_ADDITION,CR_DECAY_MULT,
        //    TURN_ACCELERATION_MULT, TURN_ACCELERATION_ADDITION, MAX_TURN_RATE_MULT, MAX_TURN_RATE_ADDITION;
        //in category zeroFluxBoostFeature
        ZERO_FLUX_SPEED_BOOST_MINIMUM_FLUX = (float)settings.getDouble("zeroFluxSpeedBoostMinimumFlux");

        //in category shipAccelerationFeature
        TURN_ACCELERATION_MULT = (float)settings.getDouble("turnAccelerationMult");
        TURN_ACCELERATION_ADDITION = (float)settings.getDouble("turnAccelerationAddition");
        MAX_TURN_RATE_MULT = (float)settings.getDouble("maxTurnRateMult");
        MAX_TURN_RATE_ADDITION = (float)settings.getDouble("maxTurnRateAddition");

        ENABLE_PPT = settings.getBoolean("pptFeature");
        PPT_MULT = (float)settings.getDouble("pptMult");
        PPT_ADDITION = (float)settings.getDouble("pptAddition");
        CR_DECAY_MULT = (float)settings.getDouble("crDecayMult");

        ENABLE_DAMAGE = settings.getBoolean("weaponDamageFeature");
        WEAPON_DAMAGE_MULT = (float)settings.getDouble("weaponDamageMult");

        PPT_PER_HULL_DAMAGE = (float) settings.getDouble("pptPerHullDamage");

        //customValuesFeature
        ENABLE_CUSTOM_VALUES = settings.getBoolean("customValuesFeature");

        //after deployment feature
        ENABLE_AFTER_DEPLOYMENT = settings.getBoolean("afterDeploymentFeature");
        DEPLOYMENT_SLOWDOWN_AMOUNT = (float) settings.getDouble("slowdownAfterDeploymentAmount");
        DEPLOYMENT_SLOWDOWN_TIME = (float) settings.getDouble("slowdownAfterDeploymentTime");
        DEPLOYMENT_IMMUNITY_TIME = (float) settings.getDouble("deploymentImmunityTime");

        //noFluxBurnSystemsFeature
        ENABLE_NO_FLUX_BURN_SYSTEMS = settings.getBoolean("noFluxBurnSystemsFeature");

        ENABLE_SHIP_BREAK = settings.getBoolean("shipBreakFeature");
        BREAK_PROB_MULT = (float)settings.getDouble("breakProbabilityMult");
        MIN_PIECES_MULT = (float)settings.getDouble("minPiecesMult");
        MIN_PIECES_ADDITION = settings.getInt("minPiecesAddition");
        MAX_PIECES_MULT = (float) settings.getDouble("maxPiecesMult");
        MAX_PIECES_ADDITION = settings.getInt("maxPiecesAddition");
        PIECES_LIMIT = settings.getInt("piecesLimit");

        if(getBooleanSetting("ENABLE_CUSTOM_VALUES",ENABLE_CUSTOM_VALUES)) {
            //These are the same object type.
            JSONArray customValuesWeapons = settings.getJSONArray("customValues");

            boolean useJsonSettings = true;
            if(isLunalib()){
                useJsonSettings = LunaSettings.getBoolean("bcom","INCLUDE_SETTINGS_CUSTOM_FEATURES");

                //adding luna settings
                int startIndex = 1; //index starts at 1 to be consistent with headers and such
                int weaponBlocks = 3;//must match what is in luna settings
                int shipBlocks = 3;
                for (int i = startIndex; i<startIndex+weaponBlocks+shipBlocks;i++){
                    CustomValue value = new CustomValue();
                    if(i<=weaponBlocks){
                        String projId = LunaSettings.getString("bcom","customValuesWeaponProjectile"+i);
                        if(projId!=null && !projId.equals("bcom_none")){
                            value.getProjectileId().add(projId);
                        }
                        //weapon values population
                        String size = LunaSettings.getString("bcom","customValuesWeaponSize"+i);
                        if(size!=null && !size.equals("None")){
                            value.getWeaponSize().add(WeaponAPI.WeaponSize.valueOf(size));
                        }
                        String type = LunaSettings.getString("bcom","customValuesWeaponType"+i);
                        if(type!=null && !type.equals("None")){
                            value.getWeaponType().add(WeaponAPI.WeaponType.valueOf(type));
                        }
                    }else{
                        //ship values population
                        String hullSize = LunaSettings.getString("bcom","customValuesShipSize"+i);
                        if(hullSize!=null && !hullSize.equals("None")){
                            value.getHullSize().add(ShipAPI.HullSize.valueOf(hullSize));
                        }
                    }
                    String customValuesId = LunaSettings.getString("bcom","customValuesId"+i);
                    if(customValuesId!=null && !customValuesId.equals("bcom_none")){
                        value.getId().add(customValuesId);
                    }
                    String techStyle =  LunaSettings.getString("bcom","customValuesTechStyle"+i);
                    if(techStyle!=null && !techStyle.equals("bcom_none")){
                        value.getTechStyle().add(techStyle);
                    }
                    String tag = LunaSettings.getString("bcom","customValuesTag"+i);
                    if(tag!=null && !tag.equals("bcom_none")){
                        value.getTags().add(tag);
                    }
                    String hint = LunaSettings.getString("bcom","customValuesHint"+i);
                    if(hint!=null && !hint.equals("bcom_none")){
                        value.getTags().add(hint);
                    }
                    String setting1 =LunaSettings.getString("bcom","customValuesEffectFirst"+i);
                    float value1 = LunaSettings.getDouble("bcom","customValuesFactorFirst"+i).floatValue();
                    String setting2 =LunaSettings.getString("bcom","customValuesEffectFirst"+i);
                    float value2 =LunaSettings.getDouble("bcom","customValuesFactorFirst"+i).floatValue();
                    String setting3 =LunaSettings.getString("bcom","customValuesEffectFirst"+i);
                    float value3 =LunaSettings.getDouble("bcom","customValuesFactorFirst"+i).floatValue();

                    if(setting1!=null&& !setting1.equals("none")){
                        value.getEffects().put(setting1,value1);
                    }
                    if(setting2!=null&& !setting2.equals("none")){
                        value.getEffects().put(setting2,value2);
                    }
                    if(setting3!=null&& !setting3.equals("none")){
                        value.getEffects().put(setting3,value3);
                    }
                    //TODO add hints to lunalib

                    customValues.add(value);
                }
            }
            if(useJsonSettings) {
                for (int i = 0; i < customValuesWeapons.length(); i++) {
                    CustomValue value = new CustomValue();
                    JSONObject customValueJson = (JSONObject) customValuesWeapons.get(i);
                    if (customValueJson.has("ids")) {
                        JSONArray ids = customValueJson.getJSONArray("ids");
                        for (int j = 0; j < ids.length(); j++) {
                            value.getId().add((String) ids.get(j));
                        }
                    }
                    if (customValueJson.has("projectileIds")) {
                        JSONArray ids = customValueJson.getJSONArray("projectileIds");
                        for (int j = 0; j < ids.length(); j++) {
                            value.getProjectileId().add((String) ids.get(j));
                        }
                    }
                    if (customValueJson.has("weaponSizes")) {
                        JSONArray weaponSizes = customValueJson.getJSONArray("weaponSizes");
                        for (int j = 0; j < weaponSizes.length(); j++) {
                            value.getWeaponSize().add(WeaponAPI.WeaponSize.valueOf((String) weaponSizes.get(j)));
                        }
                    }
                    if (customValueJson.has("weaponTypes")) {
                        JSONArray weaponTypes = customValueJson.getJSONArray("weaponTypes");
                        for (int j = 0; j < weaponTypes.length(); j++) {
                            value.getWeaponType().add(WeaponAPI.WeaponType.valueOf((String) weaponTypes.get(j)));
                        }
                    }
                    if (customValueJson.has("shipSizes")) {
                        JSONArray shipSizes = customValueJson.getJSONArray("shipSizes");
                        for (int j = 0; j < shipSizes.length(); j++) {
                            value.getHullSize().add(ShipAPI.HullSize.valueOf((String) shipSizes.get(j)));
                        }
                    }
                    if (customValueJson.has("techStyles")) {
                        JSONArray techStyle = customValueJson.getJSONArray("techStyles");
                        for (int j = 0; j < techStyle.length(); j++) {
                            value.getTechStyle().add((String) techStyle.get(j));
                        }
                    }
                    if (customValueJson.has("tags")) {
                        JSONArray tags = customValueJson.getJSONArray("tags");
                        for (int j = 0; j < tags.length(); j++) {
                            value.getTags().add(tags.getString(j));
                        }
                    }
                    if (customValueJson.has("hints")){
                        JSONArray hints = customValueJson.getJSONArray("hints");
                        for (int j = 0; j < hints.length(); j++) {
                            value.getHints().add(hints.getString(j));
                        }
                    }
                    if (customValueJson.has("effects")) {
                        JSONArray effects = customValueJson.getJSONArray("effects");
                        for (int j = 0; j < effects.length(); j++) {
                            JSONObject effect = effects.getJSONObject(j);
                            Iterator effectIterator = effect.keys();
                            while (effectIterator.hasNext()) {
                                String effectString = (String) effectIterator.next();
                                value.getEffects().put(effectString, (float) effect.getDouble(effectString));
                            }
                        }
                    }
                    customValues.add(value);
                }
            }
        }
        for(int i=0;i<shipSettings.length();i++){
            SHIP_EXCLUSIONS.add(shipSettings.getJSONObject(i).getString("id"));
        }
        for(int i=0;i<missileTargetingSettings.length();i++){
            MISSILE_TARGETING_EXCLUSIONS.add(missileTargetingSettings.getJSONObject(i).getString("id"));
        }
        for(int i=0;i<missileTimerSettings.length();i++){
            MISSILE_TIMER_EXCLUSIONS.add(missileTimerSettings.getJSONObject(i).getString("id"));
        }
        for(int i=0;i<weaponExclusions.length();i++){
            WEAPON_EXCLUSIONS.add(weaponExclusions.getJSONObject(i).getString("id"));
        }
        for(int i=0;i<shipSystems.length();i++){
            SHIP_SYSTEM_TO_MODIFY.add(shipSystems.getJSONObject(i).getString("id"));
        }


    }

}
