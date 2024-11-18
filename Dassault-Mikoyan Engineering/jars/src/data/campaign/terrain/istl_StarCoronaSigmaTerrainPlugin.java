package data.campaign.terrain;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.terrain.AuroraRenderer;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager;
import com.fs.starfarer.api.impl.campaign.terrain.RangeBlockerUtil;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.EnumSet;

public class istl_StarCoronaSigmaTerrainPlugin extends StarCoronaTerrainPlugin {

//	public static final float ARC = 100f;

//      // Turning this off for now, but I wanna do fun shit with directional coronae later.
//	@Override
//	public float getAuroraAlphaMultForAngle(float angle) {
//		SectorEntityToken star = params.relatedEntity.getLightSource();
//		if (star != null) {
//			float toStar = Misc.getAngleInDegrees(params.relatedEntity.getLocation(), star.getLocation());
//			float diff = Misc.getAngleDiff(toStar, angle);
//			float max = ARC / 2f;
//			if (diff < max) {
//				return Math.max(0, 1f - diff / max);
//			}
//			return 0f;
//		}
//		
//		return 1f;
//	}

	@Override
	public Color getAuroraColorForAngle(float angle) {
		if (color == null) {
			if (params.relatedEntity instanceof PlanetAPI) {
                            color = new Color(50, 30, 100, 120);
			} else {
                            color = Color.white;
			}
                            color = Misc.setAlpha(color, 155);
		}
		if (flareManager.isInActiveFlareArc(angle)) {
			return flareManager.getColorForAngle(color, angle);
		}
		return super.getAuroraColorForAngle(angle);
	}

//	@Override
//	public boolean containsPoint(Vector2f point, float radius) {
//		SectorEntityToken star = params.relatedEntity.getLightSource();
//		if (star != null) {
//			float toStar = Misc.getAngleInDegrees(params.relatedEntity.getLocation(), star.getLocation());
//			if (!Misc.isInArc(toStar, ARC, params.relatedEntity.getLocation(), point)) {
//				return false;
//			}
//		}
//		return super.containsPoint(point, radius);
//	}

	@Override
	public String getTerrainName() {
		return "Dimensional Turbulence";
	}
        
        	@Override
	protected Object readResolve() {
		super.readResolve();
		texture = Global.getSettings().getSprite("terrain", "aurora");
		//layers = EnumSet.of(CampaignEngineLayers.TERRAIN_7);
		if (renderer == null) {
			renderer = new AuroraRenderer(this);
		}
		if (flareManager == null) {
			flareManager = new FlareManager(this);
		}
		if (blocker == null) {
			blocker = new RangeBlockerUtil(360, super.params.bandWidthInEngine + 1000f);
		}
		return this;
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		float pad = 10f;
		float small = 5f;
		tooltip.addTitle("Dimensional Turbulence");
		tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);
		float nextPad = pad;
		if (expanded) {
			tooltip.addSectionHeading("Travel", Alignment.MID, small);
			nextPad = small;
		}
		tooltip.addPara("The intense radiation and gravitational distortion reduces the combat readiness of " +
						"all ships in the turbulent region at a steady pace.", nextPad);
		tooltip.addPara("The dimensional brane shifts make the planet difficult to approach.", pad);
		
		if (expanded) {
			tooltip.addSectionHeading("Combat", Alignment.MID, pad);
			tooltip.addPara("Reduces the peak performance time of ships and increases the rate of combat readiness degradation in protracted engagements.", small);
		}
	}
}
