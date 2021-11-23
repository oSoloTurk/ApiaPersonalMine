package me.osoloturk.personalmine.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.utils.Pair;

public class MinePlayer {
	
	private final UUID uuid;
	private Map<Location, Pair<AppearanceType, Pair<Material, Byte>>> blocks;
	private long renevalDate, lastExitDate;
	private Map<String, Boolean> chancePacks;
	// world*x*z,world*x*z
	private List<String> loadedChunks;
	// world*x*z,world*x*z for unloaded apperances
	private List<String> unloadedChunks;
	private int maxChance;
	
	public MinePlayer(UUID uuid) {
		this.uuid = uuid;
		this.blocks = new HashMap<>();
		this.renevalDate = 0;
		this.lastExitDate = 0;
		loadedChunks = new ArrayList<>();
		unloadedChunks = new ArrayList<>();
		chancePacks = new HashMap<>();
		for(Entry<String, ChancePack> entry : APM.getInstance().getChanceManager().getChances().entrySet()) {
			if(entry.getValue().isDefaultMode())
				chancePacks.put(entry.getKey(), false);
		}
		calculateMaxChance();
	}
	
	public MinePlayer(UUID uuid, Map<Location, Pair<AppearanceType, Pair<Material, Byte>>> apperances, long renevalDate, Map<String, Boolean> chancePacks, List<String> loadedChunks, long lastExitDate) {
		this.uuid = uuid;
		this.blocks = apperances;
		this.renevalDate = renevalDate;
		this.chancePacks = chancePacks;
		this.unloadedChunks = loadedChunks;
		this.lastExitDate = lastExitDate;
		this.loadedChunks = new ArrayList<>();
		for(Entry<String, ChancePack> entry : APM.getInstance().getChanceManager().getChances().entrySet()) {
			if(entry.getValue().isDefaultMode() && !chancePacks.containsKey(entry.getKey()))
				chancePacks.put(entry.getKey(), false);
		}
		calculateMaxChance();
	}
	
	public void removeBlock(Location loc) {
		blocks.remove(loc);
	}
	
	public long getLastExitDate() {
		return lastExitDate;
	}
	
	public Pair<AppearanceType, Pair<Material, Byte>> getAppearance(Location loc) {
		return blocks.get(loc);
	}
	
	public Map<Location, Pair<AppearanceType, Pair<Material, Byte>>> getApperances() {
		return blocks;
	}
	
	public void addApperance(Location loc, Pair<AppearanceType, Pair<Material, Byte>> apperance) {
		blocks.put(loc, apperance);
	}
	
	public void resetApperances() {
		blocks = new HashMap<>();
		unloadedChunks = new ArrayList<>();
		loadedChunks = new ArrayList<>();
		renevalDate = System.currentTimeMillis();
		APM.getInstance().getChanceManager().createApperances(this, Bukkit.getPlayer(uuid));
	}
	
	public long getRenevalDate() {
		return renevalDate;
	}
	
	public void setRenevalDate(long date) {
		renevalDate = date;
	}
	
	public List<String> getActiveChancePacks() {
		List<String> activeChancePacks = new ArrayList<>();
		for(Entry<String, Boolean> entry : chancePacks.entrySet())
			if(entry.getValue())
				activeChancePacks.add(entry.getKey());
		if(activeChancePacks.isEmpty()) {
			for(Entry<String, ChancePack> pack : APM.getInstance().getChanceManager().getChances().entrySet()) {
				if(pack.getValue().isDefaultMode())
					chancePacks.put(pack.getKey(), false);
			}
			activeChancePacks.addAll(chancePacks.keySet());
		}
		return activeChancePacks;
	}
	
	public Map<String, Boolean> getChancePacks() {
		return chancePacks;
	}
	
	public void setChancePack(String packName, boolean newMode) {
		chancePacks.put(packName, newMode);
	}
	
	public void addChancePack(String packName) {
		chancePacks.put(packName, true);
		calculateMaxChance();
	}
	
	public boolean isHave(String packName) {
		return chancePacks.containsKey(packName);
	}
	
	public void removeChancePack(String packName) {
		chancePacks.remove(packName);
		calculateMaxChance();
	}

	public boolean loadChunk(Chunk chunk) {
		String chunkText = chunk.getWorld().getName() + "*" + chunk.getX() + "*" + chunk.getZ();
		if(loadedChunks.contains(chunkText)) return false;
		loadedChunks.add(chunkText);
		return true;
	}
	
	public boolean loadChunk(String chunk) {
		if(loadedChunks.contains(chunk)) return false;
		loadedChunks.add(chunk);
		return true;
	}
	
	public List<String> getLoadedChunks() {
		return loadedChunks;
	}
	
	public boolean isLoaded(String chunk) {
		return loadedChunks.contains(chunk);
	}
	
	public boolean isUnloaded(String chunk) {
		return unloadedChunks.contains(chunk);
	}
	
	public void reloadChunk(String chunk) {
		unloadedChunks.remove(chunk);
		loadedChunks.add(chunk);
	}
	
	public void unloadChunk(String chunk) {
		unloadedChunks.add(chunk);
	}
	
	public int getTravledChunkSize() {
		return loadedChunks.size() + unloadedChunks.size();
	}
	
	public boolean hasChunkReleation(String chunk) {
		return loadedChunks.contains(chunk) || unloadedChunks.contains(chunk);
	}
	
	public List<String> getAllChunks(){
		List<String> allChunks = new ArrayList<>(loadedChunks);
		allChunks.addAll(unloadedChunks);
		return allChunks;
	}
	
	public boolean hasChancePack(String packName) {
		return chancePacks.containsKey(packName);
	}
	
	public int getMaxChance() {
		return maxChance;
	}
	
	public void calculateMaxChance() {
		maxChance = 0;
		for(String packName : new ArrayList<>(getActiveChancePacks())) {
			ChancePack pack = APM.getInstance().getChanceManager().getChancePack(packName);
			if(pack == null) {
				removeChancePack(packName);
				continue;
			}
			for(Entry<Pair<Material, Byte>, Integer> entry : pack.getChances().entrySet()) {
				maxChance += entry.getValue();
			}
		}
	}
}
