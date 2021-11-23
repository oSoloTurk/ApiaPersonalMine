package me.osoloturk.personalmine.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

import me.osoloturk.personalmine.utils.Utils;

public class GeneratorMineBlock extends MineBlock {
	
	private final Location loc;
	private final Location productLocation;
	private List<UUID> allowedUUID;
	
	public GeneratorMineBlock(Location loc, long createDate) {
		super(loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4), createDate, AppearanceType.GENERATOR);
		this.productLocation = loc.clone().add(0, 1, 0);
		this.loc = loc;
		allowedUUID = new ArrayList<>();
	}
	
	@Override
	public boolean isContains(Location loc) {
		return (this.loc.equals(loc) || productLocation.equals(loc));
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
		return Arrays.asList(productLocation);
	}
	
	public void addPermission(UUID uuid) {
		allowedUUID.add(uuid);
	}

	public void removePermission(UUID uuid) {
		allowedUUID.remove(uuid);
	}
	
	public boolean hasPermission(UUID uuid) {
		return allowedUUID.contains(uuid);
	}
	
	public boolean isGeneratorBlock(Location loc) {
		return this.loc.equals(loc);
	}
	
	public Location getProductLocation() {
		return productLocation;
	}
}