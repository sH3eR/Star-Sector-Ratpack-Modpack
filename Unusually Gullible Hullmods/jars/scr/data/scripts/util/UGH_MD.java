package data.scripts.util;

import com.fs.starfarer.api.Global;

public class UGH_MD {
    public static String str(String key) {
        return Global.getSettings().getString("ugh_hullmod", key);
    }
}
