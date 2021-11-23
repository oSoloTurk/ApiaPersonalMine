package me.osoloturk.personalmine.misc;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;

import me.osoloturk.personalmine.utils.Utils;

public class SingleMineBlock extends MineBlock {
	
	private final Location loc;
	
	public SingleMineBlock(Location loc, long createDate, AppearanceType type) {
		super(loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4), createDate, type);
		this.loc = loc;
	}
	
	@Override
	public boolean isContains(Location loc) {
		return this.loc.equals(loc);
	}
	
	@Override
	public String getLocationAsString() {
		return Utils.getStringFromLocation(loc);
	}
	
	@Override
	public List<Location> getLocations() {
		return Arrays.asList(loc);
	}

	@Override
	public List<Location> getLocationsForApperances() {
		return Arrays.asList(loc);
	}
}
