package data.scripts.ix.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class AssistedCommand {
	
	public static float ACCESS = 0.3f;
	public static float FLEET_SIZE = 25f;
	public static String CORE = "ix_panopticon";
	public static String NODE = "ix_panopticon_node";
	public static String PLAYER_CORE = "ix_panopticon_player_core";
	public static String PLAYER_NODE = "ix_panopticon_player_node";
	
	public static class Level1 implements MarketSkillEffect {
		
		//can't use colony condition check, effect does not apply for player admin
		public void apply(MarketAPI market, String id, float level) {
			if (market == null) return;
			if (market.hasFunctionalIndustry(CORE)
						|| market.hasFunctionalIndustry(NODE)
						|| market.hasFunctionalIndustry(PLAYER_CORE)
						|| market.hasFunctionalIndustry(PLAYER_NODE)) {
				if (market.getAccessibilityMod() != null) market.getAccessibilityMod().modifyFlat(id, ACCESS, "AI assisted command");
			}
		}

		public void unapply(MarketAPI market, String id) {
			if (market == null) return;
			if (market.getAccessibilityMod() != null) market.getAccessibilityMod().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)Math.round(ACCESS * 100f) + "% accessibility when colony is under Panopticon Monitoring";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level2 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if (market == null) return;
			if (market.hasFunctionalIndustry(CORE)
					|| market.hasFunctionalIndustry(NODE)
					|| market.hasFunctionalIndustry(PLAYER_CORE)
					|| market.hasFunctionalIndustry(PLAYER_NODE)) {
				market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, FLEET_SIZE / 100f, "AI assisted command");
			}
		}
		
		public void unapply(MarketAPI market, String id) {
			if (market == null) return;
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)Math.round(FLEET_SIZE) + "% fleet size when colony is under Panopticon Monitoring";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}