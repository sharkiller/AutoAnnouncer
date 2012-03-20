package com.sectorgamer.sharkiller.AutoAnnouncer;

import java.io.*;
import java.util.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
//import org.bukkit.util.config.Configuration;

import net.milkbowl.vault.permission.Permission;

public class AutoAnnouncer extends JavaPlugin
{
	private static PluginDescriptionFile pdfFile;
	private static final String DIR = "plugins" + File.separator + "AutoAnnouncer" + File.separator;
	private static final String CONFIG_FILE = "settings.yml";
	
	private static YamlConfiguration Settings = YamlConfiguration.loadConfiguration(new File(DIR + CONFIG_FILE));
	private static String Tag;
	private static int Interval, taskId = -1, counter = 0;
	private static boolean isScheduling = false, isRandom, permissionsEnabled = false, InSeconds = false, permission, toGroups;
	private static List<String> strings, Groups;
	private static Permission Permissions = null;

	@Override
    public void onEnable() {
    	pdfFile = this.getDescription();
    	File fDir = new File(DIR);
		if (!fDir.exists())
			fDir.mkdir();
		try{
			File configFile = new File(DIR + CONFIG_FILE);
			if (!configFile.exists()) {
				configFile.getParentFile().mkdirs();
	            copy(getResource(CONFIG_FILE), configFile);
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
    	load();
    	if(permission)
    		enablePermissions();
    	else
    		System.out.println("[AutoAnnouncer] WARNING! No permission system enabled!");
    	System.out.println("[AutoAnnouncer] Settings Loaded ("+strings.size()+" announces).");
    	isScheduling = scheduleOn(null);
    	System.out.println("[AutoAnnouncer] v"+pdfFile.getVersion()+" is enabled!" );
		System.out.println("[AutoAnnouncer] Developed by: "+pdfFile.getAuthors());
	}

    @Override
    public void onDisable(){
    	scheduleOff(true, null);
    	System.out.println("[AutoAnnouncer] v"+pdfFile.getVersion()+" is disabled!.");
    }
    
    private void enablePermissions() {
    	RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            Permissions = permissionProvider.getProvider();
            permissionsEnabled = true;
            System.out.println("[milkAdmin] Permission support enabled!");
        } else
        	System.out.println("[milkAdmin] Permission system not found!");
    }
    
    private boolean permission(Player player, String line, Boolean op){
    	if(permissionsEnabled) {
    		return Permissions.has(player, line);
    	} else {
    		return op;
    	}
    }
    
    private void scheduleOff(boolean Disabling, CommandSender sender){
    	if(isScheduling){
    		getServer().getScheduler().cancelTask(taskId);
    		if(sender != null) sender.sendMessage(ChatColor.DARK_GREEN+"Scheduling finished!");
    		System.out.println("[AutoAnnouncer] Scheduling finished!");
	    	isScheduling = false;
    	}else{
    		if(!Disabling)
    			if(sender != null) sender.sendMessage(ChatColor.DARK_RED+"No schedule running!");
    			System.out.println("[AutoAnnouncer] No schedule running!" );
    	}
    }
    
    private boolean scheduleOn(CommandSender sender){
    	if(!isScheduling){
	    	if(strings.size() > 0){
	    		int TimeToTicks = InSeconds? 20:1200;
	    		taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new printAnnounce(), Interval * TimeToTicks, Interval * TimeToTicks);
		    	if(taskId == -1){
		    		if(sender != null) sender.sendMessage(ChatColor.DARK_RED+"Scheduling failed!");
		    		System.out.println("[AutoAnnouncer] Scheduling failed!" );
		    		return false;
		    	}else{
		    		counter = 0;
		    		if(sender != null) sender.sendMessage(ChatColor.DARK_GREEN+"Scheduled every "+ Interval + (InSeconds? " seconds!":" minutes!"));
		    		System.out.println("[AutoAnnouncer] Scheduled every "+ Interval + (InSeconds? " seconds!":" minutes!"));
		    		return true;
		    	}
	    	}else{
	    		if(sender != null) sender.sendMessage(ChatColor.DARK_RED+"Scheduling failed! There are no announcements to do.");
	    		System.out.println("[AutoAnnouncer] Scheduling failed! There are no announcements to do." );
	    		return false;
	    	}
    	}else{
    		if(sender != null) sender.sendMessage(ChatColor.DARK_RED+"Scheduler already running.");
    		System.out.println("[AutoAnnouncer] Scheduler already running." );
    		return true;
    	}
    }
    
    private void scheduleRestart(CommandSender sender){
    	if(isScheduling){
    		scheduleOff(false, null);
    		load();
    		sender.sendMessage(ChatColor.DARK_GREEN+"Settings Loaded ("+strings.size()+" announces).");
    		isScheduling = scheduleOn(sender);
    	}else{
    		sender.sendMessage(ChatColor.DARK_RED+"No schedule running!");
    	}
    }
    
