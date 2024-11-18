package data.scripts.ix.luna;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lunalib.lunaRefit.BaseRefitButton;

public class NanoReplicatorButton extends BaseRefitButton {

	private static String REPLICATOR_ID = "ix_nano_replicator";
	
	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Apply Nano-replicator";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "graphics/icons/ix_nano_replicator.png";
	}

	@Override
	public int getOrder(FleetMemberAPI member, ShipVariantAPI variant) {
		return 4000;
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		tooltip.addPara("Industrial Nano-replicator", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor());
		tooltip.addSpacer(5f);

		tooltip.addPara("Temporarily connect the gamma core that controls a large stock of self replicating nanites to an automated warship. Should repair all d-mods without destroying the ship, in theory.", 0f);
		
		if (!member.getVariant().hasDMods()) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Only usable on automated ship with d-mods.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
		}
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		List<HullModSpecAPI> dMods = DModManager.getModsWithTags("dmod");
		for (HullModSpecAPI mod : dMods) {
			variant.getHullMods().remove(mod.getId());
		}
		removeReplicator();
		refreshVariant();
		refreshButtonList();
	}

	//Makes the button not clickable if mod cannot be fitted
	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		if (!variant.hasDMods()) return false;
		return (variant.hasHullMod("automated") && hasReplicator());
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return (variant.hasHullMod("automated") && hasReplicator());
	}
		
	private boolean hasReplicator() {
		boolean hasNano = false;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoStackAPI s : cargo.getStacksCopy()) {
			if (s.isSpecialStack() && s.getSpecialItemSpecIfSpecial().getId().equals(REPLICATOR_ID)) {
				hasNano = true;
			}
		}
		return hasNano;
	}
	
	private void removeReplicator() {
		boolean hasDeleted = false;
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoStackAPI s : cargo.getStacksCopy()) {
			if (s.isSpecialStack() && s.getSpecialItemSpecIfSpecial().getId().equals(REPLICATOR_ID)) {
				if (!hasDeleted) {
					cargo.removeStack(s);
					hasDeleted = true;
				}
			}
		}
	}
}