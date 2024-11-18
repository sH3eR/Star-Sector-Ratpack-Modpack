package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

//Original code by Tartiflette, modified to enable swapping individual weapons
public class MSS_SkysplitterMWeaponSwap extends BaseHullMod {
        
    // map containing all the different weapon options for the center slot
    public Map<Integer,String> CENTER_SELECTOR = new HashMap<>();
    {
        CENTER_SELECTOR.put(0, "MSS_MegaLaserCannon");
        CENTER_SELECTOR.put(1, "MSS_MarkIXR");
        CENTER_SELECTOR.put(2, "MSS_TigerGL");
        CENTER_SELECTOR.put(3, "MSS_Onbu_Railgun");
    }
    
    // map that swaps to the weapon and assigns it an index, so use the options you want to see
    private final Map<String, Integer> SWITCH_TO_CENTER = new HashMap<>();
    {
        SWITCH_TO_CENTER.put("MSS_MegaLaserCannon", 3);
        SWITCH_TO_CENTER.put("MSS_MarkIXR", 0);
        SWITCH_TO_CENTER.put("MSS_TigerGL", 1);
        SWITCH_TO_CENTER.put("MSS_Onbu_Railgun", 2);
    }
    
    // map containing the hullmod that corresponds to each weapon option
    private final Map<Integer,String> CENTERSWITCH = new HashMap<>();
    {
        CENTERSWITCH.put(0,"MSS_selector_Avenger");
        CENTERSWITCH.put(1,"MSS_selector_MarkIXR");
        CENTERSWITCH.put(2,"MSS_selector_TigerGL");
        CENTERSWITCH.put(3,"MSS_selector_Onbu");
    }
    
    // ID of the center slot
    private final String centerslotID = "CENTERB";    
    
  // checks if any of the selector hullmods are present, if not it triggers the weapon switch
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) 
    {
    boolean toSwitchCenter=true;
        for(int i=0; i<SWITCH_TO_CENTER.size(); i++){
            if(stats.getVariant().getHullMods().contains(CENTERSWITCH.get(i))){
                toSwitchCenter=false;
            }
        }

        //remove the weapons to change and swap the hullmod for the next fire mode
        if(toSwitchCenter){        
            //select new fire mode
            int selected;       
            boolean random=false;
            if(stats.getVariant().getWeaponSpec(centerslotID)!=null){
                selected=SWITCH_TO_CENTER.get(stats.getVariant().getWeaponSpec(centerslotID).getWeaponId());
            } else {
                selected=MathUtils.getRandomNumberInRange(0, SWITCH_TO_CENTER.size()-1);
                random=true;
            }
            
            //add the proper hullmod
            stats.getVariant().addMod(CENTERSWITCH.get(selected));

            //clear the weapons to replace
            stats.getVariant().clearSlot(centerslotID);
            //select and place the proper weapon              
            String toInstallCenter=CENTER_SELECTOR.get(selected);
            stats.getVariant().addWeapon(centerslotID, toInstallCenter);
            
            if(random){
                stats.getVariant().autoGenerateWeaponGroups();
            }
        }
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        
        if(ship.getOriginalOwner()<0){
            //this emoves the weapons if you strip, but basically useless if you scuttle...
			//TODO: Make script that removes these weapons if they ever appear in cargo
            if(
                    Global.getSector()!=null && 
                    Global.getSector().getPlayerFleet()!=null && 
                    Global.getSector().getPlayerFleet().getCargo()!=null && 
                    Global.getSector().getPlayerFleet().getCargo().getStacksCopy()!=null &&
                    !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()
                    ){
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()){
                    if(
                            s.isWeaponStack() && (
                                CENTER_SELECTOR.containsValue(s.getWeaponSpecIfWeapon().getWeaponId())
                                ) 
                            ){
                        Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                    }
                }
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) { 
        if (index == 0) return "A";
        if (index == 1) return "B";
        if (index == 2) return "C";
        if (index == 3) return "D";              
        return null;
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ( ship.getHullSpec().getHullId().startsWith("MSS_"));	
    }
}
