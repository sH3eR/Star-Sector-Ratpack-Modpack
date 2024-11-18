/*
By Tartiflette, thank you tart; from, KnightChase
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class MSS_txt {   
    private static final String MSS="MSS";
    public static String txt(String id){
        return Global.getSettings().getString(MSS, id);
    }    
}