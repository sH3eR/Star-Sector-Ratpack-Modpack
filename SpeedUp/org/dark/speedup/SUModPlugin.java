package org.dark.speedup;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import java.io.IOException;
import org.apache.log4j.Level;
import org.json.JSONException;

public class SUModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() {
        try {
            SU_SpeedUpEveryFrame.reloadSettings();
            SU_SpeedUpCampaign.reloadSettings();
        } catch (IOException | JSONException e) {
            Global.getLogger(SUModPlugin.class).log(Level.ERROR, "SpeedUp load failed: " + e.getMessage());
        }
    }
    
    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().getListenerManager().addListener(new SU_SpeedUpCampaign(), true);
    }
    
    @Override
    public void onDevModeF8Reload() {
        this.onApplicationLoad();
    }
}
