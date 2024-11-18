package data.console.commands;

import java.util.Arrays;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.lazywizard.lazylib.CollectionUtils;

public class AddAdminBC implements BaseCommand
{
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if (!context.isInCampaign())
        {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (args.isEmpty())
        {
            return runCommand("0 " + Factions.PLAYER, context);
        }

        final String[] tmp = args.split(" ");
        FullName name = null;
        switch (tmp.length)
        {
            case 1:
                return runCommand(args + " " + Factions.PLAYER, context);
            case 2:
                break;
            // Custom name support
            case 3:
                name = new FullName(tmp[2], "", Gender.MALE);
                break;
            default:
                name = new FullName(tmp[2], CollectionUtils.implode(Arrays.asList(
                        Arrays.copyOfRange(tmp, 4, tmp.length)), " "), Gender.MALE);
        }
		
        try {
            Integer.parseInt(tmp[0]);
        }
        catch (NumberFormatException ex) {
            Console.showMessage("Error: admin tier must be a whole number from 0 to 3!");
            return CommandResult.BAD_SYNTAX;
        }

        final int tier = Math.min(3, Math.max(0, Integer.parseInt(tmp[0])));

        final FactionAPI faction = findBestFactionMatch(tmp[1]);
        if (faction == null)
        {
            Console.showMessage("No faction found with id '" + tmp[1] + "'!");
            return CommandResult.ERROR;
        }

        final PersonAPI person = OfficerManagerEvent.createAdmin(faction, tier, new Random());
        final CharacterDataAPI characterData = Global.getSector().getCharacterData();
        if (name != null)
        {
            person.setName(name);
        }
        characterData.addAdmin(person);

        Console.showMessage("Created tier " + tier + " administrator " + person.getName().getFullName() + ".");
        return CommandResult.SUCCESS;
    }
	//Taken from https://github.com/LazyWizard/console-commands/blob/master/src/main/java/org/lazywizard/console/CommandUtils.java
	private FactionAPI findBestFactionMatch(String name)
    {
        name = name.toLowerCase();
        FactionAPI bestMatch = null;
        double closestDistance = Console.getSettings().getTypoCorrectionThreshold();

        // Check IDs first in case multiple factions share the same name
        for (FactionAPI faction : Global.getSector().getAllFactions())
        {
            double distance = calcSimilarity(name, faction.getId().toLowerCase());

            if (distance == 1.0)
            {
                return faction;
            }

            if (distance > closestDistance)
            {
                closestDistance = distance;
                bestMatch = faction;
            }
        }

        // Search again by name if no matching ID is found
        if (bestMatch == null)
        {
            for (FactionAPI faction : Global.getSector().getAllFactions())
            {
                double distance = calcSimilarity(name, faction.getDisplayName().toLowerCase());

                if (distance == 1.0)
                {
                    return faction;
                }

                if (distance > closestDistance)
                {
                    closestDistance = distance;
                    bestMatch = faction;
                }
            }
        }

        return bestMatch;
    }
	
	// Taken from: https://github.com/larsga/Duke/blob/master/duke-core/src/main/java/no/priv/garshol/duke/comparators/JaroWinkler.java
    private double calcSimilarity(String s1, String s2)
    {
        if (s1.equals(s2))
        {
            return 1.0;
        }

        // ensure that s1 is shorter than or same length as s2
        if (s1.length() > s2.length())
        {
            String tmp = s2;
            s2 = s1;
            s1 = tmp;
        }

        // (1) find the number of characters the two strings have in common.
        // note that matching characters can only be half the length of the
        // longer string apart.
        int maxdist = s2.length() / 2;
        int c = 0; // count of common characters
        int t = 0; // count of transpositions
        int prevpos = -1;
        for (int ix = 0; ix < s1.length(); ix++)
        {
            char ch = s1.charAt(ix);

            // now try to find it in s2
            for (int ix2 = Math.max(0, ix - maxdist);
                 ix2 < Math.min(s2.length(), ix + maxdist);
                 ix2++)
            {
                if (ch == s2.charAt(ix2))
                {
                    c++; // we found a common character
                    if (prevpos != -1 && ix2 < prevpos)
                    {
                        t++; // moved back before earlier
                    }
                    prevpos = ix2;
                    break;
                }
            }
        }

        // we don't divide t by 2 because as far as we can tell, the above
        // code counts transpositions directly.
        // System.out.println("c: " + c);
        // System.out.println("t: " + t);
        // System.out.println("c/m: " + (c / (double) s1.length()));
        // System.out.println("c/n: " + (c / (double) s2.length()));
        // System.out.println("(c-t)/c: " + ((c - t) / (double) c));
        // we might have to give up right here
        if (c == 0)
        {
            return 0.0;
        }

        // first compute the score
        double score = ((c / (double) s1.length())
                + (c / (double) s2.length())
                + ((c - t) / (double) c)) / 3.0;

        // (2) common prefix modification
        int p = 0; // length of prefix
        int last = Math.min(4, s1.length());
        while (p < last && s1.charAt(p) == s2.charAt(p))
        {
            p++;
        }

        score += ((p * (1 - score)) / 10);

        // (3) longer string adjustment
        // I'm confused about this part. Winkler's original source code includes
        // it, and Yancey's 2005 paper describes it. However, Winkler's list of
        // test cases in his 2006 paper does not include this modification. So
        // is this part of Jaro-Winkler, or is it not? Hard to say.
        if (s1.length() >= 5 // both strings at least 5 characters long
                && c - p >= 2// at least two common characters besides prefix
                && c - p >= ((s1.length() - p) / 2)) // fairly rich in common chars
        {
            score = score + ((1 - score) * ((c - (p + 1))
                    / ((double) ((s1.length() + s2.length())
                    - (2 * (p - 1))))));
        }

        // (4) similar characters adjustment
        // the same holds for this as for (3) above.
        return score;
    }
	
	
}
