package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import exerelin.campaign.submarkets.PrismMarket;
import exerelin.utilities.StringHelper;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.*;


public class HMI_MaxSwap extends PaginatedOptions {
	
	public static final String POINTS_KEY = "hmi_BPSwapPoints";
	public static final String STOCK_ARRAY_KEY = "$hmi_BPSwapStock";
	public static final String ALREADY_SOLD_KEY = "$hmi_BPSwapAlreadySold";
	public static final float STOCK_KEEP_DAYS = 30;
	public static final int STOCK_COUNT_MIN = 7;
	public static final int STOCK_COUNT_MAX = 10;
	public static final float PRICE_POINT_MULT = 0.01f;
	//public static final float ALREADY_SOLD_MULT = 0.25f;
	public static final String PERSISTENT_RANDOM_KEY = "hmi_blueprintSwapRandom";
	
	public static final String DIALOG_OPTION_PREFIX = "hmi_blueprintSwap_pick_";
	
	public static Logger log = Global.getLogger(HMI_MaxSwap.class);
	
	protected static PurchaseInfo toPurchase = null;
	
	// Things that count as blueprints for trade-in
	//public static final Set<String> ALLOWED_IDS = new HashSet<>(Arrays.asList(new String[] {
	//	"ai_cores"
	//}));
	
	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected MarketAPI market;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected CargoAPI playerCargo;
	protected PersonAPI person;
	protected FactionAPI faction;
	protected float points;
	protected List<String> disabledOpts = new ArrayList<>();
	
