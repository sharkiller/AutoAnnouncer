package ar.com.sharkale.AutoAnnouncer;

import java.io.*;
import java.util.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class AutoAnnouncer extends JavaPlugin
{
	private static PluginDescriptionFile pdfFile;
	private static final String DIR = "plugins" + File.separator + "AutoAnnouncer" + File.separator;
	private static final String CONFIG_FILE = "settings.yml";
	
	private static Configuration Settings = new Configuration(new File(DIR + CONFIG_FILE));
	private static String Tag;
	private static int Interval, taskId = -1, counter = 0;
	private static boolean isScheduling = false, isRandom, permissionsEnabled = false, permission, toGroups;
	private static List<String> strings, Groups;
	public static PermissionHandler Permissions;

    public void onEnable()
    {
    	pdfFile = this.getDescription();
    	File fDir = new File(DIR);
		if (!fDir.exists())
			fDir.mkdir();
		try{
			File fAuths = new File(DIR + CONFIG_FILE);
			if (!fAuths.exists())
				fAuths.createNewFile();
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

    public void onDisable()
    {
    	scheduleOff(true, null);
    	System.out.println("[AutoAnnouncer] v"+pdfFile.getVersion()+" is disabled!.");
    }
    
    private void enablePermissions() {
    	Plugin p = getServer().getPluginManager().getPlugin("Permissions");
    	if(p != null) {
    		if(!p.isEnabled())
    			getServer().getPluginManager().enablePlugin(p);
    		Permissions = ((Permissions)p).getHandler();
    		permissionsEnabled = true;
    		
    		System.out.println("[AutoAnnouncer] Permissions support enabled!");
    	} else
    		System.out.println("[AutoAnnouncer] Permissions system is enabled but could not be loaded!");
    }
    
    private boolean permission(Player player, String line, Boolean op){
    	if(permissionsEnabled) {
    		return Permissions.has(player, line);
    	} else {
    		return op;
    	}
    }
    
    private void scheduleOff(boolean Disabling, Player player){
    	if(isScheduling){
    		getServer().getScheduler().cancelTask(taskId);
    		if(player != null) player.sendMessage(ChatColor.DARK_GREEN+"Scheduling finished!");
    		System.out.println("[AutoAnnouncer] Scheduling finished!");
	    	isScheduling = false;
    	}else{
    		if(!Disabling)
    			if(player != null) player.sendMessage(ChatColor.DARK_RED+"No schedule running!");
    			System.out.println("[AutoAnnouncer] No schedule running!" );
    	}
    }
    
    private boolean scheduleOn(Player player){
    	if(!isScheduling){
	    	if(strings.size() > 0){
	    		taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new printAnnounce(), Interval * 1200, Interval * 1200);
		    	if(taskId == -1){
		    		if(player != null) player.sendMessage(ChatColor.DARK_RED+"Scheduling failed!");
		    		System.out.println("[AutoAnnouncer] Scheduling failed!" );
		    		return false;
		    	}else{
		    		counter = 0;
		    		if(player != null) player.sendMessage(ChatColor.DARK_GREEN+"Scheduled every "+ Interval +" minutes!");
		    		System.out.println("[AutoAnnouncer] Scheduled every "+ Interval +" minutes!" );
		    		return true;
		    	}
	    	}else{
	    		if(player != null) player.sendMessage(ChatColor.DARK_RED+"Scheduling failed! There are no announcements to do.");
	    		System.out.println("[AutoAnnouncer] Scheduling failed! There are no announcements to do." );
	    		return false;
	    	}
    	}else{
    		if(player != null) player.sendMessage(ChatColor.DARK_RED+"Scheduler already running.");
    		System.out.println("[AutoAnnouncer] Scheduler already running." );
    		return true;
    	}
    }
    
    private void scheduleRestart(Player player){
    	if(isScheduling){
    		scheduleOff(false, null);
    		load();
    		player.sendMessage(ChatColor.DARK_GREEN+"Settings Loaded ("+strings.size()+" announces).");
    		isScheduling = scheduleOn(player);
    	}else{
    		player.sendMessage(ChatColor.DARK_RED+"No schedule running!");
    	}
    }
    
    private void setInterval(String[] args, Player player){
    	if(args.length == 2) {
    		try{
				int interval = Integer.parseInt(args[1], 10);
				Settings.setProperty("Settings.Interval", interval);
				Settings.save();
				player.sendMessage(ChatColor.DARK_GREEN+"Interval changed successfully to "+args[1]+" minutes.");
				if(isScheduling) player.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
			}catch(NumberFormatException err){
				player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer interval 5");
			}
    	}else{
    		player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer interval 5");
    	}
    }
    
    private void setRandom(String[] args, Player player){
    	if(args.length == 2) {
    		if(args[1].equals("on")){
    			Settings.setProperty("Settings.Random", true);
    			Settings.save();
    			player.sendMessage(ChatColor.DARK_GREEN+"Changed to random transition.");
    			if(isScheduling) player.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
    		}else if(args[1].equals("off")){
    			Settings.setProperty("Settings.Random", false);
    			Settings.save();
    			player.sendMessage(ChatColor.DARK_GREEN+"Changed to consecutive transition.");
    			if(isScheduling) player.sendMessage(ChatColor.GOLD+"Restart the schedule to apply changes.");
    		}else{
    			player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer random off");
    		}
    	}else{
    		player.sendMessage(ChatColor.DARK_RED+"Error! Usage: /announcer random off");
    	}
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
    	String commandName = cmd.getName();
    	if(sender instanceof Player)
    	{
    		Player player = (Player)sender;
    		if(commandName.equalsIgnoreCase("announcer"))
    		{
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
    	}
    	return false;
    }
    
    public void auctionHelp(Player player) {
    	String or = ChatColor.WHITE + " | ";
    	String auctionStatusColor = ChatColor.DARK_GREEN.toString();
    	String helpMainColor = ChatColor.GOLD.toString();
    	String helpCommandColor = ChatColor.AQUA.toString();
    	String helpObligatoryColor = ChatColor.DARK_RED.toString();
        player.sendMessage(helpMainColor + " -----[ " + auctionStatusColor + "Help for AutoAnnouncer" + helpMainColor + " ]----- ");
        player.sendMessage(helpCommandColor + "/announcer help" + or + helpCommandColor + "?" + helpMainColor + " - Show this message.");
        player.sendMessage(helpCommandColor + "/announcer on" + helpMainColor + " - Start AutoAnnouncer.");
        player.sendMessage(helpCommandColor + "/announcer off" + helpMainColor + " - Stop AutoAnnouncer.");
        player.sendMessage(helpCommandColor + "/announcer restart" + helpMainColor + " - Restart AutoAnnouncer.");
        player.sendMessage(helpCommandColor + "/announcer interval" + or + helpCommandColor + "i" + helpObligatoryColor + " <minutes>" + helpMainColor + " - Set the interval time.");
        player.sendMessage(helpCommandColor + "/announcer random" + or + helpCommandColor + "r" + helpObligatoryColor + " <on|off>" + helpMainColor + " - Set random or consecutive.");
    }
    
	private void load()
	{
		Settings.load();
		Interval = Settings.getInt("Settings.Interval", 5);
		isRandom = Settings.getBoolean("Settings.Random", false);
		permission = Settings.getBoolean("Settings.Permission", true);
		strings = Settings.getStringList("Announcer.Strings", new ArrayList<String>());
		Tag = colorize(Settings.getString("Announcer.Tag", "&GOLD;[AutoAnnouncer]"));
		toGroups = Settings.getBoolean("Announcer.ToGroups", true);
		Groups = Settings.getStringList("Announcer.Groups", new ArrayList<String>());
	}
	
	private String colorize(String announce)
	{
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
		return announce;
	}

    class printAnnounce implements Runnable
    {
        public void run()
        {
        	String announce = "";
        	
        	if(isRandom)
        	{
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
       					if(Permissions.inGroup(p.getWorld().getName(), p.getName(), group)){
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

    public boolean isDebugging(Player player)
    {
        if(debugees.containsKey(player))
            return ((Boolean)debugees.get(player)).booleanValue();
        else
            return false;
    }

    public void setDebugging(Player player, boolean value)
    {
        debugees.put(player, Boolean.valueOf(value));
    }

    private final HashMap<Object, Object> debugees = new HashMap<Object, Object>();

}