package fr.zetioz.puncheffects.commands;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.coreutils.TimeConverterUtils;
import fr.zetioz.puncheffects.PunchEffectsMain;
import fr.zetioz.puncheffects.utils.FilesUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.zetioz.puncheffects.objects.PunchEffect;

import static fr.zetioz.coreutils.ColorUtils.color;
import static fr.zetioz.coreutils.ColorUtils.sendMessage;

public class PunchEffectsCommand implements CommandExecutor, Listener, FilesManagerUtils.ReloadableFiles
{
	private final PunchEffectsMain instance;
	private YamlConfiguration messages;
	private YamlConfiguration config;
	private YamlConfiguration database;
	private String prefix;
	private Map<String, Map<String, Long>> playersTempsEffect;
	
	public PunchEffectsCommand(PunchEffectsMain instance) throws FileNotFoundException
	{
		this.instance = instance;
		instance.getFilesManager().addReloadable(this);
		reloadFiles();
	}

	@Override
	public void reloadFiles() throws FileNotFoundException
	{
		this.messages = this.instance.getFilesManager().getSimpleYaml("messages");
		this.config = this.instance.getFilesManager().getSimpleYaml("config");
		this.database = this.instance.getFilesManager().getSimpleYaml("database");
		this.prefix = color(messages.getString("prefix"));
		this.playersTempsEffect = instance.getPlayersTempsEffect();
	}

	@Override
	@Deprecated
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("puncheffects"))
		{
			if(args.length == 0)
			{
				sendMessage(sender, messages.getStringList("help-page"), prefix);
			}
			else if(args.length == 1)
			{
				if(args[0].equalsIgnoreCase("giveitem") || args[0].equalsIgnoreCase("gi"))
				{
					sendMessage(sender, messages.getStringList("errors.no-item-input"), prefix);
				}
				else if(args[0].equalsIgnoreCase("list"))
				{
					sendMessage(sender, messages.getString("effects-list-header"), prefix);

					for(String effectName : instance.getEffectsMap().keySet())
					{
						sendMessage(sender, messages.getString("effects-list-item"), prefix, "{effect}", effectName);
					}
				}
				else if(args[0].equalsIgnoreCase("help"))
				{
					sendMessage(sender, messages.getStringList("help-page"), prefix);
				}
				else if(args[0].equalsIgnoreCase("reload"))
				{
					if(sender.hasPermission("puncheffects.reload"))
					{
						instance.getFilesManager().reloadAllSimpleYaml();
						sendMessage(sender, messages.getStringList("reload-command"), prefix);
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.not-enough-permissions"), prefix);
					}
				}
				else
				{
					sendMessage(sender, messages.getStringList("help-page"), prefix);
				}
			}
			else if(args.length == 2)
			{
				if(args[0].equalsIgnoreCase("giveitem") || args[0].equalsIgnoreCase("gi"))
				{
					if(!(sender instanceof Player)) sendMessage(sender, messages.getStringList("errors.must-be-player"), prefix);

					final Player player = (Player) sender;
					if(!player.hasPermission("puncheffects.giveitem")) sendMessage(player, messages.getStringList("errors.not-enough-permissions"), prefix);

					final Map<String, PunchEffect> effectsMap = instance.getEffectsMap();
					if(!effectsMap.containsKey(args[1])) sendMessage(player, messages.getStringList("errors.unknown-effect"), prefix, "{effect}", args[1]);

					final PunchEffect itemPE = effectsMap.get(args[1]);
					if(!(itemPE.getHoldingItem().getType() != Material.AIR || config.getString("punch_effects." + args[1] + ".holding_item.material").equalsIgnoreCase("PROJECTILE"))) sendMessage(player, messages.getStringList("errors.no-item-give"), prefix);

					final ItemStack itemToGive = itemPE.getHoldingItem().getType() != Material.AIR ? itemPE.getHoldingItem() : new ItemStack(Material.BOW);
					if(itemToGive.getType() == Material.BOW)
					{
						final ItemMeta itemToGiveMeta = itemToGive.getItemMeta();
						if(!config.getString("punch_effects." + args[1] + ".holding_item.display_name").equalsIgnoreCase("NONE"))
						{
							itemToGiveMeta.setDisplayName(color(config.getString("punch_effects." + args[1] + ".holding_item.display_name")));
						}
						itemToGiveMeta.setLore(color(config.getStringList("punch_effects." + args[1] + ".holding_item.lore")));
						itemToGive.setItemMeta(itemToGiveMeta);
					}
					player.getInventory().addItem(itemToGive);
					sendMessage(player, messages.getStringList("weapon-given"), prefix, "{effect}", args[1]);
				}
				else
				{
					sendMessage(sender, messages.getStringList("errors.help-page"), prefix);
				}
			}
			else if(args.length == 3)
			{
				if(args[0].equalsIgnoreCase("removetempeffect") || args[0].equalsIgnoreCase("rte"))
				{
					if(sender.hasPermission("puncheffects.removetempeffect"))
					{
						final String effectName = args[2];
						final String playerUUID = instance.getServer().getOfflinePlayer(args[1]).getUniqueId().toString();
						if(playersTempsEffect.containsKey(playerUUID))
						{
							if(playersTempsEffect.get(playerUUID).containsKey(effectName))
							{
								playersTempsEffect.get(playerUUID).remove(effectName);
								if(playersTempsEffect.get(playerUUID).isEmpty())
								{
									playersTempsEffect.remove(playerUUID);
								}
								FilesUtils.saveDatabase(instance, database);
								sendMessage(sender, messages.getStringList("timed-effect-revoked"), prefix, "{player}", args[1], "{effect}", args[2]);
							}
							else
							{
								sendMessage(sender, messages.getStringList("errors.effect-not-listed"), prefix, "{player}", args[1], "{effect}", args[2]);
							}
						}
						else
						{
							sendMessage(sender, messages.getStringList("errors.player-empty-effects-list"), prefix, "{player}", args[1]);
						}
					}
					else
					{
						sendMessage(sender, messages.getStringList("errors.not-enough-permissions"), prefix);
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
						if(instance.getEffectsMap().containsKey(effectName))
						{
							final long endTimeEffect = System.currentTimeMillis() + TimeConverterUtils.stringTimeToMillis(args[3]);
							final String playerUUID = instance.getServer().getOfflinePlayer(args[1]).getUniqueId().toString();
							if(!playersTempsEffect.containsKey(playerUUID))
							{
								playersTempsEffect.put(playerUUID, new HashMap<>());
							}
							playersTempsEffect.get(playerUUID).put(effectName, endTimeEffect);
							FilesUtils.saveDatabase(instance, database);
							sendMessage(sender, messages.getStringList("timed-effect-given"), prefix, "{receiver}", args[1], "{effect}", args[2], "{time}", args[3]);
							if(instance.getServer().getOfflinePlayer(args[1]).isOnline())
							{
								sendMessage(sender, messages.getStringList("timed-effect-received"), "{giver}", sender.getName(), "{effect}", args[2], "{time}", args[3]);
							}
							else
							{
								sendMessage(sender, messages.getStringList("errors.non-existing-effect"), prefix, "{effect}", args[2]);
							}
						}
						else
						{
							sendMessage(sender, messages.getStringList("errors.not-enough-permissions"), prefix);
						}
					}
				}
				else
				{
					sendMessage(sender, messages.getStringList("help-page"), prefix);
				}
			}
			return true;
		}
		return false;
	}
}
