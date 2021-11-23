package me.osoloturk.personalmine.hooks.skyblocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;

public class PlotSquaredHook extends ISkyBlock {
	private PlotSquared plotSquared;
	
	public PlotSquaredHook() {
		this.plotSquared = PlotSquared.get();
	}
	
	@Override
	public boolean isMemberOfThisIsland(Player player) {
		com.plotsquared.core.location.Location plotLocation = getCustomLocation(player.getLocation());
		PlotArea plotArea = plotSquared.getPlotAreaAbs(plotLocation);
		if(plotArea == null)
			return false;
		Plot plot = plotArea.getPlot(plotLocation);
		return plot.getMembers().contains(player.getUniqueId());
		
	}
	
	@Override
	public boolean isSkyBlockWorld(Location loc) {
		return !plotSquared.getPlotAreas(loc.getWorld().getName()).isEmpty();
	}
	
	@Override
	public String getSkyBlockWorld() {
		return plotSquared.getPlotAreas().iterator().next().getWorldName();
	}
	
	private com.plotsquared.core.location.Location getCustomLocation(Location loc) {
		return new com.plotsquared.core.location.Location(loc.getWorld().getName(), (int) loc.getX(), (int) loc.getY(), (int) loc.getZ(), loc.getYaw(), loc.getPitch());
	}
}
