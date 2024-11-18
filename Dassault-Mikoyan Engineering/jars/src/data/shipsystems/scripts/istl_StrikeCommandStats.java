package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;
import com.fs.starfarer.api.util.Misc;

public class istl_StrikeCommandStats extends BaseShipSystemScript {
	public static final Object KEY_JITTER = new Object();
	
	public static final float SPEED_INCREASE_PERCENT = 33f;
	public static final float MANEUVER_INCREASE_PERCENT = 10f;
        public static final float AUTOFIRE_BONUS = 60f;
	
	public static final Color JITTER_COLOR = new Color(45,75,255,255);
	public static final Color JITTER_UNDER_COLOR = new Color(45,75,255,125);

	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		
		if (effectLevel > 0) {
			float jitterLevel = effectLevel;
			float maxRangeBonus = 5f;
			float jitterRangeBonus = jitterLevel * maxRangeBonus;
			for (ShipAPI fighter : getFighters(ship)) {
				if (fighter.isHulk()) continue;
				MutableShipStatsAPI fStats = fighter.getMutableStats();
	
                                fStats.getMaxSpeed().modifyPercent(id, SPEED_INCREASE_PERCENT * effectLevel);
                                fStats.getAcceleration().modifyPercent(id, SPEED_INCREASE_PERCENT * effectLevel);
                                
                                fStats.getAutofireAimAccuracy().modifyFlat(id, AUTOFIRE_BONUS * 0.01f);
                                fStats.getMaxRecoilMult().modifyMult(id, 1f - AUTOFIRE_BONUS * 0.01f);
                                fStats.getRecoilPerShotMult().modifyMult(id, 1f - AUTOFIRE_BONUS * 0.01f);
                                
                                fStats.getDeceleration().modifyPercent(id, MANEUVER_INCREASE_PERCENT * effectLevel);
                                fStats.getMaxTurnRate().modifyPercent(id, MANEUVER_INCREASE_PERCENT * effectLevel);
                                fStats.getTurnAcceleration().modifyPercent(id, MANEUVER_INCREASE_PERCENT * effectLevel);
                                

                                
				if (jitterLevel > 0) {
                                        fighter.getEngineController().fadeToOtherColor(this, new Color(145,175,255,255), new Color(45,75,255,25), jitterLevel, 1f);
					fighter.getEngineController().extendFlame(this, 0.6f, 0.15f, 0.6f);
                                        
					fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 3, 0f, jitterRangeBonus);
					fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 1, 0f, 0 + jitterRangeBonus * 1f);
					Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
				}
			}
		}
	}
	
	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
//		this didn't catch fighters returning for refit		
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}
		
		return result;
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		for (ShipAPI fighter : getFighters(ship)) {
			if (fighter.isHulk()) continue;
			MutableShipStatsAPI fStats = fighter.getMutableStats();
			fStats.getBallisticWeaponDamageMult().unmodify(id);
			fStats.getEnergyWeaponDamageMult().unmodify(id);
			fStats.getMissileWeaponDamageMult().unmodify(id);
		}
	}
	
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float percent = SPEED_INCREASE_PERCENT * effectLevel;
		if (index == 0) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(SPEED_INCREASE_PERCENT * effectLevel) + "% more fighter top speed", false);
		}
                if (index == 1) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(MANEUVER_INCREASE_PERCENT * effectLevel) + "% more fighter agility", false);
		}
                if (index == 2) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(AUTOFIRE_BONUS * effectLevel) + "% more fighter accuracy", false);
		}
		return null;
	}

	
}








