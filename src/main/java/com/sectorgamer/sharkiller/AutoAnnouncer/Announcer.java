package com.sectorgamer.sharkiller.AutoAnnouncer;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class Announcer extends JavaPlugin {
	private static PluginDescriptionFile pdfFile;
	private static final String DIR = "plugins" + File.separator + "AutoAnnouncer" + File.separator;
	private static final String CONFIG_FILE = "settings.yml";
	
	private static YamlConfiguration Settings;
	private static String Tag;
	private static int Interval, taskId = -1, counter = 0;
	public boolean isScheduling = false, isRandom, InSeconds = false, permission, toGroups;
	private static List<String> strings, Groups;
	protected AnnouncerPerm perm = null;

	@Override
	public void onEnable() {
		pdfFile = this.getDescription();
		File fDir = new File(DIR);
		if (!fDir.exists()) {
			fDir.mkdir();
		}
		try {
			File configFile = new File(DIR + CONFIG_FILE);
			if (!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				AnnouncerUtils.copy(getResource(CONFIG_FILE), configFile);
			}
		} catch (Exception e){
			AnnouncerLog.severe("Failed to copy default config!", e);
		}
		loadSettings();
		perm = new AnnouncerPerm(this);
		
		if (permission) {
			perm.enablePermissions();
		} else {
			AnnouncerLog.warning("No permission system enabled!");
		}
		
		// Register Command
		getCommand("announcer").setExecutor(new CommandListener(this));
		
		AnnouncerLog.info("Settings Loaded ("+strings.size()+" announces).");
		isScheduling = scheduleOn(null);
		AnnouncerLog.info("v"+pdfFile.getVersion()+" is enabled!" );
		AnnouncerLog.info("Developed by: "+pdfFile.getAuthors());
	}

	@Override
	public void onDisable() {
		scheduleOff(true, null);
		AnnouncerLog.info("v"+pdfFile.getVersion()+" is disabled!.");
	}
	
	public void scheduleOff(boolean Disabling, CommandSender sender) {
		if (isScheduling) {
			getServer().getScheduler().cancelTask(taskId);
			if (sender != null) {
				sender.sendMessage(ChatColor.DARK_GREEN+"Scheduling finished!");
			}
			AnnouncerLog.info("Scheduling finished!");
			isScheduling = false;
		} else {
			if (!Disabling) {
				if (sender != null) {
					sender.sendMessage(ChatColor.DARK_RED+"No schedule running!");
				}
				AnnouncerLog.info("No schedule running!" );
			}
		}
	}
	
	public boolean scheduleOn(CommandSender sender) {
		if (!isScheduling) {
			if (strings.size() > 0) {
				int TimeToTicks = InSeconds? 20:1200;
				taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new printAnnounce(), Interval * TimeToTicks, Interval * TimeToTicks);
				if (taskId == -1) {
					if (sender != null) {
						sender.sendMessage(ChatColor.DARK_RED+"Scheduling failed!");
					}
					AnnouncerLog.warning("Scheduling failed!" );
					return false;
				} else {
					counter = 0;
					if (sender != null) {
						sender.sendMessage(ChatColor.DARK_GREEN+"Scheduled every "+ Interval + (InSeconds? " seconds!":" minutes!"));
					}
					AnnouncerLog.info("Scheduled every "+ Interval + (InSeconds? " seconds!":" minutes!"));
					return true;
				}
			} else {
				if (sender != null) {
					sender.sendMessage(ChatColor.DARK_RED+"Scheduling failed! There are no announcements to do.");
				}
				AnnouncerLog.warning("Scheduling failed! There are no announcements to do." );
				return false;
			}
		} else {
			if (sender != null) {
				sender.sendMessage(ChatColor.DARK_RED+"Scheduler already running.");
			}
			AnnouncerLog.info("Scheduler already running." );
			return true;
		}
	}
	
	public void scheduleRestart(CommandSender sender) {
		if (isScheduling) {
			scheduleOff(false, null);
			loadSettings();
			sender.sendMessage(ChatColor.DARK_GREEN+"Settings Loaded ("+strings.size()+" announces).");
			isScheduling = scheduleOn(sender);
		} else {
			sender.sendMessage(ChatColor.DARK_RED+"No schedule running!");
		}
	}
	
	public void setInterval(String[] args, CommandSender sender) {
		if (args.length == 2) {
			try {
				int interval = Integer.parseInt(args[1], 10);
				Settings.set("Settings.Interval", interval);
				saveSettings();
				sender.sendMessage(ChatColor.DARK_GREEN+"Interval changed successfully to "+args[1]+ (InSeconds? " seconds.":" minutes."));
				if (isScheduling) {
					scheduleRestart(sender);
				}
			} catch (NumberFormatException err) {
				sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer interval 5");
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer interval 5");
		}
	}
	
	public void setRandom(String[] args, CommandSender sender) {
		if (args.length == 2) {
			if (args[1].equals("on")) {
				Settings.set("Settings.Random", true);
				saveSettings();
				sender.sendMessage(ChatColor.DARK_GREEN+"Changed to random transition.");
				if (isScheduling) {
					scheduleRestart(sender);
				}
			} else if (args[1].equals("off")) {
				Settings.set("Settings.Random", false);
				saveSettings();
				sender.sendMessage(ChatColor.DARK_GREEN+"Changed to consecutive transition.");
				if (isScheduling) {
					scheduleRestart(sender);
				}
			} else {
				sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer random off");
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer random off");
		}
	}
	
	public void addAnnounce(String[] args, CommandSender sender) {
		if (args.length > 1) {
			String com = StringUtils.join(args, " ", 1, args.length);
			strings.add(com);
			Settings.set("Announcer.Strings", strings);
			saveSettings();
			sender.sendMessage(ChatColor.DARK_GREEN+"New announce added!");
		} else {
			sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer add [announce here]");
		}
	}
	
	public void listAnnounces(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_GREEN+"List of announces with ids: (Total: "+strings.size()+")");
		int i = 0, j = 0;
		for (String announce : strings) {
			j++;
			for (String line : announce.split("&NEW_LINE;")) {
				i++;
				if (i == 1) {
					sender.sendMessage(ChatColor.GOLD+"["+j+"] "+ChatColor.RESET+AnnouncerUtils.colorize(line));
				} else {
					sender.sendMessage(AnnouncerUtils.colorize(line));
				}
			}
			i = 0;
		}
	}
	
	public void removeAnnounce(String[] args, CommandSender sender) {
		if (args.length == 2) {
			try {
				int announceid = Integer.parseInt(args[1]);
				if (announceid < 1 || announceid > strings.size()) {
					sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer remove [announce id]");
				} else {
					strings.remove(announceid-1);
					Settings.set("Announcer.Strings", strings);
					saveSettings();
					sender.sendMessage(ChatColor.DARK_GREEN+"Announce deleted!");
					if (isScheduling) {
						scheduleRestart(sender);
					}
				}
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer remove [announce id]");
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer remove [announce id]");
		}
	}
	
	public void announcerHelp(CommandSender sender) {
		String or = ChatColor.WHITE + " | ";
		String auctionStatusColor = ChatColor.DARK_GREEN.toString();
		String helpMainColor = ChatColor.GOLD.toString();
		String helpCommandColor = ChatColor.AQUA.toString();
		String helpObligatoryColor = ChatColor.DARK_RED.toString();
		sender.sendMessage(helpMainColor + " -----[ " + auctionStatusColor + "Help for AutoAnnouncer" + helpMainColor + " ]----- ");
		sender.sendMessage(helpCommandColor + "/announcer help" + or + helpCommandColor + "?" + helpMainColor + " - Show this message.");
		sender.sendMessage(helpCommandColor + "/announcer on" + helpMainColor + " - Start AutoAnnouncer.");
		sender.sendMessage(helpCommandColor + "/announcer off" + helpMainColor + " - Stop AutoAnnouncer.");
		sender.sendMessage(helpCommandColor + "/announcer restart" + helpMainColor + " - Restart AutoAnnouncer.");
		sender.sendMessage(helpCommandColor + "/announcer interval" + or + helpCommandColor + "i" + helpObligatoryColor + " <minutes|seconds>" + helpMainColor + " - Set the interval time.");
		sender.sendMessage(helpCommandColor + "/announcer random" + or + helpCommandColor + "r" + helpObligatoryColor + " <on|off>" + helpMainColor + " - Set random or consecutive.");
	}
	
	private void loadSettings() {
		Settings = YamlConfiguration.loadConfiguration(new File(DIR + CONFIG_FILE));
		Interval = Settings.getInt("Settings.Interval", 5);
		InSeconds = Settings.getBoolean("Settings.InSeconds", false);
		isRandom = Settings.getBoolean("Settings.Random", false);
		permission = Settings.getBoolean("Settings.Permission", true);
		strings = Settings.getStringList("Announcer.Strings");
		Tag = AnnouncerUtils.colorize(Settings.getString("Announcer.Tag", "&GOLD;[AutoAnnouncer]"));
		toGroups = Settings.getBoolean("Announcer.ToGroups", true);
		Groups = Settings.getStringList("Announcer.Groups");
	}

	private void saveSettings() {
		try {
			Settings.save(new File(DIR + CONFIG_FILE));
		} catch (IOException e) {
			AnnouncerLog.warning("Failed to save config!");
		}
	}

	class printAnnounce implements Runnable {
		public void run() {
			String announce = "";
			
			if (isRandom) {
				Random randomise = new Random();
				int selection = randomise.nextInt(strings.size());
				announce = strings.get(selection);
			} else {
				announce = strings.get(counter);
				counter++;
				if(counter >= strings.size()) {
					counter = 0;
				}
			}

			if (permission && toGroups) {
				Player[] players = getServer().getOnlinePlayers();
				for (Player p: players) {
					for (String group: Groups) {
						if (perm.group(p, group)) {
							for (String line : announce.split("&NEW_LINE;")) {
								p.sendMessage(Tag+" "+AnnouncerUtils.colorize(line));
							}
							break;
						}
					}
				}
			} else {
				for (String line : announce.split("&NEW_LINE;")) {
					getServer().broadcastMessage(Tag+" "+AnnouncerUtils.colorize(line));
				}
			}
		}
	}
}
