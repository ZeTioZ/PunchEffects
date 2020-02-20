package fr.zetioz.puncheffects.legacy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

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
	    catch (IOException | InvalidConfigurationException e) {
	    	plugin.getLogger().severe("An error occured while loading the messages file!");
	    }
	}
	//endregion
}