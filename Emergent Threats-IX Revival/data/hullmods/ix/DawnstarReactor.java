package data.hullmods.ix;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

public class DawnstarReactor extends BaseHullMod {
	
	//on hit effect handled by weapon script
	private static String PROTON_MOD = "ix_dawnstar_proton";
	private static String NEUTRON_MOD = "ix_dawnstar_neutron";
	private static String ELECTRON_MOD = "ix_dawnstar_electron";
	//private static String EXOTIC_MOD = "ix_dawnstar_exotic";
	
	//dawnstar L/dawnstar H/daybreak (adaptive), both emitter types
	private static String P_DAM_L = "20";
	private static String P_DAM_H = "30 armor";
	private static String P_DAM_A = "10";
	private static String N_DAM_L = "100";
	private static String N_DAM_H = "150 kinetic";
	private static String N_DAM_A = "50";
	private static String E_DAM_L = "300";
	private static String E_DAM_H = "450 EMP";
	private static String E_DAM_A = "150";
	
	private String DAM_L = "";
	private String DAM_H = "";
	private String DAM_A = "";
	
	private static String CPB_L_ID = "dawnstar_lcpb_ix";
	private static String CPB_H_ID = "dawnstar_hcpb_ix";
	private static String APB_M_ID = "daythorn_mapb_ix";
	private static String APB_T_ID = "daythorn_tapb_ix";
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (stats.getVariant().hasHullMod(PROTON_MOD)) {
			DAM_L = P_DAM_L;
			DAM_H = P_DAM_H;
			DAM_A = P_DAM_A;
		}
		else if (stats.getVariant().hasHullMod(NEUTRON_MOD)) {
			DAM_L = N_DAM_L;
			DAM_H = N_DAM_H;
			DAM_A = N_DAM_A;
		}
		else if (stats.getVariant().hasHullMod(ELECTRON_MOD)) {
			DAM_L = E_DAM_L;
			DAM_H = E_DAM_H;
			DAM_A = E_DAM_A;
		}
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new DawnstarHardFlux(ship));
	}
	
	public static class DawnstarHardFlux implements DamageDealtModifier {
		protected ShipAPI ship;
		public DawnstarHardFlux(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			if (param instanceof BeamAPI) {
				BeamAPI beam = (BeamAPI) param;
				String weaponId = beam.getWeapon().getSpec().getWeaponId();
				if (weaponId.equals(CPB_L_ID) 
						|| weaponId.equals(CPB_H_ID)
						|| weaponId.equals(APB_M_ID)
						|| weaponId.equals(APB_T_ID)) damage.setForceHardFlux(true);
			}
			return null;
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return DAM_L;
		if (index == 1) return DAM_H;
		if (index == 2) return DAM_A;
		return null;
	}
}