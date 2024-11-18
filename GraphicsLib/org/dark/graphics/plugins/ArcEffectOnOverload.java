package org.dark.graphics.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Level;
import org.dark.graphics.util.ShipColors;
import org.dark.shaders.util.ShaderLib;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class ArcEffectOnOverload extends BaseEveryFrameCombatPlugin {

    private static final float OFFSCREEN_GRACE_CONSTANT = 500f;
    private static final float OFFSCREEN_GRACE_FACTOR = 2f;

    private static final String SETTINGS_FILE = "GRAPHICS_OPTIONS.ini";

    private static boolean enabled = true;
    private static boolean useVentColors = false;
    private static boolean offscreen = false;

    static {
        try {
            loadSettings();
        } catch (IOException | JSONException e) {
            Global.getLogger(ArcEffectOnOverload.class).log(Level.ERROR, "Failed to load performance settings: "
                    + e.getMessage());
            enabled = false;
        }
    }

    private static void loadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);

        enabled = settings.getBoolean("enableOverloadArcs");
        useVentColors = settings.getBoolean("useVentColorsForOverloadArcs");
        offscreen = settings.getBoolean("drawOffscreenParticles");
    }

    private CombatEngineAPI engine;
    private final IntervalUtil interval = new IntervalUtil(0.25f, 0.5f);

    /* We're not going to bother with per-ship time manipulation applying to this.  An overloaded ship probably won't be warping time. */
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || !enabled) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            List<ShipAPI> ships = engine.getShips();
            int size = ships.size();
            for (int i = 0; i < size; i++) {
                ShipAPI ship = ships.get(i);
                if (ship.isHulk()) {
                    continue;
                }

                if (ship.getFluxTracker().isOverloaded()) {
                    if (offscreen || ShaderLib.isOnScreen(ship.getLocation(), ship.getCollisionRadius()
                            * OFFSCREEN_GRACE_FACTOR + OFFSCREEN_GRACE_CONSTANT)) {
                        int arcs = 1;
                        switch (ship.getHullSize()) {
                            case FIGHTER:
                                arcs = 0;
                                break;
                            case FRIGATE:
                            case DEFAULT:
                                arcs = 1;
                                break;
                            case DESTROYER:
                                arcs = 1;
                                break;
                            case CRUISER:
                                arcs = 2;
                                break;
                            case CAPITAL_SHIP:
                                arcs = 3;
                                break;
                            default:
                                break;
                        }

                        ShipAPI empTarget = ship;
                        for (int a = 0; a < arcs; a++) {
                            Vector2f point = new Vector2f(ship.getLocation());

                            float angle = MathUtils.getRandomNumberInRange(0f, 360f);
                            Vector2f test = new Vector2f(ship.getCollisionRadius(), 0f);
                            VectorUtils.rotate(test, angle, test);
                            Vector2f.add(test, point, test);
                            float radiusAtAngle = Misc.getTargetingRadius(test, empTarget, false);

                            Vector2f add = new Vector2f(radiusAtAngle * (float) Math.random(), 0f);
                            VectorUtils.rotate(add, angle, add);
                            Vector2f.add(add, point, point);

                            if (ship.getOverloadColor() != null) {
                                Color core;
                                Color fringe;
                                if (useVentColors) {
                                    core = ship.getVentCoreColor();
                                    fringe = ship.getVentFringeColor();
                                } else {
                                    core = new Color(
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getRed() * 0.75f) + 63.75f)),
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getGreen() * 0.75f) + 63.75f)),
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getBlue() * 0.75f) + 63.75f)),
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getAlpha() * 0.5f) + 127.5f)));
                                    fringe = new Color(
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getRed() * 0.9f) + 25.5f)),
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getGreen() * 0.9f) + 25.5f)),
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getBlue() * 0.9f) + 25.5f)),
                                            ShipColors.clamp255(Math.round((ship.getOverloadColor().getAlpha() * 0.75f) + 63.75f)));
                                }
                                engine.spawnEmpArc(ship, point, empTarget, empTarget, DamageType.OTHER, 0f, 0f,
                                        ship.getCollisionRadius(), null, 12f,
                                        fringe, core);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
