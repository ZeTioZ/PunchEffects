package fr.zetioz.puncheffects.utils;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class WorldGuardHook implements Listener
{	
	public boolean isRegionPvp(Player player)
	{
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		World weWorld = BukkitAdapter.adapt(player.getWorld());
		RegionManager regions = container.get(weWorld);
		Map<String, ProtectedRegion> regionsMap = regions.getRegions();
		for(Entry<String, ProtectedRegion> r : regionsMap.entrySet())
		{
			if(!r.getValue().getFlags().containsKey(Flags.PVP) || (r.getValue().getFlags().containsKey(Flags.PVP) && r.getValue().getFlag(Flags.PVP) == State.ALLOW))
			{
				return true;
			}
		}
		return false;
	}
}
