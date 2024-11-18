package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem;
import com.fs.starfarer.api.campaign.impl.items.ModSpecItemPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.ArrayList;
import java.util.Random;


public class cum_ttt extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
                if (dialog.getInteractionTarget() == null) return false;
                if (dialog.getInteractionTarget().getMarket() == null) return false;
                if (params.size() > 1) {dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate().set("$cum_ttt_tradeagain2", Misc.getAtLeastStringForDays((int) dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate().getExpire("$cum_ttt_tradeagain")), 0); return false;}
		List<SalvageEntityGenDataSpec.DropData> dropRandom = new ArrayList<SalvageEntityGenDataSpec.DropData>();
		List<SalvageEntityGenDataSpec.DropData> dropValue = new ArrayList<SalvageEntityGenDataSpec.DropData>();
			
		SalvageEntityGenDataSpec.DropData d = new SalvageEntityGenDataSpec.DropData();	
		d.chances = 1;	
		d.group = "blueprints_low";	
		//d.addCustom("item_:{tags:[single_bp], p:{tags:[rare_bp]}}", 1f);	
		dropRandom.add(d);	
			
		d = new SalvageEntityGenDataSpec.DropData();	
		d.chances = 1;	
		d.group = "rare_tech_low";	
		d.valueMult = 0.1f;	
		dropRandom.add(d);	
			
		d = new SalvageEntityGenDataSpec.DropData();	
		d.chances = 1;	
		d.group = "ai_cores3";	
		//d.valueMult = 0.1f; // already a high chance to get nothing due to group setup, so don't reduce further	
		dropRandom.add(d);	
			
		d = new SalvageEntityGenDataSpec.DropData();	
		d.chances = 1;	
		d.group = "any_hullmod_low";	
		dropRandom.add(d);	
			
		d = new SalvageEntityGenDataSpec.DropData();	
		d.chances = 5;	
		d.group = "weapons2";	
		dropRandom.add(d);	
			
		d = new SalvageEntityGenDataSpec.DropData();	
		//d.chances = 100;	
		d.group = "basic";	
		d.value = 10000;	
		dropValue.add(d);
                
                d = new SalvageEntityGenDataSpec.DropData();
		d.chances = 1;
		d.group = "techmining_first_find";
		dropRandom.add(d);
		
		CargoAPI result = SalvageEntity.generateSalvage(Misc.getRandom(new Random().nextLong(), 11), 1f, 1f, params.get(0).getFloat(memoryMap), 1f, dropValue, dropRandom);
		
		FactionAPI pf = Global.getSector().getPlayerFaction();
		OUTER: for (CargoStackAPI stack : result.getStacksCopy()) {
			if (stack.getPlugin() instanceof BlueprintProviderItem) {
				BlueprintProviderItem bp = (BlueprintProviderItem) stack.getPlugin();
				List<String> list = bp.getProvidedShips();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsShip(id)) continue OUTER;
					}
				}
				
				list = bp.getProvidedWeapons();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsWeapon(id)) continue OUTER;
					}
				}
				
				list = bp.getProvidedFighters();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsFighter(id)) continue OUTER;
					}
				}
				
				list = bp.getProvidedIndustries();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsIndustry(id)) continue OUTER;
					}
				}
				result.removeStack(stack);
			} else if (stack.getPlugin() instanceof ModSpecItemPlugin) {
				ModSpecItemPlugin mod = (ModSpecItemPlugin) stack.getPlugin();
				if (!pf.knowsHullMod(mod.getModId())) continue OUTER;
				result.removeStack(stack);
			}
		}
                if (!Misc.playerHasStorageAccess(dialog.getInteractionTarget().getMarket())) {
                    ((StoragePlugin)dialog.getInteractionTarget().getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
                    dialog.getTextPanel().setFontSmallInsignia();dialog.getTextPanel().addPara("You have been granted free access to "+dialog.getInteractionTarget().getMarket().getName()+"'s storage", Misc.getHighlightColor());dialog.getTextPanel().setFontInsignia();
                }
                if (Misc.playerHasStorageAccess(dialog.getInteractionTarget().getMarket())) {
                    dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate().set("$cum_ttt_tradeagain", true, 90f);
                    Misc.getStorageCargo(dialog.getInteractionTarget().getMarket()).addAll(result, true);
                }
		return true;
	}
}

