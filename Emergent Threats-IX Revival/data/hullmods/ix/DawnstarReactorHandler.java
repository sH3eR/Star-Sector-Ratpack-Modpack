package data.hullmods.ix;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class DawnstarReactorHandler extends BaseHullMod {
	
	//proton -> neutron -> electron
	private static String PROTON_MOD = "ix_dawnstar_proton";
	private static String PROTON_MOD_HANDLER = "ix_dawnstar_proton_handler";
	private static String NEUTRON_MOD = "ix_dawnstar_neutron";
	private static String NEUTRON_MOD_HANDLER = "ix_dawnstar_neutron_handler";
	private static String ELECTRON_MOD = "ix_dawnstar_electron";
	private static String ELECTRON_MOD_HANDLER = "ix_dawnstar_electron_handler";

	private static String CONTROLLER_MOD = "ix_dawnstar_controller";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		if (!variant.hasHullMod(CONTROLLER_MOD)) return;
		
		boolean isProtonHandler = id.equals(PROTON_MOD_HANDLER);
		boolean isNeutronHandler = id.equals(NEUTRON_MOD_HANDLER);
		boolean isElectronHandler = id.equals(ELECTRON_MOD_HANDLER);
		
		boolean hasProton = variant.hasHullMod(PROTON_MOD);
		boolean hasNeutron = variant.hasHullMod(NEUTRON_MOD);
		boolean hasElectron = variant.hasHullMod(ELECTRON_MOD);
		
		if (isProtonHandler && !hasProton) {
			variant.getHullMods().remove(id);
			variant.addMod(NEUTRON_MOD);
			variant.addMod(NEUTRON_MOD_HANDLER);
		}
		else if (isNeutronHandler && !hasNeutron) {
			variant.getHullMods().remove(id);
			variant.addMod(ELECTRON_MOD);
			variant.addMod(ELECTRON_MOD_HANDLER);
		}
		else if (isElectronHandler && !hasElectron) {
			variant.getHullMods().remove(id);
			variant.addMod(PROTON_MOD);
			variant.addMod(PROTON_MOD_HANDLER);
		}
	}
}