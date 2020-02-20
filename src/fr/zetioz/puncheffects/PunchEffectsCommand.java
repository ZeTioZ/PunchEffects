package fr.zetioz.puncheffects;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class PunchEffectsCommand implements CommandExecutor, Listener
{

	private Main main;
	private YamlConfiguration messagesFile;
	private YamlConfiguration configsFile;
	private String prefix;
	
	public PunchEffectsCommand(Main main)
	{
		this.main = main;
		this.messagesFile = this.main.getFilesManager().getMessagesFile();
		this.configsFile = this.main.getFilesManager().getConfigsFile();
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
				if(args[0].equalsIgnoreCase("give"))
				{
					for(String line : messagesFile.getStringList("errors.no-item-inputted"))
					{
						line = ChatColor.translateAlternateColorCodes('&', line);
						sender.sendMessage(prefix + line);
					}
				}
				else if(args[0].equalsIgnoreCase("help"))
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
			else if(args.length == 2)
			{
				if(args[0].equalsIgnoreCase("give"))
				{
					if(sender instanceof Player)
					{				
						if(sender.hasPermission("puncheffects.reload"))
						{						
							Map<String, PunchEffect> pe = main.getPED().getEffectsMap();
							if(pe.keySet().contains(args[1]))
							{
								PunchEffect itemPE = pe.get(args[1]);
								if((itemPE.getHoldingItem().getType() != Material.AIR
										|| configsFile.getString("punch_effects." + args[1] + ".holding_item.material").equalsIgnoreCase("PROJECTILE")))
								{
									ItemStack itemToGive = itemPE.getHoldingItem().getType() != Material.AIR ? itemPE.getHoldingItem() : new ItemStack(Material.BOW);
									if(itemToGive.getType() == Material.BOW)
									{
										ItemMeta itemToGiveMeta = itemToGive.getItemMeta();
										if(!configsFile.getString("punch_effects." + args[1] + ".holding_item.display_name").equalsIgnoreCase("NONE"))
										{
											itemToGiveMeta.setDisplayName(configsFile.getString("punch_effects." + args[1] + ".holding_item.display_name"));
										}
										itemToGiveMeta.setLore(configsFile.getStringList("punch_effects." + args[1] + ".holding_item.lore"));
										itemToGive.setItemMeta(itemToGiveMeta);
									}
									Player p = (Player) sender;
									p.getInventory().addItem(itemToGive);
								}
								else
								{
									for(String line : messagesFile.getStringList("errors.no-item-give"))
									{
										line = ChatColor.translateAlternateColorCodes('&', line);
										sender.sendMessage(prefix + line);
									}
								}
							}
							else
							{
								for(String line : messagesFile.getStringList("errors.unknown-effect"))
								{
									line = line.replace("{effect}", args[1]);
									line = ChatColor.translateAlternateColorCodes('&', line);
									sender.sendMessage(prefix + line);
								}
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
						for(String line : messagesFile.getStringList("errors.must-be-player"))
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
