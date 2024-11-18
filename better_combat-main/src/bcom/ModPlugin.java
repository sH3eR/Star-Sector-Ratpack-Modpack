package bcom;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.PhaseCloakStats;
import com.fs.starfarer.api.loading.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ModPlugin extends BaseModPlugin {
    static List<String> missilesModified = new ArrayList<>();
    static List<String> missilesDamageModified = new ArrayList<>();
    static List<String> projectilesDamageModified = new ArrayList<>();

    public void onDevModeF8Reload() {
        try {
            loadBcom();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        //battle size plugin
        if (Settings.isEnableMap()) {
            Global.getSector().registerPlugin(new CampaignPlugin());
        }
    }

    public void onApplicationLoad() {
        try {
            loadBcom();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void loadBcom() throws Throwable {
        Settings.load();
        Logger logger = Logger.getLogger(this.getClass().getName());

        final JSONArray weaponDataCSV;

        try {
            weaponDataCSV = Global.getSettings().getMergedSpreadsheetData("id",
                    "data/weapons/weapon_data.csv");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < weaponDataCSV.length(); i++) {
            final String weaponId = getWeaponId(weaponDataCSV, i);
            if (weaponId.isEmpty() || Settings.getWeaponExclusions().contains(weaponId)) continue;
            final WeaponSpecAPI weaponSpec;
            try {
                weaponSpec = Global.getSettings().getWeaponSpec(weaponId);
            } catch (Throwable t) {
                t.printStackTrace();
                continue;
            }
            if (weaponSpec.getProjectileSpec() instanceof MissileSpecAPI && Settings.isEnableMissileRange())
                modifyMissileWeaponSpec(weaponSpec);
            if (!(weaponSpec.getProjectileSpec() instanceof MissileSpecAPI))
                modifyNonMissileWeaponSpec(weaponSpec);
            if (!weaponSpec.isBeam() && !(weaponSpec.getProjectileSpec() instanceof MissileSpecAPI))
                modifyProjectileWeaponSpec(weaponSpec);
            modifyDamage(weaponSpec);
        }
        modifyFighterWingSpecs();
        modifyShipSystemSpecs();
        for (final ShipHullSpecAPI shipHullSpec : Global.getSettings().getAllShipHullSpecs()) {
            if (Settings.isEnableFighterSpeedBoost() && shipHullSpec.getHullSize().equals((ShipAPI.HullSize.FIGHTER)))
                modifyAllFighterShipHullSpecs(shipHullSpec);
            modifyShipHullSpecs(shipHullSpec);
        }
        if (Settings.isEnableCombatZoom()) {
            Global.getSettings().getSettingsJSON().put("maxCombatZoom", Settings.getMaxCombatCameraZoom());
        }

        JSONObject graphics = (JSONObject) Global.getSettings().getSettingsJSON().get("graphics");
        JSONObject terrain = graphics.getJSONObject("terrain");
        int asteriodTextureNumber = 0;
        List<String> textures = new ArrayList<>();
        if (Settings.useAdditionalAsteroidSprites()) {//Settings.useLargeAsteroidTextures()
            JSONArray asteroidTextures = Global.getSettings().getMergedSpreadsheetDataForMod("path", "data/config/bcom/asteroids.csv", "bcom");
            textures = new ArrayList<>();
            for (int i = 0; i < asteroidTextures.length(); i++) {
                textures.add(asteroidTextures.getJSONObject(i).getString("path"));
            }
        }
        for (int i = 1; true; i++) {
            if (terrain.has("asteroid_" + i)) {
                //IDK why, I just feel compelled to do this
                String path = terrain.getString("asteroid_" + i);
                textures.remove(path);
                asteriodTextureNumber++;
            } else {
                break;
            }
        }
        for (String texture : textures) {
            terrain.put("asteroid_" + (asteriodTextureNumber + 1), texture);
            asteriodTextureNumber++;
            Global.getSettings().loadTexture(texture);
        }
        //we store asteroid texture number in settings for use in extra asteroid plugin
        Settings.setAsteroidIndex(asteriodTextureNumber);
        if (Settings.isPhaseConstants()) {
            PhaseCloakStats.MIN_SPEED_MULT = Settings.getPhaseMinimumSlowdown();
            PhaseCloakStats.BASE_FLUX_LEVEL_FOR_MIN_SPEED = Settings.getFluxLevelForMinSpeed();
            PhaseCloakStats.MAX_TIME_MULT = Settings.getPhaseTimeMult();
        }

//        if(Settings.isEnableCommandPoints()){
//            Global.getSettings().getSettingsJSON().put("startingCommandPoints",Settings.getStartingCommandPoints());
//            Global.getSettings().getSettingsJSON().put("baseSecondsPerCommandPoint",Settings.getBaseSecondsPerCommandPoint());
//        }
        if (Settings.isEnableArbitrarySetting()) {//arbitrary settings
            JSONObject ssSettings = Global.getSettings().getSettingsJSON();
            JSONArray bcomSettings = Settings.getArbitrarySettings();
            for (int i = 0; i < bcomSettings.length(); i++) {
                JSONObject o = (JSONObject) bcomSettings.get(i);
                if (o.has("key") && o.has("type") && o.has("value")) {
                    String key = o.getString("key");
                    switch (o.getString("type")) {
                        case "int":
                            int valueInt = o.getInt("value");
                            ssSettings.put(key, valueInt);
                            break;
                        case "double":
                            float valueFloat = (float) o.getDouble("value");
                            ssSettings.put(key, valueFloat);
                            break;
                        case "boolean":
                            boolean valueBoolean = o.getBoolean("value");
                            ssSettings.put(key, valueBoolean);
                            break;
                        default:
                            logger.info("bad type: " + o.getString("type"));
                    }
                } else {
                    logger.info("Better Combat Error parsing file: " + o.toString());
                }
            }
        }


    }

    private static String getWeaponId(final JSONArray weaponDataCSV, final int index) {
        try {
            return weaponDataCSV.getJSONObject(index).getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void modifyProjectileWeaponSpec(WeaponSpecAPI weaponSpec) {
        if (Settings.isEnableProjectileCoast()) {
            ((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).setMaxRange(weaponSpec.getMaxRange() + weaponSpec.getMaxRange() * Settings.getProjectileCoastMult(weaponSpec));
        }
        if (Settings.isEnableProjectileSpeed()) {
            ((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).setMoveSpeed(((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).getMoveSpeed(null, null) * Settings.getProjectileSpeedMult(weaponSpec));
            weaponSpec.setProjectileSpeed(((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).getMoveSpeed(null, null) * Settings.getProjectileSpeedMult(weaponSpec));
//            ((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).setFadeTime(((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).getFadeTime()/Settings.getProjectileSpeedMult());
        }
        if (Settings.isEnableRecoil()) {
            weaponSpec.setSpreadBuildup(weaponSpec.getSpreadBuildup() * Settings.getSpreadBuildupMult(weaponSpec));
            weaponSpec.setSpreadDecayRate(weaponSpec.getSpreadDecayRate() * Settings.getSpreadDecayRateMult(weaponSpec));
            weaponSpec.setMaxSpread(weaponSpec.getMaxSpread() * Settings.getMaxSpreadMult(weaponSpec));
//            weaponSpec.setMinSpread();
        }
    }

    private static void modifyMissileWeaponSpec(WeaponSpecAPI weaponSpec) throws JSONException {
        if (Settings.isEnableMissileRange()) {
            if (!Settings.getMissileTargetingExclusions().contains(weaponSpec.getWeaponId())) {
                weaponSpec.setMaxRange(weaponSpec.getMaxRange() * Settings.getMissileRangeMult(weaponSpec));
            }
            if (!Settings.getMissileTimerExclusions().contains(weaponSpec.getWeaponId())) {
                ((MissileSpecAPI) weaponSpec.getProjectileSpec()).setMaxFlightTime(((MissileSpecAPI) weaponSpec.getProjectileSpec()).getMaxFlightTime() * Settings.getMissileRangeMult(weaponSpec));
            }
        }
        if (!missilesModified.contains(((MissileSpecAPI) weaponSpec.getProjectileSpec()).getHullSpec().getHullId())) {
            final MissileSpecAPI missileSpec = (MissileSpecAPI) weaponSpec.getProjectileSpec();
            if (Settings.isEnableMissileSpeed()) {
                missileSpec.getHullSpec().getEngineSpec().setMaxSpeed(missileSpec.getHullSpec().getEngineSpec().getMaxSpeed() * Settings.getMissileSpeedMult(weaponSpec));
                missileSpec.getHullSpec().getEngineSpec().setAcceleration(missileSpec.getHullSpec().getEngineSpec().getAcceleration() * Settings.getMissileAccelerationMult(weaponSpec));
                missileSpec.getHullSpec().getEngineSpec().setDeceleration(missileSpec.getHullSpec().getEngineSpec().getDeceleration() * Settings.getMissileDecelerationMult(weaponSpec));
                missileSpec.getHullSpec().getEngineSpec().setTurnAcceleration(missileSpec.getHullSpec().getEngineSpec().getTurnAcceleration() * Settings.getMissileTurnAccelerationMult(weaponSpec));
                missileSpec.getHullSpec().getEngineSpec().setMaxTurnRate(missileSpec.getHullSpec().getEngineSpec().getMaxTurnRate() * Settings.getMissileMaxTurnRateMult(weaponSpec));
            }
            if (Settings.isEnableSubmunitionFeature() && missileSpec.getBehaviorSpec() != null) {
                JSONObject spec = missileSpec.getBehaviorSpec().getParams();
                if (spec.getString("behavior").equals("MIRV")) {
                    if (spec.has("splitRange")) {
                        spec.put("splitRange", (Integer) spec.getInt("splitRange") * Settings.getSubmunitionSplitRange(weaponSpec));
                    }
                    if (spec.has("splitRangeRange")) {
                        spec.put("splitRangeRange", (Integer) spec.getInt("splitRangeRange") * Settings.getSubmunitionSplitRangeRange(weaponSpec));
                    }
                    if (spec.has("spreadSpeed")) {
                        spec.put("spreadSpeed", (Integer) spec.getInt("spreadSpeed") * Settings.getSubmunitionSpreadSpeed(weaponSpec));
                    }
                    if (spec.has("spreadSpeedRange")) {
                        spec.put("spreadSpeedRange", (Integer) spec.getInt("spreadSpeedRange") * Settings.getSubmunitionSpreadSpeedRange(weaponSpec));
                    }
                    if (spec.has("projectileRange")) {
                        spec.put("projectileRange", (Integer) spec.getInt("projectileRange") * Settings.getSubmunitionProjectileRange(weaponSpec));
                    }
                    if (spec.has("arc")) {
                        spec.put("arc", (Integer) spec.getInt("arc") * Settings.getSubmunitionArc(weaponSpec));
                    }
                }
            }
            missilesModified.add(missileSpec.getHullSpec().getHullId());
        }

    }

    private static void modifyNonMissileWeaponSpec(WeaponSpecAPI weaponSpec) {
        if (Settings.isEnableWeaponRange()) {
            float newRange = (float) ((Math.pow(weaponSpec.getMaxRange(), Settings.getWeaponRangeExp(weaponSpec)) * Settings.getWeaponRangeMult(weaponSpec)) + Settings.getWeaponRangeAddition(weaponSpec));
            weaponSpec.setMaxRange(newRange);
            if (!weaponSpec.isBeam()) {
                //this calculation must be the same as above
                ((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).setMaxRange(newRange);
            }
        }
        if (Settings.isEnableInstantBeams()) {
            if (weaponSpec.isBeam()) {
                weaponSpec.setBeamSpeed(10000000000000f);
            }
        }
    }

    private static void modifyAllFighterShipHullSpecs(ShipHullSpecAPI shipHullSpec) {
        final ShipHullSpecAPI.EngineSpecAPI engineSpec = shipHullSpec.getEngineSpec();
        if (engineSpec == null) return;
        engineSpec.setMaxSpeed(((Settings.getSetMaxSpeedForFighters(shipHullSpec) - engineSpec.getMaxSpeed()) * Settings.getFighterSpeedBoostFactor(shipHullSpec)) + engineSpec.getMaxSpeed());
    }

    private static void modifyShipHullSpecs(ShipHullSpecAPI shipHullSpec) throws Throwable {
        final ShipHullSpecAPI.EngineSpecAPI engineSpec = shipHullSpec.getEngineSpec();
        if (engineSpec == null || Settings.getShipExclusions().contains(shipHullSpec.getHullId())) return;
        if (Settings.isEnableShipSpeed())
            engineSpec.setMaxSpeed(((Settings.getSetMaxSpeedForShips(shipHullSpec) - engineSpec.getMaxSpeed()) * Settings.getShipSpeed(shipHullSpec)) + engineSpec.getMaxSpeed());
        if (Settings.isEnableShipAcceleration()) {
            engineSpec.setAcceleration(engineSpec.getAcceleration() * Settings.getShipAccelerationMult(shipHullSpec) + Settings.getShipAccelerationAddition(shipHullSpec));
            engineSpec.setDeceleration(engineSpec.getDeceleration() * Settings.getShipDecelerationMult(shipHullSpec) + Settings.getShipDecelerationAddition(shipHullSpec));
            engineSpec.setTurnAcceleration(engineSpec.getTurnAcceleration() * Settings.getShipTurnAccelerationMult(shipHullSpec) + Settings.getShipTurnAccelerationAddition(shipHullSpec));
            engineSpec.setMaxTurnRate(engineSpec.getMaxTurnRate() * Settings.getShipMaxTurnRateMult(shipHullSpec) + engineSpec.getMaxTurnRate() * Settings.getShipMaxTurnRateAddition(shipHullSpec));
        }
        if ( Settings.isEnableShipBreak() || Settings.isEnableShipDefence() || Settings.isEnableShipShieldAndPhase() || Settings.isEnableShipFlux() || Settings.isEnableOp() || Settings.isEnablePptValues()) {//TODO other ship stats


            //this will get the method to get set ship armor/hull
            //I will not be using the word. The windows gremlins do not like it
            Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());

            MethodHandle setAccessMethod = MethodHandles.lookup().findVirtual(methodClass, "setAccessible", MethodType.methodType(Void.TYPE, boolean.class));
            MethodHandle invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
            MethodHandle setField = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Object.class, Object.class));
            MethodHandle setFieldAccess = MethodHandles.lookup().findVirtual(fieldClass, "setAccessible", MethodType.methodType(Void.TYPE, boolean.class));

            if (Settings.isEnableShipDefence()) {
                Object getArmorSpec = shipHullSpec.getClass().getDeclaredMethod("getArmorSpec");
                setAccessMethod.invoke(getArmorSpec, true);
                //arg 1 is the method to run. arg 2 is the object it is running on
                Object armor = invokeMethod.invoke(getArmorSpec, shipHullSpec);
                Object[] armorFields = armor.getClass().getDeclaredFields();
                setFieldAccess.invoke(armorFields[0], true);
                setFieldAccess.invoke(armorFields[1], true);

                float currentArmor = shipHullSpec.getArmorRating();
                float currentHp = shipHullSpec.getHitpoints();

                setField.invoke(armorFields[0], armor, currentHp * Settings.getShipHpMult(shipHullSpec) + Settings.getShipHpAddition(shipHullSpec)); //this is armor
                setField.invoke(armorFields[1], armor, currentArmor * Settings.getArmorMult(shipHullSpec) + Settings.getArmorAddition(shipHullSpec)); //this is hull
            }
            if (Settings.isEnableShipShieldAndPhase()) {
                ShipHullSpecAPI.ShieldSpecAPI shieldSpec = shipHullSpec.getShieldSpec();//was Object if this causes problems
                Object[] shieldFields = shieldSpec.getClass().getDeclaredFields();
                for (int i = 0; i < shieldFields.length; i++) {
                    setFieldAccess.invoke(shieldFields[i], true);
                }
                //0:type
                //1: upkeep cost
                //2: flux/dmg absorbed
                //3: arc
                //4: radius
                //5:center x
                //6:center y
                //7:color
                //8:color
                //9:phase cost
                //10:phase upkeep
                setField.invoke(shieldFields[1], shieldSpec, shieldSpec.getUpkeepCost() * Settings.getShieldUpkeepCostMult(shipHullSpec) + Settings.getShieldUpkeepCostAddition(shipHullSpec));
                setField.invoke(shieldFields[2], shieldSpec, shieldSpec.getFluxPerDamageAbsorbed() * Settings.getShieldEfficiencyMult(shipHullSpec) + Settings.getShieldEfficiencyAddition(shipHullSpec));
                setField.invoke(shieldFields[3], shieldSpec, shieldSpec.getArc() * Settings.getShieldArcMult(shipHullSpec) + Settings.getShieldArcAddition(shipHullSpec));
                setField.invoke(shieldFields[4], shieldSpec, shieldSpec.getRadius() * Settings.getShieldRadiusMult(shipHullSpec) + Settings.getShieldRadiusAddition(shipHullSpec));
                setField.invoke(shieldFields[9], shieldSpec, shieldSpec.getPhaseCost() * Settings.getPhaseCostMult(shipHullSpec) + Settings.getPhaseCostAddition(shipHullSpec));
                setField.invoke(shieldFields[10], shieldSpec, shieldSpec.getPhaseUpkeep() * Settings.getPhaseUpkeepMult(shipHullSpec) + Settings.getPhaseUpkeepAddition(shipHullSpec));
            }
            if (Settings.isEnableShipFlux()) {
                //flux
                Object getReactorSpec = shipHullSpec.getClass().getDeclaredMethod("getReactorSpec");
                setAccessMethod.invoke(getReactorSpec, true);
                Object reactorSpec = invokeMethod.invoke(getReactorSpec, shipHullSpec);
                Object[] reactorFields = reactorSpec.getClass().getDeclaredFields();
                setFieldAccess.invoke(reactorFields[0], true);
                setFieldAccess.invoke(reactorFields[1], true);
                setField.invoke(reactorFields[0], reactorSpec, shipHullSpec.getFluxCapacity() * Settings.getFluxCapacityMult(shipHullSpec) + Settings.getFluxCapacityAddition(shipHullSpec));
                setField.invoke(reactorFields[1], reactorSpec, shipHullSpec.getFluxDissipation() * Settings.getFluxDissipationMult(shipHullSpec) + Settings.getFluxDissipationAddition(shipHullSpec));
            }
            if (Settings.isEnableOp()) {
                int finalOp = (int) ((int) shipHullSpec.getOrdnancePoints(null) * Settings.getOpMult(shipHullSpec) + Settings.getOpAddition(shipHullSpec));
                //setOrdnancePoints  int
                Object setOrdnancePoints = shipHullSpec.getClass().getDeclaredMethod("setOrdnancePoints", Integer.TYPE);
                setAccessMethod.invoke(setOrdnancePoints, true);
                invokeMethod.invoke(setOrdnancePoints, shipHullSpec, finalOp);
            }
            if (Settings.isEnablePptValues()) {
                float pptAddition = Settings.getPptAddition(shipHullSpec);
                float pptMult = Settings.getPptMult(shipHullSpec);
                float crLossPerSecondMult = Settings.getCrLossPerSecondMult(shipHullSpec);

                float ppt = shipHullSpec.getNoCRLossSeconds() * pptMult + pptAddition;
                float crLossPerSecond = shipHullSpec.getCRLossPerSecond() * crLossPerSecondMult;
                //CR LOSS AND PPT

                //    public float getNoCRLossSeconds()
                //    public void setNoCRLossSeconds(float var1)

                //    public float getCRLossPerSecond()
                //    public void setCRLossPerSecond(float var1)
                Object setNoCRLossSeconds = shipHullSpec.getClass().getDeclaredMethod("setNoCRLossSeconds", Float.TYPE);
                setAccessMethod.invoke(setNoCRLossSeconds, true);
                invokeMethod.invoke(setNoCRLossSeconds, shipHullSpec, ppt);

                Object setCRLossPerSecond = shipHullSpec.getClass().getDeclaredMethod("setCRLossPerSecond", Float.TYPE);
                setAccessMethod.invoke(setCRLossPerSecond, true);
                invokeMethod.invoke(setCRLossPerSecond, shipHullSpec, crLossPerSecond);

            }
            if(Settings.isEnableShipBreak()) {

                // min/max pieces mult, addition
                // absolute min of 1
                // absolute max
                float minPieces = ( shipHullSpec.getMinPieces()) * Settings.getBreakMinPiecesMult(shipHullSpec) + Settings.getBreakMinPeicesAddition(shipHullSpec);
                float maxPieces =  (shipHullSpec.getMaxPieces() * Settings.getBreakMaxPiecesMult(shipHullSpec) + Settings.getBreakMaxPeicesAddition(shipHullSpec));
                float breakProb = shipHullSpec.getBreakProb() * Settings.getBreakProb(shipHullSpec);
                int breakCeiling = Settings.getBreakCeiling(shipHullSpec);
                if(minPieces > breakCeiling){
                    minPieces = breakCeiling;
                }
                if(maxPieces > breakCeiling){
                    maxPieces = breakCeiling;
                }
                Object setMinPieces = shipHullSpec.getClass().getDeclaredMethod("setMinPieces",Float.TYPE);
                Object setMaxPieces = shipHullSpec.getClass().getDeclaredMethod("setMaxPieces",Float.TYPE);
                Object setBreakProb = shipHullSpec.getClass().getDeclaredMethod("setBreakProb",Float.TYPE);

                setAccessMethod.invoke(setMinPieces, true);
                setAccessMethod.invoke(setMaxPieces, true);
                setAccessMethod.invoke(setBreakProb, true);

                invokeMethod.invoke(setMinPieces, shipHullSpec, minPieces);
                invokeMethod.invoke(setMaxPieces, shipHullSpec, maxPieces);
                invokeMethod.invoke(setBreakProb, shipHullSpec, breakProb);


            }
        }

    }

    private void modifyDamage(WeaponSpecAPI weaponSpec) throws Throwable {
        //TODO flux cost
        if (Settings.isEnableDamage()) {
            if (weaponSpec.isBeam()) {
                ((BeamWeaponSpecAPI) weaponSpec).setDamagePerSecond(((BeamWeaponSpecAPI) weaponSpec).getDamagePerSecond() * Settings.getWeaponDamageMult(weaponSpec));
            } else if (weaponSpec.getProjectileSpec() instanceof MissileSpecAPI && !missilesDamageModified.contains(((MissileSpecAPI) weaponSpec.getProjectileSpec()).getHullSpec().getHullId())) {
                missilesDamageModified.add(((MissileSpecAPI) weaponSpec.getProjectileSpec()).getHullSpec().getHullId());
                ((MissileSpecAPI) weaponSpec.getProjectileSpec()).getDamage().setDamage(((MissileSpecAPI) weaponSpec.getProjectileSpec()).getDamage().getDamage() * Settings.getWeaponDamageMult(weaponSpec));
                if (weaponSpec.getProjectileSpec() != null && ((MissileSpecAPI) weaponSpec.getProjectileSpec()).getBehaviorSpec() != null && ((MissileSpecAPI) weaponSpec.getProjectileSpec()).getBehaviorSpec().getParams() != null) {
                    JSONObject spec = ((MissileSpecAPI) weaponSpec.getProjectileSpec()).getBehaviorSpec().getParams();
                    if (spec.getString("behavior").equals("MIRV")) {
                        if (spec.has("damage")) {
                            spec.put("damage", (Integer) spec.getInt("damage") * Settings.getWeaponDamageMult(weaponSpec));
                        }
                    }
                }
            } else if (weaponSpec.getProjectileSpec() instanceof ProjectileSpecAPI && !projectilesDamageModified.contains(((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).getId())) {
                projectilesDamageModified.add(((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).getId());
                ((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).getDamage().setDamage(((ProjectileSpecAPI) weaponSpec.getProjectileSpec()).getDamage().getDamage() * Settings.getWeaponDamageMult(weaponSpec));
            }

            float damageMult = Settings.getWeaponDamageMult(weaponSpec);
            WeaponAPI.DerivedWeaponStatsAPI dws = weaponSpec.getDerivedStats();
            //fixing the derived values:
            //I will not be using the word. The windows gremlins do not like it
            Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());

            MethodHandle setAccessMethod = MethodHandles.lookup().findVirtual(methodClass, "setAccessible", MethodType.methodType(Void.TYPE, boolean.class));
            MethodHandle invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
            MethodHandle setField = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Object.class, Object.class));
            MethodHandle setFieldAccess = MethodHandles.lookup().findVirtual(fieldClass, "setAccessible", MethodType.methodType(Void.TYPE, boolean.class));

//                Object setOrdnancePoints = shipHullSpec.getClass().getDeclaredMethod("setOrdnancePoints", Integer.TYPE);
//                setAccessMethod.invoke(setOrdnancePoints, true);
//                invokeMethod.invoke(setOrdnancePoints, shipHullSpec, finalOp);
            //                Object[] reactorFields = reactorSpec.getClass().getDeclaredFields();
            //                setFieldAccess.invoke(reactorFields[0], true);
            //                setFieldAccess.invoke(reactorFields[1], true);
            //                setField.invoke(reactorFields[0], reactorSpec, shipHullSpec.getFluxCapacity() * Settings.getFluxCapacityMult(shipHullSpec) + Settings.getFluxCapacityAddition(shipHullSpec));
            //                setField.invoke(reactorFields[1], reactorSpec, shipHullSpec.getFluxDissipation() * Settings.getFluxDissipationMult(shipHullSpec) + Settings.getFluxDissipationAddition(shipHullSpec));
            Object dps = weaponSpec.getDerivedStats().getClass().getDeclaredField("dps");
            Object sustainedDps = weaponSpec.getDerivedStats().getClass().getDeclaredField("sustainedDps");
            Object burstDamage = weaponSpec.getDerivedStats().getClass().getDeclaredField("burstDamage");
            Object fluxPerDam = weaponSpec.getDerivedStats().getClass().getDeclaredField("fluxPerDam");
            Object damageOver30Sec = weaponSpec.getDerivedStats().getClass().getDeclaredField("damageOver30Sec");
            Object damagePerShot = weaponSpec.getDerivedStats().getClass().getDeclaredField("damagePerShot");
            setFieldAccess.invoke(dps, true);
            setFieldAccess.invoke(sustainedDps, true);
            setFieldAccess.invoke(burstDamage, true);
            setFieldAccess.invoke(fluxPerDam, true);
            setFieldAccess.invoke(damageOver30Sec, true);
            setFieldAccess.invoke(damagePerShot, true);
            setField.invoke(dps, dws, dws.getDps() * damageMult);
            setField.invoke(sustainedDps, dws, dws.getSustainedDps() * damageMult);
            if (dws.getBurstDamage() != -1) {
                setField.invoke(burstDamage, dws, dws.getBurstDamage() * damageMult);
            }
            setField.invoke(fluxPerDam, dws, dws.getFluxPerDam() / damageMult);
            setField.invoke(damageOver30Sec, dws, dws.getDamageOver30Sec() * damageMult);
            setField.invoke(damagePerShot, dws, dws.getDamagePerShot() * damageMult);


        }
    }

    private void modifyFighterWingSpecs() {
        for (final FighterWingSpecAPI wingSpec : Global.getSettings().getAllFighterWingSpecs())
            if (Settings.isEnableFighterRangeBoost()) {
                wingSpec.setRange(Settings.getFighterRangeMult(wingSpec.getVariant().getHullSpec()) * wingSpec.getRange());
            }
    }

    private void modifyShipSystemSpecs() throws Throwable {
        if (Settings.isEnableZeroFluxBurnSystems()) {
            for (ShipSystemSpecAPI shipSystem : Global.getSettings().getAllShipSystemSpecs()) {
                if( Settings.getShipSystemsToModify().contains(shipSystem.getId())){
                shipSystem.setFluxPerSecond(0f);
                }
            }
        }
    }
}