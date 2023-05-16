package fr.zetioz.puncheffects.utils;

import fr.zetioz.coreutils.MaterialUtils;
import fr.zetioz.itembuilderutils.ItemBuilderUtils;
import fr.zetioz.puncheffects.PunchEffectsMain;
import fr.zetioz.puncheffects.objects.PunchEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static fr.zetioz.coreutils.ColorUtils.color;

public class FilesUtils
{
	public static Map<String, PunchEffect> loadConfiguration(PunchEffectsMain instance, YamlConfiguration config)
	{
		final Map<String, PunchEffect> effectsMap = new HashMap<>();
		if(!config.isConfigurationSection("punch_effects")) return effectsMap;
		final Set<String> effectsPerms = config.getConfigurationSection("punch_effects").getKeys(false);
		for(String effectName : effectsPerms)
		{
			final String effectPerm = config.getString("punch_effects." + effectName + ".permission");
			final String effectType = config.getString("punch_effects." + effectName + ".effect");
			final ItemStack holdingItem = new ItemBuilderUtils(instance).material(MaterialUtils.getMaterial(instance, config.getString("punch_effects." + effectName + ".holding_item.material").toUpperCase()))
																		 .name(color(config.getString("punch_effects." + effectName + ".holding_item.display_name")))
																		 .lore(color(config.getStringList("punch_effects." + effectName + ".holding_item.lore"))).build();
			boolean mobEffect = config.getBoolean("punch_effects." + effectName + ".mob_effect");
			boolean damagerEffect = config.getBoolean("punch_effects." + effectName + ".damager_effect");
			boolean victimEffect = config.getBoolean("punch_effects." + effectName + ".victim_effect");
			boolean worldGuardCheck = config.getBoolean("punch_effects." + effectName + ".worldguard_check");
			boolean usePermission = config.getBoolean("punch_effects." + effectName + ".use_permission");
			int effectDuration = config.getInt("punch_effects." + effectName + ".duration");
			int effectLevel = config.getInt("punch_effects." + effectName + ".level") - 1;
			int effectCooldown = config.getInt("punch_effects." + effectName + ".cooldown");
			int triggerChances = config.getInt("punch_effects." + effectName + ".trigger_chances");
			effectsMap.put(effectName, new PunchEffect(effectPerm, effectType, holdingItem, mobEffect, damagerEffect, victimEffect, worldGuardCheck, usePermission, effectDuration, effectLevel, effectCooldown, triggerChances));
		}
		return effectsMap;
	}

	public static Map<String, Map<String, Long>> loadDatabase(PunchEffectsMain instance, YamlConfiguration database) throws FileNotFoundException
	{
		if(database != null)
		{
			if(database.isConfigurationSection("players_temp_effects"))
			{
				final Map<String, Map<String, Long>> playersTempEffects = new HashMap<>();
				final ConfigurationSection databaseConfigSection = database.getConfigurationSection("players_temp_effects");
				for(final String UUID : databaseConfigSection.getKeys(false))
				{
					ConfigurationSection playerSection = databaseConfigSection.getConfigurationSection(UUID);
					for(final String effectName : playerSection.getKeys(false))
					{
						final Long endTimeTempEffect = playerSection.getLong(effectName);
						if(!playersTempEffects.containsKey(UUID))
						{
							playersTempEffects.put(UUID, new HashMap<>());
						}
						playersTempEffects.get(UUID).put(effectName, endTimeTempEffect);
					}
				}
				instance.getLogger().info("Database loaded successfully!");
				return playersTempEffects;
			}
			return new HashMap<>();
		}
		instance.getFilesManager().createSimpleYaml("database");
		return loadDatabase(instance, instance.getFilesManager().getSimpleYaml("database"));
	}

	public static void saveDatabase(PunchEffectsMain instance, YamlConfiguration database)
	{
		final Map<String, Map<String, Long>> playersTempEffects = instance.getPlayersTempsEffect();
		if(!database.isConfigurationSection("players_temp_effects"))
		{
			database.createSection("players_temp_effects");
		}
		final ConfigurationSection databaseConfigSection = database.getConfigurationSection("players_temp_effects");
		for(String playerUUID : databaseConfigSection.getKeys(false))
		{
			if(!playersTempEffects.containsKey(playerUUID))
			{
				database.set("players_temp_effects." + playerUUID, null);
			}
		}
		for(Map.Entry<String, Map<String, Long>> playerTempEffects : playersTempEffects.entrySet())
		{
			if(database.isConfigurationSection("players_temp_effects." + playerTempEffects.getKey()))
			{
				final ConfigurationSection playerSection = databaseConfigSection.getConfigurationSection(playerTempEffects.getKey());
				for(String effectName : playerSection.getKeys(false))
				{
					if(!playerTempEffects.getValue().containsKey(effectName))
					{
						database.set("players_temp_effects." + playerTempEffects.getKey() + "." + effectName, null);
					}
				}
			}
			for(Map.Entry<String, Long> TempEffect : playerTempEffects.getValue().entrySet())
			{
				database.set("players_temp_effects." + playerTempEffects.getKey() + "." + TempEffect.getKey(), TempEffect.getValue());
			}
		}
		instance.getFilesManager().saveSimpleYaml("database");
	}
}
