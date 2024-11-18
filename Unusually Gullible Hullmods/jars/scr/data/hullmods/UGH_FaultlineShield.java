package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShieldAPI.*;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class UGH_FaultlineShield extends BaseHullMod {
        private static final String ugh_faultline_id = "UGH_FaultlineShield";
        
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
            stats.getFluxDissipation().modifyMult(id, 0.5f);
            stats.getShieldUpkeepMult().modifyMult(id, 2f);
            stats.getShieldAbsorptionMult().modifyMult(id, 1.5f);
	}

        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "Hard Flux";
		if (index == 1) return "2x";
		if (index == 2) return "50%";
		if (index == 3) return "halves";
		if (index == 4) return "25%";
		if (index == 5) return "75%";
		if (index == 6) return "50%";
		return null;
	}
	
        @Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship == null || ship.getShield() == null) return "Ship has no shields";
            return null;
	}
		
        @Override
	public boolean isApplicableToShip(ShipAPI ship) {
            return ship != null && (ship.getShield() != null) && 
                    (ship.getHullSpec().getDefenseType() == ShieldType.FRONT || 
                    ship.getHullSpec().getDefenseType() == ShieldType.OMNI);
	}
	
        @Override
	public void advanceInCombat(ShipAPI ship, float amount) {
            if (!ship.isAlive()) return;		
            MutableShipStatsAPI stats = ship.getMutableStats();
            ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
            float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
            float Flux = ship.getFluxTracker().getFluxLevel();
            float InFlux = 0;
            float ReFlux = 1f;
			
            ship.getFluxTracker().setHardFlux(0);
		
            if (Flux > 0.25 && Flux < 0.75) { 
                InFlux = Flux - 0.25f; 
                ReFlux = 4f * (Flux - 0.25f);
                //ReFlux = (1f - Flux) * 0.5f;
            }
            else if (Flux >= 0.75) { 
                InFlux = 0.5f; 
                ReFlux = 2f;
            }
            if (ship.getFluxTracker().isVenting() == true) {
                ReFlux = 2f;
            }
            
            //stats.getCriticalMalfunctionChance().modifyFlat(ugh_faultline_id, InFlux * effect);
            //stats.getWeaponMalfunctionChance().modifyFlat(ugh_faultline_id, InFlux * effect * 0.1f);
            if (!ship.getFluxTracker().isVenting()){
                stats.getFluxDissipation().modifyMult(ugh_faultline_id, ReFlux);
            }
            
            if (Flux > 0.25f){
                if (ship.getShield().isOn()) {
                    stats.getCriticalMalfunctionChance().modifyFlat(ugh_faultline_id, InFlux * effect);
                    stats.getWeaponMalfunctionChance().modifyFlat(ugh_faultline_id, InFlux * effect * 0.1f);
                } else {
                    stats.getCriticalMalfunctionChance().unmodify(ugh_faultline_id);
                    stats.getWeaponMalfunctionChance().unmodify(ugh_faultline_id);
                }
                if (ship == playerShip) {
                    Global.getCombatEngine().maintainStatusForPlayerShip("ugh_faultline_ui1", "graphics/icons/hullsys/phase_cloak.png",
			"Faultline:", "" + Math.round((ReFlux * 100 / 4f) + 50f) + "% total Flux Dissipation",true);
                    if (ship.getShield().isOn()) {
                        Global.getCombatEngine().maintainStatusForPlayerShip("ugh_faultline_ui2", "graphics/icons/hullsys/phase_cloak.png",
                            "Faultline:", "" + Math.round((ReFlux * 100 / 4f)) + "% ill-advised events",true);
                    }
                }
            }
	}
}