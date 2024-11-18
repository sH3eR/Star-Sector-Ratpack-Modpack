package data.princess;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.EngineSpecAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.StringBuilder;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import java.util.List;

import java.awt.Color;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lazywizard.lazylib.ui.LazyFont.DrawableString;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class CarrierUI extends BaseEveryFrameCombatPlugin
	{
	private static Logger logger = Global.getLogger(CarrierUI.class);
	
//	private static volatile JavaSourceClassLoader loader;

	private Class ac;
	
	private Object heresy;
	private MethodHandle heresyLoader;
	
	public Color GREEN;
	public Color BLUE;
	
	public DrawableString font;
	
	public void init(CombatEngineAPI engine)
		{
		GREEN = Global.getSettings().getColor("textFriendColor");
		BLUE = Global.getSettings().getColor("textNeutralColor");
		
			{
			// It doesn't matter if the code is too long - the basic compiler will spit equally long IL from shorter code. Maybe with less variable usage, but that's VM's job and, frankly, only happens once on game load.
			try
				{
				LazyFont fontd = LazyFont.loadFont("graphics/fonts/victor14.fnt");
				font = fontd.createText();
				}
			catch (Throwable ex)
				{
				logger.log(Level.INFO, "oop", ex);
				}
			}
		}
		
	public void advance(float amount, List<InputEventAPI> events)
		{
		if (Global.getCombatEngine().getCombatUI() == null || Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD())
			{
		//	logger.log(Level.INFO, "beep");
			return;
			}
		
		ShipAPI ship = Global.getCombatEngine().getPlayerShip();
		if (ship == null || !ship.hasLaunchBays())
			{
		//	logger.log(Level.INFO, "boop");
			return;
			}
		
		List<FighterLaunchBayAPI> bays = ship.getLaunchBaysCopy();
		
	//	logger.log(Level.INFO, "baap" + bays.size());
		
		for (int i = 0; i < bays.size(); i++)
			{
			FighterLaunchBayAPI bay = (FighterLaunchBayAPI)bays.get(i);
			
			
			float perc = bay.getTimeUntilNextReplacement();
			if (perc + 1f < 0.00005f) continue; // perc is only negative if it's -1; no abs necessary.
			perc = ((1f - perc / bay.getCurrReplacementIntervalDuration()) * 100);
		
			Color borderCol = GREEN;
			if (!ship.isAlive())
				borderCol = BLUE;
				
			float alpha = 1;
			if (Global.getCombatEngine().isUIShowingDialog())
				alpha = 0.28f;
			
			// For the record this isn't quite how alpha works.
			Color shadowcolor = new Color(0f, 0f, 0f, 1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
			Color color = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f, alpha * (borderCol.getAlpha() / 255f) * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
			
			Vector2f loc = new Vector2f(232f + 53f * i, 31f);
			
			if(Global.getSettings().getScreenScaleMult() != 1)
				{
				loc.scale(Global.getSettings().getScreenScaleMult());
				font.setFontSize(14 * Global.getSettings().getScreenScaleMult());
				}
			
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			font.setText(String.format("%s%%", (int) perc)); // Easiest way to toString in java, don't judge. Also, cast from float to int does floor.
			font.setMaxWidth(46 * Global.getSettings().getScreenScaleMult());
			font.setMaxHeight(14 * Global.getSettings().getScreenScaleMult());
			font.setColor(shadowcolor);
			font.draw(loc);
			font.setColor(color);
			font.draw(loc.translate(-1f, 1f));
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
			GL11.glPopAttrib();
			}
		}
	}
