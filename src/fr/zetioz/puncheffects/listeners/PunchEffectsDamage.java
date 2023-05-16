package fr.zetioz.puncheffects.listeners;

import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import fr.zetioz.coreutils.FilesManagerUtils;
import fr.zetioz.puncheffects.PunchEffectsMain;
import fr.zetioz.puncheffects.utils.FilesUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.zetioz.puncheffects.objects.PunchEffect;
import fr.zetioz.puncheffects.utils.WorldGuardHook;
import org.bukkit.util.Vector;

import static fr.zetioz.coreutils.ColorUtils.color;

public class PunchEffectsDamage implements Listener, FilesManagerUtils.ReloadableFiles
{
	private final PunchEffectsMain instance;
	private final WorldGuardHook worldGuardHook;
	private final Random rand = new SecureRandom();
	private final Map<UUID, Map<String, Long>> cooldownMap;
	private Map<String, PunchEffect> effectsMap;
	private Map<String, Map<String, Long>> playersTempsEffect;
	private YamlConfiguration config;
	private YamlConfiguration database;

	public PunchEffectsDamage(PunchEffectsMain instance) throws FileNotFoundException
	{
		this.instance = instance;
		this.cooldownMap = new HashMap<>();
		this.worldGuardHook = this.instance.getWorldGuardEnabled() ? new WorldGuardHook() : null;
		instance.getFilesManager().addReloadable(this);
		reloadFiles();
	}

	@Override
	public void reloadFiles() throws FileNotFoundException
	{
		this.config = instance.getFilesManager().getSimpleYaml("config");
		this.database = instance.getFilesManager().getSimpleYaml("database");
		this.effectsMap = instance.getEffectsMap();
		this.playersTempsEffect = instance.getPlayersTempsEffect();
	}
	
