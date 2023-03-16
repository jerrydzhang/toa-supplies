package com.toasupplies;

import com.google.inject.Provides;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

@PluginDescriptor(
	name = "Toa Supplies"
)
public class ToaSuppliesPlugin extends Plugin
{
//	replace with 810
//	looting bag 516
	private final int SUPPLY_ID = 810;
	private boolean checkSnapshot = false;
	private int waitTicks = 1;
	private final Map<Integer, Integer> itemHash = new HashMap<>();
	private final Map<Integer, Integer> inventorySnapshot = new HashMap<>();


	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ToaSuppliesOverlay overlay;

	@Inject
	private Client client;


	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
//		change looting bag group id to toa supply 778
//		WidgetInfo.LOOTING_BAG_CONTAINER.getGroupId()
		if (widgetLoaded.getGroupId() != 778)
		{
			return;
		}
		updateValue(itemHash,SUPPLY_ID);
	}
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != SUPPLY_ID)
		{
			return;
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "ItemContainerChange", null);
		updateValue(itemHash,SUPPLY_ID);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getWidget() != null)
		{
			if ((event.getWidget().getItemId() == ItemID.SUPPLIES || event.getWidget().getItemId() == ItemID.LOOTING_BAG_22586)
					&& (event.getMenuOption().equals("Withdraw 1") || event.getMenuOption().equals("Withdraw All") || event.getMenuOption().equals("Resupply")))

			{
				if (updateValue(inventorySnapshot,InventoryID.INVENTORY.getId()))
				{
					checkSnapshot = true;
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", checkSnapshot + "", null);
				}
			}
			else if (event.getMenuOption().equals("Use")
				&& event.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET
				&& client.getSelectedWidget() != null)
			{
				int firstSelectedItemID = client.getSelectedWidget().getItemId();
				int secondSelectedItemID = event.getWidget().getItemId();

				RaidsPotions raidsPotion = RaidsPotions.findItem(firstSelectedItemID);

				if (raidsPotion != null
						&& (secondSelectedItemID == ItemID.SUPPLIES || secondSelectedItemID == ItemID.LOOTING_BAG_22586))
				{
					itemHash.merge(raidsPotion.getPotionType(), raidsPotion.getCharges(), Integer::sum);
				}
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick event){
		if (!checkSnapshot)
		{
			return;
		}

		if (waitTicks > 0)
		{
			waitTicks--;
			return;
		}

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",  "ran", null);

		checkSnapshot = false;
		waitTicks = 1;

		Set<Integer> prevInventoryItemIdsSet = inventorySnapshot.keySet();

		Map<Integer,Integer> currentInventoryHash = new LinkedHashMap<>();
		updateValue(currentInventoryHash,InventoryID.INVENTORY.getId());
		Set<Integer> currentInventoryItemIdsSet = currentInventoryHash.keySet();

		Set<Integer> allPossibleItemIds = new HashSet<>();
		allPossibleItemIds.addAll(prevInventoryItemIdsSet);
		allPossibleItemIds.addAll(currentInventoryItemIdsSet);

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", allPossibleItemIds + "", null);

		for(Integer itemId:allPossibleItemIds)
		{
			int prevAmount = inventorySnapshot.get(itemId) != null ? inventorySnapshot.get(itemId) : 0;
			int currentAmount = currentInventoryHash.get(itemId) != null ? currentInventoryHash.get(itemId) : 0;

			int changeAmount = prevAmount - currentAmount;
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", changeAmount + "", null);


			itemHash.merge(itemId, changeAmount, Integer::sum);
		}
	}

	private Boolean updateValue(Map<Integer,Integer> hashMap, int inventoryId)
	{
		ItemContainer itemContainer = client.getItemContainer(inventoryId);

		if (itemContainer == null)
		{
			return false;
		}

		hashMap.clear();

		Arrays.stream(itemContainer.getItems())
				.filter(item -> RaidsPotions.findItem(item.getId()) != null).forEach(i -> hashMap
						.merge(RaidsPotions.findItem(i.getId()).getPotionType(), RaidsPotions.findItem(i.getId()).getCharges(), Integer::sum));
		return true;
	}

	public int getAmount(int id)
	{
		return itemHash.get(id) != null ? itemHash.get(id) : 0;
	}
	@Provides
	ToaSuppliesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToaSuppliesConfig.class);
	}
}
