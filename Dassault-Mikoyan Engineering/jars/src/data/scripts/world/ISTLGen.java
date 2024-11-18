package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import static com.fs.starfarer.api.impl.campaign.procgen.SectorProcGen.getIndex;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SectorThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.ids.istl_People;
import data.campaign.procgen.themes.BladeBreakerThemeGenerator;

import data.scripts.world.systems.Nikolaev;
import data.scripts.world.systems.Martinique;
import data.scripts.world.systems.Kostroma;
import data.scripts.world.systems.Hejaz;
import data.scripts.world.systems.BessonConstellation;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;


public class ISTLGen implements SectorGeneratorPlugin
{

    @Override
    public void generate(SectorAPI sector)
    {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("dassault_mikoyan");
        initFactionRelationships(sector);
        //load systems
        new Nikolaev().generate(sector);
        new Martinique().generate(sector);
        new Kostroma().generate(sector);
        new Hejaz().generate(sector);
        new BessonConstellation().generate(sector);
        //load characters
        new istl_People().advance(); 
    }
    
//    OBSOLETE: Files are no longer needed.
//    public void handmadebreakers(SectorAPI sector)
//    {    
//        new AlephKafConstellation().generate(sector);
//        //new Aleph().generate(sector);
//        //new Kaf().generate(sector);
//        new DaletYodConstellation().generate(sector);
//        //new Dalet().generate(sector);
//        //new Yod().generate(sector);
//    }
    
    public void randombreakers(SectorAPI sector)
    {
//        ThemeGenContext context = new ThemeGenContext();
//        BladeBreakerThemeGenerator gen = new BladeBreakerThemeGenerator();
//        float w = Global.getSettings().getFloat("sectorWidth");
//        float h = Global.getSettings().getFloat("sectorHeight");
//
//        int cellsWide = (int) (w / 2000.0F);
//        int cellsHigh = (int) (h / 2000.0F);
//
//        boolean[][] cells = new boolean[cellsWide][cellsHigh];
//        int count = 10;
//
//        int vPad = 5;
//        int hPad = 5;
//
//        hPad = 15;
//        vPad = 9;
//        for (int i = 0; i < cells.length; i++) {
//            for (int j = 0; j < cells[0].length; j++) {
//                if ((i <= hPad) || (j <= vPad) || (i >= cellsWide - hPad) || (j >= cellsHigh - vPad)) {
//                    cells[i][j] = true; //'true' was 1, which doesn't compile. Pretty sure this breaks it.
//                }
//            }
//        }
//        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
//            int[] index = getIndex(system.getLocation());
//            int x = index[0];
//            int y = index[1];
//            if (x < 0) {
//                x = 0;
//            }
//            if (y < 0) {
//                y = 0;
//            }
//            if (x > cellsWide - 1) {
//                x = cellsWide - 1;
//            }
//            if (y > cellsHigh - 1) {
//                y = cellsHigh - 1;
//            }
//            blotOut(cells, x, y, 8);
//        }
//        blotOut(cells, 0, 0, 12);
//        blotOut(cells, 6, 0, 12);
//        blotOut(cells, 12, 0, 12);
//
//        List<Constellation> constellations = new ArrayList();
//        for (int k = 0; k < count; k++) {
//            WeightedRandomPicker<Pair<Integer, Integer>> picker = new WeightedRandomPicker(StarSystemGenerator.random);
//            for (int i = 0; i < cells.length; i++) {
//                for (int j = 0; j < cells[0].length; j++) {
//                    if (cells[i][j] == false) { //'false' was 0, which doesn't compile. Pretty sure this breaks it.
//                        Pair<Integer, Integer> p = new Pair(Integer.valueOf(i), Integer.valueOf(j));
//                        picker.add(p);
//                    }
//                }
//            }
//            Pair<Integer, Integer> pick = (Pair) picker.pick();
//            if (pick != null) {
//                blotOut(cells, ((Integer) pick.one).intValue(), ((Integer) pick.two).intValue(), 10);
//
//                float x = ((Integer) pick.one).intValue() * 2000.0F - w / 2.0F;
//                float y = ((Integer) pick.two).intValue() * 2000.0F - h / 2.0F;
//
//                StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
//
//                StarAge age = StarAge.ANY;
//                if (age == StarAge.ANY) {
//                    WeightedRandomPicker<StarAge> agePicker = new WeightedRandomPicker(StarSystemGenerator.random);
//                    agePicker.add(StarAge.YOUNG);
//                    agePicker.add(StarAge.AVERAGE);
//                    agePicker.add(StarAge.OLD);
//                    age = (StarAge) agePicker.pick();
//                }
//                params.age = age;
//
//                params.location = new Vector2f(x, y);
//                Constellation c = new StarSystemGenerator(params).generate();
//                constellations.add(c);
//            }
//        }
//        context.constellations = constellations;
//        gen.generateForSector(context, 3.0F);
        
        SectorThemeGenerator.generators.add(1, new BladeBreakerThemeGenerator());
    }

