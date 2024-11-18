package data.hullmods.vice;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AttunedEmitterDiodes extends BaseHullMod {

	private static float BEAM_RANGE_PENALTY = 100f;
	private static float BEAM_DAMAGE_PENALTY = 15f;
	private static float BEAM_DAMAGE_PENALTY_SMOD = 10f;
	private static String FIRST_BONUS_TEXT = "hard flux";
	private static String COHERER = "coherer";
	private static String COHERER_2 = "vice_modular_bolt_coherer";
	private static String CONFLICT_MOD_1 = "advancedoptics";
	private static String CONFLICT_MOD_2 = "high_scatter_amp";
	private static String CONFLICT_MOD_3 = "ix_laser_collimator";
	private static String OTHER_EMITTER = "vice_adaptive_emitter_diodes";
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float beamDamagePenalty = isSMod(stats) ? BEAM_DAMAGE_PENALTY_SMOD : BEAM_DAMAGE_PENALTY;
		stats.getBeamWeaponRangeBonus().modifyFlat(id, -BEAM_RANGE_PENALTY);
		stats.getBeamWeaponDamageMult().modifyPercent(id, -beamDamagePenalty);
		if (isSMod(stats)) {
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_1);
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_2);
			stats.getVariant().getHullMods().remove(CONFLICT_MOD_3);
		}
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (!ship.getVariant().hasHullMod(COHERER) && !ship.getVariant().hasHullMod(COHERER_2)) return;
		ship.addListener(new AttunedHardFlux(ship));
	}
	
	public static class AttunedHardFlux implements DamageDealtModifier {
		protected ShipAPI ship;
		public AttunedHardFlux(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			
			if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
				damage.setForceHardFlux(true);
			}
			return null;
		}
	}

	private boolean hasEmitterModOverlap(ShipAPI ship) {
		return (ship.getVariant().getHullMods().contains(CONFLICT_MOD_1) 
				|| ship.getVariant().getHullMods().contains(CONFLICT_MOD_2)
				|| ship.getVariant().getHullMods().contains(CONFLICT_MOD_3)
				|| ship.getVariant().getHullMods().contains(OTHER_EMITTER));
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (isForModSpec || ship == null) return;
		if (!ship.getVariant().hasHullMod(COHERER) && !ship.getVariant().hasHullMod(COHERER_2)) {
			tooltip.addPara("System offline due to mission Modular Bolt Coherer", 10f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
		}
	}
	
	@Override
    public boolean isApplicableToShip(ShipAPI ship) {
		if (!ship.getVariant().getHullMods().contains(COHERER) && !ship.getVariant().getHullMods().contains(COHERER_2)) return false;
		return (!hasEmitterModOverlap(ship));
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (!ship.getVariant().getHullMods().contains(COHERER) && !ship.getVariant().getHullMods().contains(COHERER_2)) {
			return "Requires Energy or Modular Bolt Coherer";
		}
		if (hasEmitterModOverlap(ship)) return "Incompatible emitter modification present";
		return null;
	}	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BEAM_RANGE_PENALTY;
		if (index == 1) return "" + (int) BEAM_DAMAGE_PENALTY + "%";
		if (index == 2) return FIRST_BONUS_TEXT;
		return null;
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BEAM_DAMAGE_PENALTY_SMOD + "%";
		return null;
	}
}