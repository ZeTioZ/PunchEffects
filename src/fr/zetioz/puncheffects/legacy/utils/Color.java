package fr.zetioz.puncheffects.legacy.utils;

import org.bukkit.ChatColor;

public final class Color
{
	private Color() {}
	
	public static final String color(String textToColor)
	{
		return ChatColor.translateAlternateColorCodes('&', textToColor);
	}
}
