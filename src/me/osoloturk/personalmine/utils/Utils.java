package me.osoloturk.personalmine.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Utils {
	private static final String NMS_VERSION;
	private static final int NMS_VERSION_CODE;
	private static SecureRandom random;
	private static Class<?> chatPacket, chatComponent, chatBaseComponent;
	private static Constructor<?> titleConstructor, chatComponentConstructor, chatPacketConstructor;
	private static Method chatTitleMethod, chatComponentMethod, getMaterial, getBlockTypeId, getBlockData;
	private static Object titleObject, subtitleObject;
	
	static {
		NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		NMS_VERSION_CODE = Integer.parseInt(NMS_VERSION.replace("v", "").replace("_", "").replaceAll("(?<=R).*$", "").replace("R", ""));
		random = new SecureRandom();
		try {
			if(isLegacy(113)) {
				getMaterial = Material.class.getMethod("getMaterial", int.class);
				getBlockTypeId = ChunkSnapshot.class.getMethod("getBlockTypeId", int.class, int.class, int.class);
				getBlockData = ChunkSnapshot.class.getMethod("getBlockData", int.class, int.class, int.class);
			}
			if(isLegacy(111)) {
				chatTitleMethod = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class);
				titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class,
						int.class);
				titleObject = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
				subtitleObject = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
			}
			
			if(isLegacy(110)) {
				boolean is_1_8_R1 = NMS_VERSION.equalsIgnoreCase("v1_8_R1");
				if(is_1_8_R1) {
					chatComponent = getNMSClass("ChatSerializer");
					chatComponentMethod = chatComponent.getDeclaredMethod("a", String.class);
				} else {
					chatComponent = getNMSClass("ChatComponentText");
				}
				chatComponentConstructor = chatComponent.getConstructor(new Class[] { String.class });
				chatPacket = getNMSClass("PacketPlayOutChat");
				chatBaseComponent = getNMSClass("IChatBaseComponent");
				chatPacketConstructor = chatPacket.getConstructor(new Class[] { chatBaseComponent, Byte.TYPE });
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isLegacy() {
		return NMS_VERSION_CODE < 113;
	}
	
	public static boolean isLegacy(int version) {
		return NMS_VERSION_CODE <= version;
	}
	
	public static Material getMaterial(ChunkSnapshot chunk, int x, int y, int z) {
		if(isLegacy(113)) {
			try {
				return (Material) getMaterial.invoke(null, getBlockTypeId.invoke(chunk, x, y, z));
			} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				e.printStackTrace();
			}
		} else
			return chunk.getBlockType(x, y, z);
		return Material.AIR;
	}
	
	public static int getDataValue(ChunkSnapshot chunk, int x, int y, int z) {
		if(isLegacy(113)) {
			try {
				return (int) getBlockData.invoke(chunk, x, y, z);
			} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public static int getRandomNumber(int max) {
		return random.nextInt(max);
	}
	
	public static String color(String string) {
		return string != null ? ChatColor.translateAlternateColorCodes('&', string) : null;
	}
	
	public static List<String> color(List<String> list) {
		return list != null ? list.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()) : null;
	}
	
	public static int getInt(String intString, int defaultreturn) {
		try {
			return Integer.parseInt(intString);
		} catch(NumberFormatException nfe) {
			return defaultreturn;
		}
	}
	
	public static long getLong(String longString, long defaultreturn) {
		try {
			return Long.parseLong(longString);
		} catch(NumberFormatException nfe) {
			return defaultreturn;
		}
	}
	
	public static byte getByte(String byteString, byte defaultreturn) {
		try {
			return Byte.parseByte(byteString);
		} catch(NumberFormatException nfe) {
			return defaultreturn;
		}
	}
	
	public static String getNmsVersion() {
		return NMS_VERSION;
	}
	
	public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		if(isLegacy(111)) {
			try {
				Object chatTitle = chatTitleMethod.invoke(null, "{\"text\": \"" + title + "\"}");
				Object titlePacket = titleConstructor.newInstance(titleObject, chatTitle, fadeIn, stay, fadeOut);
				
				Object subtitleTitle = chatTitleMethod.invoke(null, "{\"text\": \"" + subtitle + "\"}");
				Object subtitlePacket = titleConstructor.newInstance(subtitleObject, subtitleTitle, fadeIn, stay, fadeOut);
				
				sendPacket(player, titlePacket);
				sendPacket(player, subtitlePacket);
			} catch(Exception var11) {
				var11.printStackTrace();
			}
		} else {
			player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void sendActionbar(Player player, String message) {
		if(!isLegacy(110)) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
			return;
		}
		
		try {
			Object object = chatComponentMethod == null ? chatComponentConstructor.newInstance(message)
					: chatBaseComponent.cast(chatComponentMethod.invoke(chatComponent, "{'text': '" + message + "'}"));
			Object packetPlayOutChat = chatPacketConstructor.newInstance(object, (byte) 2);
			
			sendPacket(player, packetPlayOutChat);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void sendPacket(Player player, Object packet) {
		try {
			Object getHandle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, new Object[0]);
			Object playerConnection = getHandle.getClass().getField("playerConnection").get(getHandle);
			playerConnection.getClass().getMethod("sendPacket", new Class[] { getNMSClass("Packet") }).invoke(playerConnection, new Object[] { packet });
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Class<?> getNMSClass(String name) {
		try {
			return Class.forName("net.minecraft.server." + NMS_VERSION + "." + name);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getStringFromLocation(final Location location) {
		String loc = new String();
		loc = location == null ? "" : location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ();
		loc = location.getYaw() != 0.0 ? loc + ":yaw=" + location.getYaw() : loc;
		loc = location.getPitch() != 0.0 ? loc + ":pitch=" + location.getPitch() : loc;
		return loc;
	}
	
	public static Location getLocationFromString(final String location) {
		if(location == null || location.trim() == "")
			return null;
		final String[] split = location.split(":");
		final World world = Bukkit.getWorld(split[0]);
		if(world == null)
			return null;
		final double x = Double.parseDouble(split[1]);
		final double y = Double.parseDouble(split[2]);
		final double z = Double.parseDouble(split[3]);
		if(split.length == 4) {
			return new Location(world, x, y, z);
		} else if(split.length > 4) {
			final float yaw = (float) (split[4].contains("yaw") ? Float.parseFloat(split[4].replace("yaw=", "")) : 0.0);
			final float pitch = (float) (split.length > 5 && split[5] != null && split[5].contains("pitch") ? Float.parseFloat(split[5].replace("pitch=", ""))
					: split[4].contains("pitch") ? Float.parseFloat(split[5].replace("pitch=", "")) : 0.0);
			return new Location(world, x, y, z, yaw, pitch);
		}
		return null;
	}
	
	public static String getPrettyStringFromLocation(final Location location) {
		return "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
	}
}
