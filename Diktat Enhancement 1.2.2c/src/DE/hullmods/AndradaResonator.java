package DE.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.magiclib.util.MagicRender;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;


public class AndradaResonator extends BaseHullMod {
    // Thanks for Nia's Adlerauge effect script for parts of this
    private static final float RANGE = 1000f;
    private static final String id = "andradamodulator_data";
    public static final Color JITTER_COLOR = new Color(200,0,120,255);
    public static final Color JITTER_UNDER_COLOR = new Color(140,0,120,255);
    private final HashSet<ShipAPI> affectedships = new HashSet<ShipAPI>();
    private final List<ShipAPI> purgelist = new ArrayList<ShipAPI>();
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        List<ShipAPI> shiplist = CombatUtils.getShipsWithinRange(ship.getLocation(), RANGE);

        MagicRender.objectspace(
                // this needs to be loaded in settings.json, and then you can just stick in the file path
                Global.getSettings().getSprite("graphics/DE/fx/de_drivecircle.png"),
                // what the ring will be centered on. In this case, the ship using the system. what "ship" means
                // was determined in the second part of this public void thingamajig.
                ship,
                new Vector2f(),
                new Vector2f(),
                // the radius of the circle will be about half this
                new Vector2f(2000, 2000),
                new Vector2f(-100, -100),
                0,
                120,
                // set to true to make the ring turn with the ship
                false,
                // the color, obviously. I don't think the alpha value really works, instead I'm using a
                // very transparent sprite
                new Color(200,0,120,255),
                true,
                0.5f,
                0f,
                0.5f,
                true
        );
        // add the ship to a list of targets
        for (ShipAPI ship2 : shiplist) {
            if (ship2.getVariant().hasHullMod("andrada_mods") && ship2.getOwner() == ship.getOwner() && ship2 != ship) {
                //String str4 = String.format("In range: %s, has hull mod %s, same owner %s", MathUtils.isWithinRange(ship2, ship, RANGE), ship2.getVariant().hasHullMod("andrada_mods"), ship2.getOwner() == ship.getOwner());
                //Global.getLogger(this.getClass()).info(str4);
                affectedships.add(ship2);
            }
        }
        // applying effects to targets
        for (ShipAPI target : affectedships) {
            // check if our dear target is still truly in range
            if (MathUtils.getDistance(target.getLocation(), ship.getLocation()) <= RANGE && target.isAlive()) {
                // will apply when ship is in range
                target.getMutableStats().getMaxSpeed().modifyFlat(id, 10f);
                target.getMutableStats().getAcceleration().modifyPercent(id, 20f);
                target.getMutableStats().getDeceleration().modifyPercent(id, 20f);
                target.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(id, 10f);
                target.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(id, 10f);
                target.getMutableStats().getEnergyRoFMult().modifyPercent(id, 10f);
                target.getMutableStats().getEnergyRoFMult().modifyPercent(id, 10f);
                target.getMutableStats().getTimeMult().modifyPercent(id, 5f);
                target.setJitter(this, JITTER_COLOR, 0.5f, 1, 0, 0);
                target.setJitterUnder(this, JITTER_UNDER_COLOR, 0.5f, 3, 0f, 0);
            } else {
                // he's not! rattle em code!
                // applies when ship is out of range
                target.getMutableStats().getMaxSpeed().unmodifyFlat(id);
                target.getMutableStats().getAcceleration().unmodify(id);
                target.getMutableStats().getDeceleration().unmodify(id);
                target.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(id);
                target.getMutableStats().getEnergyWeaponRangeBonus().unmodify(id);
                target.getMutableStats().getEnergyRoFMult().unmodify(id);
                target.getMutableStats().getEnergyRoFMult().unmodify(id);
                target.getMutableStats().getTimeMult().unmodify(id);
                purgelist.add(target);
            }
        }
        // clears affected ships list when combat ends
        if (engine.isCombatOver()) {
            affectedships.clear();
        }
        // removes ship from the affected ships list
        for (ShipAPI purge : purgelist) {
            affectedships.remove(purge);
            //purgelist.clear();
        }
    }
}






