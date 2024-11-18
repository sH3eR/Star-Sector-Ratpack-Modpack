package data.scripts.ix.listeners;

import java.util.List;
import lunalib.lunaSettings.LunaSettings;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;

public class IXReputationResetListener extends BaseCampaignEventListener {
	
	private static String IX_FAC_ID = "ix_battlegroup";
	private static String MZ_FAC_ID = "ix_marzanna";
	private static String TW_FAC_ID = "ix_trinity";
	private static String BRIGHTON_ID = "brighton";
	private static String EMBASSY_PLAYER = "ix_embassy_player";

	public IXReputationResetListener() {
		super(true);
	}
	
	private String getEmbassyFactionId() {
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
		for (MarketAPI m : markets) {
			if (m == null) continue;
			if (m.hasIndustry(EMBASSY_PLAYER)) return m.getFactionId();
		}
		return "";
	}
	
	//sync reputations of Marzanna to player with IX Battlegroup to player
	private void syncMarzannaToIx() {
		SectorAPI sector = Global.getSector();
		if (sector.getFaction(IX_FAC_ID) != null && sector.getFaction(MZ_FAC_ID) != null) {
			float ixRep = sector.getFaction(IX_FAC_ID).getRelToPlayer().getRel();
			float marRep = sector.getFaction(MZ_FAC_ID).getRelToPlayer().getRel();
			if (ixRep != marRep) sector.getFaction(MZ_FAC_ID).getRelToPlayer().setRel(ixRep);
		}
	}
	
	private void lockReputationToHostileForAll() {
		SectorAPI sector = Global.getSector();
		String commissionFacId = Misc.getCommissionFactionId();
		if (commissionFacId == null) commissionFacId = "";
		
		FactionAPI ix = sector.getFaction(IX_FAC_ID);
		FactionAPI mz = sector.getFaction(MZ_FAC_ID);
		FactionAPI tw = sector.getFaction(TW_FAC_ID);
		FactionAPI player = sector.getFaction("player");
		FactionAPI exo = sector.getFaction("rat_exotech");
		FactionAPI bw = sector.getFaction("tahlan_legioelite");
		FactionAPI pirates = sector.getFaction(Factions.PIRATES);
		FactionAPI indep = sector.getFaction(Factions.INDEPENDENT);
		FactionAPI herd = sector.getFaction("magellan_theherd");
		FactionAPI brighton = sector.getFaction("brighton");
		//FactionAPI dust = sector.getFaction("sotf_dustkeepers"); //initialized
		
		List<FactionAPI> factionList = sector.getAllFactions();
		factionList.remove(ix);
		factionList.remove(mz);
		factionList.remove(tw);
		factionList.remove(exo);
		factionList.remove(player);
		factionList.remove(pirates);
		factionList.remove(herd);
		factionList.remove(indep);
		
		String embassyFacId = getEmbassyFactionId();
		
		//brighton handled separately
		factionList.remove(brighton);
				
		//exempt embassy faction/player commissioned faction from reputation lock
		if (!embassyFacId.isEmpty()) {
			if (embassyFacId.equals("player")) {
				if (!commissionFacId.isEmpty() && !commissionFacId.equals(IX_FAC_ID) && !commissionFacId.equals(TW_FAC_ID)) embassyFacId = commissionFacId;
			}
			factionList.remove(sector.getFaction(embassyFacId));
		}
		
		if (!LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_brighton_neutral")
				&& !BRIGHTON_ID.equals(embassyFacId)) {
			if (ix.getRelationship(BRIGHTON_ID) > -0.50f) ix.setRelationship(BRIGHTON_ID, -0.50f);
			if (mz.getRelationship(BRIGHTON_ID) > -0.50f) mz.setRelationship(BRIGHTON_ID, -0.50f);
			if (tw.getRelationship(BRIGHTON_ID) > -0.50f) tw.setRelationship(BRIGHTON_ID, -0.50f);
		}
		else if (LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_brighton_neutral")
				&& !BRIGHTON_ID.equals(embassyFacId)) {
			if (ix.getRelationship(BRIGHTON_ID) > 0f) ix.setRelationship(BRIGHTON_ID, 0f);
			if (mz.getRelationship(BRIGHTON_ID) > 0f) mz.setRelationship(BRIGHTON_ID, 0f);
			if (tw.getRelationship(BRIGHTON_ID) > 0f) tw.setRelationship(BRIGHTON_ID, 0f);
		}
		
		//suppress all visible faction reputations for IX Battlegroup and Trinity Worlds
		for (FactionAPI faction : factionList) {
			if (!faction.isShowInIntelTab()) continue;
			String id = faction.getId();
			if (ix.getRelationship(id) > -0.50f) {
				ix.setRelationship(id, -0.50f);
				if (mz.getRelationship(id) > -0.50f) mz.setRelationship(id, -0.50f);
				if (IX_FAC_ID.equals(commissionFacId) && player.getRelationship(id) > -0.50f) {
					player.setRelationship(id, -0.50f);	
				}
			}
			if (tw.getRelationship(id) > -0.50f) {
				tw.setRelationship(id, -0.50f);
				if (mz.getRelationship(id) > -0.50f) mz.setRelationship(id, -0.50f);
				if (TW_FAC_ID.equals(commissionFacId) && player.getRelationship(id) > -0.50f) {
					player.setRelationship(id, -0.50f);	
				}
			}
		}
		
		//make Trinity Worlds and IX Battlegroup permanently friendly
		if (sector.getFaction(IX_FAC_ID) != null && sector.getFaction(TW_FAC_ID) != null) {
			String commissionId = Misc.getCommissionFactionId();
			if (commissionId == null) commissionId = "";
			if (ix.getRelationship(TW_FAC_ID) < 0.75f) {
				ix.setRelationship(TW_FAC_ID, 0.75f);
				mz.setRelationship(TW_FAC_ID, 0.75f);
				if (commissionId.equals(IX_FAC_ID)) tw.setRelationship("player", 0.75f);
				if (commissionId.equals(TW_FAC_ID)) ix.setRelationship("player", 0.75f);
			}
		}
		
		syncMarzannaToIx();
	}
	
