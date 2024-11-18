//by Tartiflette, 
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.scripts.plugins.FakeBeamPlugin;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class MSS_fastbeamEffect implements BeamEffectPlugin
{
    private final Color PARTICLE_COLOR = new Color(50, 175, 250, 255);
    private boolean hasFired=false;
    private final float WIDTH = 16;
    private float timer = 0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        
        if(beam.getBrightness()==1) {            
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            
            if (MathUtils.getDistanceSquared(start, end)==0){
                return;
            }
            
            timer+=10*amount;
            if (timer>=2f){
                timer=0;
                hasFired=false;
            }
               
            if (!hasFired){   
                hasFired=true;
                
                if (beam.getDamageTarget()!=null){
                    //visual effect
                    engine.spawnExplosion(
                        //where
                        end,
                        //speed
                        (Vector2f) new Vector2f(0,0),
                        //color
                        PARTICLE_COLOR,
                        //size
                        MathUtils.getRandomNumberInRange(50f,100f),
                        //duration
                        0.2f
                    );
                }
                
                //play sound (to avoid limitations with the way weapon sounds are handled)
                Global.getSoundPlayer().playSound("MSS_beamcannon_fire", 1f, 1f, start, beam.getSource().getVelocity());

                //create the beam
                Map <String,Float> VALUES = new HashMap<>();
                VALUES.put("t", 0.5f); //duration
                VALUES.put("w", WIDTH/2); //width
                VALUES.put("h", MathUtils.getDistance(start, end)); //length
                VALUES.put("x", start.x); //origin X
                VALUES.put("y", start.y); //origin Y
                VALUES.put("a", VectorUtils.getAngle(start, end)); //angle

                //Add the beam to the plugin
                FakeBeamPlugin.addMember(VALUES);
                
                //weapon glow
                engine.addHitParticle(
                        start,
                        new Vector2f(),
                        75,
                        1f,
                        0.3f,
                        new Color(50,100,255,255)
                );
                engine.addHitParticle(
                        start,
                        new Vector2f(),
                        50,
                        1f,
                        0.1f,
                        Color.WHITE
                );
            }
            
            if (beam.didDamageThisFrame()){
                //visual effect
                engine.spawnExplosion(
                    //where
                    end,
                    //speed
                    (Vector2f) new Vector2f(0,0),
                    //color
                    PARTICLE_COLOR,
                    //size
                    MathUtils.getRandomNumberInRange(50f,100f),
                    //duration
                    0.2f
                );
            }
        }
        if(beam.getWeapon().getChargeLevel()<1)
        {
            hasFired=false;
            timer = 0;
        }
    }
}