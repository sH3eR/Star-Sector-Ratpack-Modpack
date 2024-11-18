package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;

import static data.scripts.SCVE_Utils.getString;

public class SCVE_AddOfficer extends BaseHullMod {

    public static Logger log = Global.getLogger(SCVE_AddOfficer.class);
    private static final String CUSTOM_OFFICER_FILE_PATH = "custom_officer.json";
    private static final String OFFICER_DETAILS_HULLMOD_ID = "SCVE_officerdetails";

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //this needs to do nothing if done in campaign
        if (Global.getSettings().getCurrentState() != GameState.TITLE
                || !stats.getFleetMember().getCaptain().getNameString().isEmpty() // easy way to tell there's no officer already on the ship
        ) {
            return;
        }

        PersonAPI officer = createCustomOfficer();
        stats.getFleetMember().setCaptain(officer);
        addOfficerDetailsHullmod(stats);
        SCVE_OfficerDetails.firstFrame = true;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getVariant().removeMod(spec.getId());
        ship.getVariant().removePermaMod(spec.getId());
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        //this needs to do nothing if done in campaign
        return (Global.getSettings().getCurrentState() != GameState.TITLE) ? getString("hullModCampaignError") : getString("hullModOfficerPresent");
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        //this needs to do nothing if done in campaign or if there is an officer
        return Global.getSettings().getCurrentState() == GameState.TITLE && ship.getCaptain().getNameString().isEmpty();
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return CUSTOM_OFFICER_FILE_PATH;
        }
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        try {
            JSONObject settings = Global.getSettings().loadJSON(CUSTOM_OFFICER_FILE_PATH);
            String personality = settings.optString("personality", "steady");
            tooltip.addPara(getString("hullModAddOfficer"), 10f, Misc.getHighlightColor(), personality);
            JSONArray keys = settings.names();
            for (int i = 0; i < keys.length(); i++) {
                String skillId = keys.getString(i);
                int skillLevel = Math.min(2, Math.max(0, settings.optInt(skillId)));
                if (skillLevel > 0) {
                    SkillSpecAPI skill = Global.getSettings().getSkillSpec(skillId);
                    String skillSprite = skill.getSpriteName();
                    String skillName = skill.getName();
                    String eliteText = (skillLevel == 2) ? String.format(" (%s)",getString("hullModOfficerDetailElite")) : "";
                    TooltipMakerAPI skillImageWithText = tooltip.beginImageWithText(skillSprite, 40);
                    skillImageWithText.addPara(skillName + eliteText, 0, Color.GREEN, eliteText);
                    tooltip.addImageWithText(10f);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static PersonAPI createCustomOfficer() {
        PersonAPI officer = Global.getSector().getFaction(Factions.PLAYER).createRandomPerson();

        officer.getStats().setSkipRefresh(true);

        try {
            JSONObject settings = Global.getSettings().loadJSON(CUSTOM_OFFICER_FILE_PATH);
            String personality = settings.optString("personality", "steady");
            officer.setPersonality(personality);
            JSONArray keys = settings.names();
            int officerLevel = 0;
            for (int i = 0; i < keys.length(); i++) {
                String skillId = keys.getString(i);
                if (skillId.equals("personality")) continue;
                int skillLevel = Math.min(2, Math.max(0, settings.optInt(skillId)));
                if (skillLevel == 0) continue;
                officer.getStats().setSkillLevel(skillId, skillLevel);
                officerLevel++;
            }
            officer.getStats().setLevel(officerLevel);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        officer.getStats().setSkipRefresh(false);
        officer.getStats().refreshCharacterStatsEffects();
        return officer;
    }

    // works with FleetMemberAPI or MutableShipStatsAPI.getFleetMember(), but NOT ShipAPI
    public static void addOfficerDetailsHullmod(FleetMemberAPI member) {
        if (member.getCaptain().getNameString().isEmpty()) {
            log.info("Member " + member.getShipName() + " has no officer");
            return;
        }
        //can someone tell me why I have to do this
        //also TODO: check if cloning the variant is necessary
        if (member.getVariant().isEmptyHullVariant()) {
            ShipVariantAPI clone = member.getVariant().clone();
            member.setVariant(clone, false, false);
            member.getVariant().addPermaMod(OFFICER_DETAILS_HULLMOD_ID);
            //log.info("Officer details hullmod added");
        } else {
            ShipVariantAPI clone = member.getVariant().clone();
            clone.setHullVariantId(clone.getHullVariantId() + "_clone");
            member.setVariant(Global.getSettings().getVariant(clone.getHullVariantId()), false, false);
            member.getVariant().addPermaMod(OFFICER_DETAILS_HULLMOD_ID);
            //log.info("Officer details hullmod added");
        }
    }

    // this one is used in conjunction with the AddOfficer hullmod
    public static void addOfficerDetailsHullmod(MutableShipStatsAPI stats) {
        FleetMemberAPI member = stats.getFleetMember();
        addOfficerDetailsHullmod(member);
    }
}