package fr.zetioz.puncheffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

public class PunchEffectsCommand implements CommandExecutor, Listener
{

	private Main main;
	private YamlConfiguration messagesFile;
	private String prefix;
	
	public PunchEffectsCommand(Main main)
	{
		this.main = main;
		this.messagesFile = this.main.getFilesManager().getMessagesFile();
		this.prefix = ChatColor.translateAlternateColorCodes('&', messagesFile.getString("prefix"));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		if(cmd.getName().equalsIgnoreCase("puncheffects"))
		{
			if(args.length == 0)
			{
				sendHelpPage(sender);
			}
			else if(args.length == 1)
			{
				if(args[0].equalsIgnoreCase("help"))
				{
					sendHelpPage(sender);
				}
				else if(args[0].equalsIgnoreCase("reload"))
				{
					if(sender.hasPermission("puncheffects.reload"))
					{
						Bukkit.getPluginManager().disablePlugin(main);
						Bukkit.getPluginManager().enablePlugin(main);
						for(String line : messagesFile.getStringList("plugin-reload"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							sender.sendMessage(prefix + line);
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.not-enought-permissions"))
						{
							line = ChatColor.translateAlternateColorCodes('&', line);
							sender.sendMessage(prefix + line);
						}
					}
				}
				else
				{
					sendHelpPage(sender);
				}
			}
			else
			{
				sendHelpPage(sender);
			}
		}
		return false;
	}
	
	public void sendHelpPage(CommandSender player)
	{
		for(String line : messagesFile.getStringList("help-page"))
		{
			line = ChatColor.translateAlternateColorCodes('&', line);
			player.sendMessage(prefix + line);
		}
	}

}
