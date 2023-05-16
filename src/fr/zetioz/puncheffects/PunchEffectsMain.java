package fr.zetioz.puncheffects;

import fr.zetioz.coreutils.EnabledDependenciesUtils;
import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.puncheffects.commands.PunchEffectsCommand;
import fr.zetioz.puncheffects.listeners.PunchEffectsDamage;
import fr.zetioz.puncheffects.objects.PunchEffect;
import fr.zetioz.puncheffects.utils.FilesUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.util.*;

@Getter
public class PunchEffectsMain extends JavaPlugin implements Listener, FilesManagerUtils.ReloadableFiles
{
	private Plugin plugin;
	private FilesManagerUtils filesManager;
	@Setter
	private Map<String, Map<String, Long>> playersTempsEffect;
	@Setter
	private Map<String, PunchEffect> effectsMap;
	private boolean worldGuardEnabled;

	@Override
	public void onEnable()
	{
		plugin = this;
		
		filesManager = new FilesManagerUtils(this);
		filesManager.createSimpleYaml("config");
		filesManager.createSimpleYaml("messages");
		filesManager.createSimpleYaml("database");

		final List<String> enabledDependencies = new EnabledDependenciesUtils(this).getEnabledDependencies();
		this.worldGuardEnabled = enabledDependencies.contains("WorldGuard");
		try
		{
			filesManager.addReloadable(this);
			reloadFiles();
			registerEvents(this, new PunchEffectsDamage(this));
			getCommand("puncheffects").setExecutor(new PunchEffectsCommand(this));
		}
		catch(FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void onDisable()
	{
		this.plugin = null;
	}

	@Override
	public void reloadFiles() throws FileNotFoundException
	{
		final YamlConfiguration config = filesManager.getSimpleYaml("config");
		final YamlConfiguration database = filesManager.getSimpleYaml("database");

		this.setEffectsMap(FilesUtils.loadConfiguration(this, config));
		getLogger().info("Loaded " + effectsMap.size() + " punch effects");
		this.setPlayersTempsEffect(FilesUtils.loadDatabase(this, database));
		getLogger().info("Loaded " + playersTempsEffect.size() + " players temps effects");
	}
	private void registerEvents(Plugin plugin, Listener... listeners)
	{
		for(Listener listener : listeners)
		{
			Bukkit.getPluginManager().registerEvents(listener, plugin);
		}
	}
	
	public Plugin getPlugin()
	{
		return this.plugin;
	}
	
	public FilesManagerUtils getFilesManager()
	{
		return this.filesManager;
	}
	
	public boolean getWorldGuardEnabled()
	{	
		return this.worldGuardEnabled;
	}
}