package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class MSS_safetiesAICapital implements ShipSystemAIScript {
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private boolean runOnce = false;
    private List<WeaponAPI> weapons=new ArrayList<>();
    private IntervalUtil timer= new IntervalUtil(0.5f,1);
    private boolean SO_build=false;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
        timer.randomize();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){       
        
        if (engine.isPaused() || ship.getShipAI()==null || SO_build) {
            return;
        }        

        if(!runOnce){
            runOnce=true;
            weapons=ship.getAllWeapons();
            if(ship.getVariant().getHullMods().contains("safetyoverrides")){
                SO_build=true;
                return;
            }
                
        }
        
        //prevent the system activation if retreating
        if (ship.isRetreating()){
            if(system.isActive()){ship.useSystem();}
            return;
        }
        
        timer.advance(amount);
        
        if (timer.intervalElapsed()) {
            if(target!=null && !weapons.isEmpty()){
                
                float active=weapons.size();
                for(WeaponAPI w:weapons){
                    if(w.isDisabled()||w.isPermanentlyDisabled()){
                        active--;
                    }
                }
                active/=weapons.size();
                
                if(!system.isActive()){
                    if (
                            MathUtils.isWithinRange(ship, target, 600) //target in range once the system is active
                            && ship.getFluxTracker().getFluxLevel()<0.5  //enough flux to make some damage
                            && active>0.75f //weapons' ready
                            && AIUtils.canUseSystemThisFrame(ship) //system's ready
                            ){                        
                        ship.useSystem();
                        timer.setElapsed(-3); //minimum time before checking again
                    }
                } else if(!MathUtils.isWithinRange(ship, target, 550)
                        || ship.getFluxTracker().getFluxLevel()>0.9
                        || active<=0.5f                        
                        ){                    
                    ship.useSystem();
                }
            }
        }
    }
}