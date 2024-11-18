package org.niatahl.tahlan.weapons.deco;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class AdleraugeEffectScript implements EveryFrameWeaponEffectPlugin {

    private static final String ADLER_ID = "Adlerauge_ID";
    private static final float EFFECT_RANGE = 2000f;
    private static final float AUTOAIM_BONUS = 50f;
    private static final float RANGE_BOOST = 100f;
    private static final float SPEED_BOOST = 20f;

    // sprite path - necessary if loaded here and not in settings.json
    public static final String SPRITE_PATH = "graphics/fx/shields256.png";
    public static final Color COLOR = new Color(186, 47, 52, 185);
    public static final float ROTATION_SPEED = 20f;

    private boolean loaded = false;
    private float rotation = 0f;
    private SpriteAPI sprite = null;


    private static final EnumSet<WeaponAPI.WeaponType> WEAPON_TYPES = EnumSet.of(WeaponAPI.WeaponType.MISSILE,WeaponAPI.WeaponType.BALLISTIC,WeaponAPI.WeaponType.ENERGY);

    private final List<ShipAPI> targetList = new ArrayList<ShipAPI>();

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (ship == null) {
            return;
        }

        if (!ship.isAlive() || ship.isHulk() || ship.isPiece()) {
            return;
        }

        //Glows off in refit screen
        if (ship.getOriginalOwner() == -1) {
            return;
        }

        if (sprite == null) {
            // Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
            if (!loaded) {
                try {
                    Global.getSettings().loadTexture(SPRITE_PATH);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to load sprite '" + SPRITE_PATH + "'!", ex);
                }

                loaded = true;
            }
            sprite = Global.getSettings().getSprite(SPRITE_PATH);
        }

        final Vector2f loc = ship.getLocation();
        final ViewportAPI view = Global.getCombatEngine().getViewport();
        if (view.isNearViewport(loc, EFFECT_RANGE)) {
            glPushAttrib(GL_ENABLE_BIT);
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
            glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_BLEND);
            float scale = Global.getSettings().getScreenScaleMult();
            final float radius = (EFFECT_RANGE * 2f) * scale / view.getViewMult();
            sprite.setSize(radius, radius);
            sprite.setColor(COLOR);
            sprite.setAlphaMult((float) (0.1 / Math.ceil(ship.getMutableStats().getTimeMult().getModifiedValue())));
            sprite.renderAtCenter(view.convertWorldXtoScreenX(loc.x) * scale, view.convertWorldYtoScreenY(loc.y) * scale);
            sprite.setAngle(rotation);
            glPopMatrix();
            glPopAttrib();
        }

        // Spin it
        rotation += ROTATION_SPEED * amount;
        if (rotation > 360f){
            rotation -= 360f;
        }

        for (ShipAPI target : CombatUtils.getShipsWithinRange(ship.getLocation(), EFFECT_RANGE)) {
            if (target.getOwner() == ship.getOwner()) {
                if (target.getOwner() == ship.getOwner() && !targetList.contains(target)
                        && (target.getVariant().getHullMods().contains("tahlan_silberherz") || target.getVariant().getHullMods().contains("tahlan_silberherz_minor"))) {
                    targetList.add(target);
                }
            }
        }
        List<ShipAPI> purgeList = new ArrayList<>();
        for (ShipAPI target : targetList) {
            if (MathUtils.getDistance(target.getLocation(), ship.getLocation()) <= EFFECT_RANGE) {

                target.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(ADLER_ID, RANGE_BOOST);
                target.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(ADLER_ID, RANGE_BOOST);
                target.getMutableStats().getAutofireAimAccuracy().modifyFlat(ADLER_ID, AUTOAIM_BONUS * 0.01f);
                target.getMutableStats().getProjectileSpeedMult().modifyPercent(ADLER_ID, SPEED_BOOST);

                target.setWeaponGlow(0.7f,COLOR, WEAPON_TYPES);

            } else {

                target.getMutableStats().getEnergyWeaponRangeBonus().unmodify(ADLER_ID);
                target.getMutableStats().getBallisticWeaponRangeBonus().unmodify(ADLER_ID);
                target.getMutableStats().getAutofireAimAccuracy().unmodify(ADLER_ID);
                target.getMutableStats().getProjectileSpeedMult().unmodify(ADLER_ID);

                target.setWeaponGlow(0f,COLOR, WEAPON_TYPES);
                purgeList.add(target);
            }

        }
        for (ShipAPI purge : purgeList) {
            targetList.remove(purge);
        }

    }
}
