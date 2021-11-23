package me.osoloturk.personalmine.hooks.skyblocks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.ipazu.advancedrealm.events.RealmUnclaimEvent;
import fr.ipazu.advancedrealm.realm.Realm;
import fr.ipazu.advancedrealm.realm.RealmUtils;
import me.osoloturk.personalmine.APM;

public class AdvancedRealmHook extends ISkyBlock implements Listener {
	private final APM instance;
	private RealmUtils realmUtils;
	
	public AdvancedRealmHook(APM instance) {
		this.realmUtils = new RealmUtils();
		this.instance = instance;
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}
	
	@Override
	public boolean isMemberOfThisIsland(Player player) {
		return realmUtils.getRealmFromLocation(player.getLocation()).getRealmMembers().stream().filter(member -> member.getUniqueId().equals(player.getUniqueId().toString())).findAny().isPresent();
	}
	
	@Override
	public boolean isSkyBlockWorld(Location loc) {
		return Realm.allrealm.get(0).getCenter().getWorld().equals(loc.getWorld());
	}
	
	@Override
	public String getSkyBlockWorld() {
		return Realm.allrealm.get(0).getCenter().getWorld().getName();
	}
	
	@EventHandler
	public void onRealmUnclaim(RealmUnclaimEvent event) {
		instance.getMineBlockManager().removePermissionForGenerators(UUID.fromString(event.getRealmPlayer().getUniqueId()));
	}
}
