package bcom;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomValue {
    List<String> id = new ArrayList<>();
    List<String> projectileId = new ArrayList<>();
    List<WeaponAPI.WeaponSize> weaponSize = new ArrayList<>();
    List<WeaponAPI.WeaponType> weaponType = new ArrayList<>();
    List<ShipAPI.HullSize> hullSize = new ArrayList<>();
    List<String> techStyle = new ArrayList<>();
    List<String> tags = new ArrayList<>();
    List<String> hints = new ArrayList<>();
    Map<String,Float> effects = new HashMap<>();
    public List<String> getHints(){return hints;}

    public List<String> getId() {
        return id;
    }

    public List<String> getProjectileId() {
        return projectileId;
    }

    public List<WeaponAPI.WeaponType> getWeaponType() {
        return weaponType;
    }

    public List<WeaponAPI.WeaponSize> getWeaponSize() {
        return weaponSize;
    }
    public List<ShipAPI.HullSize> getHullSize() {
        return hullSize;
    }
    public List<String> getTechStyle() {
        return techStyle;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, Float> getEffects() {
        return effects;
    }

}
