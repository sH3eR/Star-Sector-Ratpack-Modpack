package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;

import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import static com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin.getEntityMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import data.campaign.ids.istl_Commodities;
import data.campaign.ids.istl_Ranks;

/**
 *
 * @author Harmful Mechanic
 * I sort of understand what I'm doing here!
 */
public class istl_SigmaMatterDataRecovery extends BaseCommandPlugin {

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected PersonAPI person;
    protected FactionAPI faction;

    protected boolean buysSMatter;
    protected float valueMult;
    protected float repMult;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        person = dialog.getInteractionTarget().getActivePerson();
        faction = person.getFaction();

        buysSMatter = faction.getCustomBoolean("buysSMatter");
        valueMult = faction.getCustomFloat("SMatterValueMult");
        repMult = faction.getCustomFloat("SMatterRepMult");

        switch (command) {
            case "selectSMatter":
                selectSMatter();
                break;
            case "playerHasSMatter":
                return playerHasSMatter();
            case "personCanAcceptSMatter":
                return personCanAcceptSMatter();
            default:
                break;
        }
//        if (command.equals("selectSMatter")) {
//            selectSMatter();
//        } else if (command.equals("playerHasSMatter")) {
//            return playerHasSMatter();
//        } else if (command.equals("personCanAcceptSMatter")) {
//            return personCanAcceptSMatter();
//        }
        return true;
    }

    protected boolean personCanAcceptSMatter() {
        if (person == null || !buysSMatter) return false;

        return
           Ranks.POST_SCIENTIST.equals(person.getPostId()) ||
           istl_Ranks.SMATTER_RESEARCHER.equals(person.getRankId()) ||
           istl_Ranks.POST_SNRI_REPRESENTATIVE.equals(person.getPostId());
           //eventually, I'd like to make this more complex, with only researchers being able to accept the unstable stuff.
    }

    protected void selectSMatter() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            //if (spec != null && spec.getDemandClass().equals(istl_Commodities.SIGMA_MATTER)) {
            if (spec != null && spec.hasTag("sigma_matter")) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select Sigma matter to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            @Override
            public void pickedCargo(CargoAPI cargo) {
                cargo.sort();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                    if (stack.isCommodityStack()) { // should be always, but just in case
                        AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
                    }
                }

                float bounty = computeSMatterCreditValue(cargo);
                float repChange = computeSMatterReputationValue(cargo);

                if (bounty > 0) {
                    playerCargo.getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, text);
                }

                if (repChange >= 1f) {
                    CustomRepImpact impact = new CustomRepImpact();
                    impact.delta = repChange * 0.01f;
                    Global.getSector().adjustPlayerReputation(
                        new RepActionEnvelope(RepActions.CUSTOM, impact,
                                              null, text, true), 
                                              faction.getId());

                    impact.delta *= 0.25f;
                    if (impact.delta >= 0.01f) {
                            Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM, impact,
                                                      null, text, true), 
                                                      person);
                    }
                }
                FireBest.fire(null, dialog, memoryMap, "SigmaMatterTurnedIn");
            }
            @Override
            public void cancelledCargoSelection() {
            }
            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                float bounty = computeSMatterCreditValue(combined);
                float repChange = computeSMatterReputationValue(combined);

                float pad = 3f;
                float small = 5f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();

                panel.addImage(faction.getLogo(), width * 1f, 3f);
                panel.addPara("Compared to dealing with other factions, turning Sigma matter in to " + 
                                faction.getDisplayNameLongWithArticle() + " " +
                                "will result in:", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Payment value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Reputation gain", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected Sigma matter, you will receive %s in compensation " +
                                "and your standing with " + faction.getDisplayNameWithArticle() + " will improve by %s points.",
                                opad * 1f, Misc.getHighlightColor(),
                                Misc.getWithDGS(bounty) + Strings.C,
                                "" + (int) repChange);
            }
        });
    }

protected float computeSMatterCreditValue(CargoAPI cargo) {
        float bounty = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            //if (spec != null && spec.getDemandClass().equals(istl_Commodities.SIGMA_MATTER)) {
            if (spec != null && spec.hasTag("sigma_matter")) {
                    bounty += spec.getBasePrice() * stack.getSize();
            }
        }
        bounty *= valueMult;
        return bounty;
    }

    protected float computeSMatterReputationValue(CargoAPI cargo) {
        float rep = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            //if (spec != null && spec.getDemandClass().equals(istl_Commodities.SIGMA_MATTER)) {
            if (spec != null && spec.hasTag("sigma_matter")) {
                    rep += getBaseRepValue(spec.getId()) * stack.getSize();
            }
        }
        rep *= repMult;
        //if (rep < 1f) rep = 1f;
        return rep;
    }

    public static float getBaseRepValue(String istl_SigmaMatterType) {
        if (istl_Commodities.SIGMA_MATTER_HIGH.equals(istl_SigmaMatterType)) {
            return 7f;
        }

        if (istl_Commodities.SIGMA_MATTER_LOW.equals(istl_SigmaMatterType)) {
            return 5f;    
        }
        if (istl_Commodities.SIGMA_MATTER_UNSTABLE.equals(istl_SigmaMatterType)) {
                return 2f;
        }
        return 1f;
    }

    protected boolean playerHasSMatter() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            //if (spec != null && spec.getDemandClass().equals(istl_Commodities.SIGMA_MATTER)) {
            if (spec != null && spec.hasTag("sigma_matter")) {
                    return true;
            }
        }
        return false;
    }
}