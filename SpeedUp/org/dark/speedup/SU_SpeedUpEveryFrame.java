package org.dark.speedup;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SU_SpeedUpEveryFrame extends BaseEveryFrameCombatPlugin {

    private static int ACTIVATE_KEY[];
    private static int ACTIVATE_MOUSE[];
    private static EnumSet<Modifier> ACTIVATE_MODS[];
    private static int TOGGLE_KEY[];
    private static int TOGGLE_MOUSE[];
    private static EnumSet<Modifier> TOGGLE_MODS[];
    private static float SPEED_UP_MULT[];
    private static boolean NO_BULLET_TIME[];
    private static boolean PRINT_MESSAGE[];
    private static boolean ON_AT_START[];
    private static float CAP_TO_FPS[];

    private static final String DATA_KEY = "SU_SpeedUpEveryFrame";
    private static final String SETTINGS_FILE = "SPEED_UP.ini";
    private static final String SOUND_ID = "ui_noise_static_message";
    private static final String STAT_ID_BASE = "SU_SpeedUpEveryFrame";
    private static final String STAT_ID_NBT = STAT_ID_BASE + "NBT";
    private static final String TEXT_COLOR = "standardTextColor";

    private static boolean initialized = false;

    public static void reloadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);
        JSONArray options = settings.getJSONArray("speedOptions");

        if (options.length() == 0) {
            return;
        }

        ACTIVATE_KEY = new int[options.length()];
        ACTIVATE_MOUSE = new int[options.length()];
        ACTIVATE_MODS = new EnumSet[options.length()];
        TOGGLE_KEY = new int[options.length()];
        TOGGLE_MOUSE = new int[options.length()];
        TOGGLE_MODS = new EnumSet[options.length()];
        SPEED_UP_MULT = new float[options.length()];
        NO_BULLET_TIME = new boolean[options.length()];
        PRINT_MESSAGE = new boolean[options.length()];
        ON_AT_START = new boolean[options.length()];
        CAP_TO_FPS = new float[options.length()];

        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.getJSONObject(i);

            ACTIVATE_KEY[i] = option.optInt("activateKey", -1);
            ACTIVATE_MOUSE[i] = option.optInt("activateMouse", -1);
            ACTIVATE_MODS[i] = EnumSet.noneOf(Modifier.class);
            if (option.optBoolean("activateCtrl", false)) {
                ACTIVATE_MODS[i].add(Modifier.CTRL);
            }
            if (option.optBoolean("activateAlt", false)) {
                ACTIVATE_MODS[i].add(Modifier.ALT);
            }
            if (option.optBoolean("activateShift", false)) {
                ACTIVATE_MODS[i].add(Modifier.SHIFT);
            }

            TOGGLE_KEY[i] = option.optInt("toggleKey", -1);
            TOGGLE_MOUSE[i] = option.optInt("toggleMouse", -1);
            TOGGLE_MODS[i] = EnumSet.noneOf(Modifier.class);
            if (option.optBoolean("toggleCtrl", false)) {
                TOGGLE_MODS[i].add(Modifier.CTRL);
            }
            if (option.optBoolean("toggleAlt", false)) {
                TOGGLE_MODS[i].add(Modifier.ALT);
            }
            if (option.optBoolean("toggleShift", false)) {
                TOGGLE_MODS[i].add(Modifier.SHIFT);
            }

            SPEED_UP_MULT[i] = (float) option.optDouble("speedUpMult", 1.0);
            NO_BULLET_TIME[i] = option.optBoolean("noBulletTime", false);
            PRINT_MESSAGE[i] = option.optBoolean("printMessage", false);
            ON_AT_START[i] = option.optBoolean("onAtStart", false);
            CAP_TO_FPS[i] = (float) option.optDouble("capToFPS", 0.0);
        }

        initialized = true;
    }

    private CombatEngineAPI engine;
    private boolean firstFrame;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if ((engine == null) || (engine.getCombatUI() == null)) {
            return;
        }

        if (!initialized) {
            return;
        }

        /* Exclude the starting screen */
        ShipAPI player = engine.getPlayerShip();
        if ((player == null || !engine.isEntityInPlay(player)) && !engine.isInCampaign() && !engine.isInCampaignSim() && !engine.isUIShowingHUD()) {
            return;
        }

        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData(ACTIVATE_KEY.length));
        }

        LocalData data = (LocalData) engine.getCustomData().get(DATA_KEY);
        boolean wasActive[] = data.active.clone();

        for (InputEventAPI event : events) {
            if (event.isConsumed()) {
                continue;
            }

            boolean handledInput = false;

            for (int i = 0; i < ACTIVATE_KEY.length; i++) {
                if ((ACTIVATE_KEY[i] != -1)
                        && (event.getEventType() == InputEventType.KEY_UP) && (event.getEventValue() == ACTIVATE_KEY[i])) {
                    data.active[i] = data.toggled[i];
                    handledInput = true;
                }

                if ((ACTIVATE_MOUSE[i] != -1)
                        && (event.getEventType() == InputEventType.MOUSE_UP) && (event.getEventValue() == ACTIVATE_MOUSE[i])) {
                    data.active[i] = data.toggled[i];
                    handledInput = true;
                }
            }

            if (handledInput) {
                event.consume();
                continue;
            }

            int mostMods = 0;
            int commandPrecedent = -1;

            for (int i = 0; i < ACTIVATE_KEY.length; i++) {
                if ((ACTIVATE_KEY[i] != -1)
                        && (event.getEventType() == InputEventType.KEY_DOWN) && (event.getEventValue() == ACTIVATE_KEY[i])) {
                    int condition = ACTIVATE_MODS[i].size();
                    for (Modifier m : ACTIVATE_MODS[i]) {
                        if (!ACTIVATE_MODS[i].contains(m)) {
                            continue;
                        }
                        switch (m) {
                            case CTRL:
                                if (event.isCtrlDown()) {
                                    condition--;
                                }
                                break;
                            case ALT:
                                if (event.isAltDown()) {
                                    condition--;
                                }
                                break;
                            case SHIFT:
                                if (event.isShiftDown()) {
                                    condition--;
                                }
                                break;
                        }
                    }

                    if ((ACTIVATE_MODS[i].size() >= mostMods) && (condition == 0)) {
                        mostMods = ACTIVATE_MODS[i].size();
                        commandPrecedent = i;
                    }
                }

                if ((ACTIVATE_MOUSE[i] != -1)
                        && (event.getEventType() == InputEventType.MOUSE_DOWN) && (event.getEventValue() == ACTIVATE_MOUSE[i])) {
                    int condition = ACTIVATE_MODS[i].size();
                    for (Modifier m : ACTIVATE_MODS[i]) {
                        if (!ACTIVATE_MODS[i].contains(m)) {
                            continue;
                        }
                        switch (m) {
                            case CTRL:
                                if (event.isCtrlDown()) {
                                    condition--;
                                }
                                break;
                            case ALT:
                                if (event.isAltDown()) {
                                    condition--;
                                }
                                break;
                            case SHIFT:
                                if (event.isShiftDown()) {
                                    condition--;
                                }
                                break;
                        }
                    }

                    if ((ACTIVATE_MODS[i].size() >= mostMods) && (condition == 0)) {
                        mostMods = ACTIVATE_MODS[i].size();
                        commandPrecedent = i;
                    }
                }

                if ((TOGGLE_KEY[i] != -1)
                        && (event.getEventType() == InputEventType.KEY_DOWN) && (event.getEventValue() == TOGGLE_KEY[i])) {
                    int condition = TOGGLE_MODS[i].size();
                    for (Modifier m : TOGGLE_MODS[i]) {
                        if (!TOGGLE_MODS[i].contains(m)) {
                            continue;
                        }
                        switch (m) {
                            case CTRL:
                                if (event.isCtrlDown()) {
                                    condition--;
                                }
                                break;
                            case ALT:
                                if (event.isAltDown()) {
                                    condition--;
                                }
                                break;
                            case SHIFT:
                                if (event.isShiftDown()) {
                                    condition--;
                                }
                                break;
                        }
                    }

                    if ((TOGGLE_MODS[i].size() >= mostMods) && (condition == 0)) {
                        mostMods = TOGGLE_MODS[i].size();
                        commandPrecedent = i;
                    }
                }

                if ((TOGGLE_MOUSE[i] != -1)
                        && (event.getEventType() == InputEventType.MOUSE_DOWN) && (event.getEventValue() == TOGGLE_MOUSE[i])) {
                    int condition = TOGGLE_MODS[i].size();
                    for (Modifier m : TOGGLE_MODS[i]) {
                        if (!TOGGLE_MODS[i].contains(m)) {
                            continue;
                        }
                        switch (m) {
                            case CTRL:
                                if (event.isCtrlDown()) {
                                    condition--;
                                }
                                break;
                            case ALT:
                                if (event.isAltDown()) {
                                    condition--;
                                }
                                break;
                            case SHIFT:
                                if (event.isShiftDown()) {
                                    condition--;
                                }
                                break;
                        }
                    }

                    if ((TOGGLE_MODS[i].size() >= mostMods) && (condition == 0)) {
                        mostMods = TOGGLE_MODS[i].size();
                        commandPrecedent = i;
                    }
                }
            }

            if (commandPrecedent < 0) {
                continue;
            }

            int i = commandPrecedent;
            if ((ACTIVATE_KEY[i] != -1)
                    && (event.getEventType() == InputEventType.KEY_DOWN) && (event.getEventValue() == ACTIVATE_KEY[i])) {
                int condition = ACTIVATE_MODS[i].size();
                for (Modifier m : ACTIVATE_MODS[i]) {
                    switch (m) {
                        case CTRL:
                            if (event.isCtrlDown()) {
                                condition--;
                            }
                            break;
                        case ALT:
                            if (event.isAltDown()) {
                                condition--;
                            }
                            break;
                        case SHIFT:
                            if (event.isShiftDown()) {
                                condition--;
                            }
                            break;
                    }
                }

                if (condition == 0) {
                    data.active[i] = !data.toggled[i];
                }
            }

            if ((ACTIVATE_MOUSE[i] != -1)
                    && (event.getEventType() == InputEventType.MOUSE_DOWN) && (event.getEventValue() == ACTIVATE_MOUSE[i])) {
                int condition = ACTIVATE_MODS[i].size();
                for (Modifier m : ACTIVATE_MODS[i]) {
                    switch (m) {
                        case CTRL:
                            if (event.isCtrlDown()) {
                                condition--;
                            }
                            break;
                        case ALT:
                            if (event.isAltDown()) {
                                condition--;
                            }
                            break;
                        case SHIFT:
                            if (event.isShiftDown()) {
                                condition--;
                            }
                            break;
                    }
                }

                if (condition == 0) {
                    data.active[i] = !data.toggled[i];
                }
            }

            if ((TOGGLE_KEY[i] != -1)
                    && (event.getEventType() == InputEventType.KEY_DOWN) && (event.getEventValue() == TOGGLE_KEY[i])) {
                int condition = TOGGLE_MODS[i].size();
                for (Modifier m : TOGGLE_MODS[i]) {
                    switch (m) {
                        case CTRL:
                            if (event.isCtrlDown()) {
                                condition--;
                            }
                            break;
                        case ALT:
                            if (event.isAltDown()) {
                                condition--;
                            }
                            break;
                        case SHIFT:
                            if (event.isShiftDown()) {
                                condition--;
                            }
                            break;
                    }
                }

                if (condition == 0) {
                    data.active[i] = !data.active[i];
                    data.toggled[i] = !data.toggled[i];
                }
            }

            if ((TOGGLE_MOUSE[i] != -1)
                    && (event.getEventType() == InputEventType.MOUSE_DOWN) && (event.getEventValue() == TOGGLE_MOUSE[i])) {
                int condition = TOGGLE_MODS[i].size();
                for (Modifier m : TOGGLE_MODS[i]) {
                    switch (m) {
                        case CTRL:
                            if (event.isCtrlDown()) {
                                condition--;
                            }
                            break;
                        case ALT:
                            if (event.isAltDown()) {
                                condition--;
                            }
                            break;
                        case SHIFT:
                            if (event.isShiftDown()) {
                                condition--;
                            }
                            break;
                    }
                }

                if (condition == 0) {
                    data.active[i] = !data.active[i];
                    data.toggled[i] = !data.toggled[i];
                }
            }

            event.consume();
        }

        if (firstFrame) {
            for (int i = 0; i < ACTIVATE_KEY.length; i++) {
                if (ON_AT_START[i]) {
                    data.active[i] = true;
                    data.toggled[i] = true;
                }
            }

            firstFrame = false;
        }

        for (int i = 0; i < ACTIVATE_KEY.length; i++) {
            if (data.active[i]) {
                /* Turned on */
                if (SPEED_UP_MULT[i] != 1f) {
                    String statId = STAT_ID_BASE + i;

                    float timeMult = SPEED_UP_MULT[i];
                    if ((timeMult > 1.0) && (CAP_TO_FPS[i] > 0.0)) {
                        float trueFrameTime = Global.getCombatEngine().getElapsedInLastFrame();
                        float trueFPS = 1 / trueFrameTime;
                        float cappedTimeMult = Math.max(1f, trueFPS / CAP_TO_FPS[i]);
                        timeMult = Math.min(cappedTimeMult, timeMult);
                    }

                    Global.getCombatEngine().getTimeMult().modifyMult(statId, timeMult, "SpeedUp");
                }
                if (NO_BULLET_TIME[i]) {
                    float totalPercentMod = 0f;
                    HashMap<String, StatMod> percentMods = Global.getCombatEngine().getTimeMult().getPercentMods();
                    for (Entry<String, StatMod> modEntry : percentMods.entrySet()) {
                        String id = modEntry.getKey();
                        if (id.startsWith(STAT_ID_BASE)) {
                            continue;
                        }
                        StatMod mod = modEntry.getValue();
                        totalPercentMod += mod.getValue();
                    }

                    float baseTimeMult = 1f + (totalPercentMod / 100f);
                    HashMap<String, StatMod> flatMods = Global.getCombatEngine().getTimeMult().getFlatMods();
                    for (Entry<String, StatMod> modEntry : flatMods.entrySet()) {
                        String id = modEntry.getKey();
                        if (id.startsWith(STAT_ID_BASE)) {
                            continue;
                        }
                        StatMod mod = modEntry.getValue();
                        baseTimeMult += mod.getValue();
                    }

                    HashMap<String, StatMod> multMods = Global.getCombatEngine().getTimeMult().getMultMods();
                    for (Entry<String, StatMod> modEntry : multMods.entrySet()) {
                        String id = modEntry.getKey();
                        if (id.startsWith(STAT_ID_BASE)) {
                            continue;
                        }
                        StatMod mod = modEntry.getValue();
                        baseTimeMult *= mod.getValue();
                    }

                    baseTimeMult = Math.min(Math.max(baseTimeMult, 1e-7f), 1f);
                    if (baseTimeMult < 1f) {
                        Global.getCombatEngine().getTimeMult().modifyMult(STAT_ID_NBT, 1f / baseTimeMult, "SpeedUp NBT");
                    } else {
                        Global.getCombatEngine().getTimeMult().unmodify(STAT_ID_NBT);
                    }
                }
                if (!wasActive[i] && PRINT_MESSAGE[i]) {
                    if (SPEED_UP_MULT[i] > 1f) {
                        if (Math.abs(SPEED_UP_MULT[i] - Math.round(SPEED_UP_MULT[i])) < 0.05f) {
                            if (CAP_TO_FPS[i] > 0.0) {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged 1–%d%s speed-up.", Math.round(SPEED_UP_MULT[i]), Strings.X));
                            } else {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged %d%s speed-up.", Math.round(SPEED_UP_MULT[i]), Strings.X));
                            }
                        } else {
                            if (CAP_TO_FPS[i] > 0.0) {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged 1–%.1f%s speed-up.", SPEED_UP_MULT[i], Strings.X));
                            } else {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Engaged %.1f%s speed-up.", SPEED_UP_MULT[i], Strings.X));
                            }
                        }
                    } else if (SPEED_UP_MULT[i] < 1f) {
                        if (Math.abs(SPEED_UP_MULT[i] - Math.round(SPEED_UP_MULT[i])) < 0.05f) {
                            Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                    String.format("Engaged %d%s slow-mo.", Math.round(SPEED_UP_MULT[i]), Strings.X));
                        } else {
                            Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                    String.format("Engaged %.1f%s slow-mo.", SPEED_UP_MULT[i], Strings.X));
                        }
                    }
                    if (NO_BULLET_TIME[i]) {
                        Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                String.format("Bullet-time effects disabled."));
                    }
                    Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
                }
            } else if (!data.active[i] && wasActive[i]) {
                /* Turned off */
                if (SPEED_UP_MULT[i] != 1f) {
                    String statId = STAT_ID_BASE + i;
                    Global.getCombatEngine().getTimeMult().unmodify(statId);
                }
                if (NO_BULLET_TIME[i]) {
                    Global.getCombatEngine().getTimeMult().unmodify(STAT_ID_NBT);
                }
                if (PRINT_MESSAGE[i]) {
                    if (SPEED_UP_MULT[i] > 1f) {
                        if (Math.abs(SPEED_UP_MULT[i] - Math.round(SPEED_UP_MULT[i])) < 0.05f) {
                            if (CAP_TO_FPS[i] > 0.0) {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Disengaged 1–%d%s speed-up.", Math.round(SPEED_UP_MULT[i]), Strings.X));
                            } else {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Disengaged %d%s speed-up.", Math.round(SPEED_UP_MULT[i]), Strings.X));
                            }
                        } else {
                            if (CAP_TO_FPS[i] > 0.0) {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Disengaged 1–%.1f%s speed-up.", SPEED_UP_MULT[i], Strings.X));
                            } else {
                                Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                        String.format("Disengaged %.1f%s speed-up.", SPEED_UP_MULT[i], Strings.X));
                            }
                        }
                    } else if (SPEED_UP_MULT[i] < 1f) {
                        if (Math.abs(SPEED_UP_MULT[i] - Math.round(SPEED_UP_MULT[i])) < 0.05f) {
                            Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                    String.format("Disengaged %d%s slow-mo.", Math.round(SPEED_UP_MULT[i]), Strings.X));
                        } else {
                            Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                    String.format("Disengaged %.1f%s slow-mo.", SPEED_UP_MULT[i], Strings.X));
                        }
                    }
                    if (NO_BULLET_TIME[i]) {
                        Global.getCombatEngine().getCombatUI().addMessage(0, Global.getSettings().getColor(TEXT_COLOR),
                                String.format("Bullet-time effects enabled."));
                    }
                    Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData(ACTIVATE_KEY.length));
        this.firstFrame = true;
    }

    protected static final class LocalData {

        boolean active[];
        boolean toggled[];

        LocalData(int len) {
            active = new boolean[len];
            toggled = new boolean[len];
        }
    }

    protected static enum Modifier {
        CTRL, ALT, SHIFT
    }
}
