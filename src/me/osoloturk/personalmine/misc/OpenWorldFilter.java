package me.osoloturk.personalmine.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class OpenWorldFilter {
	private final APM instance;
	private Map<Pair<Material, Byte>, Integer> filterBlocks;
	private Map<String, ChunkFilter> chunkFilters;
	private List<String> scannedChunks;
	
	public OpenWorldFilter(APM instance) {
		this.instance = instance;
		loadFilters();
	}
	
	public void loadFilters() {
		filterBlocks = new HashMap<>();
		chunkFilters = new HashMap<>();
		instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				for(String filter : Settings.OPEN_WORLD_FILTER_BLOCKS.getStringList()) {
					String[] filterDetails = filter.split(";");
					filterBlocks.put(Pair.of(Material.valueOf(filterDetails[0]), Byte.parseByte(filterDetails[1])), Utils.getInt(filterDetails[2], 0));
				}
				for(String world : Settings.OPEN_WORLD_CHUNKS.getConfigurationSection(false)) {
					chunkFilters.put(world, new ChunkFilter(world));
				}
				if(scannedChunks == null) scannedChunks = instance.getDatabase().getOpenWorldScannedChunks();
			}
		});
	}
	
	public Map<String, ChunkFilter> getChunkFilters() {
		return chunkFilters;
	}
	
	public boolean haveFilter(String world) {
		return chunkFilters.containsKey(world);
	}
	
	public boolean haveFilter(Chunk chunk) {
		return scannedChunks != null && !scannedChunks.contains(chunk.getWorld().getName() + "*" + chunk.getX() + "*" + chunk.getZ()) && chunkFilters.get(chunk.getWorld().getName()).isContains(chunk);
	}
	
	public int[] getYBorders(Chunk chunk) {
		return chunkFilters.get(chunk.getWorld().getName()).getYCoords();
	}
	
	public Map<Pair<Material, Byte>, Integer> getFilterBlocks() {
		return filterBlocks;
	}
	
	public List<String> getScannedChunks() {
		return scannedChunks;
	}
	
	public void scanChunk(Chunk chunk) {
		if(scannedChunks != null) scannedChunks = new ArrayList<>();
		scannedChunks.add(chunk.getWorld().getName() + "*" + chunk.getX() + "*" + chunk.getZ());
	}

	public class ChunkFilter {
		private String world;
		
		// min,max #as chunkcoord
		private int[] xChunkCoords;
		
		// min,max #as chunkcoord
		private int[] zChunkCoords;
		
		// min,max #as blockcoord
		private int[] xBlockCoords;
		
		// min,max #as blockcoord
		private int[] yBlockCoords;
		
		// min,max #as blockcoord
		private int[] zBlockCoords;
		
		public ChunkFilter(String world) {
			this.world = world;
			xBlockCoords = new int[] { instance.getConfig().getInt(Settings.OPEN_WORLD_CHUNKS.getPath() + "." + world + ".x_coord.min"),
					instance.getConfig().getInt(Settings.OPEN_WORLD_CHUNKS.getPath() + "." + world + ".x_coord.max") };
			yBlockCoords = new int[] { instance.getConfig().getInt(Settings.OPEN_WORLD_CHUNKS.getPath() + "." + world + ".y_coord.min"),
					instance.getConfig().getInt(Settings.OPEN_WORLD_CHUNKS.getPath() + "." + world + ".y_coord.max") };
			zBlockCoords = new int[] { instance.getConfig().getInt(Settings.OPEN_WORLD_CHUNKS.getPath() + "." + world + ".z_coord.min"),
					instance.getConfig().getInt(Settings.OPEN_WORLD_CHUNKS.getPath() + "." + world + ".z_coord.max") };
			xChunkCoords = new int[] { xBlockCoords[0] >> 4, xBlockCoords[1] >> 4 };
			zChunkCoords = new int[] { zBlockCoords[0] >> 4, zBlockCoords[1] >> 4 };
		}
		
		public boolean isContains(Location loc) {
			return (loc.getWorld().getName().equals(world)) && ((loc.getX() >= xBlockCoords[0] && loc.getX() <= xBlockCoords[1]) && (loc.getY() >= yBlockCoords[0] && loc.getY() <= yBlockCoords[1])
					&& (loc.getZ() >= zBlockCoords[0] && loc.getZ() <= zBlockCoords[1]));
		}
		
		public boolean isContains(Chunk chunk) {
			return ((chunk.getX() >= xChunkCoords[0] && chunk.getX() <= xChunkCoords[1]) && (chunk.getZ() >= zChunkCoords[0] && chunk.getZ() <= zChunkCoords[1]));
		}
		
		public int[] getYCoords() {
			return yBlockCoords;
		}
		
		public String getBorders() {
			return "(" + xBlockCoords[0] + "," + yBlockCoords[0] + "," + zBlockCoords[0] + ") - (" + xBlockCoords[0] + "," + yBlockCoords[1] + "," + zBlockCoords[1] + ")";
		}
	}
}
