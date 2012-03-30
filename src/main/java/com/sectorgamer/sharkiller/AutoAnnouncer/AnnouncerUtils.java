package com.sectorgamer.sharkiller.AutoAnnouncer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.ChatColor;

public class AnnouncerUtils {
	public static String colorize(String announce) {
		// Color Names
		announce = announce.replaceAll("&AQUA;",		ChatColor.AQUA.toString());
		announce = announce.replaceAll("&BLACK;",		ChatColor.BLACK.toString());
		announce = announce.replaceAll("&BLUE;",		ChatColor.BLUE.toString());
		announce = announce.replaceAll("&DARK_AQUA;",	ChatColor.DARK_AQUA.toString());
		announce = announce.replaceAll("&DARK_BLUE;",	ChatColor.DARK_BLUE.toString());
		announce = announce.replaceAll("&DARK_GRAY;",	ChatColor.DARK_GRAY.toString());
		announce = announce.replaceAll("&DARK_GREEN;", 	ChatColor.DARK_GREEN.toString());
		announce = announce.replaceAll("&DARK_PURPLE;",	ChatColor.DARK_PURPLE.toString());
		announce = announce.replaceAll("&DARK_RED;",	ChatColor.DARK_RED.toString());
		announce = announce.replaceAll("&GOLD;",		ChatColor.GOLD.toString());
		announce = announce.replaceAll("&GRAY;",		ChatColor.GRAY.toString());
		announce = announce.replaceAll("&GREEN;",		ChatColor.GREEN.toString());
		announce = announce.replaceAll("&LIGHT_PURPLE;",ChatColor.LIGHT_PURPLE.toString());
		announce = announce.replaceAll("&RED;",			ChatColor.RED.toString());
		announce = announce.replaceAll("&WHITE;",		ChatColor.WHITE.toString());
		announce = announce.replaceAll("&YELLOW;",		ChatColor.YELLOW.toString());
		announce = announce.replaceAll("&MAGIC;",		ChatColor.MAGIC.toString());
		announce = announce.replaceAll("&BOLD;",		ChatColor.BOLD.toString());
		announce = announce.replaceAll("&STRIKE;",		ChatColor.STRIKETHROUGH.toString());
		announce = announce.replaceAll("&UNDERLINE;",	ChatColor.UNDERLINE.toString());
		announce = announce.replaceAll("&ITALIC;",		ChatColor.ITALIC.toString());
		announce = announce.replaceAll("&RESET;",		ChatColor.RESET.toString());
		// Color codes
		announce = announce.replaceAll("&0",		ChatColor.BLACK.toString());
		announce = announce.replaceAll("&1",		ChatColor.DARK_BLUE.toString());
		announce = announce.replaceAll("&2",		ChatColor.DARK_GREEN.toString());
		announce = announce.replaceAll("&3",		ChatColor.DARK_AQUA.toString());
		announce = announce.replaceAll("&4",		ChatColor.DARK_RED.toString());
		announce = announce.replaceAll("&5",		ChatColor.DARK_PURPLE.toString());
		announce = announce.replaceAll("&6",		ChatColor.GOLD.toString());
		announce = announce.replaceAll("&7",		ChatColor.GRAY.toString());
		announce = announce.replaceAll("&8",		ChatColor.DARK_GRAY.toString());
		announce = announce.replaceAll("&9",		ChatColor.BLUE.toString());
		announce = announce.replaceAll("&a",		ChatColor.GREEN.toString());
		announce = announce.replaceAll("&b",		ChatColor.AQUA.toString());
		announce = announce.replaceAll("&c",		ChatColor.RED.toString());
		announce = announce.replaceAll("&d",		ChatColor.LIGHT_PURPLE.toString());
		announce = announce.replaceAll("&e",		ChatColor.YELLOW.toString());
		announce = announce.replaceAll("&f",		ChatColor.WHITE.toString());
		announce = announce.replaceAll("&k",		ChatColor.MAGIC.toString());
		announce = announce.replaceAll("&l",		ChatColor.BOLD.toString());
		announce = announce.replaceAll("&m",		ChatColor.STRIKETHROUGH.toString());
		announce = announce.replaceAll("&n",		ChatColor.UNDERLINE.toString());
		announce = announce.replaceAll("&o",		ChatColor.ITALIC.toString());
		announce = announce.replaceAll("&r",		ChatColor.RESET.toString());
		return announce;
	}

	public static void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len=in.read(buf)) > 0) {
				out.write(buf,0,len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
