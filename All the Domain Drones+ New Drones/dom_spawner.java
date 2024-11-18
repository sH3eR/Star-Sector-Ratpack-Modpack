package data.hullmods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;

import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class dom_spawner extends BaseHullMod {

    CombatEngineAPI engine;
    List<ShipAPI> ships = new ArrayList<>();
    static List<String> variants = new ArrayList<>();
    static Map<String, Integer> spawner = new HashMap<String, Integer>();

	static {
        //variant id
        variants.add("variant_id");
        variants.add("variant_id");

        //spawner ship, number to spawn
        spawner.put("ship_id", 2);
    }

    private String getRandomVariant(){
        Random rand = new Random();
        return variants.get(rand.nextInt(variants.size()));
    }

    private boolean getShipValidity(ShipAPI shipToTest){
        if(!ships.contains(shipToTest) && !shipToTest.isHulk()){
            return true;
        }
        return false;
    }

    private void setShipValidity(ShipAPI shipToAdd){
        this.ships.add(shipToAdd);
    }

    private void resetShipValidity(){
        this.ships.clear();
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

	public void advanceInCombat(ShipAPI ship, float amount) {
        if(Global.getCurrentState() == GameState.CAMPAIGN){
            resetShipValidity();
        } else if(getShipValidity(ship) && Global.getCurrentState() == GameState.COMBAT){

            if(!spawner.containsKey(ship.getHullSpec().getBaseHullId())) return;

            engine = Global.getCombatEngine();
            setShipValidity(ship);

            int number_to_spawn = spawner.get(ship.getHullSpec().getBaseHullId());

            for (int i = 0; i < number_to_spawn; i++) {
                float facing = ship.getFacing() + 15f * ((float) Math.random() - 0.5f);

                Vector2f loc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 3);

                for (int j = 0; j < 10; j++){
                    loc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 3);
                    boolean retry = false;

                    for (ShipAPI s : CombatUtils.getShipsWithinRange(loc, 200)){
                        if(CollisionUtils.isPointWithinCollisionCircle(loc, s)){
                            retry = true;
                            break;
                        }
                    }

                    if(!retry) break;
                }

                String variantId = null;
                variantId = getRandomVariant();

                PersonAPI captain = Global.getSettings().createPerson();
			    captain.setPersonality(Personalities.RECKLESS);
                engine.getFleetManager(ship.getOriginalOwner()).spawnShipOrWing(variantId, loc, facing, 0f, captain);
            }
        }
    }

    @Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

    public String getUnapplicableReason(ShipAPI ship) {	
		return null;
	}
}
