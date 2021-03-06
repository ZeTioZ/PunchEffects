package fr.zetioz.puncheffects;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

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

public class PunchEffectsDamage implements Listener{

	private Main main;
	private Map<String, PunchEffect> effectsMap;
	private WorldGuardHook wgh;
	private Map<UUID, Map<String, Long>> cooldownMap;
	private Random rand = new SecureRandom();
	private YamlConfiguration configsFile;
	private Map<String, Map<String, Long>> playersTempsEffect;
	
	public PunchEffectsDamage(Main main)
	{
		this.main = main;
		this.effectsMap = new HashMap<>();
		this.cooldownMap = new HashMap<>();
		this.wgh = this.main.getWorldGuardEnabled() ? new WorldGuardHook() : null;
		this.configsFile = main.getFilesManager().getConfigsFile();
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
	
	public void setEffectsMap(Map<String, PunchEffect> effectsMap)
	{
		this.effectsMap = effectsMap;
	}
	
	public Map<String, PunchEffect> getEffectsMap()
	{
		return this.effectsMap;
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
			Player damager = e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player ? (Player) ((Projectile) e.getDamager()).getShooter() : (Player) e.getDamager();
			LivingEntity victim = (LivingEntity) e.getEntity();
			for(Entry<String, PunchEffect> permissionEffect :  effectsMap.entrySet())
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
					main.getPEC().setPlayersTempsEffect(playersTempsEffect);
					main.getFilesManager().saveDatabase();
				}
				if(damager.hasPermission(permissionEffect.getValue().getEffectPermission())
						|| !permissionEffect.getValue().getUsePermission()
						|| (playersTempsEffect.containsKey(damager.getUniqueId().toString())
								&& playersTempsEffect.get(damager.getUniqueId().toString()).containsKey(permissionEffect.getKey())
								&& playersTempsEffect.get(damager.getUniqueId().toString()).get(permissionEffect.getKey()) > System.currentTimeMillis()))
				{
					PunchEffect pe = permissionEffect.getValue();
					String itemType = configsFile.getString("punch_effects." + permissionEffect.getKey() + ".holding_item.material");
					String itemName = pe.getHoldingItem().getType() != Material.AIR && pe.getHoldingItem().getItemMeta().getDisplayName() != null ? pe.getHoldingItem().getItemMeta().getDisplayName() : itemType.equalsIgnoreCase("PROJECTILE") ? configsFile.getString("punch_effects." + permissionEffect.getKey() + ".holding_item.display_name") : "none";
					List<String> itemLore = pe.getHoldingItem().getType() != Material.AIR && pe.getHoldingItem().getItemMeta().getLore() != null ? pe.getHoldingItem().getItemMeta().getLore() : itemType.equalsIgnoreCase("PROJECTILE") ? configsFile.getStringList("punch_effects." + permissionEffect.getKey() + ".holding_item.lore") : new ArrayList<>();
					boolean onlyArrow = configsFile.getBoolean("punch_effects." + permissionEffect.getKey() + ".holding_item.only_arrow");
					
					String damagerItemName = damager.getInventory().getItemInMainHand().getType() != Material.AIR && damager.getInventory().getItemInMainHand().getItemMeta().getDisplayName() != null ? damager.getInventory().getItemInMainHand().getItemMeta().getDisplayName() : "none";
					List<String> damagerItemLore = damager.getInventory().getItemInMainHand().getType() != Material.AIR && damager.getInventory().getItemInMainHand().getItemMeta().getLore() != null ? damager.getInventory().getItemInMainHand().getItemMeta().getLore() : new ArrayList<>();
					
					// WorldGuard Check
					if(((wgh != null
						&& pe.getWorldGuardCheck()
						&& wgh.isRegionPvp(damager))
						|| !pe.getWorldGuardCheck()
						|| wgh == null)
						// Item check
						// Set item check
						&& ((pe.getHoldingItem().getType() != Material.AIR
							&& pe.getHoldingItem().getType() == damager.getInventory().getItemInMainHand().getType()
							&& itemName.equals(damagerItemName)
							&& itemLore.equals(damagerItemLore))
						// No item only hand check
						|| (pe.getHoldingItem().getType() == Material.AIR
							&& itemType.equalsIgnoreCase("HAND")
							&& damager.getInventory().getItemInMainHand().getType() == Material.AIR)
						// Any item
						|| (pe.getHoldingItem().getType() == Material.AIR
							&& !itemType.equalsIgnoreCase("HAND")
							&& !itemType.equalsIgnoreCase("PROJECTILE"))
						// Projectile check
						|| (pe.getHoldingItem().getType() == Material.AIR
							&& e.getDamager() instanceof Projectile
							&& itemType.equalsIgnoreCase("PROJECTILE"))
							&& itemName.equals(damagerItemName)
							&& itemLore.equals(damagerItemLore)
							// Only Arrow check
							&& ((onlyArrow && e.getDamager().getType() == EntityType.ARROW)
								|| !onlyArrow))
						// Trigger Chances check
						&& rand.nextInt(100) < pe.getTriggerChances()
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
						cooldownMap.get(damager.getUniqueId()).put(permissionEffect.getKey(), (pe.getEffectCooldown() * 1000) + System.currentTimeMillis());
						if(PotionEffectType.getByName(pe.getEffectType()) != null)
						{
							if(PotionEffectType.getByName(pe.getEffectType()).isInstant())
							{
								if(pe.getDamagerEffect())
								{
									damager.addPotionEffect(new PotionEffect(PotionEffectType.getByName(pe.getEffectType()), 1, pe.getEffectLevel()));
								}
								if(!(victim instanceof Player) && pe.getMobEffect() || victim instanceof Player && pe.getVictimEffect())
								{
									victim.addPotionEffect(new PotionEffect(PotionEffectType.getByName(pe.getEffectType()), 1, pe.getEffectLevel()));
								}
							}
							else
							{
								if(pe.getDamagerEffect())
								{
									damager.addPotionEffect(new PotionEffect(PotionEffectType.getByName(pe.getEffectType()), 20 * pe.getEffectDuration(), pe.getEffectLevel()));
								}
								if(!(victim instanceof Player) && pe.getMobEffect() || victim instanceof Player && pe.getVictimEffect())
								{
									victim.addPotionEffect(new PotionEffect(PotionEffectType.getByName(pe.getEffectType()), 20 * pe.getEffectDuration(), pe.getEffectLevel()));
								}
							}
						}
						else if(pe.getEffectType().equalsIgnoreCase("FLAME") || pe.getEffectType().equalsIgnoreCase("FIRE"))
						{
							if(pe.getDamagerEffect())
							{
								damager.setFireTicks(pe.getEffectDuration() * 20);
							}
							if((!(victim instanceof Player) && pe.getMobEffect()) || (victim instanceof Player && pe.getVictimEffect()))
							{
								victim.setFireTicks(pe.getEffectDuration() * 20);
							}
						}
						else if(pe.getEffectType().equalsIgnoreCase("SMITE"))
						{
							if(pe.getDamagerEffect())
							{
								for(PotionEffect effect : damager.getActivePotionEffects())
								{
									damager.removePotionEffect(effect.getType());
								}
							}
							if((!(victim instanceof Player) && pe.getMobEffect()) || (victim instanceof Player && pe.getVictimEffect()))
							{
								for(PotionEffect effect : victim.getActivePotionEffects())
								{
									victim.removePotionEffect(effect.getType());									
								}
							}
						}
						else if(pe.getEffectType().equalsIgnoreCase("VAMPIRE"))
						{
							if(pe.getDamagerEffect())
							{
								damager.setHealth(damager.getHealth() + e.getDamage() > damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() ? damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : damager.getHealth() + e.getDamage());
							}
							if((!(victim instanceof Player) && pe.getMobEffect()) || (victim instanceof Player && pe.getVictimEffect()))
							{
								victim.setHealth(victim.getHealth() + e.getDamage() > victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() ? victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : victim.getHealth() + e.getDamage());
							}
						}
						else if(pe.getEffectType().equalsIgnoreCase("STEAL"))
						{
							if(victim instanceof Player && pe.getVictimEffect())
							{
								damager.addPotionEffects(victim.getActivePotionEffects());
								for(PotionEffect potion : victim.getActivePotionEffects())
								{
									victim.removePotionEffect(potion.getType());
								}
							}
						}
						else
						{
							cooldownMap.get(damager.getUniqueId()).remove(permissionEffect.getKey());
							String effectType = pe.getEffectType();
							main.getLogger().severe("�cThe punch potion effect �d\"{wrongEffect}\" �cdoens't exist!\nPlease remove it from the configs file and reload the plugin!".replace("{wrongEffect}", effectType));
						}
					}
				}
			}
		}
	}
}