	//reimpose IX and TW sector wide hostilities on market open, reputation change, and end of month
	@Override
	public void reportEconomyMonthEnd() {
		lockReputationToHostileForAll();
		String embassyFacId = getEmbassyFactionId();
		String commissionFacId = Misc.getCommissionFactionId();
		if (commissionFacId == null) commissionFacId = "";
		
		if (embassyFacId.isEmpty() || embassyFacId.equals(IX_FAC_ID) || embassyFacId.equals(TW_FAC_ID)) return;
		
		//apply bonus if player is commissioned with faction and has embassy built on their own colony
		if (embassyFacId.equals("player")) {
			if (!commissionFacId.isEmpty() && !commissionFacId.equals(IX_FAC_ID) && !commissionFacId.equals(TW_FAC_ID)) embassyFacId = commissionFacId; 
		}
		
		SectorAPI sector = Global.getSector();
		FactionAPI embassyFac = sector.getFaction(embassyFacId);		
		FactionAPI player = sector.getFaction("player");
		FactionAPI ix = sector.getFaction(IX_FAC_ID);
		FactionAPI tw = sector.getFaction(TW_FAC_ID);
		
		float ixRep = ix.getRelationship(embassyFacId);
		float twRep = tw.getRelationship(embassyFacId);
		
		//adds 10 IX/TW/commission rep to embassy faction if rep is below 35 (welcoming) 
		if (ixRep < 0.35f && ixRep >= 0.25f) ix.setRelationship(embassyFacId, 0.35f);
		if (ixRep < 0.25f) ix.setRelationship(embassyFacId, ixRep += 0.10f);
		if (twRep < 0.35f && twRep >= 0.25f) tw.setRelationship(embassyFacId, 0.35f);
		if (twRep < 0.25f) tw.setRelationship(embassyFacId, twRep += 0.10f);
		
		//adds 10 IX/TW rep to player faction if embassy built on player colony
		if (getEmbassyFactionId().equals("player")) {
			float ixToPlayerRep = ix.getRelationship("player");
			float twToPlayerRep = tw.getRelationship("player");
			if (ixToPlayerRep < 0.35f && ixToPlayerRep >= 0.25f) ix.setRelationship(embassyFacId, 0.35f);
			if (ixToPlayerRep < 0.25f) ix.setRelationship(embassyFacId, ixToPlayerRep += 0.10f);
			if (twToPlayerRep < 0.35f && twToPlayerRep >= 0.25f) tw.setRelationship(embassyFacId, 0.35f);
			if (twToPlayerRep < 0.25f) tw.setRelationship(embassyFacId, twToPlayerRep += 0.10f);
		}
		
		//sync IX, TW, or Embassy commissioned player to updated rep
		boolean isCommToFleet = commissionFacId.equals(IX_FAC_ID);
		boolean isCommToTrinity = commissionFacId.equals(TW_FAC_ID);
		boolean isCommToEmbassy = commissionFacId.equals(embassyFacId);
		if (isCommToFleet) embassyFac.getRelToPlayer().setRel(ixRep);
		if (isCommToTrinity) embassyFac.getRelToPlayer().setRel(twRep);
		if (isCommToEmbassy) {
			ix.getRelToPlayer().setRel(ix.getRelationship(embassyFacId));
			tw.getRelToPlayer().setRel(tw.getRelationship(embassyFacId));
		}
		
		syncMarzannaToIx();
	}
	
	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		lockReputationToHostileForAll();
	}
	
	@Override
	public void reportPlayerReputationChange(String faction, float delta) {
		//do not run during initial setup
		if (Global.getSector().getPlayerMemoryWithoutUpdate().is("$reputationIsSetIX", false)) return;
		else lockReputationToHostileForAll();
	}
}