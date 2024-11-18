package data.scripts.plugins;

import java.util.List;
import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.MutableStat;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import org.apache.log4j.Level;

public class TheScriptTBH extends BaseEveryFrameCombatPlugin{
	
	private CombatEngineAPI engine;
	private FluxTrackerAPI tracker;
	private static final IntervalUtil interval8 = new IntervalUtil(10f,10f);
	private static final IntervalUtil interval9 = new IntervalUtil(20f,20f);
	private static final String Settings_file = "Settings.ini";
	
	private static float flux_high_2_trigger = 0.85f;
	private static float hull_low_1_trigger = 0.5f;
	private static float hull_low_2_trigger = 0.2f;
	private static float damage_hull_1_trigger = 600;
	//key to stop
	private static float flux_high_2_volume = 0.85f;
	private static float hull_low_1_volume =  1.3f;
	private static float hull_low_2_volume = 1f;
	private static float damage_hull_1_volume = 1.6f;
	private static float flameout_1_volume = 1f;
	
	private static float hull1 = 0f;
	private static float hull2 = 0f;
	private static float hull3 = 0f;
	private static float stopHull = 0f;
	private static int wasFlamedOut = 0;
	private static int nowFlamedOut = 0;
	private static int playedHullSound = 0;
	private static int canPlay = 1;
	private static boolean initialized = false;
	
	private void loadSettings() throws Exception {
		JSONObject settings = Global.getSettings().loadJSON(Settings_file);
		
		flux_high_2_trigger = (float)settings.getDouble("flux_high_2_trigger");
		hull_low_1_trigger = (float)settings.getDouble("hull_low_1_trigger");
		hull_low_2_trigger = (float)settings.getDouble("hull_low_2_trigger");
		damage_hull_1_trigger = settings.getInt("damage_hull_1_trigger");
		flux_high_2_volume = (float)settings.getDouble("flux_high_2_volume");
		hull_low_1_volume = (float)settings.getDouble("hull_low_1_volume");
		hull_low_2_volume = (float)settings.getDouble("hull_low_2_volume");
		damage_hull_1_volume = (float)settings.getDouble("damage_hull_1_volume");
		flameout_1_volume = (float)settings.getDouble("flameout_1_volume");
		
		initialized = true;
	}
	
	public void init(CombatEngineAPI engine){
        this.engine=engine;
		try{
			loadSettings();
		}
		catch(Exception e){
			Global.getLogger(TheScriptTBH.class).log(Level.INFO, "CAS cannot load: " + e.getMessage());
		}
    }
	
	public void advance(float amount, List events){
		
		if (engine == null) return;
        if (engine.isPaused()) return;
        if (Global.getCurrentState() != GameState.COMBAT) return;
		if (!initialized) return;
		
		ShipAPI playerShip = engine.getPlayerShip();
		float flux = playerShip.getFluxTracker().getFluxLevel();
		float hull = playerShip.getHullLevel();
		float timeMult = engine.getTimeMult().getModifiedValue();

		if(playerShip.getEngineController().isFlamedOut() == true)
			nowFlamedOut = 1;
		else
			nowFlamedOut = 0;
		
		for (int i = 0; i < events.size(); i++) {
			InputEventAPI event = (InputEventAPI) events.get(i);
			if (event.isConsumed()) continue;
			if (event.isKeyDownEvent()) {
				if (event.getEventValue() == Keyboard.KEY_O) {
					stopHull = playerShip.getHitpoints();
					canPlay = 0;
					event.consume();
				}
			}
		}

		interval8.advance(amount);
		interval9.advance(amount);
		
		if(flux > flux_high_2_trigger && playerShip.isAlive())
			Global.getSoundPlayer().playUILoop("flux_high_2", 1f*timeMult, flux_high_2_volume);
		
		if(hull < hull_low_1_trigger && playerShip.isAlive() && canPlay == 1){ //change ifs for efficiency
				Global.getSoundPlayer().playUILoop("hull_low_1", 1f*timeMult, hull_low_1_volume);
			if(interval9.intervalElapsed())
					engine.getCombatUI().addMessage(1, playerShip,"Low hull; press O to suppress warnings");
		}
		
		if(hull<hull_low_2_trigger && playerShip.isAlive() && canPlay == 1)
			Global.getSoundPlayer().playUILoop("hull_low_2", 1f*timeMult, hull_low_2_volume);
		
		if(playerShip.getEngineController().isFlamedOut()==true && playerShip.isAlive())
			Global.getSoundPlayer().playUILoop("flameout_1", 1f*timeMult, flameout_1_volume);

		if(interval8.intervalElapsed()){
			playedHullSound = 0;
		}
		
		hull1=hull2;
		hull2=hull3;
		hull3=playerShip.getHitpoints();
		if((hull1-hull3) > damage_hull_1_trigger && playerShip.isAlive() && playedHullSound == 0 && !playerShip.isShuttlePod()){
			playedHullSound = 1;
			Global.getSoundPlayer().playUISound("damage_hull_1", 1f*timeMult, damage_hull_1_volume);
		}
		if(stopHull-hull3 > 200f){
			canPlay = 1;
		}
		
		if(hull3 > stopHull){
			stopHull = hull3;
		}
	}
}
