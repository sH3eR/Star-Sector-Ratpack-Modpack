package data.campaign.ids;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Voices;

public class istl_People extends People {
        //Faction leader
        public static final String DME_ARI = "istl_dme_ari";
        //La Reole
        public static final String DME_NIKO = "istl_dme_niko";
        public static final String DME_MAYA = "istl_dme_maya";
        //Lavoisier Base
        public static final String DME_EMILE = "istl_dme_emile";
        public static final String DME_ARMSDEV = "istl_dme_armsdev";
        //Cousteau Base (later)
//        public static final String DME_BLANK = "istl_blank_id";
//        public static final String DME_BLANK = "istl_blank_id";
        //spaaaaace asshoooo-ooles, spaaace asshoooo-ooles...
//        public static final String BREAKER_DESERTER = "istl_breaker_deserter";
//        public static final String BREAKER_LEADER = "istl_breaker_leader";
//        public static final String BREAKER_NIKO = "istl_breaker_niko"; //the boy has crossed a line
        //anyone else? They go down here
//        public static final String DME_BLANK = "istl_blank_id";
        
    	public static PersonAPI getPerson(final String id) {
            return Global.getSector().getImportantPeople().getPerson(id);
	}
    	public void advance() {
            createFactionLeaders();
            createLavoisierBaseCharacters();
            createLaReoleCharacters();
            //createCousteauBaseCharacters();
            //createBreakerCharacters();
            //createMiscCharacters();
	}
        
    public static void createFactionLeaders() {
        final ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        MarketAPI market = null;
        //Ariane Soisson, President of the Nikolaev System Authority's council
        //both a controversial figure to the public, and well-liked by members of her own party
        //facing a vote of 'no confidence' as of Cycle 207
        market =  Global.getSector().getEconomy().getMarket("istl_planet_peremohy");
        if (market != null) {
                PersonAPI person = Global.getFactory().createPerson();
		person.setId(DME_ARI);
                person.setFaction(istl_Factions.DASSAULT);
                person.setGender(FullName.Gender.FEMALE);
                person.setRankId(Ranks.FACTION_LEADER);
                person.setPostId(Ranks.POST_FACTION_LEADER);
                person.setImportance(PersonImportance.VERY_HIGH);
                person.getName().setFirst("Ariane");
                person.getName().setLast("Soisson");
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "DME_ari"));
                person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
                person.setVoice(Voices.OFFICIAL);

                market.getCommDirectory().addPerson(person, 0);
                market.addPerson(person);
		ip.addPerson(person); // so the person can be retrieved by id
        }
    }
    //All the characters who're spawned at Lavoisier Base
    public static void createLavoisierBaseCharacters() {
        final ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        MarketAPI market = null;
        market =  Global.getSector().getEconomy().getMarket("nikolaev_lab");
        //Emile Nguyen, the guy who fucks around with Sigma matter
        //Franco-Vietnamese, unspeakably clever, a little bit amoral
        //has a sideline in forbidden AI research, which he keeps on the d/l
        if (market != null) {
                PersonAPI person = Global.getFactory().createPerson();
		person.setId(DME_EMILE);
                person.setFaction(istl_Factions.DASSAULT);
                person.setGender(FullName.Gender.MALE);
                person.setRankId(istl_Ranks.SMATTER_RESEARCHER);
                person.setPostId(Ranks.POST_SCIENTIST);
                person.setImportance(PersonImportance.HIGH);
                person.getName().setFirst("Émile");
                person.getName().setLast("Nguyen");
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "DME_emile"));
                person.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);
                person.setVoice(Voices.SCIENTIST);

                market.getCommDirectory().addPerson(person, 0);
                market.addPerson(person);
		ip.addPerson(person); // so the person can be retrieved by id
        }
        //weapons engineer - random person, but defined rank and post.
        if (market != null) {
                PersonAPI person = Global.getSector().getFaction(istl_Factions.DASSAULT).createRandomPerson();
		person.setId(DME_ARMSDEV);
                person.setRankId(Ranks.CITIZEN);
                person.setPostId(istl_Ranks.POST_ARMS_RESEARCHER);
                person.setImportance(PersonImportance.MEDIUM);
                person.setVoice(Voices.SCIENTIST);

                market.getCommDirectory().addPerson(person, 1);
                market.addPerson(person);
		ip.addPerson(person); // so the person can be retrieved by id
        }
    }
    //All the characters who're spawned at La Réole
    public static void createLaReoleCharacters() {
        final ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        MarketAPI market = null;
        market =  Global.getSector().getEconomy().getMarket("istl_planet_lareole");
        //Niko Sphaleros, an up-and-coming officer affiliated with the Strategic Naval Research Insitute
        //member of the Greek-speaking minority on Peremohy
        //secretly in league with the Blade Breakers (oh no! spoilers!)
        if (market != null) {
                PersonAPI person = Global.getFactory().createPerson();
		person.setId(DME_NIKO);
                person.setFaction(istl_Factions.DASSAULT);
                person.setGender(FullName.Gender.MALE);
                person.setRankId(Ranks.SPACE_CAPTAIN);
                person.setPostId(istl_Ranks.POST_SNRI_REPRESENTATIVE);
                person.setImportance(PersonImportance.HIGH);
                person.getName().setFirst("Niko");
                person.getName().setLast("Sphaleros");
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "DME_niko"));
		person.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 3);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);

                market.getCommDirectory().addPerson(person, 0);
                market.addPerson(person);
		ip.addPerson(person); // so the person can be retrieved by id
        }
        //Maya Zabiarov, formerly of the La Réole Gendermerie, now seconded to Sixth Bureau service
        //Ukrainian, harder than a very hard thing that's hard as fuck
        //
        if (market != null) {
                PersonAPI person = Global.getFactory().createPerson();
		person.setId(DME_MAYA);
                person.setFaction(istl_Factions.DASSAULT);
                person.setGender(FullName.Gender.FEMALE);
                person.setRankId(Ranks.GROUND_MAJOR);
                person.setPostId(Ranks.POST_SPECIAL_AGENT);
                person.setImportance(PersonImportance.MEDIUM);
                person.getName().setFirst("Maya");
                person.getName().setLast("Zabiarov");
                person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "DME_maya"));
                person.getStats().setSkillLevel(Skills.WEAPON_DRILLS, 3);
                person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 3);
                person.setVoice(Voices.SOLDIER);

                market.getCommDirectory().addPerson(person, 1);
                market.addPerson(person);
		ip.addPerson(person); // so the person can be retrieved by id
        }
    }
    //All the characters who're spawned at Cousteau Base
//        public static void createCousteauBaseCharacters() {
//        final ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
//        MarketAPI market = null;
//        //empty for now
//    }
    //All the Blade Breaker characters who fuck with you on a personal level
//        public static void createCousteauBaseCharacters() {
//        final ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
//        MarketAPI market = null;
//        //empty for now
//    }    
    //Everything that doesn't fit elsewhere - free agents, weirdos
//    public static void createMiscCharacters() {
//        final ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
//        MarketAPI market = null;
//        //empty for now
//    }
}
