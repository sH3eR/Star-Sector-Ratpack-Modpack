package DE.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;

// thanks boggled
public class DE_Unpropaganda extends BaseIndustry {

	/*public Boggled_Atmosphere_Processor() {
	}*/
	public static float IMPROVE_STABILITY_BONUS = 1.0F;
	private int daysspent = 0;
	private int timespent = 0;
	public static int requiredDaysToRemovePatriot = 200;
	// should be 200
	public static float UPKEEP_MULT = 0.75F;
	public static int DEMAND_REDUCTION = 1;

	public boolean canBeDisrupted() {
		return true;
	}

	public void advance(float amount) {
		super.advance(amount);
		CampaignClockAPI clock;
		MessageIntel intel;
		if (this.market.hasCondition("DE_Patrioticfervor") && this.isFunctional()) {
			clock = Global.getSector().getClock();

			if (clock.getDay() != this.timespent) {
				++this.daysspent;
				this.timespent = clock.getDay();
			}

			if (this.daysspent >= requiredDaysToRemovePatriot) {
				if (this.market.isPlayerOwned()) {
					intel = new MessageIntel("Patriotic Fervor on " + this.market.getName(), Misc.getBasePlayerColor());
					intel.addLine("    - Dispelled");
					intel.setIcon(Global.getSector().getFaction(Factions.DIKTAT).getCrest());
					intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
					Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
				}

				if (this.market.hasCondition("DE_Patrioticfervor")) {
					this.market.removeCondition("DE_Patrioticfervor");
				}

				if (this.market.hasCondition("DE_Patrioticfervor_PAGSM")) {
					this.market.removeCondition("DE_Patrioticfervor_PAGSM");
				}
			}
		}
	}

		public void apply () {
			super.apply(true);
			int size = this.market.getSize();
			// this.demand("heavy_machinery", size);
		}

		public void unapply () {
			super.unapply();
		}

		public boolean isAvailableToBuild () {
			return (this.market.hasCondition("DE_Patrioticfervor") || this.market.hasCondition("DE_Patrioticfervor_PAGSM"));
		}

		public boolean showWhenUnavailable () {
			return !(this.market.hasCondition("DE_Patrioticfervor") || this.market.hasCondition("DE_Patrioticfervor_PAGSM"));
		}

		public String getUnavailableReason () {
			if (!this.market.hasCondition("DE_Patrioticfervor") || !this.market.hasCondition("DE_Patrioticfervor_PAGSM")) {
				return "Planet does not have Patriotic Fervor or Company Loyalty";
			} else {
				return "no idea why this is popping up tbh";
			}
		}

		protected void addRightAfterDescriptionSection (TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode){
			float opad = 10.0F;
			Color highlight = Misc.getHighlightColor();
			Color bad = Misc.getNegativeHighlightColor();
			Color good = Misc.getPositiveHighlightColor();
			if (this.isDisrupted() && (this.market.hasCondition("DE_Patrioticfervor") || this.market.hasCondition("DE_Patrioticfervor_PAGSM")) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !this.isBuilding()) {
				tooltip.addPara("De-propagandisation progress is stalled while the facility is disrupted.", bad, opad);
			}

		}

		protected boolean hasPostDemandSection ( boolean hasDemand, Industry.IndustryTooltipMode mode){
			return false;
		}

	/*protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
		boolean shortage = false;
		Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"heavy_machinery"});
		if ((Integer)deficit.two != 0) {
			shortage = true;
		}

		if (shortage && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !this.isBuilding()) {
			float opad = 10.0F;
			Color bad = Misc.getNegativeHighlightColor();
			if ((Integer)deficit.two != 0) {
				tooltip.addPara("The atmosphere processor is inactive due to a shortage of heavy machinery.", bad, opad);
			}
		}

	}*/

	/*public float getPatherInterest() {
		return super.getPatherInterest() + 2.0F;
	}*/

		public boolean canImprove () {
			return false;
		}

		public boolean canInstallAICores () {
			return false;
		}
	}
