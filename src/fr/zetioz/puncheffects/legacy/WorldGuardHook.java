package fr.zetioz.puncheffects.legacy;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardHook implements Listener
{	
	public boolean isRegionPvp(Player player)
	{
		ApplicableRegionSet appRegion = WGBukkit.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
		if(appRegion.size() == 0)
		{
			ProtectedRegion globalRegion = WGBukkit.getPlugin().getRegionManager(player.getWorld()).getRegion("__global__");
			return !globalRegion.getFlags().containsKey(DefaultFlag.PVP) || (globalRegion.getFlags().containsKey(DefaultFlag.PVP) && globalRegion.getFlag(DefaultFlag.PVP) == State.ALLOW);
		}
		else
		{			
			for(ProtectedRegion r : appRegion)
			{
				if(!r.getFlags().containsKey(DefaultFlag.PVP) || (r.getFlags().containsKey(DefaultFlag.PVP) && r.getFlag(DefaultFlag.PVP) == State.ALLOW))
				{
					return true;
				}
			}
			return false;
		}
	}
}
