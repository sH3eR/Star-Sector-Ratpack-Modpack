//by Tartiflette, 
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MSS_gatlingEffect implements BeamEffectPlugin {
    private final Color PARTICLE_COLOR = new Color(215, 225, 255, 255);
    private boolean hasFired = false;
    private final float WIDTH = 60;
    private float timer = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }

        if (beam.getBrightness() == 1) {
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();

            if (MathUtils.getDistanceSquared(start, end) == 0) {
                return;
            }

            if (!hasFired) {
                hasFired = true;

                if (beam.getDamageTarget() != null) {
                    //visual effect
                    engine.addHitParticle(
                            end,
                            new Vector2f(),
                            150,
                            1f,
                            0.1f,
                            Color.WHITE
                    );
                    engine.spawnExplosion(
                            //where
                            end,
                            //speed
                            (Vector2f) new Vector2f(0, 0),
                            //color
                            PARTICLE_COLOR,
                            //size
                            MathUtils.getRandomNumberInRange(50f, 100f),
                            //duration
                            0.2f
                    );
                }

                //play sound (to avoid limitations with the way weapon sounds are handled)
                Global.getSoundPlayer().playSound("SKR_gatling_fire", 1f, 1f, start, beam.getSource().getVelocity());

                //weapon glow
                              engine.addHitParticle(
                                     start,
                                       new Vector2f(),
                                       75,
                                       1f,
                                       0.3f,
                                       new Color(215,225,255,255)
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

                if (beam.didDamageThisFrame()) {
                    //visual effect
                    engine.spawnExplosion(
                            //where
                            end,
                            //speed
                            (Vector2f) new Vector2f(0, 0),
                            //color
                            PARTICLE_COLOR,
                            //size
                            MathUtils.getRandomNumberInRange(50f, 100f),
                            //duration
                            0.2f
                    );
                }

                float theWidth = WIDTH * (0.5f * (float) FastTrig.cos(60 * Math.PI * Math.min(timer, 0.05f)) + 0.5f);
                beam.setWidth(theWidth);


                timer += amount;
                if (timer >= 0.125f) {
                    timer = 0 + (float) Math.random() * 0.02f;
                    hasFired = false;
                    float offset = ((float) Math.random() - 0.5f) * 5f;
                    beam.getWeapon().ensureClonedSpec();
                    beam.getWeapon().getSpec().getHardpointAngleOffsets().set(0, offset);
                    beam.getWeapon().getSpec().getTurretAngleOffsets().set(0, offset);
                    beam.getWeapon().getSpec().getHiddenAngleOffsets().set(0, offset);
                }
            }

            if (beam.getWeapon().getChargeLevel() < 1) {
                hasFired = false;
                timer = 0;
            }
        }
    }
