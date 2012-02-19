package ar.com.sharkale.AutoAnnouncer;

import java.util.*;

import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permissible;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class AutoAnnouncer extends JavaPlugin {

    private static final Random sharedRandom = new Random();
    private PluginDescriptionFile pdfFile;
    private String tag;
    private int interval, taskId = -1, counter = 0;
    private boolean isScheduling = false, isRandom, permissionsEnabled = false, permission, toGroups;
    private List<String> announcements, groups;
    public static PermissionHandler permissions;

    @Override
    public void onEnable() {
        pdfFile = this.getDescription();
        load();
        final Logger logger = getLogger();
        if (permission) {
            enablePermissions();
        }
        logger.info("Settings Loaded (" + announcements.size() + " announces).");
        isScheduling = scheduleOn(null);
        logger.info("v" + pdfFile.getVersion() + " is enabled!");
        logger.info("Developed by: " + pdfFile.getAuthors());
    }

    @Override
    public void onDisable() {
        scheduleOff(true, null);
        getLogger().info("v" + pdfFile.getVersion() + " is disabled!.");
    }

    private void enablePermissions() {
        Plugin p = getServer().getPluginManager().getPlugin("Permissions");
        if (p != null) {
            if (!p.isEnabled()) {
                getServer().getPluginManager().enablePlugin(p);
            }
            permissions = ((Permissions) p).getHandler();
            permissionsEnabled = true;

            getLogger().info("Permissions support enabled!");
        } else {
            getLogger().warning("Permissions system is enabled but could not be loaded!");
        }
    }

    private boolean permission(final Permissible player, final String permission) {
        if (permissionsEnabled && player instanceof Player) {
            return permissions.has((Player) player, permission);
        } else {
            return player.hasPermission(permission);
        }
    }

    private void scheduleOff(final boolean disabling, final CommandSender player) {
        if (isScheduling) {
            getServer().getScheduler().cancelTask(taskId);
            if (player != null) {
                player.sendMessage(ChatColor.DARK_GREEN + "Scheduling finished!");
            }
            getLogger().info("Scheduling finished!");
            isScheduling = false;
        } else {
            if (!disabling) {
                if (player != null) {
                    player.sendMessage(ChatColor.DARK_RED + "No schedule running!");
                }
            }
            getLogger().info("No schedule running!");
        }
    }

    private boolean scheduleOn(final CommandSender player) {
        if (isScheduling) {
            if (player != null) {
                player.sendMessage(ChatColor.DARK_RED + "Scheduler already running.");
            }
            getLogger().warning("Scheduler already running.");
            return true;
        }

        if (!announcements.isEmpty()) {
            taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new printAnnounce(), interval * 1200, interval * 1200);
            if (taskId == -1) {
                if (player != null) {
                    player.sendMessage(ChatColor.DARK_RED + "Scheduling failed!");
                }
                getLogger().severe("Scheduling failed!");
                return false;
            } else {
                counter = 0;
                if (player != null) {
                    player.sendMessage(ChatColor.DARK_GREEN + "Scheduled every " + interval + " minutes!");
                }
                getLogger().info("Scheduled every " + interval + " minutes!");
                return true;
            }
        } else {
            if (player != null) {
                player.sendMessage(ChatColor.DARK_RED + "Scheduling failed! There are no announcements to do.");
            }
            getLogger().warning("Scheduling failed! There are no announcements to do.");
            return false;
        }
    }

    private void scheduleRestart(final CommandSender player) {
        if (isScheduling) {
            scheduleOff(false, null);
            load();
            player.sendMessage(ChatColor.DARK_GREEN + "Settings Loaded (" + announcements.size() + " announces).");
            isScheduling = scheduleOn(player);
        } else {
            player.sendMessage(ChatColor.DARK_RED + "No schedule running!");
        }
    }

    private void setInterval(final String[] args, final CommandSender player) {
        if (args.length == 2) {
            try {
                getConfig().set("interval", Integer.valueOf(args[1], 10));
                saveConfig();
                player.sendMessage(ChatColor.DARK_GREEN + "Interval changed successfully to " + args[1] + " minutes.");
                if (isScheduling) {
                    player.sendMessage(ChatColor.GOLD + "Restart the schedule to apply changes.");
                }
            } catch (NumberFormatException err) {
                player.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer interval 5");
            }
        } else {
            player.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer interval 5");
        }
    }

    private void setRandom(final String[] args, final CommandSender player) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("on")) {
                getConfig().set("random", true);
                saveConfig();
                player.sendMessage(ChatColor.DARK_GREEN + "Changed to random transition.");
                if (isScheduling) {
                    player.sendMessage(ChatColor.GOLD + "Restart the schedule to apply changes.");
                }
            } else if (args[1].equalsIgnoreCase("off")) {
                getConfig().set("random", false);
                saveConfig();
                player.sendMessage(ChatColor.DARK_GREEN + "Changed to consecutive transition.");
                if (isScheduling) {
                    player.sendMessage(ChatColor.GOLD + "Restart the schedule to apply changes.");
                }
            } else {
                player.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer random off");
            }
        } else {
            player.sendMessage(ChatColor.DARK_RED + "Error! Usage: /announcer random off");
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        String commandName = cmd.getName();
        if (commandName.equalsIgnoreCase("announcer")) {
            if (permission(sender, "announcer.admin")) {
                try {
                    if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                        showHelpTo(sender);
                    } else if (args[0].equalsIgnoreCase("off")) {
                        scheduleOff(false, sender);
                    } else if (args[0].equalsIgnoreCase("on")) {
                        isScheduling = scheduleOn(sender);
                    } else if (args[0].equalsIgnoreCase("interval") || args[0].equalsIgnoreCase("i")) {
                        setInterval(args, sender);
                    } else if (args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("r")) {
                        setRandom(args, sender);
                    } else if (args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("reload")) {
                        scheduleRestart(sender);
                    }
                    return true;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
        }
        return false;
    }

    public void showHelpTo(final CommandSender player) {
        final String or = ChatColor.WHITE + " | ";
        final String helpTitleColor = ChatColor.DARK_GREEN.toString();
        final String helpMainColor = ChatColor.GOLD.toString();
        final String helpCommandColor = ChatColor.AQUA.toString();
        final String helpObligatoryColor = ChatColor.DARK_RED.toString();
        player.sendMessage(helpMainColor + " -----[ " + helpTitleColor + "Help for AutoAnnouncer" + helpMainColor + " ]----- ");
        player.sendMessage(helpCommandColor + "/announcer help" + or + helpCommandColor + "?" + helpMainColor + " - Show this message.");
        player.sendMessage(helpCommandColor + "/announcer on" + helpMainColor + " - Start AutoAnnouncer.");
        player.sendMessage(helpCommandColor + "/announcer off" + helpMainColor + " - Stop AutoAnnouncer.");
        player.sendMessage(helpCommandColor + "/announcer restart" + helpMainColor + " - Restart AutoAnnouncer.");
        player.sendMessage(helpCommandColor + "/announcer interval" + or + helpCommandColor + "i" + helpObligatoryColor + " <minutes>" + helpMainColor + " - Set the interval time.");
        player.sendMessage(helpCommandColor + "/announcer random" + or + helpCommandColor + "r" + helpObligatoryColor + " <on|off>" + helpMainColor + " - Set random or consecutive.");
    }

    private void load() {
        reloadConfig();
        final ConfigurationSection config = getConfig();
        interval = config.getInt("interval", 5);
        isRandom = config.getBoolean("random", false);
        permission = config.getBoolean("use-old-permissions", true);
        final ConfigurationSection announcerSection = config.getConfigurationSection("announcer");
        if (announcerSection == null) {
            announcements = new ArrayList<String>();
            tag = "\2476[AutoAnnouncer]";
            toGroups = false;
            groups = new ArrayList<String>();
        } else {
            announcements = announcerSection.getStringList("announcements");
            tag = colorize(announcerSection.getString("tag", "&GOLD;[AutoAnnouncer]"));
            toGroups = announcerSection.getBoolean("send-to-specific-groups", true);
            groups = announcerSection.getStringList("groups-to-send-to");
        }
    }

    private static void replaceAll(final StringBuilder subject, final String search, final String replace) {
        int index = 0;
        while ((index = subject.indexOf(search, index)) != -1) {
            subject.replace(index, index + search.length(), replace);
            index += replace.length();
        }
    }

    private String colorize(final String announce) {
        final StringBuilder newAnnounce = new StringBuilder(announce);

        replaceAll(newAnnounce, "&AQUA;", ChatColor.AQUA.toString());
        replaceAll(newAnnounce, "&BLACK;", ChatColor.BLACK.toString());
        replaceAll(newAnnounce, "&BLUE;", ChatColor.BLUE.toString());
        replaceAll(newAnnounce, "&DARK_AQUA;", ChatColor.DARK_AQUA.toString());
        replaceAll(newAnnounce, "&DARK_BLUE;", ChatColor.DARK_BLUE.toString());
        replaceAll(newAnnounce, "&DARK_GRAY;", ChatColor.DARK_GRAY.toString());
        replaceAll(newAnnounce, "&DARK_GREEN;", ChatColor.DARK_GREEN.toString());
        replaceAll(newAnnounce, "&DARK_PURPLE;", ChatColor.DARK_PURPLE.toString());
        replaceAll(newAnnounce, "&DARK_RED;", ChatColor.DARK_RED.toString());
        replaceAll(newAnnounce, "&GOLD;", ChatColor.GOLD.toString());
        replaceAll(newAnnounce, "&GRAY;", ChatColor.GRAY.toString());
        replaceAll(newAnnounce, "&GREEN;", ChatColor.GREEN.toString());
        replaceAll(newAnnounce, "&LIGHT_PURPLE;", ChatColor.LIGHT_PURPLE.toString());
        replaceAll(newAnnounce, "&RED;", ChatColor.RED.toString());
        replaceAll(newAnnounce, "&WHITE;", ChatColor.WHITE.toString());
        replaceAll(newAnnounce, "&YELLOW;", ChatColor.YELLOW.toString());
        return newAnnounce.toString();
    }

    private class printAnnounce implements Runnable {

        @Override
        public void run() {
            final String announce;

            if (isRandom) {
                int selection = sharedRandom.nextInt(announcements.size());
                announce = announcements.get(selection);
            } else {
                announce = announcements.get(counter);
                counter++;
                if (counter >= announcements.size()) {
                    counter = 0;
                }
            }

            final String[] lines = announce.split("&NEW_LINE;");

            if (permission && toGroups) {
                for (final Player p : getServer().getOnlinePlayers()) {
                    final String worldName = p.getWorld().getName();
                    final String playerName = p.getName();
                    for (final String group : groups) {
                        if (permissions.inGroup(worldName, playerName, group)) {
                            for (final String line : lines) {
                                p.sendMessage(tag + " " + colorize(line));
                            }
                            break;
                        }
                    }
                }
            } else {
                for (final String line : lines) {
                    getServer().broadcastMessage(tag + " " + colorize(line));
                }
            }

        }
    }
}