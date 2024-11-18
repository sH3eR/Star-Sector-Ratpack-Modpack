package scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;

import java.util.Objects;
import java.util.Random;

public class MusicSwitcherPlugin extends MusicPlayerPluginImpl
{
    protected String CheckFactionid = null;
    protected String CheckMusicOption = null;
    protected Random coin = new Random();
    protected Boolean flip = coin.nextBoolean();

    protected String CombineFactionid(String Factionid)
    //Combines faction ids for grouped music options
    {
        switch (Factionid)
        {
            case "knights_of_ludd":
            case "luddic_church":
                Factionid = "luddic_church";
                break;
            case "lions_guard":
            case "sindrian_diktat":
                Factionid = "sindrian_diktat";
                break;
            case "player":
            case "hegemony":
            case "luddic_path":
            case "persean":
            case "remnant":
            case "omega":
            case "pirates":
            case "tritachyon":
                break;
            case "independent":
            case "derelict":
            case "scavengers":
            case "neutral":
            case "sleeper":
            case "poor":
                Factionid = "default";
                break;
            default:
                //mod faction
                Factionid = "other_faction";
                break;
        }
        System.out.println("faction id combined into " + Factionid);
        return Factionid;
    }

    protected String SetMusic(String MusicName, String MusicOption)
    //For non-faction non-combat music
    {
        if (MusicName != null && !MusicName.equals("music_none") && MusicName.startsWith("music"))
        {
            switch (MusicOption)
            {
                case "Custom":
                    System.out.println("custom, music was " + MusicName);
                    return "Custom_" + MusicName;
                case "Both":
                    flip = coin.nextBoolean();
                    System.out.println("flip, music was " + MusicName);
                    return (flip) ? ("Custom_" + MusicName) : (MusicName);
                case "Vanilla":
                default:
                    //MusicOption null
                    System.out.println("default music set as " + MusicName);
                    return MusicName;
            }
        }
        System.out.println("set music failed, music currently is " + MusicName);
        return MusicName;
    }

    protected String SetFactionMusicType(String FactionMusicType, String MusicOption)
    {
        System.out.println("faction music type was " + FactionMusicType);
        if (MusicOption != null)
        {
            System.out.println("MusicOption not null");
            switch (MusicOption)
            {
                case "Custom":
                    FactionMusicType = "Custom_" + FactionMusicType;
                    break;
                case "Both":
                    flip = coin.nextBoolean();
                    FactionMusicType = (flip) ? FactionMusicType : "Custom_" + FactionMusicType;
                    break;
                case "Vanilla":
                default:
                    //MusicOption null, or somehow faction id didn't combine to it
                    break;
            }
            System.out.println("faction music type now set as " + FactionMusicType);
            return FactionMusicType;
        }
        System.out.println("MusicOption null");
        return null;
    }

    public String getMusicSetIdForCombat(CombatEngineAPI engine)
    {
        String musicSetId = super.getMusicSetIdForCombat(engine);
        if (engine.isInCampaign())
        {
            FactionAPI faction = engine.getContext().getOtherFleet().getFaction();
            CheckFactionid = CombineFactionid(faction.getId());
            CheckMusicOption = LunaSettings.getString("Music_Switcher", CheckFactionid + "_combat_Music");
            if (CheckMusicOption != null)
            {
                if (!Objects.equals(CheckFactionid, "other_faction"))

                {
                    System.out.println("combat not other faction");
                    switch (CheckMusicOption)
                    {
                        case "Faction Specific":
                            musicSetId = faction.getMusicMap().get("Combat");
                            break;
                        case "Generic Combat Music":
                            musicSetId = "Custom_music_combat";
                            break;
                        case "Faction + Generic Shuffle":
                            flip = coin.nextBoolean();
                            musicSetId = (flip) ? "Custom_music_combat" : faction.getMusicMap().get("Combat");
                            break;
                        case "Entire Sample Playlist":
                            musicSetId = "All_music_combat";
                            break;
                        case "Vanilla":
                        default:
                            //MusicOption null, or somehow faction id didn't combine to it
                            break;
                    }
                }
                else  // third party mod faction
                {
                    switch (CheckMusicOption)
                    {
                        case "Faction + Generic Shuffle":
                            flip = coin.nextBoolean();
                            if (faction.getMusicMap().get("combat") != null)
                            {musicSetId = (flip) ? "Custom_music_combat" : faction.getMusicMap().get("Combat");}
                            else {musicSetId = "Custom_music_combat";}
                            break;
                        case "Generic Combat Music":
                            musicSetId = "Custom_music_combat";
                            break;
                        case "Faction Specific":
                            if (faction.getMusicMap().get("Combat") != null)
                            {musicSetId = faction.getMusicMap().get("Combat");}
                            else {musicSetId = "Custom_music_combat";}
                            break;
                        case "Entire Sample Playlist":
                            musicSetId = "All_music_combat";
                            break;
                        case "Vanilla":
                        default:
                            //MusicOption for "other_faction" null, or somehow faction id didn't combine to it
                            break;
                    }
                }
            }
        }
        else
        {//not in campaign, is in simulation or arcade
            CheckMusicOption = LunaSettings.getString("Music_Switcher", "default_combat_Music");
            switch (CheckMusicOption)
            {
                case "Faction + Generic Shuffle":
                case "Generic Combat Music":
                case "Faction Specific":
                    musicSetId = "Custom_music_combat";
                    break;
                case "Entire Sample Playlist":
                    musicSetId = "All_music_combat";
                    break;
                case "Vanilla":
                default:
                    //MusicOption is sth else
                    musicSetId = null;
                    break;
            }
        }
        if (musicSetId != null)
        {
            System.out.println("combat music is now " + musicSetId);
            return musicSetId;
        }
        System.out.println("combat super");
        return super.getMusicSetIdForCombat(engine);
    }

