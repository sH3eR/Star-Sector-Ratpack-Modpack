package data.scripts.vice.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;

import data.scripts.vice.util.NameListUtil;

public class ETReputationListener extends BaseCampaignEventListener {
	
	public ETReputationListener() {
		super(true);
	}
	
	@Override
	public void reportPlayerReputationChange(String faction, float delta) {
		SectorAPI sector = Global.getSector();
		
		//Give Proteus carrier for turning in pk to Persean League
		if (faction.equals(Factions.PERSEAN)) {
			if (sector.getPlayerMemoryWithoutUpdate().is("$receivedShipPL", true)) {
				giveProteusPL();
				sector.getPlayerMemoryWithoutUpdate().set("$receivedShipPL", false);
			}
		}
	}
	
	private void giveProteusPL() {
		ShipVariantAPI v = Global.getSettings().getVariant("vice_proteus_command").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName(NameListUtil.PLS_LIBERATOR);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	}
}