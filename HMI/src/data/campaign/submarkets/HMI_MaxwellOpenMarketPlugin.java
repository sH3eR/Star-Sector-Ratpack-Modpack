package data.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class HMI_MaxwellOpenMarketPlugin extends BaseSubmarketPlugin {


    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return "Not a real market.";
        }

        return super.getIllegalTransferText(stack, action);
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return "Not a real market.";
        }

        return super.getIllegalTransferText(member, action);
    }

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }

    @Override
    public float getTariff() {
            return 0f;
    }


    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (market.hasCondition(Conditions.FREE_PORT)) {
            return false;
        }
        return submarket.getFaction().isIllegal(commodityId);
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (!stack.isCommodityStack()) {
            return false;
        }
        return isIllegalOnSubmarket((String) stack.getData(), action);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }



    public String getTooltipAppendix(CoreUIAPI ui) {
        if (isEnabled(ui)) {
            int relationshipUI = (int) (submarket.getFaction().getRelToPlayer().getRel() * 100);
            return "You currently have " + (relationshipUI) + "  Relationshares.\"";
        }
        return null;
    }


    public void updateCargoPrePlayerInteraction() {
        float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
        addAndRemoveStockpiledResources(seconds, false, true, true);
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }

        getCargo().sort();
    }

    protected Object writeReplace() {
        if (okToUpdateShipsAndWeapons()) {
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }


    public boolean shouldHaveCommodity(CommodityOnMarketAPI com) {
        return !market.isIllegal(com);
    }

    @Override
    public int getStockpileLimit(CommodityOnMarketAPI com) {

        //Just eliminate everything
        float limit = OpenMarketPlugin.getBaseStockpileLimit(com);
        limit *= 0;
        if (limit < 0) limit = 0;

        return (int) limit;
    }

    @Override
    public PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
        return PlayerEconomyImpactMode.PLAYER_SELL_ONLY;
    }


    @Override
    public boolean isBlackMarket() {
        return true;
    }

    @Override
    public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
        return super.getTooltipAppendixHighlights(ui);
    }
}