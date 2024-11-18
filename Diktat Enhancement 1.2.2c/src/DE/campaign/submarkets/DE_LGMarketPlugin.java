package DE.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.util.Misc;

public class DE_LGMarketPlugin extends BaseSubmarketPlugin {
    private final RepLevel MIN_STANDING = RepLevel.COOPERATIVE;

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }


    @Override
    public float getTariff() {
        switch (market.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER))) {
            case COOPERATIVE:
                return 0.25f;
            case FRIENDLY:
                return 0.5f;
            case WELCOMING:
                return 0.75f;
            default:
                return 1f;
        }
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        RepLevel level = market.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (market.getFaction() != Global.getSector().getFaction(Factions.DIKTAT)) {
            return "Defunct due to hostile occupation";
        }
        if (!Global.getSector().getPlayerFleet().isTransponderOn()) {
            return "Requires: Transponder on";
        }
        if (!hasDiktatCommission()) {
            return "Requires: Sindrian Diktat commission";
        }
        if (!level.isAtWorst(MIN_STANDING)) {
            return "Requires: " + market.getFaction().getDisplayName() + " - "
                    + MIN_STANDING.getDisplayName().toLowerCase();
        }
        return super.getTooltipAppendix(ui);
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        if (market.getFaction() != Global.getSector().getFaction(Factions.DIKTAT)) {
            return false;
        }
        if (!Global.getSector().getPlayerFleet().isTransponderOn()) {
            return false;
        }
        if (!hasDiktatCommission()) {
            return false;
        }
        RepLevel level = market.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        return level.isAtWorst(MIN_STANDING);
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            getCargo().getMothballedShips().clear();

            float quality = 1.0f;

            FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
            doctrineOverride.setShipSize(2);
            addShips(submarket.getFaction().getId(),
                    300f, // combat
                    0f, // freighter
                    0f, // tanker
                    0f, // transport
                    0f, // liner
                    0f, // utilityPts
                    quality, // qualityOverride
                    0f, // qualityMod
                    ShipPickMode.PRIORITY_THEN_ALL,
                    doctrineOverride);

            pruneWeapons(0f);

            addWeapons(5, 10, 5, submarket.getFaction().getId());

            pruneShips(0.75f);
        }

        getCargo().sort();
    }

    protected boolean requiresCommission(RepLevel req) {
        if (!submarket.getFaction().getCustomBoolean(Factions.CUSTOM_OFFERS_COMMISSIONS)) return false;

        if (req.isAtWorst(RepLevel.WELCOMING)) return true;
        return false;
    }

    protected boolean hasDiktatCommission() {
        return market.getFaction().getId().equals(Misc.getCommissionFactionId());
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        return "Sales only!";
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        return "Sales only!";
    }

    @Override
    public boolean isParticipatesInEconomy() {
        return false;
    }
}
