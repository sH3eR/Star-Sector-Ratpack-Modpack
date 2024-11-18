/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.world;

import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import java.util.Random;

/**
 *
 * @author HarmfulMechanic
 */
public class SigmaUtils {
    
        Random characterSaveSeed = StarSystemGenerator.random;
        Random random = new Random(characterSaveSeed.nextLong());
        float selector = random.nextFloat();
    
        //BB Guardian generation
        public static final String ISTL_GUARDIAN_ACOLYTE = "istl_guardian_turret_std";
        public static final String ISTL_GUARDIAN_DMG = "istl_bbsuperheavy_dmg";
        public static final String ISTL_GUARDIAN_STD = "istl_bbsuperheavy_std";
        public static int level = 20;
        public static float radius_star = 300f; //used for random location
        public static float radius_station = 3000f;
        public static float radius_variation = 1000f;
    
}
