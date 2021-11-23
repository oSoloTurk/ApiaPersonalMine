package me.osoloturk.personalmine.listeners;

import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;

import me.osoloturk.personalmine.APM;
import me.osoloturk.personalmine.misc.AppearanceType;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;
import me.osoloturk.personalmine.wrappers.WrapperPlayClientBlockDig;
import me.osoloturk.personalmine.wrappers.WrapperPlayServerBlockChange;
import me.osoloturk.personalmine.wrappers.WrapperPlayServerMapChunk;
import me.osoloturk.personalmine.wrappers.WrapperPlayServerUnloadChunk;

public class PacketListener {
	private final ProtocolManager protocolManager;
	
	public PacketListener(APM instance) {
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.removePacketListeners(instance);
		protocolManager.addPacketListener(new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Server.UNLOAD_CHUNK) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if(event.getPacketType() == PacketType.Play.Server.UNLOAD_CHUNK) {
					if(!Settings.ACTIVE_WORLDS.getStringList().contains(event.getPlayer().getWorld().getName()))
						return;
					WrapperPlayServerUnloadChunk wrapper = new WrapperPlayServerUnloadChunk(event.getPacket());
					instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
						MinePlayer mPlayer = instance.getPlayerManager().getMinePlayer(event.getPlayer().getUniqueId());
						String chunk = event.getPlayer().getWorld().getName() + "*" + wrapper.getChunkX() + "*" + wrapper.getChunkZ();
						if(mPlayer.isLoaded(chunk))
							mPlayer.unloadChunk(chunk);
					});
				}
			}
		});
		if(!Utils.isLegacy(113)) {
			protocolManager.addPacketListener(new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
				@SuppressWarnings("deprecation")
				@Override
				public void onPacketReceiving(PacketEvent event) {
					if(event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
						if(event.getPacket() == null)
							return;
						WrapperPlayClientBlockDig wrapper = new WrapperPlayClientBlockDig(event.getPacket());
						if(wrapper.getStatus() != PlayerDigType.ABORT_DESTROY_BLOCK || !Settings.ACTIVE_WORLDS.getStringList().contains(event.getPlayer().getWorld().getName()))
							return;
						instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
							BlockPosition pos = wrapper.getLocation();
							MinePlayer mPlayer = instance.getPlayerManager().getMinePlayer(event.getPlayer().getUniqueId());
							if(mPlayer != null) {
								Location loc = pos.toLocation(event.getPlayer().getWorld());
								Pair<AppearanceType, Pair<Material, Byte>> apperance = mPlayer.getAppearance(loc);
								if(apperance != null && loc.getBlock().getType() != apperance.getValue().getKey()) {
									if(Utils.isLegacy())
										event.getPlayer().sendBlockChange(loc, apperance.getValue().getKey(), apperance.getValue().getValue());
									else
										event.getPlayer().sendBlockChange(loc, apperance.getValue().getKey().createBlockData());
								}
							}
						});
					}
				}
			});
		}
		protocolManager.addPacketListener(new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
			@SuppressWarnings("deprecation")
			@Override
			public void onPacketSending(PacketEvent event) {
				if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
					if(!Settings.ACTIVE_WORLDS.getStringList().contains(event.getPlayer().getWorld().getName()))
						return;
					WrapperPlayServerMapChunk wrapper = new WrapperPlayServerMapChunk(event.getPacket());
					new BukkitRunnable() {
						@Override
						public void run() {
							MinePlayer mPlayer = instance.getPlayerManager().getMinePlayer(event.getPlayer().getUniqueId());
							if(mPlayer != null) {
								String chunk = event.getPlayer().getWorld().getName() + "*" + wrapper.getChunkX() + "*" + wrapper.getChunkZ();
								if(mPlayer.isUnloaded(chunk)) {
									for(Entry<Location, Pair<AppearanceType, Pair<Material, Byte>>> entry : mPlayer.getApperances().entrySet()) {
										if(entry.getKey().getChunk().getX() == wrapper.getChunkX() && entry.getKey().getChunk().getZ() == wrapper.getChunkZ()) {
											if(Utils.isLegacy())
												event.getPlayer().sendBlockChange(entry.getKey(), entry.getValue().getValue().getKey(), entry.getValue().getValue().getValue());
											else
												event.getPlayer().sendBlockChange(entry.getKey(), entry.getValue().getValue().getKey().createBlockData());
										}
									}
									mPlayer.reloadChunk(chunk);
								} else
									instance.getChanceManager().createApperances(mPlayer, event.getPlayer(), chunk);
								cancel();
							}
						}
					}.runTaskTimerAsynchronously(instance, 20, 20);
				}
			}
		});
		
		protocolManager.addPacketListener(new PacketAdapter(instance, ListenerPriority.NORMAL, PacketType.Play.Server.BLOCK_CHANGE) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
					if(event.getPacket() == null)
						return;
					if(!Settings.ACTIVE_WORLDS.getStringList().contains(event.getPlayer().getWorld().getName()))
						return;
					WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event.getPacket());
					BlockPosition pos = wrapper.getLocation();
					MinePlayer mPlayer = instance.getPlayerManager().getMinePlayer(event.getPlayer().getUniqueId());
					if(mPlayer != null) {
						Location loc = pos.toLocation(event.getPlayer().getWorld());
						Pair<AppearanceType, Pair<Material, Byte>> apperance = mPlayer.getAppearance(loc);
						if(apperance != null && wrapper.getBlockData().getType() != apperance.getValue().getKey()) {
							if(!event.isReadOnly()) {
								event.setCancelled(true);
							}
						}
					}
				}
			}
		});
	}
}
