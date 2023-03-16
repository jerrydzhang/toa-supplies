package com.toasupplies;


import net.runelite.api.*;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;
import java.awt.*;
import java.math.BigDecimal;


class ToaSuppliesOverlay extends WidgetItemOverlay
{
    private final Client client;
    private final ToaSuppliesConfig config;
    private final TooltipManager tooltipManager;
    private final ItemManager itemManager;
    private final ToaSuppliesPlugin plugin;

    private static final int[] potions = {27315, 27327, 27347};
    private static final int[] potionMax = {4,4,2};
    private static final Color[] potionColors = {Color.yellow,Color.magenta,Color.white};

    @Inject
    ToaSuppliesOverlay(Client client, ToaSuppliesConfig config, TooltipManager tooltipManager, ItemManager itemManager, ToaSuppliesPlugin plugin)
    {
        this.tooltipManager = tooltipManager;
        this.client = client;
        this.config = config;
        this.itemManager = itemManager;
        this.plugin = plugin;
        showOnInventory();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        if (itemId != ItemID.LOOTING_BAG && itemId != ItemID.LOOTING_BAG_22586 && itemId != ItemID.SUPPLIES && itemId != ItemID.ZARYTE_CROSSBOW)
        {
            return;
        }

        for (int i = 0; i < potions.length; i++)
        {
            int id = potions[1];
            BigDecimal amount = BigDecimal.valueOf(plugin.getAmount(potions[i]));

            if (config.showSips())
            {
                String text = amount + "";
                drawString(graphics, text, widgetItem,potionColors[i], i);
            }
            else
            {
                String text = amount.divide(BigDecimal.valueOf(potionMax[i])) + "";
                drawString(graphics, text, widgetItem,potionColors[i], i);

            }
        }
    }

    private void drawString(Graphics2D graphics, String text,WidgetItem widgetItem,Color color,int i)
    {
        graphics.setColor(color);

        graphics.drawString(text, widgetItem.getCanvasBounds().x, widgetItem.getCanvasBounds().y + 10 + (i*10));



//        final int height;
//        final Color color;
//
//        final Point location = widgetItem.getCanvasLocation();
//
//        if (amount < 10)
//        {
//            height = amount;
//            color = Color.RED;
//        }
//        else
//        {
//            height = Math.max(10, amount / 2);
//            color = Color.GREEN;
//        }
//
//        graphics.setColor(color);
//        graphics.fillRect(widgetItem.getCanvasBounds().x + 11, widgetItem.getCanvasBounds().y + (i*10), 2, height);
    }
}