	@EventHandler
	public void onPlayerPunch(EntityDamageByEntityEvent e)
	{
		if((e.getDamager() instanceof Player
				&& e.getEntity() instanceof LivingEntity)
			|| (e.getDamager() instanceof Projectile
				&& ((Projectile) e.getDamager()).getShooter() instanceof Player
				&& e.getEntity() instanceof LivingEntity))
		{
			final Player damager = e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player ? (Player) ((Projectile) e.getDamager()).getShooter() : (Player) e.getDamager();
			final LivingEntity victim = (LivingEntity) e.getEntity();
			for(Entry<String, PunchEffect> permissionEffect : effectsMap.entrySet())
			{
				if(playersTempsEffect.containsKey(damager.getUniqueId().toString())
						&& playersTempsEffect.get(damager.getUniqueId().toString()).containsKey(permissionEffect.getKey())
						&& playersTempsEffect.get(damager.getUniqueId().toString()).get(permissionEffect.getKey()) <= System.currentTimeMillis())
				{
					playersTempsEffect.get(damager.getUniqueId().toString()).remove(permissionEffect.getKey());
					if(playersTempsEffect.get(damager.getUniqueId().toString()).isEmpty())
					{
						playersTempsEffect.remove(damager.getUniqueId().toString());
					}
					FilesUtils.saveDatabase(instance, database);
				}
				if(damager.hasPermission(permissionEffect.getValue().getEffectPermission())
						|| !permissionEffect.getValue().getUsePermission()
						|| (playersTempsEffect.containsKey(damager.getUniqueId().toString())
								&& playersTempsEffect.get(damager.getUniqueId().toString()).containsKey(permissionEffect.getKey())
								&& playersTempsEffect.get(damager.getUniqueId().toString()).get(permissionEffect.getKey()) > System.currentTimeMillis()))
				{
					final PunchEffect punchEffect = permissionEffect.getValue();
					final String itemType = config.getString("punch_effects." + permissionEffect.getKey() + ".holding_item.material");
					final String itemName = punchEffect.getHoldingItem().getType() != Material.AIR ? punchEffect.getHoldingItem().getItemMeta().getDisplayName() : itemType.equalsIgnoreCase("PROJECTILE") ? config.getString("punch_effects." + permissionEffect.getKey() + ".holding_item.display_name") : "none";
					final List<String> itemLore = punchEffect.getHoldingItem().getType() != Material.AIR && punchEffect.getHoldingItem().getItemMeta().getLore() != null ? punchEffect.getHoldingItem().getItemMeta().getLore() : itemType.equalsIgnoreCase("PROJECTILE") ? config.getStringList("punch_effects." + permissionEffect.getKey() + ".holding_item.lore") : new ArrayList<>();
					final boolean onlyArrow = config.getBoolean("punch_effects." + permissionEffect.getKey() + ".holding_item.only_arrow");

					final String damagerItemName = damager.getInventory().getItemInMainHand().getType() != Material.AIR ? damager.getInventory().getItemInMainHand().getItemMeta().getDisplayName() : "none";
					final List<String> damagerItemLore = damager.getInventory().getItemInMainHand().getType() != Material.AIR && damager.getInventory().getItemInMainHand().getItemMeta().getLore() != null ? damager.getInventory().getItemInMainHand().getItemMeta().getLore() : new ArrayList<>();
					
					// WorldGuard Check
					if(((worldGuardHook != null
						&& punchEffect.getWorldGuardCheck()
						&& worldGuardHook.isRegionPvp(damager))
						|| !punchEffect.getWorldGuardCheck()
						|| worldGuardHook == null)
						// Item check
						// Set item check
						&& ((punchEffect.getHoldingItem().getType() != Material.AIR
							&& punchEffect.getHoldingItem().getType() == damager.getInventory().getItemInMainHand().getType()
							&& color(itemName).equals(damagerItemName)
							&& color(itemLore).equals(damagerItemLore))
						// No item only hand check
						|| (punchEffect.getHoldingItem().getType() == Material.AIR
							&& itemType.equalsIgnoreCase("HAND")
							&& damager.getInventory().getItemInMainHand().getType() == Material.AIR)
						// Any item
						|| (punchEffect.getHoldingItem().getType() == Material.AIR
							&& !itemType.equalsIgnoreCase("HAND")
							&& !itemType.equalsIgnoreCase("PROJECTILE"))
						// Projectile check
						|| (punchEffect.getHoldingItem().getType() == Material.AIR
							&& e.getDamager() instanceof Projectile
							&& itemType.equalsIgnoreCase("PROJECTILE"))
							&& color(itemName).equals(damagerItemName)
							&& color(itemLore).equals(damagerItemLore)
							// Only Arrow check
							&& (!onlyArrow || e.getDamager().getType() == EntityType.ARROW))
						// Trigger Chances check
						&& rand.nextInt(100) < punchEffect.getTriggerChances()
						// Cooldown check
						&& ((cooldownMap.containsKey(damager.getUniqueId())
							&& cooldownMap.get(damager.getUniqueId()).containsKey(permissionEffect.getKey())
							&& cooldownMap.get(damager.getUniqueId()).get(permissionEffect.getKey()) < System.currentTimeMillis())
							|| !cooldownMap.containsKey(damager.getUniqueId())
							|| !cooldownMap.get(damager.getUniqueId()).containsKey(permissionEffect.getKey())))
					{
						if(!cooldownMap.containsKey(damager.getUniqueId()))
						{
							cooldownMap.put(damager.getUniqueId(), new HashMap<>());
						}
						cooldownMap.get(damager.getUniqueId()).put(permissionEffect.getKey(), (punchEffect.getEffectCooldown() * 1000L) + System.currentTimeMillis());
						if(PotionEffectType.getByName(punchEffect.getEffectType()) != null)
						{
							if(PotionEffectType.getByName(punchEffect.getEffectType()).isInstant())
							{
								if(punchEffect.getDamagerEffect())
								{
									damager.addPotionEffect(new PotionEffect(PotionEffectType.getByName(punchEffect.getEffectType()), 1, punchEffect.getEffectLevel()));
								}
								if(!(victim instanceof Player) && punchEffect.getMobEffect() || victim instanceof Player && punchEffect.getVictimEffect())
								{
									victim.addPotionEffect(new PotionEffect(PotionEffectType.getByName(punchEffect.getEffectType()), 1, punchEffect.getEffectLevel()));
								}
							}
							else
							{
								if(punchEffect.getDamagerEffect())
								{
									damager.addPotionEffect(new PotionEffect(PotionEffectType.getByName(punchEffect.getEffectType()), 20 * punchEffect.getEffectDuration(), punchEffect.getEffectLevel()));
								}
								if(!(victim instanceof Player) && punchEffect.getMobEffect() || victim instanceof Player && punchEffect.getVictimEffect())
								{
									victim.addPotionEffect(new PotionEffect(PotionEffectType.getByName(punchEffect.getEffectType()), 20 * punchEffect.getEffectDuration(), punchEffect.getEffectLevel()));
								}
							}
						}
						else if(punchEffect.getEffectType().equalsIgnoreCase("FLAME") || punchEffect.getEffectType().equalsIgnoreCase("FIRE"))
						{
							if(punchEffect.getDamagerEffect())
							{
								damager.setFireTicks(punchEffect.getEffectDuration() * 20);
							}
							if((!(victim instanceof Player) && punchEffect.getMobEffect()) || (victim instanceof Player && punchEffect.getVictimEffect()))
							{
								victim.setFireTicks(punchEffect.getEffectDuration() * 20);
							}
						}
						else if(punchEffect.getEffectType().equalsIgnoreCase("SMITE"))
						{
							if(punchEffect.getDamagerEffect())
							{
								for(PotionEffect effect : damager.getActivePotionEffects())
								{
									damager.removePotionEffect(effect.getType());
								}
							}
							if((!(victim instanceof Player) && punchEffect.getMobEffect()) || (victim instanceof Player && punchEffect.getVictimEffect()))
							{
								for(PotionEffect effect : victim.getActivePotionEffects())
								{
									victim.removePotionEffect(effect.getType());									
								}
							}
						}
						else if(punchEffect.getEffectType().equalsIgnoreCase("VAMPIRE"))
						{
							if(punchEffect.getDamagerEffect())
							{
								damager.setHealth(Math.min(damager.getHealth() + e.getDamage(), damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
							}
							if((!(victim instanceof Player) && punchEffect.getMobEffect()) || (victim instanceof Player && punchEffect.getVictimEffect()))
							{
								victim.setHealth(Math.min(victim.getHealth() + e.getDamage(), victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
							}
						}
						else if(punchEffect.getEffectType().equalsIgnoreCase("STEAL"))
						{
							if(victim instanceof Player && punchEffect.getVictimEffect())
							{
								damager.addPotionEffects(victim.getActivePotionEffects());
								for(PotionEffect potion : victim.getActivePotionEffects())
								{
									victim.removePotionEffect(potion.getType());
								}
							}
						}
						else if(punchEffect.getEffectType().equalsIgnoreCase("KNOCKBACK"))
						{
							if(punchEffect.getVictimEffect() || (punchEffect.getMobEffect() && !(victim instanceof Player)))
							{
								victim.setVelocity(victim.getVelocity().add(damager.getLocation().getDirection().multiply((2.5 + (0.5 * punchEffect.getEffectLevel()))).add(new Vector(0,2,0))));
							}
							if(punchEffect.getDamagerEffect())
							{
								damager.setVelocity(damager.getVelocity().add(victim.getLocation().getDirection().multiply((2.5 + (0.5 * punchEffect.getEffectLevel()))).add(new Vector(0,2,0))));
							}
						}
						else
						{
							cooldownMap.get(damager.getUniqueId()).remove(permissionEffect.getKey());
							final String effectType = punchEffect.getEffectType();
							instance.getLogger().severe("The punch potion effect \"{wrongEffect}\" doens't exist!\nPlease remove it from the configs file and reload the plugin!".replace("{wrongEffect}", effectType));
						}
					}
				}
			}
		}
	}
}
