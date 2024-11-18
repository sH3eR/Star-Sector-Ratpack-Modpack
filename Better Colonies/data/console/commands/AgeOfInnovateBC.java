package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.misc.bc_SPNotification;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.GDModPlugin.SupportedFaction;
import static data.scripts.GDModPlugin.SupportedIndustries;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.lazywizard.lazylib.CollectionUtils;

public class AgeOfInnovateBC implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if (!context.isInCampaign())
        {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (!args.isEmpty())
        {
			try
			{
				Float.parseFloat(args);
			}
			catch (NumberFormatException ex)
			{
				return CommandResult.BAD_SYNTAX;
			}
            
        }
        
        if (SupportedFaction != null) {
                if (Misc.getRandom(Misc.genRandomSeed(), 1).nextFloat() <= (args.isEmpty() ? Global.getSettings().getFloat("BCAgeOfInnovation") : Float.parseFloat(args))) {
                    List<String> text = new ArrayList<String>();
                    List<Color> factionColor = new ArrayList<Color>();
                    List<String> highlighttext = new ArrayList<String>();
                    List<String> highlighttext2 = new ArrayList<String>();
                    String date = Global.getSector().getClock().getMonthString()+" "+Global.getSector().getClock().getCycleString();
                    if (Global.getSettings().getBoolean("BCIgnoreFactionWhiteflag")) {
                        for (FactionAPI faction : Global.getSector().getAllFactions()) {
							if (Global.getSector().getPlayerFaction() == faction) continue;
                            for (MarketAPI market : Misc.getFactionMarkets(faction.getId())) {
                                if (market != null && Misc.getRandom(Misc.genRandomSeed(), 1).nextFloat() <= (Global.getSector().getFaction(faction.getId()).getCustomBoolean("decentralized") ? Global.getSettings().getFloat("BCInnovatedDecentralized") : Global.getSettings().getFloat("BCInnovatedCentralized"))*market.getIndustries().size()) {
                                    if (Misc.getRandom(Misc.genRandomSeed(), 1).nextFloat() <= 1/Math.pow(Global.getSettings().getFloat("BCSPIndustryMultiplier"), Misc.getNumImprovedIndustries(market))) {
                                        WeightedRandomPicker<String> AmongUs = new WeightedRandomPicker<String>();
                                        for (Industry industry : market.getIndustries()) {
                                            if (industry.isImproved()) continue;
                                            if (industry.canImprove() && industry.isFunctional() && (!industry.isUpgrading() || industry.getId().equals(Industries.POPULATION)) && !industry.isHidden() && !industry.isDisrupted()) {
                                                float weight = 1f;
                                                if (SupportedIndustries != null && SupportedIndustries.get(industry.getId()) != null) {weight = (Float) SupportedIndustries.get(industry.getId());}
                                                AmongUs.add(industry.getId(), weight);
                                            }
                                        }
                                        String industryid = (String) AmongUs.pick();
                                        if (industryid != null) {market.getIndustry(industryid).setImproved(true);
                                            if (Global.getSettings().getBoolean("BCIntelText") && !market.isHidden()) {
                                                text.add(String.format(Global.getSettings().getString("bettercolonies", "SPDescription"), market.getName(), market.getIndustry(industryid).getCurrentName()));
                                                factionColor.add(market.getFaction().getColor());
                                                highlighttext.add(market.getName());
                                                highlighttext2.add(market.getIndustry(industryid).getCurrentName());
                                            }
                                        }
                                   }
                                } 
                            }
                        }
                        if (!text.isEmpty()) {
                            bc_SPNotification intel = new bc_SPNotification(String.format(Global.getSettings().getString("bettercolonies", "SPTitle"), date), date, text, factionColor, highlighttext, highlighttext2);
                            Global.getSector().getIntelManager().addIntel(intel, false);
                            Global.getSector().addScript(intel);
                        }
                    } else {
                        for (String factionid : SupportedFaction) {
                             for (MarketAPI market : Misc.getFactionMarkets(factionid)) {
                                if (market != null && Misc.getRandom(Misc.genRandomSeed(), 1).nextFloat() <= (Global.getSector().getFaction(factionid).getCustomBoolean("decentralized") ? Global.getSettings().getFloat("BCInnovatedDecentralized") : Global.getSettings().getFloat("BCInnovatedCentralized"))*market.getIndustries().size()) {
                                    if (Misc.getRandom(Misc.genRandomSeed(), 1).nextFloat() <= 1/Math.pow(Global.getSettings().getFloat("BCSPIndustryMultiplier"), Misc.getNumImprovedIndustries(market)+1)) {
                                        WeightedRandomPicker<String> AmongUs = new WeightedRandomPicker<String>();
                                        for (Industry industry : market.getIndustries()) {
                                            if (industry.isImproved()) continue;
                                            if (industry.canImprove() && industry.isFunctional() && (!industry.isUpgrading() || industry.getId().equals(Industries.POPULATION)) && !industry.isHidden() && !industry.isDisrupted()) {
                                                float weight = 1f;
                                                if (SupportedIndustries != null && SupportedIndustries.get(industry.getId()) != null) {weight = (Float) SupportedIndustries.get(industry.getId());}
                                                AmongUs.add(industry.getId(), weight);
                                            }
                                        }
                                        String industryid = (String) AmongUs.pick();
                                        if (industryid != null) {market.getIndustry(industryid).setImproved(true);
                                            if (Global.getSettings().getBoolean("BCIntelText") && !market.isHidden()) {
                                                text.add(String.format(Global.getSettings().getString("bettercolonies", "SPDescription"), market.getName(), market.getIndustry(industryid).getCurrentName()));
                                                factionColor.add(market.getFaction().getColor());
                                                highlighttext.add(market.getName());
                                                highlighttext2.add(market.getIndustry(industryid).getCurrentName());
                                            }
                                        }
                                   }
                                } 
                            }
                        }
                        if (!text.isEmpty()) {
                            bc_SPNotification intel = new bc_SPNotification(String.format(Global.getSettings().getString("bettercolonies", "SPTitle"), date), date, text, factionColor, highlighttext, highlighttext2);
                            Global.getSector().getIntelManager().addIntel(intel, false);
                            Global.getSector().addScript(intel);
                        }
                    }
                }
            }
        Console.showMessage("Innovating! It's okay if you don't see an intel, it didn't proc at the chance you entered!");
        return CommandResult.SUCCESS;
    }
}
