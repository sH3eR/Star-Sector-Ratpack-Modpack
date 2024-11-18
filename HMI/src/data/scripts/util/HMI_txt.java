/*
By Tartiflette, ever so slightly edited by the Mod Bashing Boogeyman
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class HMI_txt {
    private static final String ML="HMI_txt";
    
    public static String txt(String id){
        return Global.getSettings().getString(ML, id);
    }       
}