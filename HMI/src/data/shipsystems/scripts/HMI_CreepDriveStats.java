package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class HMI_CreepDriveStats extends BaseShipSystemScript {

    public static final Color JITTER_COLOR = new Color(165, 90, 255, 10);
    public static final Color JITTER_UNDER_COLOR = new Color(255, 255, 255, 0);
    public static final float MAX_TIME_MULT = 10f;
    public static final float MIN_TIME_MULT = 0.1f;

    private float totalDurationWarped = 0f;
    private boolean warping = false;

    protected Object STATUSKEY1 = new Object();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        boolean player;
        String actualId = id;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            actualId = actualId + "_" + ship.getId();
        } else {
            return;
        }

        if (player) {
            maintainStatus(ship);
        }

        float jitterLevel = effectLevel;
        float jitterRangeBonus = 0;
        float maxRangeBonus = 10f;
        if (null != state) {
            switch (state) {
                case IN:
                    jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
                    if (jitterLevel > 1) {
                        jitterLevel = 1f;
                    }
                    jitterRangeBonus = jitterLevel * maxRangeBonus;
                    break;
                case ACTIVE:
                    jitterLevel = 1f;
                    jitterRangeBonus = maxRangeBonus;
                    break;
                case OUT:
                    jitterRangeBonus = jitterLevel * maxRangeBonus;
                    break;
                default:
                    break;
            }
        }
        jitterLevel = (float) Math.sqrt(jitterLevel);

        ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);

        float shipTimeMult;
        if (!ship.isPhased()) {
            shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
            ship.getMutableStats().getCRLossPerSecondPercent().unmodify(actualId);

            warping = false;
        } else {
            /* Oh shit! */
            shipTimeMult = 100f / 3f;
            totalDurationWarped += Global.getCombatEngine().getElapsedInLastFrame();
            if (ship.getTimeDeployedForCRReduction() >= ship.getHullSpec().getNoCRLossTime()) {
                ship.getMutableStats().getCRLossPerSecondPercent().modifyMult(actualId, 3f);
            }
            ship.getMutableStats().getPeakCRDuration().modifyFlat(actualId, -totalDurationWarped * 2f
                    / ship.getMutableStats().getPeakCRDuration().getMult());

            if (!warping) {
                if (!player) {
                    Global.getSoundPlayer().playSound("gun_out_of_ammo", 1f, 0.75f, ship.getLocation(),
                            new Vector2f());
                }
                warping = true;
            }
        }
        stats.getTimeMult().modifyMult(actualId, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(actualId, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(actualId);
        }

        ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0, 0, 0, 0), effectLevel, 0.5f);
        ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        String actualId = id;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            actualId = actualId + "_" + ship.getId();
        } else {
            return;
        }

        Global.getCombatEngine().getTimeMult().unmodify(actualId);
        stats.getTimeMult().unmodify(actualId);
        stats.getCRLossPerSecondPercent().unmodify(actualId);
    }

    private void maintainStatus(ShipAPI playerShip) {
        ShipSystemAPI system = playerShip.getSystem();
        if (system == null) {
            return;
        }
        String title = system.getDisplayName();
        String display = "time flow altered";

        if (playerShip.isPhased()) {
            while (Math.random() < 0.5) {
                int index = MathUtils.getRandomNumberInRange(0, title.length() - 1);
                if (index == 0) {
                    title = (char) (MathUtils.getRandom().nextInt(26) + 'a') + title.substring(index + 1);
                } else if (index == title.length() - 1) {
                    title = title.substring(0, index) + (char) (MathUtils.getRandom().nextInt(26) + 'a');
                } else {
                    title = title.substring(0, index) + (char) (MathUtils.getRandom().nextInt(26) + 'a')
                            + title.substring(index + 1);
                }
            }
            while (Math.random() < 0.5) {
                int index = MathUtils.getRandomNumberInRange(0, display.length() - 1);
                if (index == 0) {
                    display = (char) (MathUtils.getRandom().nextInt(26) + 'a') + display.substring(index + 1);
                } else if (index == display.length() - 1) {
                    display = display.substring(0, index) + (char) (MathUtils.getRandom().nextInt(26) + 'a');
                } else {
                    display = display.substring(0, index) + (char) (MathUtils.getRandom().nextInt(26) + 'a')
                            + display.substring(index + 1);
                }
            }
        }
        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, system.getSpecAPI().getIconSpriteName(), title,
                display, false);
    }
}
