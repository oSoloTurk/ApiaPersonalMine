package me.osoloturk.personalmine.managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.ChancePack;
import me.osoloturk.personalmine.misc.GeneratorMineBlock;
import me.osoloturk.personalmine.misc.MineBlock;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class ChanceManager {
	private final APM instance;
	private Map<String, ChancePack> chances;
	
	public ChanceManager(APM instance) {
		this.instance = instance;
		loadChances();
	}
	
	public void loadChances() {
		chances = new HashMap<>();
		instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
			@Override
			public void run() {
				for(String identifier : instance.getConfig().getConfigurationSection(Settings.CHANCES_BASE.getPath()).getKeys(false)) {
					ChancePack pack = new ChancePack(instance, Settings.CHANCES_BASE.getPath() + "." + identifier);
					chances.put(identifier, pack);
				}
			}
		});
	}
	
	public Map<String, ChancePack> getChances() {
		return chances;
	}
	
	public ChancePack getChancePack(String packName) {
		return chances.get(packName);
	}
	
	public void addChancePack(String packName, ChancePack pack) {
		chances.put(packName, pack);
	}
	
	public void removeChancePack(String packName) {
		chances.remove(packName);
	}
	
	@SuppressWarnings("deprecation")
	public void refillApperances(Player player, MinePlayer mPlayer) {
		final int sum = mPlayer.getMaxChance();
		if(sum == 0) {
			instance.getLogger().warning("Appearances reset request dismiss because " + player.getDisplayName() + " have not chancepack!");
			return;
		}
		for(Location loc : new HashMap<>(mPlayer.getApperances()).keySet()) {
			boolean broke = false;
			int selectedNumber = Utils.getRandomNumber(sum);
			for(String packName : mPlayer.getActiveChancePacks()) {
				int lastSum = 0;
				for(Entry<Pair<Material, Byte>, Integer> entry : instance.getChanceManager().getChancePack(packName).getChances().entrySet()) {
					if(selectedNumber > lastSum && selectedNumber <= lastSum + entry.getValue()) {
						mPlayer.addApperance(loc, Pair.of(mPlayer.getApperances().get(loc).getKey(), entry.getKey()));
						if(Utils.isLegacy())
							player.sendBlockChange(loc, entry.getKey().getKey(), entry.getKey().getValue());
						else
							player.sendBlockChange(loc, entry.getKey().getKey().createBlockData());
						broke = true;
						break;
					}
					lastSum += entry.getValue();
				}
				if(!broke)
					break;
			}
		}
	}
	
	public void createApperances(Player player, MinePlayer mPlayer, Location loc) {
		MineBlock mineBlock = instance.getMineBlockManager().getMineBlock(loc);
		if(mineBlock != null) {
			createApperances(player, mPlayer, mineBlock);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void createApperances(Player player, MinePlayer mPlayer, MineBlock mineBlock) {
		int counter = 0;
		final int sum = mPlayer.getMaxChance();
		if(sum == 0)
			return;
		if(mineBlock instanceof GeneratorMineBlock) {
			if(Settings.GENERATOR_SKYBLOCK_SUPPORT.getBoolean() && instance.getSkyBlokHook() != null && !((GeneratorMineBlock) mineBlock).hasPermission(player
					.getUniqueId())) {
				if(instance.getSkyBlokHook().isSkyBlockWorld(((GeneratorMineBlock) mineBlock).getProductLocation())) {
					if(!instance.getSkyBlokHook().isMemberOfThisIsland(player)) {
						return;
					}
					((GeneratorMineBlock) mineBlock).addPermission(player.getUniqueId());
				}
			}
		}
		for(Location loc : mineBlock.getLocationsForApperances()) {
			if(Settings.BLOCK_BLACKLIST_ENABLED.getBoolean() && !(mineBlock instanceof GeneratorMineBlock)) {
				ChunkSnapshot chunkSnapShot = loc.getChunk().getChunkSnapshot();
				String blackList = Settings.BLOCK_BLACKLIST_WORLD_ROOT.getPath() + loc.getWorld().getName();
				if(chunkSnapShot != null && instance.getConfig().isSet(blackList) && instance.getConfig().getStringList(blackList + ".list").contains(
						getMaterial(chunkSnapShot, Math.abs(loc.getBlockX() % 16), loc.getBlockY(), Math.abs(loc.getBlockZ() % 16))))
					continue;
				
			}
			int selectedNumber = Utils.getRandomNumber(sum);
			for(String packName : mPlayer.getActiveChancePacks()) {
				boolean broke = false;
				int lastSum = 0;
				for(Entry<Pair<Material, Byte>, Integer> entry : instance.getChanceManager().getChancePack(packName).getChances().entrySet()) {
					if(selectedNumber > lastSum && selectedNumber <= lastSum + entry.getValue()) {
						mPlayer.addApperance(loc, Pair.of(mineBlock.getType(), entry.getKey() == null ? Pair.of(Material.STONE, (byte) 0) : entry.getKey()));
						if(Utils.isLegacy())
							player.sendBlockChange(loc, entry.getKey().getKey(), entry.getKey().getValue());
						else
							player.sendBlockChange(loc, entry.getKey().getKey().createBlockData());
						counter++;
						broke = true;
						break;
					}
					lastSum += entry.getValue();
				}
				if(!broke)
					break;
			}
		}
		if(counter > 0 && mPlayer.isLoaded(mineBlock.getChunk()))
			mPlayer.loadChunk(mineBlock.getChunk());
	}
	
	public void createApperances(MinePlayer mPlayer, Player player) {
		createApperances(mPlayer, player, player.getWorld().getName() + "*" + (player.getLocation().getBlockX() >> 4) + "*" + (player.getLocation()
				.getBlockZ() >> 4));
	}
	
	@SuppressWarnings("deprecation")
	public void createApperances(MinePlayer mPlayer, Player player, String chunk) {
		if(!mPlayer.hasChunkReleation(chunk)) {
			int counter = 0;
			final int sum = mPlayer.getMaxChance();
			if(sum == 0)
				return;
			for(MineBlock mineBlock : instance.getMineBlockManager().getMineBlocks(chunk)) {
				if(mineBlock instanceof GeneratorMineBlock) {
					if(Settings.GENERATOR_SKYBLOCK_SUPPORT.getBoolean() && instance.getSkyBlokHook() != null) {
						if(instance.getSkyBlokHook().isSkyBlockWorld(((GeneratorMineBlock) mineBlock).getProductLocation())) {
							if(!instance.getSkyBlokHook().isMemberOfThisIsland(player))
								continue;
						}
					}
				}
				for(Location loc : mineBlock.getLocationsForApperances()) {
					if(Settings.BLOCK_BLACKLIST_ENABLED.getBoolean()) {
						ChunkSnapshot chunkSnapShot = loc.getChunk().getChunkSnapshot();
						String blackList = Settings.BLOCK_BLACKLIST_WORLD_ROOT.getPath() + loc.getWorld().getName();
						if(chunkSnapShot != null && instance.getConfig().isSet(blackList) && instance.getConfig().getStringList(blackList + ".list").contains(
								getMaterial(chunkSnapShot, Math.abs(loc.getBlockX() % 16), loc.getBlockY(), Math.abs(loc.getBlockZ() % 16))))
							continue;
					}
					boolean broke = false;
					int selectedNumber = Utils.getRandomNumber(sum);
					for(String packName : mPlayer.getActiveChancePacks()) {
						int lastSum = 0;
						for(Entry<Pair<Material, Byte>, Integer> entry : instance.getChanceManager().getChancePack(packName).getChances().entrySet()) {
							if(selectedNumber > lastSum && selectedNumber <= lastSum + entry.getValue()) {
								mPlayer.addApperance(loc, Pair.of(mineBlock.getType(), entry.getKey()));
								if(Utils.isLegacy())
									player.sendBlockChange(loc, entry.getKey().getKey(), entry.getKey().getValue());
								else
									player.sendBlockChange(loc, entry.getKey().getKey().createBlockData());
								counter++;
								broke = true;
								break;
							}
							lastSum += entry.getValue();
						}
						if(!broke)
							break;
					}
				}
			}
			if(counter > 0)
				mPlayer.loadChunk(chunk);
		}
	}
	
	public void buyPack(Player player, MinePlayer mPlayer, Entry<String, ChancePack> packSet) {
		if(player.getTotalExperience() < packSet.getValue().getCostExp()) {
			Settings.ERROR_REQUIRED_EXP.send(player, null);
			return;
		}
		if(!instance.getEconomy().withdrawPlayer(player, packSet.getValue().getCostMoney()).transactionSuccess()) {
			Settings.ERROR_REQUIRED_MONEY.send(player, null);
			return;
		}
		player.setTotalExperience(player.getTotalExperience() - packSet.getValue().getCostExp());
		mPlayer.addChancePack(packSet.getKey());
		Settings.MESSAGE_CHANCEPACK_BUY_SUCCES.send(player, Arrays.asList("%packname%", packSet.getKey()));
		if(Settings.AUTO_REFILL_APPEARANCES_AFTER_CHANCEPACK_BOUGHT.getBoolean()) {
			refillApperances(player, mPlayer);
		}
	}
	
	private String getMaterial(ChunkSnapshot chunk, int x, int y, int z) {
		return Utils.getMaterial(chunk, x, y, z).name() + ";" + Utils.getDataValue(chunk, x, y, z);
	}
}
