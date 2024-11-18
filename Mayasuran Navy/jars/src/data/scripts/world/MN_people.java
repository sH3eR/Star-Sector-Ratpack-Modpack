package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;

public class MN_people {

   // public static String msspyotr = "msspyotr"; //totally not from highfleet
    public static String mssduke = "mssduke"; //also totally not from highfleet
    public static String mssvice = "mssvice"; //the vice admiral
    public static String msspirate = "msspirate"; //italian pirate rat
    public static String msstritach = "msstritach"; //port tse scientist

    public static void create() {
        createMNCharacters();
    }

    public static PersonAPI createMNCharacters() {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        //SectorAPI sector = Global.getSector();
        //StarSystemAPI system = sector.getStarSystem("Mayasura");
        //MarketAPI market = null;

        MarketAPI market1 = Global.getSector().getEconomy().getMarket("mairaath");
        if (market1 != null) {
            // pyotr
            PersonAPI msspyotrPerson = Global.getFactory().createPerson();
            msspyotrPerson.setId("msspyotr");
            msspyotrPerson.setFaction("Mayasura");
            msspyotrPerson.setGender(FullName.Gender.MALE);
            msspyotrPerson.setRankId(Ranks.SPACE_ADMIRAL);
            msspyotrPerson.setPostId(Ranks.POST_OFFICER);
            msspyotrPerson.addTag(Tags.CONTACT_MILITARY);
            msspyotrPerson.setImportance(PersonImportance.VERY_HIGH);
            msspyotrPerson.getName().setFirst("Pyotr");
            msspyotrPerson.getName().setLast("Shahin");
            msspyotrPerson.setVoice(Voices.SOLDIER);
            msspyotrPerson.setPersonality(Personalities.AGGRESSIVE);
            msspyotrPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "MSS_Pyotr"));
            msspyotrPerson.getStats().setLevel(12);
            msspyotrPerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
            msspyotrPerson.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
            msspyotrPerson.getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
            msspyotrPerson.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
            msspyotrPerson.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
            msspyotrPerson.addTag("coff_nocapture");
            market1.addPerson(msspyotrPerson);
            market1.getCommDirectory().addPerson(msspyotrPerson, 1);
            ip.addPerson(msspyotrPerson);

