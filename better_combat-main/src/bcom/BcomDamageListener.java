package bcom;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;

public class BcomDamageListener implements DamageListener {
    @Override
    public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
        ShipAPI ship = (ShipAPI)target;
        float pptPerHullPercent = Settings.getPptPerHullDamagePercent(ship.getHullSpec());
        if(pptPerHullPercent==0f){ //cannot be 0
            pptPerHullPercent = Float.MIN_NORMAL;
        }
        float crPerHullPercent =Settings.getCrReduction(ship.getHullSpec());

        if(result.getDamageToHull()!=0 && ship.getHullLevel()<1-Settings.getCrReductionHullOffset(ship.getHullSpec())){
             float damage = result.getDamageToHull();
            float factor = damage * (1/ship.getHullSpec().getHitpoints());
            float peakSeconds = ship.getHullSpec().getNoCRLossTime();
            float currentSeconds = ship.getPeakTimeRemaining();
            float pptDamage =  factor * pptPerHullPercent;
            if(pptDamage>=currentSeconds){
                ship.getMutableStats().getPeakCRDuration().modifyFlat("bcom",-peakSeconds);
                pptDamage = pptDamage - currentSeconds;
            }else{
                float newValue = -pptDamage;
                if(ship.getMutableStats().getPeakCRDuration().getFlatBonus("bcom")!=null){
                    newValue = newValue + ship.getMutableStats().getPeakCRDuration().getFlatBonus("bcom").value;
                }
                ship.getMutableStats().getPeakCRDuration().modifyFlat("bcom",newValue);
                 return;
            }
            float crDamage = (pptDamage /(pptPerHullPercent)) * crPerHullPercent;
            if(ship.getCurrentCR()-crDamage<0){
                ship.setCurrentCR(0f);
            }
            ship.setCurrentCR(ship.getCurrentCR()-crDamage);
        }
    }
}