    public String getMusicSetIdForTitle()
    {
        return SetMusic("music_title", LunaSettings.getString("Music_Switcher", "Menu_Music"));
    }

    protected String getPlanetSurveyMusicSetId(Object param)
    {
        return SetMusic("music_survey_and_scavenge", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
    }

    protected String getHyperspaceMusicSetId()
    {
        return SetMusic("music_campaign_hyperspace", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
    }

    protected String getStarSystemMusicSetId()

    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet.getContainingLocation() instanceof StarSystemAPI)
        {
            StarSystemAPI system = (StarSystemAPI) playerFleet.getContainingLocation();
            String musicSetId = system.getMemoryWithoutUpdate().getString(MUSIC_SET_MEM_KEY);
            if (musicSetId != null) return musicSetId;

            if (system.hasTag(Tags.THEME_CORE) || !Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty())
            {
                return SetMusic("music_campaign", LunaSettings.getString("Music_Switcher", "Campaign_Music"));
            }
        }

        return SetMusic("music_campaign_non_core", LunaSettings.getString("Music_Switcher", "Campaign_Music"));
    }

    protected String getEncounterMusicSetId(Object param)
    {
        if (param instanceof SectorEntityToken)
        {
            SectorEntityToken token = (SectorEntityToken) param;

            String musicSetId = token.getMemoryWithoutUpdate().getString(MUSIC_SET_MEM_KEY);
            if (musicSetId != null) return musicSetId;

            if (Entities.ABYSSAL_LIGHT.equals(token.getCustomEntityType()))
            {
                return SetMusic("music_encounter_neutral", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }
            if (Entities.CORONAL_TAP.equals(token.getCustomEntityType()))
            {
                return SetMusic("music_encounter_mysterious", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }
            if (Entities.WRECK.equals(token.getCustomEntityType()))
            {
                return SetMusic("music_encounter_neutral", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }
            if (Entities.DERELICT_GATEHAULER.equals(token.getCustomEntityType()))
            {
                return SetMusic("music_encounter_mysterious_non_aggressive", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }

            if (Entities.DEBRIS_FIELD_SHARED.equals(token.getCustomEntityType()))
            {
                return SetMusic("music_survey_and_scavenge", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }
            if (token.hasTag(Tags.GATE))
            {
                return SetMusic("music_encounter_neutral", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }
            if (token.hasTag(Tags.SALVAGEABLE))
            {
                if (token.getMemoryWithoutUpdate() != null && token.getMemoryWithoutUpdate().getBoolean("$hasDefenders"))
                {
                    if (token.getMemoryWithoutUpdate().getBoolean("$limboMiningStation"))
                    {
                        return SetMusic("music_encounter_mysterious", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
                    }
                    if (token.getMemoryWithoutUpdate().getBoolean("$limboWormholeCache"))
                    {
                        return SetMusic("music_encounter_mysterious", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
                    }
                    return SetMusic("music_encounter_neutral", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
                }
                return SetMusic("music_survey_and_scavenge", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }
            if (token.hasTag(Tags.SALVAGE_MUSIC))
            {
                return SetMusic("music_survey_and_scavenge", LunaSettings.getString("Music_Switcher", "Exploration_Music"));
            }

            if (token.getFaction() != null)
            {
                FactionAPI faction = (FactionAPI) token.getFaction();
                String type = null;
                //MemoryAPI mem = token.getMemoryWithoutUpdate();
                boolean hostile = false;
                boolean knowsWhoPlayerIs = false;
                if (token instanceof CampaignFleetAPI)
                {
                    CampaignFleetAPI fleet = (CampaignFleetAPI) token;
                    if (fleet.getAI() instanceof ModularFleetAIAPI)
                    {
                        hostile = ((ModularFleetAIAPI) fleet.getAI()).isHostileTo(Global.getSector().getPlayerFleet());
                    }
                    knowsWhoPlayerIs = fleet.knowsWhoPlayerIs();
                }

                if (faction.isAtWorst(Factions.PLAYER, RepLevel.FAVORABLE) && knowsWhoPlayerIs && !hostile)
                {
                    type = "encounter_friendly";
                }
                else if ((faction.isAtBest(Factions.PLAYER, RepLevel.SUSPICIOUS) && knowsWhoPlayerIs) || hostile)
                {
                    type = "encounter_hostile";
                }
                else
                {
                    type = "encounter_neutral";
                }
                CheckFactionid = CombineFactionid(faction.getId());
                CheckMusicOption = LunaSettings.getString("Music_Switcher", CheckFactionid + "_Music");
                if (!Objects.equals(CheckFactionid, "other_faction"))
                {
                    type = SetFactionMusicType(type, CheckMusicOption);
                    musicSetId = faction.getMusicMap().get(type);
                }
                else
                {
                    System.out.println("other faction");
                    switch (CheckMusicOption)
                    {
                        case "Custom":
                            return "Custom_music_default_" + type;
                        case "Both":
                            flip = coin.nextBoolean();
                            return (flip) ? "Custom_music_default_" + type : "music_default_" + type;
                        case "Vanilla":
                        default:
                            //MusicOption for "other_faction" null, or somehow faction id didn't combine to it
                            break;
                    }
                }
                if (musicSetId != null)
                {
                    System.out.println("encounter music is now " + musicSetId);
                    return musicSetId;
                }
            }
        }
        System.out.println("encounter super");
        return super.getEncounterMusicSetId(param);
    }

    protected String getMarketMusicSetId(Object param)
    {
        if (param instanceof MarketAPI)
        {
            MarketAPI market = (MarketAPI) param;
            String musicSetId = market.getMemoryWithoutUpdate().getString(MUSIC_SET_MEM_KEY);
            if (musicSetId != null) return musicSetId;

            if (market.getPrimaryEntity() != null && market.getPrimaryEntity().getMemoryWithoutUpdate().getBoolean("$abandonedStation"))
            {
                return getPlanetSurveyMusicSetId(param);
            }

            FactionAPI faction = market.getFaction();
            if (faction != null)
            {
                String type = null;
                if (faction.isAtWorst(Factions.PLAYER, RepLevel.FAVORABLE))
                {
                    type = "market_friendly";
                }
                else if (faction.isAtBest(Factions.PLAYER, RepLevel.SUSPICIOUS))
                {
                    type = "market_hostile";
                }
                else
                {
                    type = "market_neutral";
                }
                CheckFactionid = CombineFactionid(faction.getId());
                CheckMusicOption = LunaSettings.getString("Music_Switcher", CheckFactionid + "_Music");
                if (!Objects.equals(CheckFactionid, "other_faction"))
                {
                    type = SetFactionMusicType(type, CheckMusicOption);
                    musicSetId = faction.getMusicMap().get(type);
                }
                else
                {
                    System.out.println("other faction");
                    switch (CheckMusicOption)
                    {
                        case "Custom":
                            return "Custom_music_default_" + type;
                        case "Both":
                            flip = coin.nextBoolean();
                            return (flip) ? "Custom_music_default_" + type : "music_default_" + type;
                        case "Vanilla":
                        default:
                            //MusicOption for "other_faction" null, or somehow faction id didn't combine to it
                            break;
                    }
                }
                if (musicSetId != null)
                {
                    System.out.println("market music is now " + musicSetId);
                    return musicSetId;
                }
            }
        }
        System.out.println("market super");
        return super.getMarketMusicSetId(param);
    }
}