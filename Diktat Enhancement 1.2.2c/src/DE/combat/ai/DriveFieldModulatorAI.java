package DE.combat.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class DriveFieldModulatorAI implements ShipSystemAIScript{

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private IntervalUtil timer= new IntervalUtil(0.5f,1.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        if(engine.isPaused()){
            return;
        }

        timer.advance(amount);
        if(timer.intervalElapsed()){
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship) && !AIUtils.getNearbyEnemies(ship, 3000).isEmpty() && !AIUtils.getNearbyAllies(ship, 1200).isEmpty()){
                ship.useSystem();
            }
            if(system.isActive() && AIUtils.canUseSystemThisFrame(ship) && AIUtils.getNearbyAllies(ship, 1200).isEmpty()){
                ship.useSystem();
            }
        }
    }
}