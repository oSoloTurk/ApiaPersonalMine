package me.osoloturk.personalmine.listeners;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.AppearanceType;
import me.osoloturk.personalmine.misc.MineBlock;
import me.osoloturk.personalmine.misc.MultiMineBlock;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Utils;

public class SelectorListener implements Listener {
	private final APM instance;
	
	public SelectorListener(APM instance) {
		this.instance = instance;
	}
	
	@EventHandler
	public void playerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null || !Settings.ACTIVE_WORLDS.getStringList().contains(event.getPlayer().getWorld().getName()) || !event.hasItem()
				|| !(event.getPlayer().hasPermission("apm.selector") || event.getPlayer().hasPermission("apm.*")))
			return;
		Player player = event.getPlayer();
		if(event.getItem().isSimilar(instance.getSelectorManager().getSingleSelector())) {
			event.setCancelled(true);
			instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
				if(player.isSneaking()) {
					MineBlock mineBlock = instance.getMineBlockManager().getMineBlock(event.getClickedBlock().getLocation());
					if(mineBlock != null) {
						if(mineBlock instanceof MultiMineBlock) {
							deleteCheck(player, mineBlock, event.getClickedBlock().getLocation());
						} else {
							instance.getMineBlockManager().removeMineBlock(event.getClickedBlock().getLocation(), mineBlock);
							Settings.MESSAGE_SELECTOR_REMOVE_SINGLE.send(player, null);
						}
						return;
					} else {
						Settings.ERROR_MINEBLOCK_NOTFOUND.send(player, null);
						return;
					}
				} else {
					if(instance.getMineBlockManager().isMineBlock(event.getClickedBlock().getLocation())) {
						Settings.ERROR_MINEBLOCK_ALREADY_HAVE.send(player, null);
						return;
					} else {
						instance.getMineBlockManager().addMineBlock(event.getClickedBlock().getLocation(), AppearanceType.SINGLE);
						Settings.MESSAGE_SELECTOR_CREATE_SINGLE.send(player, null);
						return;
					}
				}
			});
		}
		if(event.getItem().isSimilar(instance.getSelectorManager().getMultiSelector())) {
			if(player.isSneaking()) {
				MineBlock mineBlock = instance.getMineBlockManager().getMineBlock(event.getClickedBlock().getLocation());
				if(mineBlock != null) {
					deleteCheck(player, mineBlock, event.getClickedBlock().getLocation());
					return;
				} else {
					Settings.ERROR_MINEBLOCK_NOTFOUND.send(player, null);
					return;
				}
			} else {
				event.setCancelled(true);
				instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
					Location secondPos = instance.getSelectorManager().setMultiPos(event.getPlayer().getUniqueId(), event.getAction(), event.getClickedBlock().getLocation());
					Settings.MESSAGE_SELECTOR_SELECTED_MULTI.send(event.getPlayer(), Arrays.asList("%first%", Utils.getPrettyStringFromLocation(event.getClickedBlock().getLocation()), "%second%",
							secondPos != null ? Utils.getPrettyStringFromLocation(secondPos) : "-"));
					return;
				});
			}
			
		}
		
	}
	
	private void deleteCheck(Player player, MineBlock mineBlock, Location loc) {
		instance.getGuiManager().getYesNoSelector().openSelector(player, null,
				Settings.MESSAGE_SELECTOR_REMOVE_MULTI_QUESTION.getStringList().stream().map(line -> line.replace("%posOne%", Utils.getPrettyStringFromLocation(mineBlock.getLocations().get(0)))
						.replace("%posTwo%", Utils.getPrettyStringFromLocation(mineBlock.getLocations().get(1)))).collect(Collectors.toList()),
				(customInventory, clickPlayer, clickType, shift, clickEvent) -> {
					Settings.MESSAGE_SELECTOR_REMOVE_MULTI_CANCEL.send(player, null);
					instance.getServer().getScheduler().runTask(instance, () -> clickPlayer.closeInventory());
				}, (customInventory, clickPlayer, clickType, shift, clickEvent) -> {
					instance.getMineBlockManager().removeMineBlock(loc, mineBlock);
					Settings.MESSAGE_SELECTOR_REMOVE_MULTI_SUCCES.send(player, null);
					instance.getServer().getScheduler().runTask(instance, () -> clickPlayer.closeInventory());
				});
	}
}
