package data.hullmods;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class MSS_JokerProtocols extends BaseHullMod
{

    // This Map<> contains all the weapons the fighter can get
    private static Map<Integer, String> POSSIBLE_WEAPONS = new HashMap<Integer, String>();
    static
    {
        POSSIBLE_WEAPONS.put(0, "gauss");
        POSSIBLE_WEAPONS.put(1, "MSS_big_arbalest");
        POSSIBLE_WEAPONS.put(2, "MSS_onocrotalus");
        POSSIBLE_WEAPONS.put(3, "multineedler");
        POSSIBLE_WEAPONS.put(4, "MSS_rocketlauncher");
        POSSIBLE_WEAPONS.put(5, "hephag");
        POSSIBLE_WEAPONS.put(6, "mark9");
        POSSIBLE_WEAPONS.put(7, "devastator");
        POSSIBLE_WEAPONS.put(8, "mjolnir");
        POSSIBLE_WEAPONS.put(9, "hellbore");
        POSSIBLE_WEAPONS.put(10, "MSS_tribeam");

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        String weaponToEquip = POSSIBLE_WEAPONS.get(MathUtils.getRandomNumberInRange(0, POSSIBLE_WEAPONS.size() - 1));

        // Finally, applies the weapons in the correct slots
        ship.getVariant().clearSlot("WS0001");
        ship.getVariant().addWeapon("WS0001", weaponToEquip);
    }

    // Prevents the hullmod from being put on ships
    @Override
    public boolean isApplicableToShip(ShipAPI ship)
    {
        return false;
    }
}
