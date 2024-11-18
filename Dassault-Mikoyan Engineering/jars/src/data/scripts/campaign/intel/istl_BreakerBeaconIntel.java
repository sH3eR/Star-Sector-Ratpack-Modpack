/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.intel;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.WarningBeaconIntel;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.istl_Tags;

/**
 *
 * @author HarmfulMechanic
 */
public class istl_BreakerBeaconIntel extends WarningBeaconIntel {

    public istl_BreakerBeaconIntel(SectorEntityToken beacon) {
        super(beacon);

    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        Description desc = Global.getSettings().getDescription("istl_bladebreaker_beacon", Type.CUSTOM);
        info.addPara(desc.getText1FirstPara(), opad);

        addBulletPoints(info, ListInfoMode.IN_DESC);

        if (beacon.isInHyperspace()) {
            StarSystemAPI system = Misc.getNearbyStarSystem(beacon, 1f);
            if (system != null) {
                info.addPara("This beacon is located near the " + system.getNameWithLowercaseType()
                        + ", warning of a Blade Breaker presence within.", opad);

            }
        }
    }

	@Override
	public String getIcon() {
		if (isLow()) {
			return Global.getSettings().getSpriteName("intel", "istl_hardenedbeacon_low");
		} else if (isMedium()) {
			return Global.getSettings().getSpriteName("intel", "istl_hardenedbeacon_medium");
		} else if (isHigh()) {
			return Global.getSettings().getSpriteName("intel", "istl_hardenedbeacon_high");
		}
		return Global.getSettings().getSpriteName("intel", "istl_hardenedbeacon_low");
	}

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BEACON);
        tags.add(istl_Tags.BREAKER_FACTION);
        return tags;
    }
    
    	public String getName() {
//		if (isLow()) {
//			return "Hardened Warning Beacon (Low)";
//		} else if (isMedium()) {
//			return "Hardened Warning Beacon (Medium)";
//		} else if (isHigh()) {
//			return "Hardened Warning Beacon (High)";
//		}
		return "Hardened Warning Beacon";
	}
        
}
