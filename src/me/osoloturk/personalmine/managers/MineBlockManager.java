
package me.osoloturk.personalmine.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.AppearanceType;
import me.osoloturk.personalmine.misc.GeneratorMineBlock;
import me.osoloturk.personalmine.misc.MineBlock;
import me.osoloturk.personalmine.misc.MultiMineBlock;
import me.osoloturk.personalmine.misc.SingleMineBlock;
import me.osoloturk.personalmine.utils.Pair;

public class MineBlockManager {
	private final APM instance;
	private Map<String, List<MineBlock>> mineBlocks;
	
	public MineBlockManager(APM instance) {
		this.instance = instance;
		mineBlocks = new HashMap<>();
		loadMineBlocks();
	}
	
	private void loadMineBlocks() {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				int chunkCounter = 0, mineBlockCounter = 0;
				for(MineBlock mineBlock : instance.getDatabase().getMineBlocks()) {
					List<MineBlock> mineBlocksOfChunk = mineBlocks.getOrDefault(mineBlock.getChunk(), new ArrayList<>());
					if(mineBlocksOfChunk.isEmpty())
						chunkCounter++;
					mineBlocksOfChunk.add(mineBlock);
					mineBlockCounter++;
					mineBlocks.put(mineBlock.getChunk(), mineBlocksOfChunk);
				}
				instance.getLogger().info("Loaded " + mineBlockCounter + " mine blocks from " + chunkCounter + " chunks");
			}
		});
	}
	
	private void addMineBlock(MineBlock mineBlock, String chunkAsText) {
		List<MineBlock> mineBlocksInChunk = mineBlocks.getOrDefault(chunkAsText, new ArrayList<>());
		instance.getDatabase().insertMineBlock(mineBlock);
		mineBlocksInChunk.add(mineBlock);
		mineBlocks.put(chunkAsText, mineBlocksInChunk);
		instance.getServer().getOnlinePlayers().forEach(player -> instance.getChanceManager().createApperances(player, instance.getPlayerManager().getMinePlayer(player.getUniqueId()), mineBlock));
	}
	
	public void addMineBlock(MineBlock mineBlock) {
		Location loc = mineBlock.getLocations().get(0);
		addMineBlock(mineBlock, loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4));
	}
	
	public void addMineBlock(Pair<Location, Location> poses, AppearanceType type) {
		addMineBlock(new MultiMineBlock(poses.getKey(), poses.getValue(), System.currentTimeMillis(), type),
				poses.getKey().getWorld().getName() + "*" + (poses.getKey().getBlockX() >> 4) + "*" + (poses.getKey().getBlockZ() >> 4));
	}
	
	public void addMineBlock(Location loc, AppearanceType type) {
		addMineBlock(new SingleMineBlock(loc, System.currentTimeMillis(), type), loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4));
	}
	
	public MineBlock getMineBlock(Location loc) {
		String world = loc.getWorld().getName();
		for(Entry<String, List<MineBlock>> entry : mineBlocks.entrySet()) {
			if(!entry.getKey().startsWith(world))
				continue;
			for(MineBlock mineBlock : entry.getValue()) {
				if(mineBlock.isContains(loc))
					return mineBlock;
			}
		}
		return null;
	}
	
	public boolean isMineBlock(Location loc) {
		for(MineBlock mineBlock : mineBlocks.getOrDefault(loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4), new ArrayList<>())) {
			if(mineBlock.isContains(loc))
				return true;
		}
		return false;
	}
	
	public void removeMineBlock(Location loc) {
		String chunkAsText = loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4);
		List<MineBlock> mineBlocksInChunk = mineBlocks.getOrDefault(chunkAsText, new ArrayList<>());
		MineBlock mineBlock = getMineBlock(loc);
		if(mineBlock != null) {
			instance.getDatabase().deleteMineBlock(mineBlock);
			mineBlocksInChunk.remove(mineBlock);
			mineBlocks.put(chunkAsText, mineBlocksInChunk);
		}
	}
	
	public void removeMineBlock(Location loc, MineBlock mineBlock) {
		String chunkAsText = loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4);
		List<MineBlock> mineBlocksInChunk = mineBlocks.getOrDefault(chunkAsText, new ArrayList<>());
		mineBlocksInChunk.remove(mineBlock);
		instance.getDatabase().deleteMineBlock(mineBlock);
		mineBlocks.put(chunkAsText, mineBlocksInChunk);
	}
	
	public void saveMineBlocks(List<Location> mineBlockLocs) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				for(Location loc : mineBlockLocs) {
					String chunk = loc.getWorld().getName() + "*" + (loc.getBlockX() >> 4) + "*" + (loc.getBlockZ() >> 4);
					List<MineBlock> mineBlocksOfChunk = mineBlocks.getOrDefault(chunk, new ArrayList<>());
					MineBlock mineBlock = new SingleMineBlock(loc, System.currentTimeMillis(), AppearanceType.OPEN_WORLD);
					if(mineBlocksOfChunk.isEmpty())
						mineBlocksOfChunk.add(mineBlock);
					instance.getDatabase().insertMineBlock(mineBlock);
					mineBlocks.put(chunk, mineBlocksOfChunk);
				}
			}
		});
	}
	
	public List<MineBlock> getMineBlocks(Chunk chunk) {
		return mineBlocks.get(chunk.getWorld().getName() + "*" + chunk.getX() + "*" + chunk.getZ());
	}
	
	public boolean isInBreakArea(Location location) {
		String world = location.getWorld().getName();
		for(Entry<String, List<MineBlock>> entry : mineBlocks.entrySet()) {
			if(!entry.getKey().startsWith(world))
				continue;
			for(MineBlock mineBlock : entry.getValue()) {
				if(mineBlock instanceof MultiMineBlock) {
					if(((MultiMineBlock) mineBlock).isInBreakArea(location)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void removePermissionForGenerators(UUID uuid) {
		instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
			for(List<MineBlock> mineBlocks : getMineBlocks().values()) {
				for(MineBlock mineBlock : mineBlocks) {
					if(mineBlock instanceof GeneratorMineBlock) {
						((GeneratorMineBlock) mineBlock).removePermission(uuid);
					}
				}
			}
		});
	}
	
	public List<MineBlock> getMineBlocks(String chunk) {
		return mineBlocks.getOrDefault(chunk, new ArrayList<>());
	}
	
	public Map<String, List<MineBlock>> getMineBlocks() {
		return mineBlocks;
	}
}