    public static void blotOut(boolean[][] cells, int x, int y, int c) {
        for (int i = Math.max(0, x - c / 2); (i <= x + c / 2) && (i < cells.length); i++) {
            for (int j = Math.max(0, y - c / 2); (j <= y + c / 2) && (j < cells[0].length); j++) {
                cells[i][j] = true; //'true' was 1, which doesn't compile. Pretty sure this breaks it.
            }
        }
    }

    public static void initFactionRelationships(SectorAPI sector)
    {
        FactionAPI dassault = sector.getFaction("dassault_mikoyan");
        FactionAPI dassault2 = sector.getFaction("6eme_bureau");
        FactionAPI bladebreakers = sector.getFaction("blade_breakers");
        FactionAPI breakerdeserter = sector.getFaction("the_deserter");
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
        FactionAPI kol = sector.getFaction(Factions.KOL);
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI league = sector.getFaction(Factions.PERSEAN);
        FactionAPI remnants = sector.getFaction(Factions.REMNANTS);
        FactionAPI neutral = sector.getFaction(Factions.NEUTRAL);

        
        //FactionAPI sra = sector.getFaction("shadow_industry");
        //FactionAPI birdy = sector.getFaction("blackrock_driveyards");
        //FactionAPI thi = sector.getFaction("tiandong");


        dassault.setRelationship(hegemony.getId(), RepLevel.WELCOMING);
        dassault.setRelationship(tritachyon.getId(), RepLevel.INHOSPITABLE);
        dassault.setRelationship(pirates.getId(), RepLevel.VENGEFUL);
        dassault.setRelationship(independent.getId(), RepLevel.FAVORABLE);
        dassault.setRelationship(kol.getId(), RepLevel.HOSTILE);
        dassault.setRelationship(church.getId(), RepLevel.HOSTILE);
        dassault.setRelationship(path.getId(), RepLevel.VENGEFUL);
        dassault.setRelationship(diktat.getId(), RepLevel.HOSTILE);
        dassault.setRelationship(league.getId(), RepLevel.INHOSPITABLE);
        dassault.setRelationship(remnants.getId(), RepLevel.HOSTILE);
        
        dassault.setRelationship("blade_breakers", RepLevel.VENGEFUL);
        dassault.setRelationship("magellan_protectorate", RepLevel.HOSTILE);
        dassault.setRelationship("magellan_leveller", RepLevel.SUSPICIOUS);
        dassault.setRelationship("shadow_industry", RepLevel.FAVORABLE);
        dassault.setRelationship("blackrock_driveyards", RepLevel.SUSPICIOUS);
        dassault.setRelationship("tiandong", RepLevel.WELCOMING);
        dassault.setRelationship("diableavionics", RepLevel.HOSTILE);
        dassault.setRelationship("roider", RepLevel.WELCOMING);
        dassault.setRelationship("al_ars", RepLevel.HOSTILE);
        dassault.setRelationship("scalartech", RepLevel.HOSTILE);
        dassault.setRelationship("vic", RepLevel.HOSTILE);
        dassault.setRelationship("new_galactic_order", RepLevel.VENGEFUL);
        
        dassault2.setRelationship(dassault.getId(), RepLevel.COOPERATIVE);
        dassault2.setRelationship(path.getId(), RepLevel.VENGEFUL);
        dassault2.setRelationship(diktat.getId(), RepLevel.HOSTILE);
        dassault2.setRelationship(remnants.getId(), RepLevel.VENGEFUL);
        
        dassault2.setRelationship("blade_breakers", RepLevel.VENGEFUL);
        dassault2.setRelationship("new_galactic_order", RepLevel.VENGEFUL);
        
        List<FactionAPI> factionList = sector.getAllFactions();
        factionList.remove(bladebreakers);
        for (FactionAPI faction : factionList) {
            if (faction != bladebreakers && !faction.isNeutralFaction()) 
            {
                bladebreakers.setRelationship(faction.getId(), RepLevel.VENGEFUL);
            }
        }
        bladebreakers.setRelationship("player", RepLevel.VENGEFUL);
    
//        bladebreakers.setRelationship(hegemony.getId(), RepLevel.HOSTILE);
//        bladebreakers.setRelationship(tritachyon.getId(), RepLevel.HOSTILE);
//        bladebreakers.setRelationship(pirates.getId(), RepLevel.VENGEFUL);
//        bladebreakers.setRelationship(independent.getId(), RepLevel.HOSTILE);
//        bladebreakers.setRelationship(kol.getId(), RepLevel.HOSTILE);
//        bladebreakers.setRelationship(church.getId(), RepLevel.HOSTILE);
//        bladebreakers.setRelationship(path.getId(), RepLevel.VENGEFUL);
//        bladebreakers.setRelationship(diktat.getId(), RepLevel.HOSTILE);
//        bladebreakers.setRelationship(league.getId(), RepLevel.VENGEFUL);
//        bladebreakers.setRelationship(remnants.getId(), RepLevel.HOSTILE);
//        
//        bladebreakers.setRelationship("dassault_mikoyan", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("shadow_industry", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("blackrock_driveyards", RepLevel.HOSTILE);
//        bladebreakers.setRelationship("tiandong", RepLevel.HOSTILE);
//        bladebreakers.setRelationship("diableavionics", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("ORA", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("SCY", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("neutrinocorp", RepLevel.HOSTILE);
//        bladebreakers.setRelationship("interstellarimperium", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("syndicate_asp", RepLevel.HOSTILE);
//        bladebreakers.setRelationship("pack", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("junk_pirates", RepLevel.HOSTILE);
//        bladebreakers.setRelationship("fob", RepLevel.VENGEFUL);
//        bladebreakers.setRelationship("new_galactic_order", RepLevel.VENGEFUL);
        
        for (FactionAPI faction : factionList) {
            if (faction != breakerdeserter && faction != independent && !faction.isNeutralFaction()) 
            {
                breakerdeserter.setRelationship(faction.getId(), RepLevel.HOSTILE);
            }
        }
        breakerdeserter.setRelationship(independent.getId(), RepLevel.SUSPICIOUS);

//        breakerdeserter.setRelationship(dassault.getId(), RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship(dassault2.getId(), RepLevel.VENGEFUL);
//        breakerdeserter.setRelationship(hegemony.getId(), RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship(tritachyon.getId(), RepLevel.HOSTILE);
//        breakerdeserter.setRelationship(pirates.getId(), RepLevel.VENGEFUL);
//        breakerdeserter.setRelationship(independent.getId(), RepLevel.SUSPICIOUS);
//        breakerdeserter.setRelationship(kol.getId(), RepLevel.HOSTILE);
//        breakerdeserter.setRelationship(church.getId(), RepLevel.HOSTILE);
//        breakerdeserter.setRelationship(path.getId(), RepLevel.VENGEFUL);
//        breakerdeserter.setRelationship(diktat.getId(), RepLevel.HOSTILE);
//        breakerdeserter.setRelationship(league.getId(), RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship(remnants.getId(), RepLevel.VENGEFUL);
//        //Mod faction relations
//        breakerdeserter.setRelationship("blade_breakers", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("magellan_protectorate", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("magellan_leveller", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("shadow_industry", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("blackrock_driveyards", RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship("exigency", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("tiandong", RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship("diableavionics", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("ORA", RepLevel.SUSPICIOUS);
//        breakerdeserter.setRelationship("SCY", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("neutrinocorp", RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship("interstellarimperium", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("syndicate_asp", RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship("pack", RepLevel.INHOSPITABLE);
//        breakerdeserter.setRelationship("junk_pirates", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("fob", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("nullorder", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("Coalition", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("al_ars", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("roider", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("templars", RepLevel.VENGEFUL);
//        breakerdeserter.setRelationship("cabal", RepLevel.VENGEFUL);
//        breakerdeserter.setRelationship("kadur_remnant", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("HMI", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("gmda", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("metelson", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("fringe_defence_syndicate", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("sad", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("vic", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("scalartech", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("tahlan_greathouses", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("tahlan_legioinfernalis", RepLevel.VENGEFUL);
//        //Unlikely to update, but preserved because they're already set
//        breakerdeserter.setRelationship("sylphon", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("vass_shipyards", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("nomads", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("crystanite", RepLevel.HOSTILE);
//        breakerdeserter.setRelationship("new_galactic_order", RepLevel.VENGEFUL);
    }
}