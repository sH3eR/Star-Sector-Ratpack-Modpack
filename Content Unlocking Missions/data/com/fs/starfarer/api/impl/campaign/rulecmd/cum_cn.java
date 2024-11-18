package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;


public class cum_cn extends BaseCommandPlugin {
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                BaseSalvageSpecial.clearExtraSalvage(dialog.getInteractionTarget());
                BaseSalvageSpecial.clearExtraSalvage(memoryMap);
                CargoAPI extraSalvage = Global.getFactory().createCargo(true);
                extraSalvage.addCommodity(Commodities.HAND_WEAPONS, 55f);
                extraSalvage.addFuel(85f);
                dialog.getVisualPanel().showLoot("Salvaged", extraSalvage, false, true, true, new CoreInteractionListener() {
			public void coreUIDismissed() {
			}
		});
                ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                ShipVariantAPI variant = Global.getSettings().getVariant("lasher_luddic_path_Raider").clone();
                variant.setSource(VariantSource.REFIT);
                variant.addSuppressedMod(HullMods.ILL_ADVISED);
                variant.addPermaMod(HullMods.MISSLERACKS, true);
                variant.addPermaMod(HullMods.HEAVYARMOR, true);
                variant.addPermaMod(HullMods.HARDENED_SUBSYSTEMS, true);
                ShipVariantAPI variant2 = Global.getSettings().getVariant("hound_luddic_path_Attack").clone();
                variant2.setSource(VariantSource.REFIT);
                variant2.addSuppressedMod(HullMods.ILL_ADVISED);
                variant2.addPermaMod(HullMods.HEAVYARMOR, true);
                variant2.addPermaMod(HullMods.REINFORCEDHULL, true);
                variant2.addPermaMod(HullMods.HARDENED_SUBSYSTEMS, true);
                ShipVariantAPI variant3 = Global.getSettings().getVariant("kite_luddic_path_Raider").clone();
                variant3.setSource(VariantSource.REFIT);
                variant3.addSuppressedMod(HullMods.ILL_ADVISED);
                variant3.addPermaMod(HullMods.MISSLERACKS, true);
                variant3.addPermaMod(HullMods.HEAVYARMOR, true);
                variant3.addPermaMod(HullMods.HARDENED_SUBSYSTEMS, true);
                ShipVariantAPI variant4 = Global.getSettings().getVariant("kite_luddic_path_Strike").clone();
                variant4.setSource(VariantSource.REFIT);
                variant4.addSuppressedMod(HullMods.ILL_ADVISED);
                variant4.addPermaMod(HullMods.MISSLERACKS, true);
                variant4.addPermaMod(HullMods.HEAVYARMOR, true);
                variant4.addPermaMod(HullMods.HARDENED_SUBSYSTEMS, true);
                data.addShip(new ShipRecoverySpecial.PerShipData(variant, ShipRecoverySpecial.ShipCondition.PRISTINE, "Keeper of the Flock", Factions.NEUTRAL, 0f));
                data.addShip(new ShipRecoverySpecial.PerShipData(variant2, ShipRecoverySpecial.ShipCondition.PRISTINE, "Ludd's Left Shoe", Factions.NEUTRAL, 0f));
                data.addShip(new ShipRecoverySpecial.PerShipData(variant3, ShipRecoverySpecial.ShipCondition.PRISTINE, "Wages of Sin", Factions.NEUTRAL, 0f));
                data.addShip(new ShipRecoverySpecial.PerShipData(variant4, ShipRecoverySpecial.ShipCondition.PRISTINE, "Memory of Light", Factions.NEUTRAL, 0f));
                Misc.setSalvageSpecial(dialog.getInteractionTarget(), data);
                return true;
	}
        
}

