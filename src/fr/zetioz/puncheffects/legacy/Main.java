package fr.zetioz.puncheffects.legacy;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.zetioz.puncheffects.legacy.utils.FilesManager;

public class Main extends JavaPlugin implements Listener
{
	private Plugin plugin;
	private FilesManager filesManager;
	private PunchEffectsDamage ped;
	private PunchEffectsCommand pec;
	private boolean worldGuardEnabled;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		
		filesManager = new FilesManager(this);
		filesManager.createConfigsFile();
		filesManager.createMessagesFiles();
		
		this.worldGuardEnabled = getServer().getPluginManager().isPluginEnabled("WorldGuard");
		if(this.worldGuardEnabled)
		{
			getLogger().info("WorldGuard detected and hooks have been loaded!");
		}
		else
		{
			getLogger().info("WorldGuard is missing some functions will not be available!");
		}
		ped = new PunchEffectsDamage(this);
		pec = new PunchEffectsCommand(this);
		registerEvents(this, ped);
		getCommand("puncheffects").setExecutor(pec);
		
		ped.setEffectsMap(filesManager.configsLoader());
		ped.setPlayersTempsEffect(filesManager.loadDatabase());
		pec.setPlayersTempsEffect(filesManager.loadDatabase());
	}
	
	@Override
	public void onDisable()
	{
		this.plugin = null;
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
	
	public FilesManager getFilesManager()
	{
		return this.filesManager;
	}
	
	public PunchEffectsDamage getPED()
	{
		return this.ped;
	}
	
	public PunchEffectsCommand getPEC()
	{
		return this.pec;
	}
	
	public boolean getWorldGuardEnabled()
	{	
		return this.worldGuardEnabled;
	}
}