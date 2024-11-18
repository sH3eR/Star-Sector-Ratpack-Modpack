//Nexerelin disabled version

package data.scripts.ix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lunalib.lunaRefit.LunaRefitManager;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

import data.scripts.ix.IXCampaignPlugin;
import data.scripts.ix.IXSystemCreation;
import data.scripts.ix.listeners.IXAdminEasyModeListener;
import data.scripts.ix.listeners.IXEncounterListener;
import data.scripts.ix.listeners.IXReputationListener; 
import data.scripts.ix.listeners.IXReputationResetListener;
import data.scripts.ix.listeners.PruneHaulerMarketListener;
import data.scripts.ix.listeners.UpgradeFuelProdListener;
import data.scripts.ix.luna.BiochipSotFButton;
import data.scripts.ix.luna.NanoReplicatorButton;
import data.scripts.ix.luna.PanopticCommandRefitButton;
import data.scripts.ix.luna.PanopticStrategicRefitButton;
import data.scripts.ix.luna.PanopticTacticalRefitButton;
import data.scripts.ix.luna.SalvagePanopticonCoreButton;
import data.scripts.ix.util.NameListUtil;

//import exerelin.campaign.AllianceManager;
//import exerelin.campaign.alliances.Alliance;

public class IXModPlugin extends BaseModPlugin implements SectorGeneratorPlugin {

	private static String IX_SKILL_ID = "ix_sword_of_the_fleet";
	private static String IX_ADMIN_SKILL_ID = "ix_ai_assisted_command";
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String MZ_FAC_ID = "ix_marzanna";
	private static String TW_FAC_ID = "ix_trinity";
	private static IXCampaignPlugin pCorePlugin = new IXCampaignPlugin();
			
	@Override
    public void onNewGame() {
		SectorAPI sector = Global.getSector();
        generate(sector);
		sector.getMemoryWithoutUpdate().set("$give_IX_hullmods", true);
        sector.getFaction(IX_FAC_ID).setShowInIntelTab(true);
		
		if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_trinity_enabled")) {
			sector.getFaction(TW_FAC_ID).setShowInIntelTab(true);
			//Alliance alliance = AllianceManager.createAlliance(IX_FAC_ID, TW_FAC_ID, AllianceManager.getBestAlignment(IX_FAC_ID, TW_FAC_ID));
			//alliance.setName(NameListUtil.Core_Consensus);
		}
		else sector.getFaction(TW_FAC_ID).setShowInIntelTab(false);
		
