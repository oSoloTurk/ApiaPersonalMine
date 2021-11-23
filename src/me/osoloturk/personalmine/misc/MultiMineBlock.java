package me.osoloturk.personalmine.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;

import me.osoloturk.personalmine.utils.Utils;

public class MultiMineBlock extends MineBlock {
	private final double[] borders;
	private final double[] largeBorders;
	private final String world;
	private final String locationsAsString;
	private final Location posOne, posTwo;
	
	public MultiMineBlock(Location posOne, Location posTwo, long createDate, AppearanceType type) {
		super(posOne.getWorld().getName() + "*" + (posOne.getBlockX() >> 4) + "*" + (posOne.getBlockZ() >> 4), createDate, type);
		this.posOne = posOne;
		this.posTwo = posTwo;
		borders = new double[6];
		borders[0] = posOne.getX() > posTwo.getX() ? posTwo.getX() : posOne.getX();
		borders[1] = posOne.getY() > posTwo.getY() ? posTwo.getY() : posOne.getY();
		borders[2] = posOne.getZ() > posTwo.getZ() ? posTwo.getZ() : posOne.getZ();
		borders[3] = posOne.getX() < posTwo.getX() ? posTwo.getX() : posOne.getX();
		borders[4] = posOne.getY() < posTwo.getY() ? posTwo.getY() : posOne.getY();
		borders[5] = posOne.getZ() < posTwo.getZ() ? posTwo.getZ() : posOne.getZ();
		largeBorders = new double[6];
		for(int i = 0; i < 6; i++)
			largeBorders[i] = borders[i] + (i < 3 ? (-1 * Settings.REGION_BREAK_AREA.getInt()) : Settings.REGION_BREAK_AREA.getInt());
		locationsAsString = Utils.getStringFromLocation(posOne) + "*" + Utils.getStringFromLocation(posTwo);
		world = posOne.getWorld().getName();
	}
	

	@Override
	public boolean isContains(Location loc) {
		return (loc.getWorld().getName().equals(world))
				&& ((loc.getX() >= borders[0] && loc.getX() <= borders[3]) && (loc.getY() >= borders[1] && loc.getY() <= borders[4]) && (loc.getZ() >= borders[2] && loc.getZ() <= borders[5]));
	}
	
	public boolean isInBreakArea(Location loc) {
		return (loc.getWorld().getName().equals(world)) && ((loc.getX() >= largeBorders[0] && loc.getX() <= largeBorders[3]) && (loc.getY() >= largeBorders[1] && loc.getY() <= largeBorders[4])
				&& (loc.getZ() >= largeBorders[2] && loc.getZ() <= largeBorders[5]));
	}
	
	@Override
	public String getLocationAsString() {
		return locationsAsString;
	}
	
	@Override
	public List<Location> getLocations() {
		return Arrays.asList(posOne, posTwo);
	}
	
	@Override
	public List<Location> getLocationsForApperances() {
		List<Location> blocks = new ArrayList<>();
		for(double x = borders[0]; x <= borders[3]; x++)
			for(double y = borders[1]; y <= borders[4]; y++)
				for(double z = borders[2]; z <= borders[5]; z++)
					blocks.add(new Location(posOne.getWorld(), x, y, z));
		return blocks;
	}
}
