package ar.com.sharkale.AutoAnnouncer;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.util.config.Configuration;

public class Settings
{
	private static final String[] keys = 
	{
		"Settings.Interval",
		"Settings.Random",
		"Settings.Permission",
		"Announcer.Strings"
	};

	private static Configuration config;
	private static final ConcurrentHashMap<String, Object> defaults = new ConcurrentHashMap<String, Object>();
	private static final ConcurrentHashMap<String, Object> settings = new ConcurrentHashMap<String, Object>();

	public Settings(File file)
	{
		config = new Configuration(file);
		config.load();
		fillDefaults();
		load();
		config.save();
	}

	public void fillDefaults()
	{
		defaults.put("Settings.Interval", 5);
		defaults.put("Settings.Random", false);
		defaults.put("Settings.Permission", true);
		List<String> AnnouncerStrings = new ArrayList<String>();
		AnnouncerStrings.add("&DARK_RED;Running craftbukkit server with &GOLD;AutoAnnouncer.");
		AnnouncerStrings.add("&DARK_GREEN;Development by Sharkiller!");
		defaults.put("Announcer.Strings", AnnouncerStrings);
	}

	public void load()
	{
		for (String key : keys)
		{
			if (config.getProperty(key) == null)
				config.setProperty(key, defaults.get(key));
			settings.put(key, config.getProperty(key));
		}
		//clear defaults to free memory
		defaults.clear();
	}

	public void updateValue(String key, Object value)
	{
		settings.replace(key, value);
		config.setProperty(key, value);
		config.save();
	}

	public Boolean getBool(String key)
	{
		return (Boolean)settings.get(key);
	}

	public int getInt(String key)
	{
		return (Integer)settings.get(key);
	}

	public String getStr(String key)
	{
		return (String)settings.get(key);
	}

	@SuppressWarnings("unchecked")
	public List<String> getStrList(String key)
	{
		return (List<String>)settings.get(key);
	}
}