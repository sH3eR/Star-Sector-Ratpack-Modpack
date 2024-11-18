package org.dark.speedup;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import org.apache.log4j.Logger;
import org.dark.speedup.SU_SpeedUpEveryFrame.LocalData;
import org.dark.speedup.SU_SpeedUpEveryFrame.Modifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles speed mult modifier in campaign.
 *
 * @author Histidine
 */
public class SU_SpeedUpCampaign implements CampaignInputListener {

    public static final float BASE_SPEEDUP = Global.getSettings().getFloat("campaignSpeedupMult");
    public static Logger log = Global.getLogger(SU_SpeedUpCampaign.class);

    private static int ACTIVATE_KEY[];
    private static int ACTIVATE_MOUSE[];
    private static EnumSet<Modifier> ACTIVATE_MODS[];
    private static int TOGGLE_KEY[];
    private static int TOGGLE_MOUSE[];
    private static EnumSet<Modifier> TOGGLE_MODS[];
    private static float SPEED_UP_MULT[];
    private static boolean PRINT_MESSAGE[];
    private static boolean ON_AT_START[];

    private static final String SETTINGS_FILE = "SPEED_UP.ini";
    private static final String SOUND_ID = "ui_noise_static_message";
    private static final String TEXT_COLOR = "standardTextColor";

    private static boolean initialized = false;
    private static boolean firstFrame = true;

    private static float mult = BASE_SPEEDUP;
    private static LocalData data;

    public static void reloadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);
        JSONArray options = settings.getJSONArray("speedOptionsCampaign");

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
        PRINT_MESSAGE = new boolean[options.length()];
        ON_AT_START = new boolean[options.length()];

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
            PRINT_MESSAGE[i] = option.optBoolean("printMessage", false);
            ON_AT_START[i] = option.optBoolean("onAtStart", false);
        }

        initialized = true;
    }

    // Input handling is cargo-culted from SU_SpeedUpEveryFrame
    @Override
    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {
        //Global.getLogger(this.getClass()).info("Input pre-fleet");
        if (!initialized) {
            return;
        }

        if (data == null) {
            data = new SU_SpeedUpEveryFrame.LocalData(ACTIVATE_KEY.length);
        }

        boolean wasActive[] = data.active.clone();

        if (firstFrame) {
            for (int i = 0; i < ACTIVATE_KEY.length; i++) {
                if (ON_AT_START[i]) {
                    data.active[i] = true;
                    data.toggled[i] = true;
                }
            }
            firstFrame = false;
        }

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

        float currMult = BASE_SPEEDUP;

        // iterate over all active modifiers and apply their mults to the base speedup
        for (int i = 0; i < ACTIVATE_KEY.length; i++) {
            if (data.active[i]) {
                /* Turned on */
                currMult *= SPEED_UP_MULT[i];
                if (!wasActive[i] && PRINT_MESSAGE[i]) {
                    Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
                }
            } else if (!data.active[i] && wasActive[i]) {
                /* Turned off */
                if (PRINT_MESSAGE[i]) {
                    Global.getSoundPlayer().playUISound(SOUND_ID, 1f, 0.5f);
                }
            }
        }

        if (mult != currMult) {
            mult = currMult;
            Global.getSettings().setFloat("campaignSpeedupMult", mult);
            // TODO externalize
            String str = String.format(Global.getSettings().getString("speedUp", "campaignMultMsg"), mult);
            Global.getSector().getCampaignUI().addMessage(str, Global.getSettings().getColor(TEXT_COLOR),
                    mult + "", "", Misc.getHighlightColor(), Color.BLACK);
        }
    }

    @Override
    public void processCampaignInputPreCore(List<InputEventAPI> events) {
        //log.info("Input pre-core");
    }

    @Override
    public void processCampaignInputPostCore(List<InputEventAPI> events) {
        log.info("Input post-core");
    }

    @Override
    public int getListenerInputPriority() {
        return 1;	// no idea what other listeners have
    }
}
