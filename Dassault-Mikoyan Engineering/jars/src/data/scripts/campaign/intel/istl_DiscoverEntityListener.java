/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener;
import data.campaign.ids.istl_Tags;
import org.apache.log4j.Logger;

/**
 *
 * @author HarmfulMechanic
 */
public class istl_DiscoverEntityListener implements DiscoverEntityListener {

    public static final Logger LOG = Global.getLogger(istl_DiscoverEntityListener.class);
    
    @Override
    public void reportEntityDiscovered(SectorEntityToken entity) {

        if (entity.hasTag(istl_Tags.HARDENED_WARNING_BEACON)) {
    
            istl_BreakerBeaconIntel intel = new istl_BreakerBeaconIntel(entity);
            Global.getSector().getIntelManager().addIntel(intel);
        }
    }

}