    private void setInterval(String[] args, CommandSender sender){
    	if(args.length == 2) {
    		try{
				int interval = Integer.parseInt(args[1], 10);
				Settings.set("Settings.Interval", interval);
				saveSettings();
				sender.sendMessage(ChatColor.DARK_GREEN+"Interval changed successfully to "+args[1]+ (InSeconds? " seconds.":" minutes."));
				if(isScheduling) sender.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
			}catch(NumberFormatException err){
				sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer interval 5");
			}
    	}else{
    		sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer interval 5");
    	}
    }
    
    private void setRandom(String[] args, CommandSender sender){
    	if(args.length == 2) {
    		if(args[1].equals("on")){
    			Settings.set("Settings.Random", true);
    			saveSettings();
    			sender.sendMessage(ChatColor.DARK_GREEN+"Changed to random transition.");
    			if(isScheduling) sender.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
    		}else if(args[1].equals("off")){
    			Settings.set("Settings.Random", false);
    			saveSettings();
    			sender.sendMessage(ChatColor.DARK_GREEN+"Changed to consecutive transition.");
    			if(isScheduling) sender.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
    		}else{
    			sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer random off");
    		}
    	}else{
    		sender.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer random off");
    	}
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    	String commandName = cmd.getName();
    	if(sender instanceof Player) {
    		Player player = (Player)sender;
    		if(commandName.equalsIgnoreCase("announcer")) {
    			if(permission(player, "announcer.admin", player.isOp())) {
	    			try {
	    				if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))
	    					auctionHelp(player);
	    				else if(args[0].equalsIgnoreCase("off"))
	    					scheduleOff(false, player);
	    				else if(args[0].equalsIgnoreCase("on"))
	    					isScheduling = scheduleOn(player);
	    				else if(args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("i"))
	    					setInterval(args, player);
	    				else if(args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("r"))
	    					setRandom(args, player);
	    				else if(args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload"))
	    					scheduleRestart(player);
	    				return true;
	    			} catch(ArrayIndexOutOfBoundsException ex) {
	    				return false;
	    			}
    			} else {
    				player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
    				return true;
    	    	}
    		}
    	}else if(sender instanceof ConsoleCommandSender){
    		ConsoleCommandSender Console = (ConsoleCommandSender) sender;
    		try {
    			if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))
					auctionHelp(Console);
				else if(args[0].equalsIgnoreCase("off"))
					scheduleOff(false, Console);
				else if(args[0].equalsIgnoreCase("on"))
					isScheduling = scheduleOn(Console);
				else if(args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("i"))
					setInterval(args, Console);
				else if(args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("r"))
					setRandom(args, Console);
				else if(args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload"))
					scheduleRestart(Console);
				return true;
			} catch(ArrayIndexOutOfBoundsException ex) {
				return false;
			}
    	}
    	return false;
    }
    
    public void auctionHelp(CommandSender sender) {
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
    
	private void load() {
		Settings = YamlConfiguration.loadConfiguration(new File(DIR + CONFIG_FILE));
		Interval = Settings.getInt("Settings.Interval", 5);
		InSeconds = Settings.getBoolean("Settings.InSeconds", false);
		isRandom = Settings.getBoolean("Settings.Random", false);
		permission = Settings.getBoolean("Settings.Permission", true);
		strings = Settings.getStringList("Announcer.Strings");
		Tag = colorize(Settings.getString("Announcer.Tag", "&GOLD;[AutoAnnouncer]"));
		toGroups = Settings.getBoolean("Announcer.ToGroups", true);
		Groups = Settings.getStringList("Announcer.Groups");
	}
	
	private String colorize(String announce) {
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
		return announce;
	}

    class printAnnounce implements Runnable {
        public void run() {
        	String announce = "";
        	
        	if(isRandom) {
	            Random randomise = new Random();
	            int selection = randomise.nextInt(strings.size());
	            announce = strings.get(selection);
        	}else{
        		announce = strings.get(counter);
        		counter++;
        		if(counter >= strings.size())
        			counter = 0;
        	}

        	if(permission && toGroups){
        		Player[] players = getServer().getOnlinePlayers();
       			for(Player p: players){
       				for(String group: Groups){
       					if(Permissions.playerInGroup(p, group)){
       						for (String line : announce.split("&NEW_LINE;"))
       							p.sendMessage(Tag+" "+colorize(line));
       						break;
       					}
        			}
        		}
        	}else{
        		for (String line : announce.split("&NEW_LINE;"))
        			getServer().broadcastMessage(Tag+" "+colorize(line));
        	}

        }
    }
    
    private void saveSettings(){
    	try {
			Settings.save(new File(DIR + CONFIG_FILE));
		} catch (IOException e) {
			
		}
    }
    
    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}