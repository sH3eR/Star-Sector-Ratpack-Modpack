package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import java.util.Random;

public class cum_modPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() throws Exception {
        Global.getSettings().doesVariantExist("weirdsettings"); //Probably won't be needed next update? https://fractalsoftworks.com/forum/index.php?topic=5061.msg366712#msg366712
    }
    
    @Override
    public void onGameLoad(boolean newGame) {
        if (Global.getSector().getMemoryWithoutUpdate().get("$CUM_generated") == null) {
            Global.getSector().getMemoryWithoutUpdate().set("$CUM_generated", true);
            //A Fistful of Credit
            if (Global.getSector().getStarSystem("Askonia") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Askonia");
                if (system.getEntityByName("Askonia Fringe Listening Station") != null) {
                    DerelictShipData paramsafoc = new DerelictShipData(new PerShipData(Global.getSettings().getVariant("mule_d_pirates_Smuggler"), ShipCondition.BATTERED, "Cherenkov Bloom", Factions.INDEPENDENT, 0f), false);
                    SectorEntityToken AFistfulofCreditShip = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramsafoc);
                    AFistfulofCreditShip.setDiscoverable(true);
                    AFistfulofCreditShip.setDiscoveryXP(250f);
                    AFistfulofCreditShip.setCircularOrbitPointingDown(system.getEntityByName("Askonia Fringe Listening Station"), 45, 300, 50);
                    AFistfulofCreditShip.setSalvageXP(250f);
                    AFistfulofCreditShip.getMemoryWithoutUpdate().set("$cum_afoc_eventRef", true);
                    ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
                    data.addShip(new PerShipData(Global.getSettings().getVariant("mule_d_pirates_Smuggler"), ShipCondition.BATTERED, "Cherenkov Bloom", Factions.INDEPENDENT, 0f));
                    Misc.setSalvageSpecial(AFistfulofCreditShip, data);
                }
            }
            //Turning the Tables (Handled fully in rules.csv unnecessary.)
            //For the Greater Ludd (Handled fully in rules.csv unnecessary.)
            //The Wolf Pack + Ambush
            if (Global.getSector().getStarSystem("Valhalla") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Valhalla");
                if (system.getEntityById("valhalla") != null) {
                    DerelictShipData paramstwp = new DerelictShipData(new PerShipData("buffalo_hegemony_Standard", ShipCondition.BATTERED, Factions.HEGEMONY, 0f), false);
                    SectorEntityToken TheWolfPack = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramstwp);
                    TheWolfPack.setDiscoverable(true);
                    TheWolfPack.setDiscoveryXP(250f);
                    TheWolfPack.setCircularOrbitPointingDown(system.getEntityById("valhalla"), 45, 1150, 50);
                    TheWolfPack.setSalvageXP(250f);
                    TheWolfPack.getMemoryWithoutUpdate().set("$cum_twp_eventRef", true);
                    ShipRecoverySpecialData datatwp = new ShipRecoverySpecialData(null);
                    datatwp.addShip(new PerShipData("buffalo_hegemony_Standard", ShipCondition.WRECKED, Factions.HEGEMONY, 0f));
                    Misc.setSalvageSpecial(TheWolfPack, datatwp);
                    
                    for (Object object : Global.getSector().getStarSystem("Valhalla").getEntities(CampaignTerrainAPI.class)) {
                        if (object instanceof SectorEntityToken) {
                            SectorEntityToken asteroidsociety = (SectorEntityToken) object;
                            if (asteroidsociety.getCircularOrbitRadius() == 6000 && asteroidsociety.getCircularOrbitPeriod() == 250) {
                                DerelictShipData paramsa = new DerelictShipData(new PerShipData(Global.getSettings().getVariant("enforcer_Elite"), ShipCondition.BATTERED, "HSS Judicature", Factions.HEGEMONY, 0f), false);
                                SectorEntityToken Ambush = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramsa);
                                Ambush.setCircularOrbit(asteroidsociety, 270, 200, 15); //L4 Asteroid.
                                Ambush.setDiscoverable(true);
                                Ambush.setDiscoveryXP(250f);
                                Ambush.setSalvageXP(250f);
                                Ambush.getMemoryWithoutUpdate().set("$hasDefenders", true);
                                /*FleetParamsV3 fParams = new FleetParamsV3(null, null,
                                        Factions.TRITACHYON,
                                        2f,
                                        FleetTypes.TASK_FORCE,
                                        100,
                                        0, 0, 0, 0, 0, 0);
                                fParams.flagshipVariantId = "doom_Strike";
                                fParams.onlyRetainFlagship = true;
                                CampaignFleetAPI defenders = FleetFactoryV3.createFleet(fParams);*/
                                CampaignFleetAPI defenders = FleetFactoryV3.createEmptyFleet(Factions.TRITACHYON, FleetTypes.TASK_FORCE, null);
                                defenders.setName("Phase Group Gamma III");
                                defenders.getCommander().getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
                                //FleetMemberAPI member = defenders.getFlagship();
                                FleetMemberAPI member = defenders.getFleetData().addFleetMember("doom_Strike");
                                member.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 5, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, true, 5, new Random()));
                                member.setVariant(member.getVariant().clone(), false, false);
                                member.getVariant().setSource(VariantSource.REFIT);
                                member.getVariant().addTag("no_autofit");
                                member.setShipName("TTS Invisible Hand");
                                member.getVariant().addPermaMod(HullMods.HARDENED_SUBSYSTEMS, true);
                                member.getVariant().addPermaMod(HullMods.ADAPTIVE_COILS, true);
                                member.getVariant().addMod(HullMods.REINFORCEDHULL);
                                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                                member.getRepairTracker().setCR(1f);
                                FleetMemberAPI member2 = defenders.getFleetData().addFleetMember("afflictor_Strike");
                                member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 5, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, true, 5, new Random()));
                                member2.setVariant(member2.getVariant().clone(), false, false);
                                member2.getVariant().setSource(VariantSource.REFIT);
                                member2.getVariant().addTag("no_autofit");
                                member2.setShipName("TTS Antithesis");
                                member2.getVariant().addPermaMod(HullMods.HARDENED_SUBSYSTEMS, true);
                                member2.getVariant().addPermaMod(HullMods.ADAPTIVE_COILS, true);
                                member2.getVariant().addMod(HullMods.REINFORCEDHULL);
                                member2.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                                member2.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                                member2.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                                member2.getRepairTracker().setCR(1f);
                                FleetMemberAPI member3 = defenders.getFleetData().addFleetMember("shade_Assault");
                                member3.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 5, FleetFactoryV3.getSkillPrefForShip(member3), true, null, true, true, 5, new Random()));
                                member3.setVariant(member3.getVariant().clone(), false, false);
                                member3.getVariant().setSource(VariantSource.REFIT);
                                member3.setShipName("TTS Blind Consequence");
                                member3.getVariant().addTag("no_autofit");
                                member3.getVariant().addPermaMod(HullMods.HARDENED_SUBSYSTEMS, true);
                                member3.getVariant().addPermaMod(HullMods.ADAPTIVE_COILS, true);
                                member3.getVariant().addMod(HullMods.REINFORCEDHULL);
                                member3.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                                member3.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                                member3.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                                member3.getRepairTracker().setCR(1f);
                                Ambush.getMemoryWithoutUpdate().set("$defenderFleet", defenders);
                                Ambush.getMemoryWithoutUpdate().set("$cum_a_eventRef", true);
                                ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
                                data.addShip(new PerShipData(Global.getSettings().getVariant("enforcer_Elite"), ShipCondition.BATTERED, "HSS Judicature", Factions.HEGEMONY, 0f));
                                Misc.setSalvageSpecial(Ambush, data);
                                break;
                            }
                        }
                    }
                }
            }
            //Hornet Nest (Handled fully in rules.csv unnecessary.)
            /*if (Global.getSector().getStarSystem("Magec") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Magec");
            }*/
            
            //The Last Hurrah (Handled fully in rules.csv unnecessary.)
            /*if (Global.getSector().getStarSystem("Mayasura") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Mayasura");
                if (system.getEntityById("mairaath_abandoned_station1") != null) {
                }
            }*/
            //Dire Straits (Handled fully in rules.csv unnecessary.)
            //Predator or Prey(Handled fully in rules.csv unnecessary.)
            /*if (Global.getSector().getStarSystem("Valhalla") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Valhalla");
            }*/
            
            //Sinking the Bismar
            if (Global.getSector().getStarSystem("Duzahk") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Duzahk");
                if (system.getEntityById("duzahk2") != null) {
                    DerelictShipData paramsstb = new DerelictShipData(new PerShipData(Global.getSettings().getVariant("onslaught_Outdated"), ShipCondition.WRECKED, "HSS Bismar", Factions.HEGEMONY, 0f), false);
                    SectorEntityToken StB = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramsstb);
                    StB.setDiscoverable(true);
                    StB.setDiscoveryXP(250f);
                    StB.setCircularOrbit(system.getEntityById("duzahk2"), 90, 500, 50);
                    StB.setSalvageXP(250f);
                    StB.getMemoryWithoutUpdate().set("$hasDefenders", true);
                    StB.addTag("unrecoverable");
                    /*FleetParamsV3 fParams = new FleetParamsV3(null, null,
                            Factions.TRITACHYON,
                            2f,
                            FleetTypes.TASK_FORCE,
                            100,
                            0, 0, 0, 0, 0, 0);
                    fParams.flagshipVariantId = "hyperion_Strike";
                    fParams.onlyRetainFlagship = true;*/
                    CampaignFleetAPI defenders = FleetFactoryV3.createEmptyFleet(Factions.TRITACHYON, FleetTypes.TASK_FORCE, null);
                    defenders.setName("Recon");
                    defenders.getCommander().getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
                    defenders.getCommander().getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
                    defenders.getCommander().getStats().setSkillLevel(Skills.HULL_RESTORATION, 1);
                    defenders.getCommander().getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
                    //FleetMemberAPI member = defenders.getFlagship();
                    FleetMemberAPI member = defenders.getFleetData().addFleetMember("hyperion_Strike");
                    member.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 5, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, true, 5, new Random()));
                    member.setVariant(member.getVariant().clone(), false, false);
                    member.getVariant().setSource(VariantSource.REFIT);
                    member.setShipName("TTS Chimera");
                    member.getVariant().addTag("no_autofit");
                    //member.getVariant().addMod(HullMods.REINFORCEDHULL);
                    member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                    //member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                    //member.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                    member.getRepairTracker().setCR(1f);
                    FleetMemberAPI member2 = defenders.getFleetData().addFleetMember("tempest_Attack");
                    member2.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 5, FleetFactoryV3.getSkillPrefForShip(member2), true, null, true, true, 5, new Random()));
                    member2.setVariant(member2.getVariant().clone(), false, false);
                    member2.getVariant().setSource(VariantSource.REFIT);
                    member2.setShipName("TTS Storm");
                    member2.getVariant().addTag("no_autofit");
                    //member2.getVariant().addMod(HullMods.REINFORCEDHULL);
                    member2.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                    //member2.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                    // member2.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                    member2.getRepairTracker().setCR(1f);
                    FleetMemberAPI member3 = defenders.getFleetData().addFleetMember("tempest_Attack");
                    member3.setCaptain(OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), 5, FleetFactoryV3.getSkillPrefForShip(member3), true, null, true, true, 5, new Random()));
                    member3.setVariant(member3.getVariant().clone(), false, false);
                    member3.getVariant().setSource(VariantSource.REFIT);
                    member3.setShipName("TTS Gale");
                    member3.getVariant().addTag("no_autofit");
                    //member3.getVariant().addMod(HullMods.REINFORCEDHULL);
                    member3.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                    // member3.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                    //member3.getVariant().addTag(Tags.VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE);
                    member3.getRepairTracker().setCR(1f);
                    StB.getMemoryWithoutUpdate().set("$defenderFleet", defenders);
                    StB.getMemoryWithoutUpdate().set("$cum_stb_eventRef", true);
                    ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
                    data.storyPointRecovery = false;
                    //data.addShip(new PerShipData(Global.getSettings().getVariant("onslaught_Outdated"), ShipCondition.WRECKED, "HSS Bismar", Factions.HEGEMONY, 0f));
                    Misc.setSalvageSpecial(StB, data);
                }
            }
            
            //Nothing Personal
            if (Global.getSector().getStarSystem("Tia") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Tia");
                if (system.getEntityById("abandoned_spacedock") != null) {
                    system.getEntityById("abandoned_spacedock").getMemoryWithoutUpdate().set("$cum_np_eventRef", true);
                }
            }
            
            //Coral Nebula
            if (Global.getSector().getStarSystem("Zagan") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Zagan");
                for (Object object : system.getEntities(CampaignTerrainAPI.class)) {
                    if (object instanceof SectorEntityToken) {
                        SectorEntityToken nebulasociety = (SectorEntityToken) object;
                        if (nebulasociety.getCircularOrbitRadius() == 7800 && nebulasociety.getCircularOrbitPeriod() == 500) {
                            DerelictShipData paramscn = new DerelictShipData(new PerShipData(Global.getSettings().getVariant("lasher_luddic_path_Raider"), ShipCondition.BATTERED, "Keeper of the Flock", Factions.LUDDIC_PATH, 0f), false);
                            SectorEntityToken nebulaentity = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramscn);
                            nebulaentity.setCircularOrbit(nebulasociety, 270, 200, 15);
                            nebulaentity.setDiscoveryXP(250f);
                            nebulaentity.setDiscoverable(true);
                            nebulaentity.setSalvageXP(250f);
                            nebulaentity.getMemoryWithoutUpdate().set("$cum_cn_eventRef", true);
                            break;
                        }
                    }
                }
            }
            //Forlorn Hope
            if (Global.getSector().getStarSystem("Aztlan") != null) {
                StarSystemAPI system = Global.getSector().getStarSystem("Aztlan");
                if (system.getEntityById("aztlan_jump_point_alpha") != null) {
                    DerelictShipData paramsfh = new DerelictShipData(new PerShipData(Global.getSettings().getVariant("paragon_Hull"), ShipCondition.WRECKED, "Invincible", Factions.TRITACHYON, 0f), false);
                    SectorEntityToken memorial = system.addCustomEntity("cum_fhmemorial", "Aztlan Memorial", Entities.WRECK, Factions.HEGEMONY, paramsfh);
                    memorial.setDiscoverable(true);
                    memorial.setDiscoveryXP(null);
                    memorial.setCircularOrbitPointingDown(system.getEntityById("aztlan_jump_point_alpha"), 270, 200, 30);
                    memorial.setCustomDescriptionId("cum_memorial");
                    memorial.removeTag("salvageable");
                    memorial.removeTag("neutrino_low");
                    memorial.addTag("cum_memorial");
                }
            }
        }
    }
}
