package com.sectorgamer.sharkiller.AutoAnnouncer;

import net.milkbowl.vault.permission.Permission;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.PermissionManager;

import de.bananaco.bpermissions.api.WorldManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class AnnouncerPerm {
	private static Permission vault = null;
	private static PermissionHandler permissions3 = null;
	private static PermissionManager permissionsEx = null;
	private static WorldManager bpermissions = null;
	private final Announcer plugin;
	private boolean permissionsEnabled = false;

	public AnnouncerPerm(final Announcer plugin) {
		this.plugin = plugin;
	}

	public void enablePermissions() {
		RegisteredServiceProvider<Permission> permissionProvider;
		try {
			permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		} catch (NoClassDefFoundError e) {
			permissionProvider = null;
		}
		Plugin pluginPermissions3 = plugin.getServer().getPluginManager().getPlugin("Permissions");
		if (permissionProvider != null) {
			vault = permissionProvider.getProvider();
			permissionsEnabled = true;
			AnnouncerLog.info("Permission support with Vault enabled!");
		} else if (plugin.getServer().getPluginManager().isPluginEnabled("PermissionsEx")) {
			permissionsEx = PermissionsEx.getPermissionManager();
			permissionsEnabled = true;
			AnnouncerLog.info("Permission support with PermissionsEx enabled!");
		} else if (plugin.getServer().getPluginManager().isPluginEnabled("bPermissions")) {
			bpermissions = WorldManager.getInstance();
			permissionsEnabled = true;
			AnnouncerLog.info("Permission support with bPermission enabled!");
		} else if (pluginPermissions3 != null) {
			permissions3 = ((Permissions)pluginPermissions3).getHandler();
			permissionsEnabled = true;
			AnnouncerLog.info("Permission support with Permissions3 enabled! Falling back to OP-Mode!");
		} else {
			AnnouncerLog.warning("Permission system not found!");
		}
	}

	public boolean has(Player player, String permission){
		if (permissionsEnabled) {
			if (vault != null) {
				return vault.has(player, permission);
			} else if (permissionsEx != null) {
				return permissionsEx.has(player, permission);
			} else if (bpermissions != null) {
				return player.hasPermission(permission);
			} else if (permissions3 != null) {
				return permissions3.has(player, permission);
			} else {
				return player.isOp();
			}
		} else {
			return player.isOp();
		}
	}

	public boolean group(Player player, String group){
		if (permissionsEnabled) {
			return vault.playerInGroup(player, group);
		} else {
			return false;
		}
	}
}
