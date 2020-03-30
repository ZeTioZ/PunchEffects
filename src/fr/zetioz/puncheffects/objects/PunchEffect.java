package fr.zetioz.puncheffects.objects;

import org.bukkit.inventory.ItemStack;

public class PunchEffect
{
	private String effectPermission;
	private String effectType;
	private ItemStack holdingItem;
	private boolean mobEffect;
	private boolean damagerEffect;
	private boolean victimEffect;
	private boolean worldGuardCheck;
	private boolean usePermission;
	private int effectDuration;
	private int effectLevel;
	private int effectCooldown;
	private int triggerChances;
	
	public PunchEffect(String effectPermission,
			           String effectName,
			           ItemStack holdingItem,
			           boolean mobEffect,
			           boolean damagerEffect,
			           boolean playerEffect,
			           boolean worldGuardCheck,
			           boolean usePermission,
			           int effectDuration,
			           int effectLevel,
			           int effectCooldown,
			           int triggerChances)
	{
		this.effectPermission = effectPermission;
		this.effectType = effectName;
		this.holdingItem = holdingItem;
		this.mobEffect = mobEffect;
		this.damagerEffect = damagerEffect;
		this.victimEffect = playerEffect;
		this.worldGuardCheck = worldGuardCheck;
		this.usePermission = usePermission;
		this.effectDuration = effectDuration;
		this.effectLevel = effectLevel;
		this.effectCooldown = effectCooldown;
		this.triggerChances = triggerChances;
		
	}
	
	public String getEffectPermission()
	{
		return this.effectPermission;
	}
	
	public String getEffectType()
	{
		return this.effectType;
	}

	public ItemStack getHoldingItem()
	{
		return this.holdingItem;
	}
	
	public boolean getMobEffect()
	{
		return this.mobEffect;
	}
	
	public boolean getDamagerEffect()
	{
		return this.damagerEffect;
	}
	
	public boolean getVictimEffect()
	{
		return this.victimEffect;
	}
	
	public boolean getWorldGuardCheck()
	{
		return this.worldGuardCheck;
	}
	
	public boolean getUsePermission()
	{
		return this.usePermission;
	}
	
	public int getEffectDuration()
	{
		return this.effectDuration;
	}
	
	public int getEffectLevel()
	{
		return this.effectLevel;
	}

	public int getEffectCooldown()
	{
		return this.effectCooldown;
	}
	
	public int getTriggerChances()
	{
		return this.triggerChances;
	}
}