            // grand duke
            PersonAPI mssdukePerson = Global.getFactory().createPerson();
            mssdukePerson.setId(mssduke);
            mssdukePerson.setFaction("Mayasura");
            mssdukePerson.setGender(FullName.Gender.MALE);
            mssdukePerson.setPostId(Ranks.POST_FACTION_LEADER);
            mssdukePerson.setRankId(Ranks.FACTION_LEADER);
            mssdukePerson.setVoice(Voices.ARISTO);
            mssdukePerson.addTag(Tags.CONTACT_MILITARY);
            mssdukePerson.setImportance(PersonImportance.VERY_HIGH);
            mssdukePerson.getName().setFirst("Mark");
            mssdukePerson.getName().setLast("Salemsky");
            mssdukePerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "MSS_Duke"));
            mssdukePerson.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
            market1.setAdmin(mssdukePerson);
            market1.getCommDirectory().addPerson(mssdukePerson, 0);
            market1.addPerson(mssdukePerson);
            ip.addPerson(mssdukePerson);

            // vice admiral
            PersonAPI mssvicePerson = Global.getFactory().createPerson();
            mssvicePerson.setId("mssvice");
            mssvicePerson.setFaction("Mayasura");
            mssvicePerson.setGender(FullName.Gender.FEMALE);
            mssvicePerson.setRankId(Ranks.SPACE_COMMANDER);
            mssvicePerson.setPostId(Ranks.POST_OFFICER);
            mssvicePerson.addTag(Tags.CONTACT_MILITARY);
            mssvicePerson.setImportance(PersonImportance.VERY_HIGH);
            mssvicePerson.getName().setFirst("Savina");
            mssvicePerson.getName().setLast("Mireille");
            mssvicePerson.setVoice(Voices.SOLDIER);
            mssvicePerson.setPersonality(Personalities.AGGRESSIVE);
            mssvicePerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "MSS_Vice"));
            mssvicePerson.getStats().setLevel(12);
            mssvicePerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
            mssvicePerson.getStats().setSkillLevel(Skills.CARRIER_GROUP, 1);
            mssvicePerson.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
            mssvicePerson.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
            mssvicePerson.addTag("coff_nocapture");
            market1.addPerson(mssvicePerson);
            market1.getCommDirectory().addPerson(mssvicePerson, 2);
            ip.addPerson(mssvicePerson);
            /*PersonAPI mssvicePerson = Global.getFactory().createPerson();
            mssvicePerson.setId(mssvice);
            mssvicePerson.setFaction("Mayasura");
            mssvicePerson.setGender(FullName.Gender.FEMALE);
            mssvicePerson.setPostId(Ranks.POST_FLEET_COMMANDER);
            mssvicePerson.setRankId(Ranks.SPACE_ADMIRAL);
            mssvicePerson.setPersonality(Personalities.AGGRESSIVE);
            mssvicePerson.setImportance(PersonImportance.HIGH);
            mssvicePerson.setVoice(Voices.SOLDIER);
            mssvicePerson.getName().setFirst("Savina");
            mssvicePerson.getName().setLast("Mireille");
            mssvicePerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "MSS_Vice"));
            mssvicePerson.getStats().setLevel(10);
            // fleet commander stuff
            mssvicePerson.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
            mssvicePerson.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
            // officer stuff
            mssvicePerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
            mssvicePerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
            mssvicePerson.addTag("coff_nocapture");
            //market.addPerson(mssvicePerson);
            //market.getCommDirectory().addPerson(mssvicePerson, 2);
            //market.getCommDirectory().getEntryForPerson(mssvicePerson).setHidden(true);
            ip.addPerson(mssvicePerson);*/
        }

        MarketAPI market2 = Global.getSector().getEconomy().getMarket("mairaath_abandoned_station2");
        if (market2 != null){
            // rat pirate
            PersonAPI msspiratePerson = Global.getFactory().createPerson();
            msspiratePerson.setId(msspirate);
            msspiratePerson.setFaction(Factions.PIRATES);
            msspiratePerson.setGender(FullName.Gender.MALE);
            msspiratePerson.setPostId(Ranks.POST_WARLORD);
            msspiratePerson.setRankId(Ranks.SPACE_COMMANDER);
            msspiratePerson.setVoice(Voices.SPACER);
            msspiratePerson.setImportance(PersonImportance.MEDIUM);
            msspiratePerson.addTag(Tags.CONTACT_UNDERWORLD);
            msspiratePerson.getName().setFirst("Vincent");
            msspiratePerson.getName().setLast("Ritatoni");
            msspiratePerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "MSS_Pirate"));
            market2.addPerson(msspiratePerson);
            market2.getCommDirectory().addPerson(msspiratePerson, 0);
            market2.setAdmin(msspiratePerson);
            ip.addPerson(msspiratePerson);
        }

        MarketAPI market3 = Global.getSector().getEconomy().getMarket("port_tse");
        if (market3 != null){
            // scientist lady
            PersonAPI msstritachPerson = Global.getFactory().createPerson();
            msstritachPerson.setId(msstritach);
            msstritachPerson.setFaction(Factions.TRITACHYON);
            msstritachPerson.setGender(FullName.Gender.FEMALE);
            msstritachPerson.setPostId(Ranks.POST_SCIENTIST);
            msstritachPerson.setRankId(Ranks.CITIZEN);
            msstritachPerson.setVoice(Voices.SCIENTIST);
            msstritachPerson.setImportance(PersonImportance.MEDIUM);
            msstritachPerson.addTag(Tags.CONTACT_UNDERWORLD);
            msstritachPerson.getName().setFirst("Lysandra");
            msstritachPerson.getName().setLast("Korrin");
            msstritachPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "MSS_Tritach"));
            market3.addPerson(msstritachPerson);
            market3.getCommDirectory().addPerson(msstritachPerson, 1);
            ip.addPerson(msstritachPerson);
        }

        return null;
    }
}
