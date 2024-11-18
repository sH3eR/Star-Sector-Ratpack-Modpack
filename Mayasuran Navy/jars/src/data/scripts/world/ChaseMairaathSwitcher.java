package data.scripts.world;

// bye vayra
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.MN_modPlugin;
import exerelin.ExerelinConstants;
import exerelin.campaign.ColonyManager;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.SectorManager;
import exerelin.utilities.NexUtilsReputation;

public class ChaseMairaathSwitcher implements EveryFrameScript {

    public static final String MAYASURA = "mayasura";
    public static final String MAIRAATH = "mairaath";
	public static final String MAIRAATH_ABANDONED_STATION = "mairaath_abandoned_station1";

    private boolean done = false;
    private boolean setRep = false;
    private SectorEntityToken mairaathEntity = null;
    private SectorEntityToken mairaath_abandoned_stationEntity = null;
    private MarketAPI market = null;

    @Override
    public void advance(float amount) {
        if (!done) {
            mairaathEntity = Global.getSector().getEntityById(MAIRAATH);
			mairaath_abandoned_stationEntity = Global.getSector().getEntityById(MAIRAATH_ABANDONED_STATION);
			
			

            if (!setRep) {
                FactionAPI mayasura = Global.getSector().getFaction(MAYASURA);
                SharedData.getData().getPersonBountyEventData().addParticipatingFaction(MAYASURA);

                // friends
                mayasura.setRelationship("persean", 1f);         // always together, friends 'til forever
                mayasura.setRelationship("kadur_remnant", 0.2f);        // we both saw our homes destroyed by the Hegemony

                // neutral
                mayasura.setRelationship("sindrian_diktat", 0f);        // my enemy's enemy is awfully close to my enemy             

                // enemies
                mayasura.setRelationship("hegemony", -1f);              // death to the hegemon
                mayasura.setRelationship("luddic_church", -1f);         // you let it happen
                mayasura.setRelationship("luddic_path", -1f);           // unforgivable

                setRep = true;
            }

            if (mairaathEntity != null) {
				mairaathEntity.setCustomDescriptionId("planet_mairaath_mayasura");
                market = mairaathEntity.getMarket();
            }
			if (mairaath_abandoned_stationEntity != null) {
				mairaath_abandoned_stationEntity.setCustomDescriptionId("mairaath_abandoned_station");
                MarketAPI stationMarket = mairaath_abandoned_stationEntity.getMarket();
                if(stationMarket != null && stationMarket.hasSubmarket(Submarkets.SUBMARKET_STORAGE)){
                    stationMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE).setFaction( Global.getSector().getFaction("mayasura"));
                }

			}
            if (market != null && !market.isPlanetConditionMarketOnly() && !market.isPlayerOwned()) {
                mairaathEntity.setFaction(MAYASURA);
                market.setFactionId(MAYASURA);
                market.removeSubmarket(Submarkets.SUBMARKET_OPEN);
                market.addSubmarket(Submarkets.SUBMARKET_OPEN);
                market.addSubmarket(Submarkets.GENERIC_MILITARY);
                market.addIndustry(Industries.HEAVYINDUSTRY);
                market.addIndustry(Industries.MILITARYBASE);
                market.addIndustry(Industries.GROUNDDEFENSES);
				market.addIndustry("elitemayasuranguard");
                market.getMemoryWithoutUpdate().set("$startingFactionId", MAYASURA);

                for (CommDirectoryEntryAPI dir : market.getCommDirectory().getEntriesCopy()) {
                    if (dir.getType() != CommDirectoryEntryAPI.EntryType.PERSON) {
                        continue;
                    }
                    PersonAPI person = (PersonAPI) dir.getEntryData();
                    person.setFaction(MAYASURA);
                }
				
				// Let player govern Mairaath
				if (MN_modPlugin.isExerelin) {
					market.getMemoryWithoutUpdate().set(ExerelinConstants.MEMKEY_MARKET_STARTING_FACTION, market.getFactionId());
                    market.getMemoryWithoutUpdate().set("$startingFactionId", MAYASURA);
					if (MAYASURA.equals(PlayerFactionStore.getPlayerFactionIdNGC())) {
						market.setPlayerOwned(true);
						SectorManager.updateSubmarkets(market, Factions.PLAYER, Factions.PLAYER);
						FactionAPI player = Global.getSector().getPlayerFaction();
						ColonyManager.reassignAdminIfNeeded(market, player, player);
						NexUtilsReputation.syncPlayerRelationshipsToFaction();
					}

				}

                done = true;
            }
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

}
