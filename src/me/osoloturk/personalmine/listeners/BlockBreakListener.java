package me.osoloturk.personalmine.listeners;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.AppearanceType;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.services.SoundService.Sounds;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public class BlockBreakListener implements Listener {
	private final APM instance;
	
	public BlockBreakListener(APM instance) {
		this.instance = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!Settings.ACTIVE_WORLDS.getStringList().contains(event.getPlayer().getWorld().getName())) {
			instance.getDebugService().debug("BlockBreak Trigger Stoped | cause: World Inactive");
			return;
		}
		MinePlayer mPlayer = instance.getPlayerManager().getMinePlayer(event.getPlayer().getUniqueId());
		Location loc = event.getBlock().getLocation();
		Pair<AppearanceType, Pair<Material, Byte>> apperance = mPlayer.getAppearance(loc);
		if(apperance != null) {
			instance.getDebugService().debug(Arrays.asList("Checked Appearance is " + apperance.getValue().getKey().toString() + ":" + apperance.getValue()
					.getValue(), "Type of Appearance: " + apperance.getKey().toString()));
			event.setCancelled(true);
			event.setDropItems(false);
			event.setExpToDrop(0);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(apperance.getValue().getKey()));
			instance.getServer().getScheduler().runTaskAsynchronously(instance, new Runnable() {
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					final Player player = event.getPlayer();
					switch(apperance.getKey()) {
					case GENERATOR:
						instance.getSoundService().playSound(player, Sounds.BREAK_GENERATOR_MINEBLOCK);
						break;
					case SINGLE:
						instance.getSoundService().playSound(player, Sounds.BREAK_SINGLE_MINEBLOCK);
						break;
					case MULTI:
						instance.getSoundService().playSound(player, Sounds.BREAK_MULTI_MINEBLOCK);
						break;
					case OPEN_WORLD:
						instance.getSoundService().playSound(player, Sounds.BREAK_OPEN_WORLD_MINEBLOCK);
						break;
					}
					if(apperance.getKey() == AppearanceType.GENERATOR) {
						if(Utils.isLegacy())
							player.sendBlockChange(loc, Material.AIR, (byte) 0);
						else
							player.sendBlockChange(loc, Material.AIR.createBlockData());
						instance.getServer().getScheduler().runTaskLater(instance, () -> instance.getChanceManager().createApperances(player, mPlayer, event
								.getBlock().getLocation().add(0, -1, 0)), Settings.GENERATOR_DELAY.getInt());
					} else {
						mPlayer.removeBlock(event.getBlock().getLocation());
						if(Utils.isLegacy())
							player.sendBlockChange(event.getBlock().getLocation(), event.getBlock().getType(), event.getBlock().getData());
						else
							player.sendBlockChange(event.getBlock().getLocation(), event.getBlock().getBlockData());
					}
				}
			});
		} else {
			instance.getDebugService().debug("Checked Appearance is null");
		}
	}
	
}
