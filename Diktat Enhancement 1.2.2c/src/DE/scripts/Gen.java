package DE.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import DE.scripts.world.systems.DE_Hesiod;
import DE.scripts.world.systems.DE_Valhalla;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import DE.scripts.world.systems.DE_Andor;
import DE.scripts.world.systems.DE_Askonia;

import java.util.ArrayList;
import static DE.scripts.DE_ModPlugin.DEenablelitemode;
import static DE.scripts.DE_ModPlugin.DEremovenondiktatfeatures;



public class Gen implements SectorGeneratorPlugin {

    private static final Logger log = Global.getLogger(Gen.class);
    //import static data.scripts.Gen.addMarketplace;
    //Shorthand function for adding a market - used in place of economy.json so I can restrict them with the boolean
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
                                           int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, ArrayList<String> industries, float tarrif,
                                           boolean freePort, boolean withJunkAndChatter) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", tarrif);

        //Adds submarkets
        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        //Adds market conditions
        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        //Add market industries
        for (String industry : industries) {
            newMarket.addIndustry(industry);
        }

        //Sets us to a free port, if we should
        newMarket.setFreePort(freePort);

        //Adds our connected entities, if any
        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, withJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        //Finally, return the newly-generated market
        return newMarket;
    }

    @Override
    public void generate(SectorAPI sector) {

        new DE_Askonia().generate(sector);
        if (!DEenablelitemode) {
            new DE_Andor().generate(sector);
            new DE_Hesiod().generate(sector);
            if (!DEremovenondiktatfeatures) {
                new DE_Valhalla().generate(sector);
            }
        }
    }

}




