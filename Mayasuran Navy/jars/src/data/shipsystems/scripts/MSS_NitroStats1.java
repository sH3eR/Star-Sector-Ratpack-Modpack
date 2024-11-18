package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import org.lwjgl.util.vector.Vector2f;

public class MSS_NitroStats1 extends BaseShipSystemScript {


	
	private Color color = new Color(255,90,75,255);
        
        private boolean runOnce = false;
        
        //protected Object STATUSKEY1 = new Object(); // Gotta have this for the additional status data to attach to.
	
        @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
                if (stats.getEntity() instanceof ShipAPI) {
                    ship = (ShipAPI) stats.getEntity();
                    ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
                    ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel);
                }
            
                if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().unmodify(id);
                        if (!runOnce) {
                            for (WeaponAPI w : ship.getAllWeapons()) {
                                if (w.getType() == WeaponType.DECORATIVE) {
                                    Global.getCombatEngine().spawnProjectile(ship, w, "MSS_canisterbay",
			  				w.getLocation(), 
                                                        w.getCurrAngle(), 
                                                        ship.getVelocity());
                                    Vector2f velocity = new Vector2f((float)Math.cos(Math.toRadians((double) w.getCurrAngle())), (float)Math.sin(Math.toRadians((double)w.getCurrAngle())));
                                    velocity = Vector2f.add(velocity, ship.getVelocity(), new Vector2f(0f, 0f));
                                    Global.getCombatEngine().spawnExplosion(w.getLocation(),
                                                        velocity,
                                                        color,
                                                        60,
                                                        0.5f);
	
                                    runOnce = true;
                                }
                            }
                        }
                        

		} else {
			stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 1200f * effectLevel);
                        
                        runOnce = false;
		}
		//ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
		//ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel);

	}
        
        @Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
                stats.getMaxTurnRate().unmodify(id);
                stats.getTurnAcceleration().unmodify(id);
		
	}
	
        @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
}
