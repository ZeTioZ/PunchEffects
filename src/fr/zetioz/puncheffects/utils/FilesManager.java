package fr.zetioz.puncheffects.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import fr.zetioz.puncheffects.Main;
import fr.zetioz.puncheffects.objects.PunchEffect;

public class FilesManager
{
	
	private Main main;
	private Plugin plugin;
	
	public FilesManager(Main main)
	{
		this.main = main;
		this.plugin = this.main.getPlugin();
	}
	
	//region Configs File (Creator/Getter)
    private YamlConfiguration configsFileConfig;
    
    public YamlConfiguration getConfigsFile()
    {
        return this.configsFileConfig;
    }

    public void createConfigsFile()
    {
    	File configsFile = new File(plugin.getDataFolder(), "configs.yml");
        if (!configsFile.exists())
        {
        	configsFile.getParentFile().mkdirs();
        	plugin.saveResource("configs.yml", false);
        }

        configsFileConfig = new YamlConfiguration();
        try
        {
        	configsFileConfig.load(configsFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
        	plugin.getLogger().severe("An error occured while loading the configs file!");
        	e.printStackTrace();
        }
    }
    
    public Map<String, PunchEffect> configsLoader()
    {
    	Map<String, PunchEffect> effectsMap = new HashMap<>();
    	Set<String> effectsPerms = this.configsFileConfig.getConfigurationSection("punch_effects").getKeys(false);
    	for(String effectName : effectsPerms)
    	{
    		String effectPerm = configsFileConfig.getString("punch_effects." + effectName + ".permission");
    		String effectType = configsFileConfig.getString("punch_effects." + effectName + ".effect");
    		ItemStack holdingItem = Material.getMaterial(configsFileConfig.getString("punch_effects." + effectName + ".holding_item.material").toUpperCase()) != null
    				? new ItemStack(Material.getMaterial(configsFileConfig.getString("punch_effects." + effectName + ".holding_item.material").toUpperCase())) : new ItemStack(Material.AIR);
    		if(holdingItem != null && holdingItem.getType() != Material.AIR)
    		{
    			ItemMeta holdingItemMeta = holdingItem.getItemMeta();
    			if(configsFileConfig.getString("punch_effects." + effectName + ".holding_item.display_name") != null && !configsFileConfig.getString("punch_effects." + effectName + ".holding_item.display_name").equalsIgnoreCase("none"))
    			{				
    				holdingItemMeta.setDisplayName(configsFileConfig.getString("punch_effects." + effectName + ".holding_item.display_name"));
    			}
    			List<String> itemLore = configsFileConfig.getStringList("punch_effects." + effectName + ".holding_item.lore") != null && !configsFileConfig.getStringList("punch_effects." + effectName + ".holding_item.lore").isEmpty()
    					? configsFileConfig.getStringList("punch_effects." + effectName + ".holding_item.lore") : new ArrayList<>();
    			if(!itemLore.isEmpty())
    			{
    				holdingItemMeta.setLore(itemLore);
    			}
    			holdingItem.setItemMeta(holdingItemMeta);
    		}
    		boolean mobEffect = configsFileConfig.getBoolean("punch_effects." + effectName + ".mob_effect");
    		boolean damagerEffect = configsFileConfig.getBoolean("punch_effects." + effectName + ".damager_effect");
    		boolean victimEffect = configsFileConfig.getBoolean("punch_effects." + effectName + ".victim_effect");
    		boolean worldGuardCheck = configsFileConfig.getBoolean("punch_effects." + effectName + ".worldguard_check");
    		boolean usePermission = configsFileConfig.getBoolean("punch_effects." + effectName + ".use_permission");
    		int effectDuration = configsFileConfig.getInt("punch_effects." + effectName + ".duration");
    		int effectLevel = configsFileConfig.getInt("punch_effects." + effectName + ".level") - 1;
    		int effectCooldown = configsFileConfig.getInt("punch_effects." + effectName + ".cooldown");
    		int triggerChances = configsFileConfig.getInt("punch_effects." + effectName + ".trigger_chances");
    		effectsMap.put(effectName, new PunchEffect(effectPerm, effectType, holdingItem, mobEffect, damagerEffect, victimEffect, worldGuardCheck, usePermission, effectDuration, effectLevel, effectCooldown, triggerChances));
    	}
    	return effectsMap;
    }
    //endregion
    
    //region Message File (Creator/Getter)
    private YamlConfiguration messagesFileConfig;

    public YamlConfiguration getMessagesFile()
    {
        return this.messagesFileConfig;
    }
    
	public void createMessagesFiles()
	{
		File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
		if(!messagesFile.exists())
		{
			messagesFile.getParentFile().mkdir();
			plugin.saveResource("messages.yml", false);
		}
		
		messagesFileConfig = new YamlConfiguration();
		try
	    {
			messagesFileConfig.load(messagesFile);
	    }
	    catch (IOException | InvalidConfigurationException e)
		{
	    	plugin.getLogger().severe("An error occured while loading the messages file!");
	    	e.printStackTrace();
	    }
	}
	//endregion
	
	//region Database File (Creator/Getter)
    private YamlConfiguration databaseFileConfig;
    private File databaseFile;

    public YamlConfiguration getDatabaseFile()
    {
        return this.databaseFileConfig;
    }
    
	public void createDatabaseFiles()
	{
		databaseFile = new File(plugin.getDataFolder(), "database.yml");
		if(!databaseFile.exists())
		{
			try {
				databaseFile.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().severe("An error occured while creating the database file!");
				plugin.getLogger().severe(Arrays.toString(e.getStackTrace()));
			}
		}
		
		databaseFileConfig = new YamlConfiguration();
		try
	    {
			databaseFileConfig.load(databaseFile);
	    }
	    catch (IOException | InvalidConfigurationException e) {
	    	plugin.getLogger().severe("An error occured while loading the database file!");
	    	e.printStackTrace();
	    }
	}
	
	public Map<String, Map<String, Long>> loadDatabase()
	{
		if(databaseFileConfig != null)
		{
			if(databaseFileConfig.isConfigurationSection("players_temp_effects"))
			{
				Map<String, Map<String, Long>> playersTempEffects = new HashMap<>();
				ConfigurationSection databaseConfigSection = databaseFileConfig.getConfigurationSection("players_temp_effects");
				for(String UUID : databaseConfigSection.getKeys(false))
				{
					ConfigurationSection playerSection = databaseConfigSection.getConfigurationSection(UUID);
					for(String effectName : playerSection.getKeys(false))
					{						
						Long endTimeTempEffect = playerSection.getLong(effectName);
						if(!playersTempEffects.containsKey(UUID))
						{
							playersTempEffects.put(UUID, new HashMap<>());
						}
						playersTempEffects.get(UUID).put(effectName, endTimeTempEffect);
					}
				}
				plugin.getLogger().info("Database loaded successfully!");
				return playersTempEffects;
			}
			return new HashMap<>();
		}
		createDatabaseFiles();
		return loadDatabase();
	}
	
	public void saveDatabase()
	{
		if(databaseFileConfig != null)
		{		
			Map<String, Map<String, Long>> playersTempEffects = main.getPEC().getPlayersTempsEffect();
			if(!databaseFileConfig.isConfigurationSection("players_temp_effects"))
			{
				databaseFileConfig.createSection("players_temp_effects");
			}
			ConfigurationSection databaseConfigSection = databaseFileConfig.getConfigurationSection("players_temp_effects");
			for(String playerUUID : databaseConfigSection.getKeys(false))
			{
				if(!playersTempEffects.containsKey(playerUUID))
				{
					databaseFileConfig.set("players_temp_effects." + playerUUID, null);
				}
			}
			for(Entry<String, Map<String, Long>> playerTempEffects : playersTempEffects.entrySet())
			{
				if(databaseFileConfig.isConfigurationSection("players_temp_effects." + playerTempEffects.getKey()))
				{					
					ConfigurationSection playerSection = databaseConfigSection.getConfigurationSection(playerTempEffects.getKey());
					for(String effectName : playerSection.getKeys(false))
					{
						if(!playerTempEffects.getValue().containsKey(effectName))
						{
							databaseFileConfig.set("players_temp_effects." + playerTempEffects.getKey() + "." + effectName, null);
						}
					}
				}
				for(Entry<String, Long> TempEffect : playerTempEffects.getValue().entrySet())
				{
					databaseFileConfig.set("players_temp_effects." + playerTempEffects.getKey() + "." + TempEffect.getKey(), TempEffect.getValue());
				}
			}
			try {
				databaseFileConfig.save(databaseFile);
			} catch (IOException e) {
				plugin.getLogger().severe("An error occured while saving the database file!");
				e.printStackTrace();
			}
		}
		else
		{
			createDatabaseFiles();
			saveDatabase();
		}
	}
	//endregion
}