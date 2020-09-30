package fr.zetioz.puncheffects.legacy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.zetioz.puncheffects.legacy.objects.PunchEffect;
import fr.zetioz.puncheffects.legacy.utils.TimeConverter;
import fr.zetioz.puncheffects.legacy.utils.Color;


public class PunchEffectsCommand implements CommandExecutor, Listener
{

	private Main main;
	private YamlConfiguration messagesFile;
	private YamlConfiguration configsFile;
	private String prefix;
	private Map<String, Map<String, Long>> playersTempsEffect;
	
	public PunchEffectsCommand(Main main)
	{
		this.main = main;
		this.messagesFile = this.main.getFilesManager().getMessagesFile();
		this.configsFile = this.main.getFilesManager().getConfigsFile();
		this.prefix = Color.color(messagesFile.getString("prefix"));
		this.playersTempsEffect = new HashMap<>();
	}
	
	public Map<String, Map<String, Long>> getPlayersTempsEffect()
	{
		return this.playersTempsEffect;
	}
	
	public void setPlayersTempsEffect(Map<String, Map<String, Long>> playersTempsEffect)
	{
		this.playersTempsEffect = playersTempsEffect;
	}
	
	@SuppressWarnings("deprecation")
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
				if(args[0].equalsIgnoreCase("giveitem") || args[0].equalsIgnoreCase("gi"))
				{
					for(String line : messagesFile.getStringList("errors.no-item-input"))
					{
						line = Color.color(line);
						sender.sendMessage(prefix + line);
					}
				}
				else if(args[0].equalsIgnoreCase("list"))
				{
					sender.sendMessage(prefix + Color.color(messagesFile.getString("effects-list-header")));
					
					for(String effectName : main.getPED().getEffectsMap().keySet())
					{
						sender.sendMessage(Color.color(messagesFile.getString("effects-list-color") + effectName));
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
							line = Color.color(line);
							sender.sendMessage(prefix + line);
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
						{
							line = Color.color(line);
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
				if(args[0].equalsIgnoreCase("giveitem") || args[0].equalsIgnoreCase("gi"))
				{
					if(sender instanceof Player)
					{				
						if(sender.hasPermission("puncheffects.giveitem"))
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
									for(String line : messagesFile.getStringList("weapon-given"))
									{
										line = line.replace("{effect}", args[1]);
										line = Color.color(line);
										sender.sendMessage(prefix + line);
									}
									
								}
								else
								{
									for(String line : messagesFile.getStringList("errors.no-item-give"))
									{
										line = Color.color(line);
										sender.sendMessage(prefix + line);
									}
								}
							}
							else
							{
								for(String line : messagesFile.getStringList("errors.unknown-effect"))
								{
									line = line.replace("{effect}", args[1]);
									line = Color.color(line);
									sender.sendMessage(prefix + line);
								}
							}
						}
						else
						{
							for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
							{
								line = Color.color(line);
								sender.sendMessage(prefix + line);
							}
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.must-be-player"))
						{
							line = Color.color(line);
							sender.sendMessage(prefix + line);
						}
					}
				}
				else
				{
					sendHelpPage(sender);
				}
			}
			else if(args.length == 3)
			{
				if(args[0].equalsIgnoreCase("removetempeffect") || args[0].equalsIgnoreCase("rte"))
				{
					if(sender.hasPermission("puncheffects.removetempeffect"))
					{						
						String effectName = args[2];
						String playerUUID = main.getServer().getOfflinePlayer(args[1]).getUniqueId().toString();
						if(playersTempsEffect.containsKey(playerUUID))
						{
							if(playersTempsEffect.get(playerUUID).containsKey(effectName))
							{
								playersTempsEffect.get(playerUUID).remove(effectName);
								if(playersTempsEffect.get(playerUUID).isEmpty())
								{
									playersTempsEffect.remove(playerUUID);
								}
								main.getPED().setPlayersTempsEffect(playersTempsEffect);
								main.getFilesManager().saveDatabase();
								for(String line : messagesFile.getStringList("timed-effect-revoked"))
								{
									line = line.replace("{player}", args[1]);
									line = line.replace("{effect}", args[2]);
									line = Color.color(line);
									sender.sendMessage(prefix + line);
								}
							}
							else
							{								
								for(String line : messagesFile.getStringList("errors.effect-not-listed"))
								{
									line = line.replace("{player}", args[1]);
									line = line.replace("{effect}", effectName);
									line = Color.color(line);
									sender.sendMessage(prefix + line);
								}
							}
						}
						else
						{							
							for(String line : messagesFile.getStringList("errors.player-empty-effects-list"))
							{
								line = line.replace("{player}", args[1]);
								line = Color.color(line);
								sender.sendMessage(prefix + line);
							}
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
						{
							line = Color.color(line);
							sender.sendMessage(prefix + line);
						}
					}
				}
			}
			else if(args.length == 4)
			{
				if(args[0].equalsIgnoreCase("givetempeffect") || args[0].equalsIgnoreCase("gte"))
				{
					if(sender.hasPermission("puncheffects.givetempeffect"))
					{						
						String effectName = args[2];
						if(main.getPED().getEffectsMap().containsKey(effectName))
						{							
							long endTimeEffect = System.currentTimeMillis() + TimeConverter.stringTimeToMillis(args[3]);
							String playerUUID = main.getServer().getOfflinePlayer(args[1]).getUniqueId().toString();
							if(!playersTempsEffect.containsKey(playerUUID))
							{
								playersTempsEffect.put(playerUUID, new HashMap<>());
							}
							playersTempsEffect.get(playerUUID).put(effectName, endTimeEffect);
							main.getPED().setPlayersTempsEffect(playersTempsEffect);
							main.getFilesManager().saveDatabase();
							for(String line : messagesFile.getStringList("timed-effect-given"))
							{
								line = line.replace("{receiver}", args[1]);
								line = line.replace("{effect}", args[2]);
								line = line.replace("{time}", args[3]);
								line = Color.color(line);
								sender.sendMessage(prefix + line);
							}
							if(main.getServer().getOfflinePlayer(args[1]).isOnline())
							{
								for(String line : messagesFile.getStringList("timed-effect-received"))
								{
									line = line.replace("{giver}", sender.getName());
									line = line.replace("{effect}", args[2]);
									line = line.replace("{time}", args[3]);
									line = Color.color(line);
									sender.sendMessage(prefix + line);
								}
							}
						}
						else
						{
							for(String line : messagesFile.getStringList("errors.non-existing-effect"))
							{
								line = line.replace("{effect}", args[2]);
								line = Color.color(line);
								sender.sendMessage(prefix + line);
							}
						}
					}
					else
					{
						for(String line : messagesFile.getStringList("errors.not-enough-permissions"))
						{
							line = Color.color(line);
							sender.sendMessage(prefix + line);
						}
					}
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
			line = Color.color(line);
			player.sendMessage(prefix + line);
		}
	}

}
