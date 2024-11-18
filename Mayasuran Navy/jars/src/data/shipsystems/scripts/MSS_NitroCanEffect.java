package data.shipsystems.scripts;

//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
//import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipCommand;
import java.awt.Color;

import java.util.List;
import java.util.ArrayList;
import org.lwjgl.util.vector.Vector2f;

//import java.awt.*;

public class MSS_NitroCanEffect implements EveryFrameWeaponEffectPlugin {

    private static final String GLOW_ID = "MSS_canisterbay_glow_";     // Glow weapon ID to search for
    private Color GLOW_COLOUR = new Color(255,90,75,255);
    private boolean runOnce = false;
    private float counter = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        ShipAPI ship = weapon.getShip();
        if (engine.isPaused()) {
            return;
        }
        if (!ship.isAlive()) {
            return;
        }
        
        List<WeaponAPI> glows = new ArrayList<>();          // List to store the glow overlay weapons.
        for (WeaponAPI this_wpn : ship.getAllWeapons()) {
            if (this_wpn.getId().contains(GLOW_ID)) {       // Checking a weapon ID partial match.
                glows.add(this_wpn);
            }
        }

        ShipSystemAPI sys = null;
        float sysDuration = 0f;
        SystemState sysState = SystemState.IDLE;
        float sysCooldown = 0f;
        if (ship.getSystem() != null) {
            sys = ship.getSystem();
            sysDuration = sys.getChargeActiveDur();    // Equals the value in ship_systems.csv for easy tuning!
            sysState = sys.getState();
            sysCooldown = sys.getCooldownRemaining();
        }
        
        if (sysDuration == 0)
            sysDuration = 1;
        float durationFactor = 1f - (counter / sysDuration);  // Should return a float from 0 to 1, representing how
                                                                // far through the system active duration we are.
                                                                // If I've done the math right, 0 means the system has just
                                                                // started, and 1 means it's about to end.
        
        if (!runOnce && ship.getVariant().getHullMods().contains("magazines") && sys != null) {
            sys.setAmmo(sys.getMaxAmmo()+(sys.getMaxAmmo()/2));     // +50% charges if Expanded Magazines is installed.
            runOnce = true;                                         // The "runOnce" ensures this is only checked
        }                                                           // once, as the ship enters combat.
        
        if (sysState == SystemState.IN) {
            counter = sysDuration;                         // Reset the duration counter.
            for (WeaponAPI glow : glows) {
                glow.getAnimation().setAlphaMult(0);        // Start the glow overlays with forced transparency.
                glow.getAnimation().setFrame(1);            // Set the glow overlays to their visible state.
            }
        }
        
        if (sysState == SystemState.ACTIVE) {
            if (counter <= 0f) {                            // If we have reached the "end" of the duration...
                ship.giveCommand(ShipCommand.USE_SYSTEM, new Vector2f(), 0);    // ... deactivate the system.
                                                            // As a toggle system, this is equivalent to the player
                                                            // pressing F again.
            } else {
                for (WeaponAPI glow : glows) {
                    glow.getSprite().setColor(GLOW_COLOUR);
                    glow.getAnimation().setAlphaMult(durationFactor);  // Fade the glow overlays into visibility
                }
                counter = counter - amount;                 // Decrement the duration counter.
            }
        }
        
        if (sysCooldown <= 0.5f && sys != null && !sys.isOutOfAmmo() ) {   // If the system is about to come off cooldown, and
            weapon.getAnimation().setFrame(0);              // there are charges left, switch to the "loaded" frame
            weapon.getAnimation().setAlphaMult(1f - (sysCooldown * 2f));   // and fade in.
        }
        if (sysState == SystemState.OUT) {                 // If the system active period has ended, switch to
            weapon.getAnimation().setFrame(1);              // the "empty" frame.
            for (WeaponAPI glow : glows) {
                glow.getAnimation().setFrame(0);            // Set the glow overlays to their transparent state.
            }
        }
    }
}
