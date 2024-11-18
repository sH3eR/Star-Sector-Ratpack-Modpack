package scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import data.hullmods.compmods;

public class PMMSettingsScript {

    //Settings
    public static boolean IsMastRec = false;
    public static boolean GLOW = true;
    public static boolean OMEGA = true;

    //Ships
    public static String MASTER = "pmm_derelict_master";
    public static String TRIQUETRA = "pmm_fury_omega";
    public static String AEON = "pmm_shrike_omega";
    public static String SATUS = "pmm_satus_shard";
    public static String PERCEPT = "pmm_tempest_omega";

    //Tags
    public static String AUTOREC = "auto_rec";


    public static void initMasterRec(){
        boolean masrec = PMMLunaSettings.MasterRecover();
        if(masrec){
            IsMastRec = true;
            Global.getSettings().getHullSpec(MASTER).addTag(AUTOREC);
        } else {
            IsMastRec = false;
            Global.getSettings().getHullSpec(MASTER).getTags().remove(AUTOREC);
        }

    }
    public static void initGlow(){
        Boolean glow = PMMLunaSettings.PirateGlowToggle();

        compmods.BALLISTIC_GLOW = PMMLunaSettings.PirateGlowColorBallistic();
        compmods.ENERGY_GLOW = PMMLunaSettings.PirateGlowColorEnergy();
        if(glow){
            GLOW = true;
            compmods.GLOW = true;
        } else {
            GLOW = false;
            compmods.GLOW = false;
        }
    }
    public static void initOmega(){
        Boolean omega = PMMLunaSettings.OmegaToggle();
        FactionAPI omegafac = Global.getSector().getFaction(Factions.OMEGA);

        if (omega){
            OMEGA = true;
            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants fighters = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.FIGHTER);

            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants small = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.FRIGATE);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).add("pmm_satus_shard_Attack", 10f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).add("pmm_satus_shard_Armorbreaker", 10f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).add("pmm_satus_shard_Shieldbreaker", 10f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).add("pmm_satus_shard_Defense", 10f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).add("pmm_satus_shard_Missile", 4f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).add("pmm_tempest_omega_Attack", 4f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).add("pmm_tempest_omega_Armorbreaker", 4f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).add("pmm_tempest_omega_Shieldbreaker", 4f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).add("pmm_tempest_omega_Defense", 4f);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).add("pmm_tempest_omega_Missile", 4f);

            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants medium = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.DESTROYER);
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).add("pmm_shrike_omega_Attack", 10f);
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).add("pmm_shrike_omega_Armorbreaker", 10f);
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).add("pmm_shrike_omega_Shieldbreaker", 10f);
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).add("pmm_shrike_omega_Defense", 10f);
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).add("pmm_shrike_omega_Missile", 4f);

            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants large = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.CRUISER);
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).add("pmm_fury_omega_Attack", 10f);
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).add("pmm_fury_omega_Attack2", 10f);
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).add("pmm_fury_omega_Armorbreaker", 10f);
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).add("pmm_fury_omega_Shieldbreaker", 10f);
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).add("pmm_fury_omega_Defense", 10f);
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).add("pmm_fury_omega_Missile", 4f);

            omegafac.addKnownShip(TRIQUETRA, false);
            omegafac.addKnownShip(AEON, false);
            omegafac.addKnownShip(SATUS, false);
            omegafac.addKnownShip(PERCEPT, false);
        } else {
            OMEGA = false;
            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants fighters = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.FIGHTER);

            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants small = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.FRIGATE);
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).remove("pmm_satus_shard_Attack");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).remove("pmm_satus_shard_Armorbreaker");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).remove("pmm_satus_shard_Shieldbreaker");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).remove("pmm_satus_shard_Defense");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).remove("pmm_satus_shard_Missile");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).remove("pmm_tempest_omega_Attack");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).remove("pmm_tempest_omega_Armorbreaker");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).remove("pmm_tempest_omega_Shieldbreaker");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).remove("pmm_tempest_omega_Defense");
            small.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).remove("pmm_tempest_omega_Missile");

            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants medium = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.DESTROYER);
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).remove("pmm_shrike_omega_Attack");
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).remove("pmm_shrike_omega_Armorbreaker");
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).remove("pmm_shrike_omega_Shieldbreaker");
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).remove("pmm_shrike_omega_Defense");
            medium.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).remove("pmm_shrike_omega_Missile");

            com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants large = com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData.get(ShipAPI.HullSize.CRUISER);
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).remove("pmm_fury_omega_Attack");
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.GENERAL).remove("pmm_fury_omega_Attack2");
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_ARMOR).remove("pmm_fury_omega_Armorbreaker");
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.ANTI_SHIELD).remove("pmm_fury_omega_Shieldbreaker");
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.POINT_DEFENSE).remove("pmm_fury_omega_Defense");
            large.get(com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType.MISSILE).remove("pmm_fury_omega_Missile");

            omegafac.removeKnownShip(TRIQUETRA);
            omegafac.removeKnownShip(AEON);
            omegafac.removeKnownShip(SATUS);
            omegafac.removeKnownShip(PERCEPT);
        }
    }
}
