package DE.combat;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ConduitShroudStats extends BaseShipSystemScript {

	/*private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 0.33f);
		mag.put(HullSize.FRIGATE, 0.33f);
		mag.put(HullSize.DESTROYER, 0.33f);
		mag.put(HullSize.CRUISER, 0.5f);
		mag.put(HullSize.CAPITAL_SHIP, 0.6f);
	}*/

	public static final float HE_DAM = 0.5f;
	public static final float K_DAM = 0.5f;
	public static final float FRAG_DAM = 1.75f;
	public static final float ENERGY_DAM = 1.25f;
	public static final float EMP_DAM = 1.25f;
	public static final float FLUXDOWN = 0.5f;

	boolean systemswitch = false;
	boolean fluxunder25 = false;
	float count = 0;
	float fluxdelet = 0;

	protected Object STATUSKEY1 = new Object();
	
	//public static final float INCOMING_DAMAGE_MULT = 0.25f;
	//public static final float INCOMING_DAMAGE_CAPITAL = 0.5f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		effectLevel = 1f;
		ShipAPI ship = (ShipAPI) stats.getEntity();

		/*float mult = (Float) mag.get(HullSize.CRUISER);
		if (stats.getVariant() != null) {
			mult = (Float) mag.get(stats.getVariant().getHullSize());
		}*/
		float fluxlevel = ship.getCurrFlux();
		if (!systemswitch) {
			fluxdelet = ship.getMaxFlux()*((5-count)/20);
			ship.getFluxTracker().decreaseFlux(fluxdelet);
			systemswitch = true;
			if (count <= 4) {
				count++;
			}
		}
		stats.getEmpDamageTakenMult().modifyMult(id, EMP_DAM);
		stats.getHighExplosiveDamageTakenMult().modifyMult(id, HE_DAM);
		stats.getKineticDamageTakenMult().modifyMult(id, K_DAM);
		stats.getFragmentationDamageTakenMult().modifyMult(id, FRAG_DAM);
		stats.getEnergyDamageTakenMult().modifyMult(id, ENERGY_DAM);
		//stats.getFluxDissipation().modifyPercent(id, -FLUXDOWN);
		//stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		//stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);

		/*ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		if (player) {
			ShipSystemAPI system = getDamper(ship);
			if (system != null) {
				float percent = (1f - mult) * effectLevel * 100;
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1,
					system.getSpecAPI().getIconSpriteName(), system.getDisplayName(),
					(int) Math.round(percent) + "% less high explosive damage taken", false);
			}
		}*/
	}
	
	public static ShipSystemAPI getDamper(ShipAPI ship) {
//		ShipSystemAPI system = ship.getSystem();
//		if (system != null && system.getId().equals("damper")) return system;
//		if (system != null && system.getId().equals("damper_omega")) return system;
//		if (system != null && system.getSpecAPI() != null && system.getSpecAPI().hasTag(Tags.SYSTEM_USES_DAMPER_FIELD_AI)) return system;
//		return ship.getPhaseCloak();
		ShipSystemAPI system = ship.getPhaseCloak();
		if (system != null && system.getId().equals("de_conduitshroud")) return system;
		if (system != null && system.getSpecAPI() != null && system.getSpecAPI().hasTag(Tags.SYSTEM_USES_DAMPER_FIELD_AI)) return system;
		return ship.getSystem();
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEmpDamageTakenMult().unmodify(id);
		stats.getHighExplosiveDamageTakenMult().unmodify(id);
		stats.getKineticDamageTakenMult().unmodify(id);
		stats.getFragmentationDamageTakenMult().unmodify(id);
		stats.getEnergyDamageTakenMult().unmodify(id);
		//stats.getFluxDissipation().unmodify(id);
		systemswitch = false;
	}


	public StatusData getStatusData(int index, State state, float effectLevel) {
			float mult = HE_DAM * effectLevel;
			float bonusPercent = (int) ((mult) * 100f);
			float mult2 = K_DAM * effectLevel;
			float bonusPercent2 = (int) ((mult2) * 100f);
			float mult3 = FRAG_DAM * effectLevel;
			float bonusPercent3 = (int) (((mult3) * 100f) - 100f);
		    float mult5 = ENERGY_DAM * effectLevel;
		    float bonusPercent4 = (int) (((mult5) * 100f) - 100f);
		float mult6 = EMP_DAM * effectLevel;
		float bonusPercent6 = (int) (((mult5) * 100f) - 100f);
		float bonusPercent7 = (int) ((((5-count)/20) * 100f) - 100f);
		if (index == 0) {
			return new StatusData("high explosive damage taken -" + (int) bonusPercent + "%", false);
		} else if (index == 1) {
			return new StatusData("kinetic damage taken -" + (int) bonusPercent2 + "%", false);
		} else if (index == 2) {
			return new StatusData("fragmentation damage taken +" + (int) bonusPercent3 + "%", true);
		} else if (index == 3) {
			return new StatusData("energy damage taken +" + (int) bonusPercent4 + "%", true);
		} else if (index == 4) {
			return new StatusData("EMP damage taken +" + (int) bonusPercent6 + "%", true);
		} else if (index == 5) {
		return new StatusData("flux reduced by" + (int) bonusPercent7 + "%", false);
		}
		return null;
	}
}
