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

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import ar.com.sharkale.AutoAnnouncer.Settings;

public class AutoAnnouncer extends JavaPlugin
{
	private static PluginDescriptionFile pdfFile;
	private static final String DIR = "plugins" + File.separator + "AutoAnnouncer" + File.separator;
	private static final String CONFIG_FILE = "settings.yml";
	
	private static Settings settings;
	private static int Interval = 5, taskId = -1, counter = 0;
	private static boolean isScheduling = false, isRandom = true, permissionsEnabled = false, permission = true;
	private static List<String> strings = new ArrayList<String>();
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
    	settings = new Settings(new File(DIR + CONFIG_FILE));
    	load();
    	System.out.println("[AutoAnnouncer] Settings Loaded ("+strings.size()+" announces).");
    	if(permission)
    		enablePermissions();
    	else
    		System.out.println("[AutoAnnouncer] WARNING! No permission system enabled!");
    	
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
    
    private boolean permission(Player player, String line, Boolean op)
    {
    	    if(permissionsEnabled) {
    	    	return Permissions.has(player, line);
    	    } else {
    	    	return op;
    	    }
    }
    
    private void scheduleOff(boolean Disabling, Player player){
    	if(isScheduling){
    		getServer().getScheduler().cancelTask(taskId);
    		System.out.println("[AutoAnnouncer] Scheduling finished.");
	    	isScheduling = false;
    	}else{
    		if(!Disabling)
    			System.out.println("[AutoAnnouncer] No schedule running." );
    	}
    }
    
    private boolean scheduleOn(Player player){
    	if(!isScheduling){
	    	if(strings.size() > 0){
	    		taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new printAnnounce(), Interval * 1200, Interval * 1200);
		    	if(taskId == -1){
		    		System.out.println("[AutoAnnouncer] Scheduling failed!" );
		    		return false;
		    	}else{
		    		counter = 0;
		    		System.out.println("[AutoAnnouncer] Scheduled every "+ Interval +" minutes!" );
		    		return true;
		    	}
	    	}else{
	    		System.out.println("[AutoAnnouncer] Scheduling failed! There are no announcements to do." );
	    		return false;
	    	}
    	}else{
    		System.out.println("[AutoAnnouncer] Scheduler already running." );
    		return true;
    	}
    }
    
    private void scheduleRestart(Player player){
    	
    }
    
    private void setInterval(String[] args, Player player){
    	
    }
    
    private void setRandom(String[] args, Player player){
    	
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
	    				else if(args[0].equalsIgnoreCase("restart"))
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
    	String auctionStatusColor = ChatColor.GREEN.toString();
    	String helpMainColor = ChatColor.YELLOW.toString();
    	String helpCommandColor = ChatColor.AQUA.toString();
    	String helpObligatoryColor = ChatColor.RED.toString();
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
		Interval = settings.getInt("Settings.Interval");
		isRandom = settings.getBool("Settings.Random");
		permission = settings.getBool("Settings.Permission");
		strings = settings.getStrList("Announcer.Strings");
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
        	if(isRandom){
	            Random randomise = new Random();
	            int selection = randomise.nextInt(strings.size());
	            announce = strings.get(selection);
        	}else{
        		announce = strings.get(counter);
        		counter++;
        		if(counter >= strings.size())
        			counter = 0;
        	}
            getServer().broadcastMessage(colorize(announce));
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