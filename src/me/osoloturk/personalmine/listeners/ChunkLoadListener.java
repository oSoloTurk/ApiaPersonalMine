package me.osoloturk.personalmine.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class ChunkLoadListener implements Listener {
	private final APM instance;
	
	public ChunkLoadListener(APM instance) {
		this.instance = instance;
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if(!instance.getOpenWorldFilter().haveFilter(event.getWorld().getName()))
			return;
		if(instance.getOpenWorldFilter().haveFilter(event.getChunk())) {
			instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
				@Override
				public void run() {
					instance.getOpenWorldFilter().scanChunk(event.getChunk());
					Map<Pair<Material, Byte>, Integer> filterBlocks = instance.getOpenWorldFilter().getFilterBlocks();
					int chanceMax = filterBlocks.values().stream().collect(Collectors.summingInt(Integer::intValue));
					ChunkSnapshot chunk = event.getChunk().getChunkSnapshot();
					List<Location> selectedMineBlocks = new ArrayList<>();
					// min,max
					int[] x_coords = new int[] { (event.getChunk().getX() * 16), (event.getChunk().getX() + 1) * 16 };
					int[] z_coords = new int[] { (event.getChunk().getZ() * 16), (event.getChunk().getZ() + 1) * 16 };
					int[] y_coords = instance.getOpenWorldFilter().getYBorders(event.getChunk());
					Location loc = null;
					for(int x = x_coords[0]; x < x_coords[1]; x++) {
						for(int z = z_coords[0]; z < z_coords[1]; z++) {
							for(int y = y_coords[0]; y < y_coords[1]; y++) {
								loc = new Location(event.getWorld(), x_coords[0] + x, y, z_coords[0] + z);
								if(instance.getMineBlockManager().isMineBlock(loc))
									continue;
								if(!Utils.isLegacy()) {
									BlockData blockData = chunk.getBlockData((x - x_coords[0]) % 16, y, (z - z_coords[0]) % 16);
									for(Pair<Material, Byte> filterBlock : filterBlocks.keySet()) {
										if(blockData.getMaterial() == filterBlock.getKey() && Utils.getRandomNumber(chanceMax) <= filterBlocks.get(filterBlock))
											selectedMineBlocks.add(loc);
									}
								} else {
									Material material = Utils.getMaterial(chunk, (x - x_coords[0]) % 16, y, (z - z_coords[0]) % 16);
									for(Pair<Material, Byte> filterBlock : filterBlocks.keySet()) {
										if(material == filterBlock.getKey() && Utils.getDataValue(chunk, x, y, z) == filterBlock.getValue()
												&& Utils.getRandomNumber(chanceMax) <= filterBlocks.get(filterBlock))
											selectedMineBlocks.add(loc);
									}
									
								}
							}
						}
					}
					instance.getMineBlockManager().saveMineBlocks(selectedMineBlocks);
				}
			});
			
		}
		
	}
}
