package org.niatahl.tahlan.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class NCHaloScript implements EveryFrameWeaponEffectPlugin {
    private static final float[] COLOR_NORMAL = {140f / 255f, 210f / 255f, 255f / 255f};
    private static final float[] COLOR_OVERDRIVE = {255f/255f, 100f/255f, 40f/255f};
    private static final float MAX_JITTER_DISTANCE = 0.2f;
    private static final float MAX_OPACITY = 0.8f;
    private static final float TRIGGER_PERCENTAGE = 0.3f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        ShipAPI ship = weapon.getShip();
        if (ship == null) {
            return;
        }

        //Glows off in refit screen
        if (ship.getOriginalOwner() == -1) {
            return;
        }

        float currentBrightness = 0f;

        //We glow when the system or overdrive is active
        if (ship.getHitpoints() <= ship.getMaxHitpoints() * TRIGGER_PERCENTAGE || ship.getVariant().hasHullMod("tahlan_forcedoverdrive")) {
            currentBrightness = 0.9f;
        } else if (ship.getSystem().isActive()) {
            currentBrightness = ship.getSystem().getEffectLevel();
        }

        //A piece should never have glowing lights
        if (ship.isPiece() || !ship.isAlive()) {
            currentBrightness = 0;
        }


        //Brightness clamp, cause there's some weird cases with flux level > 1f, I guess
        currentBrightness = Math.max(0f,Math.min(currentBrightness,1f));

        //Now, set the color to the one we want, and include opacity
        Color colorToUse = new Color(COLOR_OVERDRIVE[0], COLOR_OVERDRIVE[1], COLOR_OVERDRIVE[2], currentBrightness * MAX_OPACITY);

        if (ship.getSystem().isActive()) {
            colorToUse = new Color(COLOR_NORMAL[0], COLOR_NORMAL[1], COLOR_NORMAL[2], currentBrightness * MAX_OPACITY);
        }

        //Switches to the proper sprite
        if (currentBrightness > 0) {
            weapon.getAnimation().setFrame(1);
        } else {
            weapon.getAnimation().setFrame(0);
        }


        //And finally actually apply the color
        weapon.getSprite().setColor(colorToUse);

        //Jitter! Jitter based on our maximum jitter distance and our flux level
        if (currentBrightness > 0) {
            Vector2f randomOffset = MathUtils.getRandomPointInCircle(new Vector2f(weapon.getSprite().getWidth() / 2f, weapon.getSprite().getHeight() / 2f), MAX_JITTER_DISTANCE);
            weapon.getSprite().setCenter(randomOffset.x, randomOffset.y);
        }
    }
}