	@Override
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) 
	{
		String arg = params.get(0).getString(memoryMap);
		setupVars(dialog, memoryMap);
		
		switch (arg)
		{
			case "init":
				break;
			case "hasOption":
				return hasMaxwellStation(entity.getMarket());
			case "getForSale":
				setupDelegateDialog(dialog);
				addBlueprintOptions();
				showOptions();
				break;
			case "sell":
				selectBPs();
				break;
			case "buy":
				int index = Integer.parseInt(memoryMap.get(MemKeys.LOCAL).getString("$option").substring(DIALOG_OPTION_PREFIX.length()));
				showBlueprintInfoAndPreparePurchase(index, dialog.getTextPanel());
				break;
			case "confirmPurchase":
				purchase();
				break;
		}
		
		return true;
	}
	
	/**
	 * To be called only when paginated dialog options are required. 
	 * Otherwise we get nested dialogs that take multiple clicks of the exit option to actually exit.
	 * @param dialog
	 */
	protected void setupDelegateDialog(InteractionDialogAPI dialog)
	{
		originalPlugin = dialog.getPlugin();  

		dialog.setPlugin(this);  
		init(dialog);
	}
	
	protected void setupVars(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap)
	{
		this.dialog = dialog;  
		this.memoryMap = memoryMap;
		
		entity = dialog.getInteractionTarget();
		market = entity.getMarket();
		text = dialog.getTextPanel();
		
		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();
		
		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();
		
		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();
		
		updatePointsInMemory(getPoints());
	}
	
	/**
	 * Updates available blueprint points in local memory.
	 * @param newPoints
	 */
	protected void updatePointsInMemory(float newPoints)
	{
		points = newPoints;
		memoryMap.get(MemKeys.LOCAL).set("$hmimax_BPSwap_points", points, 0);
		memoryMap.get(MemKeys.LOCAL).set("$hmimax_BPSwap_pointsStr", (int)points + "", 0);
	}
	
	@Override
	public void showOptions() {
		super.showOptions();
		for (String optId : disabledOpts)
		{
			dialog.getOptionPanel().setEnabled(optId, false);
		}
		dialog.getOptionPanel().setShortcut("hmimax_blueprintSwapMenuRetun", Keyboard.KEY_ESCAPE, false, false, false, false);
	}
	
	protected void selectBPs() {
		final CargoAPI copy = Global.getFactory().createCargo(false);
		
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			if (isBlueprints(stack)) {
				copy.addFromStack(stack);
			}
		}
		copy.sort();
		
		final float width = 310f;
		// prevents an IllegalAccessError
		final InteractionDialogAPI dialog = this.dialog;
		final Map<String, MemoryAPI> memoryMap = this.memoryMap; 
		
		dialog.showCargoPickerDialog(StringHelper.getString("hmi_misc", "HMI_BPSwapSelect"),
				Misc.ucFirst(StringHelper.getString("confirm")), 
				Misc.ucFirst(StringHelper.getString("cancel")),
						true, width, copy, new CargoPickerListener() {
			public void pickedCargo(CargoAPI cargo) {
				cargo.sort();
				for (CargoStackAPI stack : cargo.getStacksCopy()) {
					//copy.removeItems(stack.getType(), stack.getData(), stack.getSize());
					playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
					if (stack.isCommodityStack()) { // should be always, but just in case
						AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
					}
				}
				// put back in player cargo the ones we didn't sell/learn
				//playerCargo.addAll(copy);
				
				int points = (int)getPointValue(cargo);
				
				if (points > 0) {
					float newPoints = addPoints(points);
					text.setFontSmallInsignia();
					String str = StringHelper.getStringAndSubstituteToken("hmi_misc",
							"HMI_BPSwapGainedPoints", "$points", (int)points + "");
					text.addPara(str, Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), (int)points + "");
					text.setFontInsignia();
					
					updatePointsInMemory(newPoints);
				}
				
				FireBest.fire(null, dialog, memoryMap, "hmimax_BlueprintsSold");
			}
			
			@Override
			public void cancelledCargoSelection() {
			}
			
			@Override
			public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
			
				int points = (int)getPointValue(cargo);
				
				float pad = 3f;
				float small = 5f;
				float opad = 10f;

				panel.setParaOrbitronLarge();
				panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), opad);
				//panel.addPara(faction.getDisplayNameLong(), faction.getBaseUIColor(), opad);
				//panel.addPara(faction.getDisplayName() + " (" + entity.getMarket().getName() + ")", faction.getBaseUIColor(), opad);
				panel.setParaFontDefault();
				
				panel.addImage(faction.getLogo(), width * 1f, pad);
				
				
				//panel.setParaFontColor(Misc.getGrayColor());
				//panel.setParaSmallInsignia();
				//panel.setParaInsigniaLarge();
				String str = StringHelper.getStringAndSubstituteToken("hmi_misc",
						"HMI_BPSwapMsg", "$points", (int)points + "");
				panel.addPara(str, 	opad * 1f, Misc.getHighlightColor(), (int)points + "");
			}
		});
	}
	
	/**
	 * Adds the dialog options to buy blueprints.
	 */
	protected void addBlueprintOptions()
	{
		dialog.getOptionPanel().clearOptions();
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		List<PurchaseInfo> bps = getBlueprintStock(mem);
		
		int index = 0;
		for (PurchaseInfo bp : bps)
		{
			addBlueprintOption(bp, index);
			index++;
		}
		
		addOptionAllPages(StringHelper.getString("back", true), "hmimax_blueprintSwapMenuReturn");
	}
	
	protected void addBlueprintOption(PurchaseInfo info, int index)
	{
		//CargoAPI temp = Global.getFactory().createCargo(true);
		String id = info.id;
		String name = info.name;
		float cost = info.cost;
		boolean alreadyHas = false;
		String designType = "";
		FactionAPI player = Global.getSector().getPlayerFaction();
		
		switch (info.type) {
			case SHIP:
				ShipHullSpecAPI hull = Global.getSettings().getHullSpec(id);
				alreadyHas = player.knowsShip(id);
				designType = hull.getManufacturer();
				break;
			case FIGHTER:
				FighterWingSpecAPI wing = Global.getSettings().getFighterWingSpec(id);
				alreadyHas = player.knowsFighter(id);
				designType = wing.getVariant().getHullSpec().getManufacturer();
				break;
			case WEAPON:
				WeaponSpecAPI wep = Global.getSettings().getWeaponSpec(id);
				alreadyHas = player.knowsWeapon(id);
				designType = wep.getManufacturer();
				break;
			default:
				return;
		}
		
		String optId = DIALOG_OPTION_PREFIX + index;
		String str = StringHelper.getString("hmi_misc", "HMI_BPSwapPurchaseOption");
		str = StringHelper.substituteToken(str, "$name", name);
		str = StringHelper.substituteToken(str, "$points", (int)cost + "");
		str = StringHelper.substituteToken(str, "$designType", designType);
		
		if (alreadyHas)
		{
			str = "[" + StringHelper.getString("hmi_misc", "alreadyKnown") + "] " + str;
		}
		
		//log.info("Adding option: " + optId + ", " + str);
		addOption(str, optId);
		if (cost > points)
		{
			log.info("Item unavailable: " + optId);
			disabledOpts.add(optId);
		}
	}
	
	/**
	 * Prints the description of the selected blueprint and marks it as the desired purchase.
	 * @param index Index of the desired {@code BlueprintInfo} within the bluepritn info array
	 * @param text
	 */
	protected void showBlueprintInfoAndPreparePurchase(int index, TextPanelAPI text)
	{
		Description desc;
		String designType = "";
		toPurchase = getBlueprintStock(market.getMemoryWithoutUpdate()).get(index);
		switch (toPurchase.type) {
			case SHIP:
				ShipHullSpecAPI hull = Global.getSettings().getHullSpec(toPurchase.id);
				desc = Global.getSettings().getDescription(hull.getDescriptionId(), Description.Type.SHIP);
				designType = hull.getManufacturer();
				break;
			case FIGHTER:
				FighterWingSpecAPI wing = Global.getSettings().getFighterWingSpec(toPurchase.id);
				desc = Global.getSettings().getDescription(wing.getVariant().getHullSpec().getDescriptionId(), Description.Type.SHIP);
				designType = wing.getVariant().getHullSpec().getManufacturer();
				break;
			case WEAPON:
				WeaponSpecAPI wep = Global.getSettings().getWeaponSpec(toPurchase.id);
				desc = Global.getSettings().getDescription(wep.getWeaponId(), Description.Type.WEAPON);
				designType = wep.getManufacturer();
				break;
			default:
				return;
		}
		Color hl = Misc.getBasePlayerColor();
		if (!designType.isEmpty()) {
			hl = Global.getSettings().getDesignTypeColor(designType);
		}
		
		text.setFontSmallInsignia();
		text.addPara(StringHelper.getString("designType", true) + ": " + designType, hl, designType);
		text.addPara(desc.getText1FirstPara());
		text.setFontInsignia();
	}
	
	protected void purchase()
	{
		playerFleet.getCargo().addSpecial(toPurchase.getItemData(), 1);
		float newPoints = addPoints(-toPurchase.cost);
		updatePointsInMemory(newPoints);
		
		text.setFontSmallInsignia();
		String str = StringHelper.getStringAndSubstituteToken("hmi_misc",
							"HMI_BPSwapPurchased", "$name", toPurchase.name);
		text.addPara(str, Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), toPurchase.name);
		
		String costStr = (int)(toPurchase.cost) + "";
		str = StringHelper.getStringAndSubstituteToken("hmi_misc",
							"HMI_BPSwapLostPoints", "$points", costStr);
		text.addPara(str, Misc.getNegativeHighlightColor(), Misc.getHighlightColor(), costStr);
		text.setFontInsignia();
		
		// remove purchased blueprint from array
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		List<PurchaseInfo> stock = getBlueprintStock(mem);
		stock.remove(toPurchase);
		setBlueprintStock(mem, stock, false);
	}
	
	
	public static List<PurchaseInfo> getBlueprintStock(MemoryAPI mem)
	{
		if (mem.contains(STOCK_ARRAY_KEY))
			return (List<PurchaseInfo>)mem.get(STOCK_ARRAY_KEY);
		
		List<PurchaseInfo> bps = generateBlueprintStock();
		setBlueprintStock(mem, bps, true);
		return bps;
	}
	
	public static void setBlueprintStock(MemoryAPI mem, List<PurchaseInfo> stock, boolean refreshTime)
	{
		float time = STOCK_KEEP_DAYS;
		if (!refreshTime && mem.contains(STOCK_ARRAY_KEY))
			time = mem.getExpire(STOCK_ARRAY_KEY);
		
		mem.set(STOCK_ARRAY_KEY, stock, time);
	}
	
	public static void unsetBlueprintStocks()
	{
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy())
		{
			market.getMemoryWithoutUpdate().unset(STOCK_ARRAY_KEY);
		}
	}
	
	public static List<PurchaseInfo> generateBlueprintStock()
	{
		List<PurchaseInfo> blueprints = new ArrayList<>();
		Random random = getRandom();
		WeightedRandomPicker<PurchaseInfo> picker = new WeightedRandomPicker<>(random);
		FactionAPI playerFaction = Global.getSector().getPlayerFaction();

		for (ShipHullSpecAPI hull : Global.getSettings().getAllShipHullSpecs()) {
			if (!hull.hasTag("maxwellsell"))
				continue;

			String hullId = hull.getHullId();

			PurchaseInfo info = new PurchaseInfo(hullId, PurchaseType.SHIP,
					hull.getNameWithDesignationWithDashClass(), 
					getBlueprintPointValue(Items.SHIP_BP, hullId, hull.getBaseValue()));

				info.itemId = Items.SHIP_BP;
			
			picker.add(info, 3 * hull.getRarity());
		}
		for (FighterWingSpecAPI wing : Global.getSettings().getAllFighterWingSpecs()) {
			if (!wing.hasTag("rare_bp") || wing.hasTag(Tags.NO_DROP) || wing.hasTag(Tags.NO_BP_DROP))
				continue;
			String wingId = wing.getId();
			if (playerFaction.knowsFighter(wingId)) continue;
			
			PurchaseInfo info = new PurchaseInfo(wingId, PurchaseType.FIGHTER, 
					wing.getWingName(), 
					getBlueprintPointValue(Items.FIGHTER_BP, wingId, wing.getBaseValue()));
				info.itemId = Items.FIGHTER_BP;

			picker.add(info, 2 * wing.getRarity());
		}
		for (WeaponSpecAPI wep : Global.getSettings().getAllWeaponSpecs()) {
			if (!wep.hasTag("rare_bp") || wep.hasTag(Tags.NO_DROP) || wep.hasTag(Tags.NO_BP_DROP))
				continue;
			String weaponId = wep.getWeaponId();
			if (playerFaction.knowsWeapon(weaponId)) continue;
			
			PurchaseInfo info = new PurchaseInfo(weaponId, PurchaseType.WEAPON, 
					wep.getWeaponName(), 
					getBlueprintPointValue(Items.WEAPON_BP, weaponId, wep.getBaseValue()));
			info.itemId = Items.WEAPON_BP;
			picker.add(info, 2 * wep.getRarity());
		}
		
		int num = STOCK_COUNT_MIN + random.nextInt(STOCK_COUNT_MAX - STOCK_COUNT_MIN + 1);
		for (int i = 0; i < num; i++)
		{
			if (picker.isEmpty()) continue;
			blueprints.add(picker.pickAndRemove());
		}
		Collections.sort(blueprints);
		return blueprints;
	}
	
	public static boolean isBlueprints(CargoStackAPI stack)
	{
		CommoditySpecAPI spec = stack.getResourceIfResource();
		if (spec == null)
			return false;

		if (!spec.getDemandClass().equals(Commodities.AI_CORES))
			return false;


		return true;
	}
	
	public static float getPointValue(CargoAPI cargo)
	{
		float totalPoints = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			if (!isBlueprints(stack)) continue;
			
			float points = getPointValue(stack);
			
			totalPoints += points;
		}
		return totalPoints;
	}
	
	public static float getPointValue(CargoStackAPI stack)
	{
		SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
		SpecialItemData data = stack.getSpecialDataIfSpecial();
		float points = 0, base = 0;
		
		switch (spec.getId())
		{
			case Items.SHIP_BP:
			case "tiandong_retrofit_bp":
			case "roider_retrofit_bp":
				base = Global.getSettings().getHullSpec(data.getData()).getBaseValue();
				break;
			case Items.FIGHTER_BP:
			case "tiandong_retrofit_fighter_bp":
				base = Global.getSettings().getFighterWingSpec(data.getData()).getBaseValue();
				break;
			case Items.WEAPON_BP:
				base = Global.getSettings().getWeaponSpec(data.getData()).getBaseValue();
				break;
		}
		points = getBlueprintPointValue(spec.getId(), data.getData(), base);
		
		points *= stack.getSize();
		
		return points;
	}
	
	/**
	 * Gets the point value of a blueprint based on its sale price.
	 * @param itemId e.g. "fighter_bp", Items.SHIP_BP
	 * @param dataId e.g. "onslaught_XIV"
	 * @param baseCost Base cost of the hull, fighter wing or weapon
	 * @return
	 */
	public static float getBlueprintPointValue(String itemId, String dataId, float baseCost)
	{
		Float setValue = PrismMarket.getBlueprintValue(itemId);
		if (setValue != null) return setValue;
		setValue = PrismMarket.getBlueprintValue(dataId);
		if (setValue != null) return setValue;
		
		SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(itemId);
		
		float cost = spec.getBasePrice() + baseCost * Global.getSettings().getFloat("blueprintPriceOriginalItemMult");
		if (spec.hasTag("tiandong_retrofit_bp") || itemId.equals("roider_retrofit_bp"))
		{
			//log.info(spec.getName() + " is retrofit, halving cost");
			cost *= 0.5f;
		}
		cost *= PRICE_POINT_MULT;

		if (spec.hasTag("package_bp"))
			cost *= 5;
		
		// rounding
		cost = 5 * Math.round(cost/5f);
		
		return cost;
	}
	
	public static float addPoints(float points)
	{
		points += getPoints();
		Global.getSector().getPersistentData().put(POINTS_KEY, points);
		
		return points;
	}
	
	public static float getPoints()
	{
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(POINTS_KEY))
			data.put(POINTS_KEY, 0f);
		
		return (float)data.get(POINTS_KEY);
	}
	
	public static Set<String> getAlreadySold() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(ALREADY_SOLD_KEY))
			data.put(ALREADY_SOLD_KEY, new HashSet<>());
		
		return (HashSet<String>)data.get(ALREADY_SOLD_KEY);
	}
	
	public static boolean hasMaxwellStation(MarketAPI market)
	{
		return market != null && (market.hasTag("HMI_STATION_MAXWELL_PICK"));
	}
	
	public static Random getRandom() {
		Map<String, Object> data = Global.getSector().getPersistentData();
		if (!data.containsKey(PERSISTENT_RANDOM_KEY)) {
			String seed = "" + Global.getSector().getClock().getCycle()
					+ Global.getSector().getClock().getMonth()
					+ Global.getSector().getClock().getDay();
			
			data.put(PERSISTENT_RANDOM_KEY, new Random(Long.parseLong(seed)));
		}
			
		
		return (Random)data.get(PERSISTENT_RANDOM_KEY);
	}
	
	public static class PurchaseInfo implements Comparable<PurchaseInfo>
	{
		public String id;
		public String itemId;
		public PurchaseType type;
		public String name;
		public float cost;
		
		public PurchaseInfo(String id, PurchaseType type, String name, float cost)
		{
			this.id = id;				
			this.type = type;
			this.name = name;
			this.cost = cost;
		}
		
		public String getItemId()
		{
			if (itemId != null) return itemId;
			switch (type) {
				case SHIP:
					return Items.SHIP_BP;
				case FIGHTER:
					return Items.FIGHTER_BP;
				case WEAPON:
					return Items.WEAPON_BP;
			}
			return null;
		}
		
		public SpecialItemData getItemData()
		{
			return new SpecialItemData(getItemId(), id);
		}

		@Override
		public int compareTo(PurchaseInfo other) {
			// ships first, then fighters, then weapons
			if (type != other.type)	
				return type.compareTo(other.type);
			
			// descending cost order
			if (cost != other.cost) return Float.compare(other.cost, cost);
			
			return name.compareTo(other.name);
		}
	}
	
	public static enum PurchaseType {
		SHIP, FIGHTER, WEAPON
	}
}
