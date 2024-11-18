package data.scripts.ix.listeners;

import java.util.List;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.util.NameListUtil;

public class IXReputationListener extends BaseCampaignEventListener {
	
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String MZ_FAC_ID = "ix_marzanna";
	private static String TW_FAC_ID = "ix_trinity";
	private static String IX_ADMIN_SKILL_ID = "ix_ai_assisted_command";
	
	public IXReputationListener() {
		super(true);
	}
	
	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		if (market == null) return;
		//transfers reconquered original colonies between IX Battlegroup, Marzanna Cartel, Trinity Worlds
		if (market.hasIndustry("ix_marzanna_base")) {
			if (market.getFactionId().equals(IX_FAC_ID) || market.getFactionId().equals(TW_FAC_ID)) {
				market.setFactionId(IX_FAC_ID);
				market.getPlanetEntity().setFaction(MZ_FAC_ID);
				for (int i = 0; i < market.getPeopleCopy().size(); i++) {
					PersonAPI p = (PersonAPI) market.getPeopleCopy().get(i);
					if (p.getFaction().getId().equals(IX_FAC_ID)) p.setFaction(MZ_FAC_ID);
				}
			}
		}
		
		else if (market.getFaction().equals(IX_FAC_ID) && getOriginalMarketOwner(market).equals(TW_FAC_ID)) {
			market.setFactionId(TW_FAC_ID);
			market.getPlanetEntity().setFaction(TW_FAC_ID);
		}
		
