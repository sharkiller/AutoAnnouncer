package com.sectorgamer.sharkiller.AutoAnnouncer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.sectorgamer.sharkiller.AutoAnnouncer.Announcer;
import com.martiansoftware.jsap.CommandLineTokenizer;

public class CommandListener implements CommandExecutor {
	private final Announcer plugin;
	
	public CommandListener(final Announcer plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		args = CommandLineTokenizer.tokenize(args);
		
		String commandName = command.getName();
		
    	if(sender instanceof Player) {
    		Player player = (Player)sender;
    		if(commandName.equalsIgnoreCase("announcer")) {
    			if(plugin.perm.has(player, "announcer.admin", player.isOp())) {
	    			try {
	    				if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))
	    					plugin.announcerHelp(player);
	    				else if(args[0].equalsIgnoreCase("off"))
	    					plugin.scheduleOff(false, player);
	    				else if(args[0].equalsIgnoreCase("on"))
	    					plugin.isScheduling = plugin.scheduleOn(player);
	    				else if(args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("i"))
	    					plugin.setInterval(args, player);
	    				else if(args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("r"))
	    					plugin.setRandom(args, player);
	    				else if(args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload"))
	    					plugin.scheduleRestart(player);
	    				else if(args[0].equalsIgnoreCase("add"))
	    					plugin.addAnnounce(args, player);
	    				else if(args[0].equalsIgnoreCase("list"))
	    					plugin.listAnnounces(player);
	    				else if(args[0].equalsIgnoreCase("remove"))
	    					plugin.removeAnnounce(args, player);
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
    				plugin.announcerHelp(Console);
				else if(args[0].equalsIgnoreCase("off"))
					plugin.scheduleOff(false, Console);
				else if(args[0].equalsIgnoreCase("on"))
					plugin.isScheduling = plugin.scheduleOn(Console);
				else if(args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("i"))
					plugin.setInterval(args, Console);
				else if(args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("r"))
					plugin.setRandom(args, Console);
				else if(args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload"))
					plugin.scheduleRestart(Console);
				else if(args[0].equalsIgnoreCase("add"))
					plugin.addAnnounce(args, Console);
				else if(args[0].equalsIgnoreCase("list"))
					plugin.listAnnounces(Console);
				else if(args[0].equalsIgnoreCase("remove"))
					plugin.removeAnnounce(args, Console);
				return true;
			} catch(ArrayIndexOutOfBoundsException ex) {
				return false;
			}
    	}
    	return false;
	}
}
