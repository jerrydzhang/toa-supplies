package com.toasupplies;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ToaSuppliesTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ToaSuppliesPlugin.class);
		RuneLite.main(args);
	}
}