		else if (market.getFaction().equals(TW_FAC_ID) && getOriginalMarketOwner(market).equals(IX_FAC_ID)) {
			market.setFactionId(IX_FAC_ID);
			market.getPlanetEntity().setFaction(IX_FAC_ID);
		}
	}
	
	private static String getOriginalMarketOwner(MarketAPI market) {
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		if (mem.contains("$startingFactionId")) {
			return mem.getString("$startingFactionId");
		}
		else return "none";
	}
	
	@Override
	public void reportPlayerReputationChange(String faction, float delta) {
		SectorAPI sector = Global.getSector();
		
		//Give ship for turning in pk to IX Battlegroup
		if (faction.equals(IX_FAC_ID)) {
			if (sector.getPlayerMemoryWithoutUpdate().is("$receivedHyperionIX", true)) {
				giveHyperionIX();
				sector.getPlayerMemoryWithoutUpdate().set("$receivedHyperionIX", false);
			}
			if (sector.getPlayerMemoryWithoutUpdate().is("$receivedRadiantIX", true)) {
				giveRadiantIX();
				sector.getPlayerMemoryWithoutUpdate().set("$receivedRadiantIX", false);
			}
		}
		
		//Give ship for turning in pk to Trinity Worlds
		if (faction.equals(TW_FAC_ID)) {
			if (sector.getPlayerMemoryWithoutUpdate().is("$receivedShipTW", true)) {
				giveRadiantTW();
				sector.getPlayerMemoryWithoutUpdate().set("$receivedShipTW", false);
			}
		}
		
		//Give skill and shuttle for turning in pk to IX/Trinity Worlds, AI core handled by rules.csv
		if (sector.getPlayerMemoryWithoutUpdate().is("$receivedAdminTraining", true)) {
			giveSkillAndShuttle();
			sector.getPlayerMemoryWithoutUpdate().set("$receivedAdminTraining", false);
		}
	}
	
	private void giveHyperionIX() {
		ShipVariantAPI v = Global.getSettings().getVariant("hyperion_ix_special").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName(NameListUtil.HGS_Judicator);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	}
	
	private void giveRadiantIX() {
		ShipVariantAPI v = Global.getSettings().getVariant("radiant_ix_custom_2").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName(NameListUtil.HGS_Judicator);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	}

	private void giveRadiantTW() {
		ShipVariantAPI v = Global.getSettings().getVariant("radiant_tw_heavy").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName(NameListUtil.TWC_Kupala);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	}
	
	private void giveSkillAndShuttle() {
		PersonAPI player = Global.getSector().getPlayerPerson();
		player.getStats().setSkillLevel(IX_ADMIN_SKILL_ID, 1f);
		
		ShipVariantAPI v = Global.getSettings().getVariant("kite_original_Stock").clone();
		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
		member.setShipName(NameListUtil.ISS_Kupala);
		Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
	}

	public static void setIXHostileToAll() {
		//runs once
		if (Global.getSector().getPlayerMemoryWithoutUpdate().is("$reputationIsSetIX", true)) return;
		SectorAPI sector = Global.getSector();
		FactionAPI ix = sector.getFaction(IX_FAC_ID);
		FactionAPI hg = sector.getFaction("ix_core");
		FactionAPI marzanna = sector.getFaction(MZ_FAC_ID);
		FactionAPI trinity = sector.getFaction(TW_FAC_ID);
		FactionAPI hvb = sector.getFaction("ix_remnant_hvb");
		FactionAPI exo = sector.getFaction("rat_exotech");
		FactionAPI bw = sector.getFaction("tahlan_legioelite");
		FactionAPI player = sector.getFaction("player");
		FactionAPI pirates = sector.getFaction(Factions.PIRATES);
		FactionAPI indep = sector.getFaction(Factions.INDEPENDENT);
		FactionAPI brighton = sector.getFaction("brighton");
		FactionAPI herd = sector.getFaction("magellan_theherd");
		
		List<FactionAPI> factionList = sector.getAllFactions();
		factionList.remove(ix);
		factionList.remove(hg);
		factionList.remove(marzanna);
		factionList.remove(trinity);
		factionList.remove(hvb);
		factionList.remove(player);
		factionList.remove(exo);
		factionList.remove(bw);
		factionList.remove(pirates);
		factionList.remove(indep);
		factionList.remove(herd);
		
		ix.setRelationship(marzanna.getId(), 1f);
		trinity.setRelationship(ix.getId(), 1f);
		trinity.setRelationship(hg.getId(), 1f);
		trinity.setRelationship(marzanna.getId(), 1f);
		hvb.setRelationship(ix.getId(), -1f);
		hvb.setRelationship(trinity.getId(), -1f);
		hvb.setRelationship(player.getId(), -1f);
		
		//if no background setting to 0.5 start
		if (ix.getRelationship(player.getId()) < 0.40f) {
			ix.setRelationship(player.getId(), -0.5f);
			marzanna.setRelationship(player.getId(), -0.5f);
			trinity.setRelationship(player.getId(), -0.5f);
		}
				
		//need to do here since faction is hidden at start of game
		if (Global.getSettings().getModManager().isModEnabled("secretsofthefrontier")) {
				ix.setRelationship("sotf_dustkeepers", -0.50f);
				trinity.setRelationship("sotf_dustkeepers", -0.50f);
				marzanna.setRelationship("sotf_dustkeepers", -0.50f);
		}
		
		if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
			if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_pirate_peace")) {
				indep.setRelationship("pirates", -0.25f);
				if (Misc.getCommissionFactionId() != null) {
					String commissionId = Misc.getCommissionFactionId();
					if (commissionId.equals("pirates")) indep.setRelationship("player", -0.25f);
				}
			}
		}
		
		if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
			if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_brighton_neutral")) {
				ix.setRelationship("brighton", 0f);
				trinity.setRelationship("brighton", 0f);
				marzanna.setRelationship("brighton", 0f);
				factionList.remove(brighton);
			}
			else {
				ix.setRelationship("brighton", -0.50f);
				trinity.setRelationship("brighton", -0.50f);
				marzanna.setRelationship("brighton", -0.50f);
			}
		}
		
		for (FactionAPI faction : factionList) {
			if (!faction.isShowInIntelTab()) continue;
			if (ix.getRelationship(faction.getId()) > -0.50f) {
				ix.setRelationship(faction.getId(), -0.50f);
				trinity.setRelationship(faction.getId(), -0.50f);
				marzanna.setRelationship(faction.getId(), -0.50f);
				if (ix.getRelationship("player") > 0.51 || trinity.getRelationship("player") > 0.51) {
					player.setRelationship(faction.getId(), -0.50f);
				}
			}
			hvb.setRelationship(faction.getId(), -1f);
		}
		
		if (Misc.getCommissionFactionId() != null) {
			String commissionId = Misc.getCommissionFactionId();
			ix.setRelationship("player", ix.getRelationship(commissionId));
			hg.setRelationship("player", ix.getRelationship(commissionId));
			marzanna.setRelationship("player", ix.getRelationship(commissionId));
			trinity.setRelationship("player", trinity.getRelationship(commissionId));
			if (commissionId.equals(IX_FAC_ID)) {
				trinity.setRelationship(IX_FAC_ID, 0.75f);
				trinity.setRelationship("player", 0.75f);
				ix.setRelationship("player", 0.60f);
				marzanna.setRelationship("player", 0.60f);
			}
			else if (commissionId.equals(TW_FAC_ID)) {
				ix.setRelationship(TW_FAC_ID, 0.75f);
				ix.setRelationship("player", 0.75f);
				marzanna.setRelationship("player", 0.75f);
				trinity.setRelationship("player", 0.60f);
			}
			else ix.setRelationship(TW_FAC_ID, 1f);
		}
		
		Global.getSector().getPlayerMemoryWithoutUpdate().set("$reputationIsSetIX", true);
	}
}