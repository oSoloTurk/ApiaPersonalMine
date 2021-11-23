
package me.osoloturk.personalmine.misc;

import java.util.List;

import org.bukkit.Location;

public abstract class MineBlock {
	String chunk;
	final long createDate;
	final AppearanceType type;
	
	public MineBlock(String chunk, long createDate, AppearanceType type) {
		this.chunk = chunk;
		this.createDate = createDate;
		this.type = type;
	}	
	
	public String getChunk() {
		return chunk;
	}
	
	public Long getCreateDate() {
		return createDate;
	}
	
	public AppearanceType getType() {
		return type;
	}

	public int getTypeCode() {
		return type.getId();
	}
	
	public abstract boolean isContains(Location loc);
	
	public abstract String getLocationAsString();
	
	
	public abstract List<Location> getLocations();
	
	public abstract List<Location> getLocationsForApperances();
}
