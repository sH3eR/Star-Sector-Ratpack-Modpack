package DE.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SpinalGenerator extends BaseHullMod {
	/*private ShipAPI ship;

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
		this.ship = ship;
		this.system = system;
		this.engine = engine;
	}*/

	/*public static class RangefinderRangeModifier implements WeaponBaseRangeModifier {
		public float small, medium, max;
		public RangefinderRangeModifier(float small, float medium, float max) {
			this.small = small;
			this.medium = medium;
			this.max = max;
		}

		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSlot() == null || weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.BALLISTIC) {
				return 0f;
			}
			if (weapon.hasAIHint(WeaponAPI.AIHints.PD)) {
				return 0f;
			}

			float bonus = 0;
			if (weapon.getSize() == WeaponAPI.WeaponSize.SMALL) {
				bonus = SMALL_RANGE_PENALTY;
			} else if (weapon.getSize() == WeaponAPI.WeaponSize.MEDIUM) {
				bonus = MEDIUM_RANGE_BONUS;
			}
			if (bonus == 0f) return 0f;

			float base = weapon.getSpec().getMaxRange();
			if (base + bonus > max) {
				bonus = max - base;
			}
			if (bonus < 0) bonus = 0;
			return bonus;
		}
	}*/

	/*public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (weapon.getSpec().getMountType() == WeaponAPI.WeaponType.HYBRID) {
			stats.getEnergyWeaponDamageMult().modifyFlat(id, MEDIUM_DAMAGE_BONUS);
			//stats.getBeamWeaponDamageMult().modifyPercent(id, -BEAM_DAMAGE_PENALTY);
			stats.getBeamWeaponTurnRateBonus().modifyMult(id, 1f - BEAM_TURN_PENALTY * 0.01f);
		}
	}*/

	private static Map mag = new HashMap();
	static {
		mag.put(WeaponAPI.WeaponSize.SMALL, 100f);
		mag.put(WeaponAPI.WeaponSize.MEDIUM,200f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.SMALL_BALLISTIC_MOD).modifyFlat(id,-(float) mag.get(WeaponAPI.WeaponSize.SMALL));
		stats.getDynamic().getMod(Stats.SMALL_ENERGY_MOD).modifyFlat(id,-(float) mag.get(WeaponAPI.WeaponSize.SMALL));
		stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id,(float) mag.get(WeaponAPI.WeaponSize.MEDIUM));
		stats.getDynamic().getMod(Stats.MEDIUM_ENERGY_MOD).modifyFlat(id,(float) mag.get(WeaponAPI.WeaponSize.MEDIUM));
	}

	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		int medbonus = 200;
		int smalldeficit = 100;

		LabelAPI label = tooltip.addPara("increases the base range of medium weapons by %s,"
						+ " but decreases the base range of small weapons by %s.", opad, h,
				"" + medbonus, "" + smalldeficit);
//		label.setHighlight("base", "Ballistic", "" + (int)BONUS_SMALL_1, "" + (int)BONUS_MAX_1);
//		label.setHighlightColors(h, Misc.MOUNT_BALLISTIC, h, h);
		label.setHighlight("" + medbonus, "" + smalldeficit);
		label.setHighlightColors(h, h);
	}

}