		sector.getPlayerMemoryWithoutUpdate().set("$reputationIsSetIX", false);
		sector.registerPlugin(pCorePlugin);
		sector.getListenerManager().addListener(new IXAdminEasyModeListener());
		sector.getListenerManager().addListener(new IXEncounterListener());
		sector.getListenerManager().addListener(new IXReputationListener());
		sector.getListenerManager().addListener(new IXReputationResetListener());
		sector.getListenerManager().addListener(new PruneHaulerMarketListener());
		sector.getListenerManager().addListener(new UpgradeFuelProdListener());
    }
	
	@Override
	public void onNewGameAfterTimePass() {
		SectorAPI sector = Global.getSector();
		PruneHaulerMarketListener pListener = new PruneHaulerMarketListener();
		for (MarketAPI market : sector.getEconomy().getMarketsCopy()) {
			pListener.pruneMarket(market);
		}
	}
	
	@Override
	public void beforeGameSave() {
		//starting reputation applies only once (happens after initial fleet placement which is not ideal)
		IXReputationListener.setIXHostileToAll();
		
		//doing it here otherwise marzanna personnel faction won't update
		applyMarzannaChanges(Global.getSector());
		
		//prune excess IX freighters from Vertex Station on startup
		if (Global.getSector().getEconomy().getMarket("ix_vertex_market") != null) {
			MarketAPI vertexMarket = Global.getSector().getEconomy().getMarket("ix_vertex_market");
			PruneHaulerMarketListener.pruneMarket(vertexMarket);
		}
		
		//give specific goodies for players that start in faction/as honor guard, runs once
		if (Global.getSector().getMemoryWithoutUpdate().is("$give_IX_hullmods", false)) return;
		String commissionID = Misc.getCommissionFactionId();
		if (commissionID == null) commissionID = "";
		
		List<FleetMemberAPI> fleetList = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
		boolean isIX = IX_FAC_ID.equals(commissionID);
		boolean isHonorGuard = false;
		for (FleetMemberAPI member : fleetList) {
			if (!isIX) continue;
			String id = member.getVariant().getHullVariantId();
			if (id.equals("hyperion_ix_special") 
					|| id.equals("tigershark_ix_custom")
					|| id.equals("odyssey_ix_custom")) isHonorGuard = true;
			if (id.equals("hyperion_ix_special")) {
				Global.getSector().getMemoryWithoutUpdate().set("$hyperion_ix_start", true);
			}
		}
		
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		CharacterDataAPI player = Global.getSector().getCharacterData();
		
		boolean isTrinity = TW_FAC_ID.equals(commissionID);
		if (isTrinity) {
			cargo.addSpecial(new SpecialItemData("ix_biochip_sotf", ""), 1);
			cargo.addSpecial(new SpecialItemData("ix_trinity_package", ""), 1);
			cargo.addSpecial(new SpecialItemData("ix_aux_bp_package", ""), 1);
		}
		
		if (isIX) {
			player.getHullMods().add("ix_ground_invasion_conversion");
			player.getHullMods().add("ix_reactive_combat_shields");
			player.getHullMods().add("ix_terminus_relay");
			if (Global.getSettings().getModManager().isModEnabled("EmergentThreats_Vice")) {
				player.getHullMods().add("vice_interdiction_array");
				player.getHullMods().add("vice_adaptive_entropy_arrester");
				player.getHullMods().add("vice_adaptive_flux_dissipator");
			}
			if (isHonorGuard) {
				player.getHullMods().add("ix_antecedent");
				player.getHullMods().add("ix_vampyr_retrofit");
				cargo.addSpecial(new SpecialItemData("ix_core_bp_package", ""), 1);
			}
			cargo.addSpecial(new SpecialItemData("ix_bp_package", ""), 1);
			cargo.addSpecial(new SpecialItemData("ix_aux_bp_package", ""), 1);
			cargo.addCommodity("ix_panopticon_core", 1);
			cargo.addSpecial(new SpecialItemData("ix_biochip_sotf", ""), 1);
			MutableCharacterStatsAPI stats = Global.getSector().getPlayerPerson().getStats();
			if (!stats.hasSkill(IX_SKILL_ID)) stats.setSkillLevel(IX_SKILL_ID, 2f);
		}
		Global.getSector().getMemoryWithoutUpdate().set("$give_IX_hullmods", false);
	}
	
	//runs once
	private void applyMarzannaChanges(SectorAPI sector) {
		//changes Marzanna officer faction to cartel and give more fitting rank titles
		if (Global.getSector().getMemoryWithoutUpdate().is("$apply_ix_marzanna_changes", false)) return;
		FactionAPI ix_battlegroup = Global.getSector().getFaction(IX_FAC_ID);
		FactionAPI ix_marzanna = Global.getSector().getFaction(MZ_FAC_ID);
		ix_marzanna.setRelationship("player", ix_battlegroup.getRelationship("player"));
		if (sector.getEconomy().getMarket("ix_marzanna_market") != null) {
			MarketAPI market = sector.getEconomy().getMarket("ix_marzanna_market");
			if (market.getPlanetEntity().getFaction() == ix_marzanna) {
				List<PersonAPI> people = market.getPeopleCopy();
				for (PersonAPI p : people) {
					if (p.getFaction() == ix_battlegroup) p.setFaction(ix_marzanna.getId());
					if (p.getRankId().equals("citizen")) p.setRankId("groundCaptain");
				}
			}
		}
		
		//give Overseer Station above Marzanna proper faction, name, and image
		if (sector.getStarSystem("Zorya") != null 
				&& sector.getStarSystem("Zorya").getEntitiesWithTag(Tags.STATION) != null) {
			List<SectorEntityToken> stations = sector.getStarSystem("Zorya").getEntitiesWithTag(Tags.STATION);
			for (SectorEntityToken s : stations) {
				if (s.getName().equals(NameListUtil.Marzanna_Station) && s.getFaction().getId().equals(IX_FAC_ID)) {
					s.setName(NameListUtil.Overseer_Station);
					s.setFaction(MZ_FAC_ID);
					s.setInteractionImage("illustrations", "ix_marzanna_illus");
					s.setCustomDescriptionId("ix_zorya_overseer");
				}
			}
		}
		
		sector.getMemoryWithoutUpdate().set("$apply_ix_marzanna_changes", false);
	}
	
	@Override
	public void onGameLoad(boolean newGame) {
		SectorAPI sector = Global.getSector();
		sector.registerPlugin(pCorePlugin);
		
		//v0.9.4 only to fix infinite stability bug, take out after next patch
		List<MarketAPI> markets = sector.getEconomy().getMarketsCopy();
		for (MarketAPI m : markets) {
			m.getStability().unmodify();
		}
		
		FactionAPI ix_battlegroup = Global.getSector().getFaction(IX_FAC_ID);
		FactionAPI ix_honor_guard = Global.getSector().getFaction("ix_core");
		FactionAPI ix_marzanna = Global.getSector().getFaction(MZ_FAC_ID);
		FactionAPI ix_trinity = Global.getSector().getFaction(TW_FAC_ID);
		if (ix_battlegroup != null) ix_battlegroup.getKnownFighters().remove("talon_wing");
		if (ix_honor_guard != null) ix_honor_guard.getKnownFighters().remove("talon_wing");
		if (ix_marzanna != null) ix_marzanna.getKnownFighters().remove("talon_wing");
		if (ix_trinity != null) ix_trinity.getKnownFighters().remove("talon_wing");
		
		String vertexId = "ix_zorya_vertex";
		String kresnikId = "ix_kresnik_mechanism";
		String culmenId = "tw_danu_culmen_station";
		String solitonId = "tw_soliton_siegebreaker";
		if (sector.getStarSystem("Zorya") != null 
				&& sector.getStarSystem("Zorya").getEntityById(vertexId) != null
				&& sector.getStarSystem("Zorya").getEntityById(vertexId).getMarket() != null) {
				
			MarketAPI m = sector.getStarSystem("Zorya").getEntityById(vertexId).getMarket();
			if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_kresnik_enabled")) {
				if (!m.hasIndustry(kresnikId)) m.addIndustry(kresnikId);
			}
			else if (m.hasIndustry(kresnikId)) m.removeIndustry(kresnikId, null, false);
		}
		if (sector.getStarSystem("Danu") != null 
				&& sector.getStarSystem("Danu").getEntityById(culmenId) != null
				&& sector.getStarSystem("Danu").getEntityById(culmenId).getMarket() != null) {
				
			MarketAPI m = sector.getStarSystem("Danu").getEntityById(culmenId).getMarket();
			if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_kresnik_enabled")) {
				if (!m.hasIndustry(solitonId)) m.addIndustry(solitonId);
			}
			else if (m.hasIndustry(solitonId)) m.removeIndustry(solitonId, null, false);
		}
		
		//one time switchover of High Command structure to new Fleet Command HQ on Vertex Station
		if (sector.getMemoryWithoutUpdate().is("$ix_vertex_updated", true)) return;
		if (sector.getStarSystem("Zorya") != null 
				&& sector.getStarSystem("Zorya").getEntityById(vertexId) != null
				&& sector.getStarSystem("Zorya").getEntityById(vertexId).getMarket() != null) {
			
			boolean hasBase = false;
			boolean hasCore = false;
			boolean hasItem = false;
			MarketAPI m = sector.getStarSystem("Zorya").getEntityById(vertexId).getMarket();
			if (m.hasIndustry(Industries.HIGHCOMMAND)) {
				hasBase = true;
				if (m.getIndustry(Industries.HIGHCOMMAND).getAICoreId().equals(Commodities.ALPHA_CORE)) hasCore = true;
				if (m.getIndustry(Industries.HIGHCOMMAND).getSpecialItem().getId().equals(Items.CRYOARITHMETIC_ENGINE)) hasItem = true;
			}
			if (hasBase) {
				m.removeIndustry(Industries.HIGHCOMMAND, null, false);
				if (hasItem) m.addIndustry("ix_fleet_command", new ArrayList<Items>(Arrays.asList(Items.CRYOARITHMETIC_ENGINE)));
				else m.addIndustry("ix_fleet_command");
				if (hasCore) m.getIndustry("ix_fleet_command").setAICoreId("ix_panopticon_instance");
			}
		}
		sector.getMemoryWithoutUpdate().set("$ix_vertex_updated", true);
	}
	
	@Override
	public void generate(SectorAPI sector) {
		IXSystemCreation.generate(sector);
	}
	
	@Override
	public void onApplicationLoad() {
		LunaRefitManager.addRefitButton(new BiochipSotFButton());
		LunaRefitManager.addRefitButton(new NanoReplicatorButton());
		LunaRefitManager.addRefitButton(new PanopticCommandRefitButton());
		LunaRefitManager.addRefitButton(new PanopticStrategicRefitButton());
		LunaRefitManager.addRefitButton(new PanopticTacticalRefitButton());
		LunaRefitManager.addRefitButton(new SalvagePanopticonCoreButton());
	}
}