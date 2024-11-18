package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

import data.scripts.weapons.MSS_SwappableRailgunClusterProjEffect;
import data.scripts.weapons.MSS_SwappableRailgunHEProjEffect;
import data.scripts.util.MSS_txt;

public class MSS_AmmoSwapHullmod extends BaseHullMod
{
    // these two are just for display, they don't do anything
    private static final float HE_SPEED_MULT = 0.4f;
    private static final float CLUSTER_SPEED_MULT = 0.5f;
    private static final int CLUSTER_SPLIT_COUNT = 5;
    public enum AmmoSwapMode
    {
        KINETIC(MSS_txt.txt("swap_kinetic_modename"), "MSS_big_swap_railgunKinetic"), 
        HE(MSS_txt.txt("swap_he_modename"), "MSS_big_swap_railgunHE"),
        CLUSTER(MSS_txt.txt("swap_cluster_modename"), "MSS_big_swap_railgunCluster");

        final String name;
        final String weapon_id;

        private AmmoSwapMode(String name, String weapon_id) {
            this.name = name;
            this.weapon_id = weapon_id;
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount)
    {
        CombatEngineAPI engine = Global.getCombatEngine();
        // Don't run if we are paused
        if (engine.isPaused() || ship == null || !engine.isEntityInPlay(ship))
        {
            return;
        }
        AmmoSwapMode currentMode;
        switch (ship.getSystem().getAmmo())
        {
            case 1:
                currentMode = AmmoSwapMode.KINETIC;
                break;
            case 2:
                currentMode = AmmoSwapMode.HE;
                break;
            case 3:
                currentMode = AmmoSwapMode.CLUSTER;
                break;
            default:
                currentMode = AmmoSwapMode.KINETIC;
        }
        // if player ship, display tooltip
        if (ship == engine.getPlayerShip())
        {
            Global.getCombatEngine().maintainStatusForPlayerShip("MSS_AmmoSwap", "graphics/icons/hullsys/ammo_feeder.png", MSS_txt.txt("swap_systemname"), currentMode.name, false);

        }
        // Finds all projectiles within a a short range from our ship
        
        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(ship.getLocation(), ship.getCollisionRadius()))
        {
            if (proj.getProjectileSpecId() == null || proj.getSource() != ship || !engine.isEntityInPlay(proj))
            {
                continue;
            }
            // railgun fires a dummy projectile that we replace with the appropriate one, depending on mode
            // dummy projectile is used to set damage
            if (proj.getProjectileSpecId().equals("MSS_SwappableRailgunDummyProj"))
            {
                WeaponAPI weapon = proj.getWeapon();
                Vector2f loc = proj.getLocation();
                float projAngle = proj.getFacing();
                float projDamage = proj.getDamageAmount();

                DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, currentMode.weapon_id, loc, projAngle, ship.getVelocity());
                switch (currentMode)
                {
                    case KINETIC:
                        newProj.setDamageAmount(projDamage);
                        engine.removeEntity(proj);
                        break;
                    case HE:
                        // slower, more damage
                        //newProj.setDamageAmount(projDamage * 1.25f);
                        //newProj.getVelocity().scale(HE_SPEED_MULT);
                        engine.addPlugin(new MSS_SwappableRailgunHEProjEffect(newProj));
                        engine.removeEntity(proj);
                        break;
                    case CLUSTER:
                        //newProj.getVelocity().scale(CLUSTER_SPEED_MULT);
                        engine.addPlugin(new MSS_SwappableRailgunClusterProjEffect(newProj));
                        engine.removeEntity(proj);
                        break;
                }
            }
        }
    }

    //For the cool extra description section
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) 
    {
        float pad = 10f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);

        //Shock cannons
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/mayasura/hullmods/variable_ammo/variable_kinetic.png", 36);
        text.addPara(MSS_txt.txt("swap_kinetic_modename"), 0, Color.ORANGE, MSS_txt.txt("swap_kinetic_modename"));
        text.addPara("Kinetic damage.", 0, Color.CYAN, "Kinetic");
        text.addPara("Deals 400 EMP damage on impact.", 0, Color.CYAN, "400 EMP");
        tooltip.addImageWithText(pad);

        //Impact Drivers
        text = tooltip.beginImageWithText("graphics/mayasura/hullmods/variable_ammo/variable_he.png", 36);
        text.addPara(MSS_txt.txt("swap_he_modename"), 0, Color.ORANGE, MSS_txt.txt("swap_he_modename"));
        text.addPara("High-Explosive Damage.", 0, Color.RED, "High-Explosive");
        text.addPara("Advanced Proximity Fuse: Projectile detonates on near-misses, dealing 50%% damage.", 0, Color.YELLOW, "Advanced Proximity Fuse", "50% damage");
        text.addPara("-" + (int)((1 - HE_SPEED_MULT) * 100) + "%% projectile speed.", 0, Color.YELLOW, "-" + (int)((1 - HE_SPEED_MULT) * 100) + "%");
        tooltip.addImageWithText(pad);

        //Type-3 Shells
        text = tooltip.beginImageWithText("graphics/mayasura/hullmods/variable_ammo/variable_cluster.png", 36);
        text.addPara(MSS_txt.txt("swap_cluster_modename"), 0, Color.ORANGE, MSS_txt.txt("swap_cluster_modename"));
        text.addPara("High-Explosive Damage.", 0, Color.RED, "High-Explosive");
        text.addPara("Projectile splits into " + CLUSTER_SPLIT_COUNT + " sticky plasma grenades.", 0, Color.YELLOW, Integer.toString(CLUSTER_SPLIT_COUNT));
        text.addPara("Magnetized Plasma: Grenades stick to targets and explode after a short duration, dealing 400 damage.", 0, Color.YELLOW, "Magnetized Plasma", "400");
        text.addPara("Advanced Proximity Fuse: Grenades detonate on near-misses, dealing 200 damage.", 0, Color.YELLOW, "Advanced Proximity Fuse", "200");
        text.addPara("-" + (int)((1 - CLUSTER_SPEED_MULT) * 100) + "%% projectile speed.", 0, Color.YELLOW, "-" + (int)((1 - CLUSTER_SPEED_MULT) * 100) + "%");
        tooltip.addImageWithText(pad);
    }
}
