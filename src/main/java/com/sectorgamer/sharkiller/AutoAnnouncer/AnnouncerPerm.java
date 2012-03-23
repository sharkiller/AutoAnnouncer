package com.sectorgamer.sharkiller.AutoAnnouncer;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class AnnouncerPerm {
	private static Permission Permissions = null;
	private final Announcer plugin;
	private boolean permissionsEnabled = false;
	
	public AnnouncerPerm(final Announcer plugin) {
		this.plugin = plugin;
	}
	
	public void enablePermissions() {
    	RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            Permissions = permissionProvider.getProvider();
            permissionsEnabled = true;
            AnnouncerLog.info("Permission support enabled!");
        } else
        	AnnouncerLog.warning("Permission system not found!");
    }
    
    public boolean has(Player player, String line, Boolean op){
    	if(permissionsEnabled) {
    		return Permissions.has(player, line);
    	} else {
    		return op;
    	}
    }
    
    public boolean group(Player player, String group){
    	if(permissionsEnabled) {
    		return Permissions.playerInGroup(player, group);
    	} else {
    		return false;
    	}
    }
    
}
