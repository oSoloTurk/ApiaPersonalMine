package me.osoloturk.personalmine.databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;

import me.osoloturk.personalmine.misc.AppearanceType;
import me.osoloturk.personalmine.misc.GeneratorMineBlock;
import me.osoloturk.personalmine.misc.MineBlock;
import me.osoloturk.personalmine.misc.MinePlayer;
import me.osoloturk.personalmine.misc.MultiMineBlock;
import me.osoloturk.personalmine.misc.Settings;
import me.osoloturk.personalmine.misc.SingleMineBlock;
import me.osoloturk.personalmine.utils.CompressTools;
import me.osoloturk.personalmine.utils.Pair;
import me.osoloturk.personalmine.utils.Utils;

public interface IDatabase {
	
	public Connection getConnection();
	
	public ResultSet query(String query);
	
	public void update(String query);
	
	public void createTable();
	
	public boolean isConnected();
	
	public void close();
	
	public default List<String> getOpenWorldScannedChunks() {
		ResultSet query = null;
		List<String> loadedChunks = new ArrayList<>();
		try {
			query = query("SELECT * FROM OpenWorldFilter");
			while(query.next()) {
				String chunkSource = query.getString("SCANNEDCHUNKS");
				chunkSource = CompressTools.unzip(chunkSource);
				loadedChunks.addAll(Arrays.asList(chunkSource.split(",")));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			if(query != null)
				try {
					query.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
		return loadedChunks;
	}
	
	public default void saveOpenWorldScannedChunks(List<String> chunks) {
		update("DELETE FROM OpenWorldFilter");
		String loadedChunks = chunks.stream().collect(Collectors.joining(","));
		if(Settings.DATABASE_COMPRESS.getBoolean()) {
			loadedChunks = CompressTools.zip(loadedChunks);
		}
		PreparedStatement state = null;
		try {
			state = getConnection().prepareStatement("INSERT INTO OpenWorldFilter (SCANNEDCHUNKS) VALUES(?);");
			state.setString(1, loadedChunks);
			state.execute();
		} catch(SQLException e1) {
			e1.printStackTrace();
		} finally {
			if(state != null)
				try {
					state.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public default MinePlayer getMinePlayer(UUID uuid) {
		ResultSet query = null;
		try {
			query = query("SELECT * FROM MinePlayers WHERE UUID = '" + uuid.toString() + "'");
			MinePlayer profile = null;
			List<String> loadedChunks;
			while(query.next()) {
				loadedChunks = new ArrayList<>();
				String chunkSource = query.getString("LOADEDCHUNKS");
				chunkSource = CompressTools.unzip(chunkSource);
				loadedChunks.addAll(Arrays.asList(chunkSource.split(",")));
				profile = new MinePlayer(uuid, getApperancesFromString(query.getString("APPERANCE")), query.getLong("RENEVALDATE"), getChancePacksFromString(query.getString("CHANCEPACKS")),
						loadedChunks, query.getLong("LASTEXITDATE"));
			}
			return profile == null ? insertMinePlayer(uuid) : profile;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			if(query != null)
				try {
					query.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
	
	public default MinePlayer insertMinePlayer(UUID uuid) {
		MinePlayer minePlayer = new MinePlayer(uuid);
		PreparedStatement state = null;
		try {
			state = getConnection().prepareStatement("INSERT INTO MinePlayers (UUID, APPERANCE, RENEVALDATE, CHANCEPACKS, LOADEDCHUNKS) VALUES(?, ?, ?, ?, ?);");
			state.setString(1, uuid.toString());
			state.setString(2, "");
			state.setLong(3, minePlayer.getRenevalDate());
			state.setString(4, "");
			state.setString(5, "");
			state.execute();
		} catch(SQLException e1) {
			e1.printStackTrace();
		} finally {
			if(state != null)
				try {
					state.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
		return minePlayer;
	}
	
	public default List<MineBlock> getMineBlocks() {
		List<MineBlock> regionList = new ArrayList<>();
		ResultSet query = null;
		try {
			query = query("SELECT * FROM MineBlocks");
			while(query.next()) {
				AppearanceType type = AppearanceType.getType(query.getString("TYPE"));
				String pos = query.getString("POS");
				if(type == AppearanceType.SINGLE || type == AppearanceType.OPEN_WORLD)
					regionList.add(new SingleMineBlock(Utils.getLocationFromString(pos), query.getLong("CREATEDATE"), type));
				else if(type == AppearanceType.MULTI) {
					String[] poses = pos.split("\\*");
					regionList.add(new MultiMineBlock(Utils.getLocationFromString(poses[0]), Utils.getLocationFromString(poses[1]), query.getLong("CREATEDATE"), type));
				} else if(type == AppearanceType.GENERATOR)
					regionList.add(new GeneratorMineBlock(Utils.getLocationFromString(pos), query.getLong("CREATEDATE")));
			}
			query.close();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			if(query != null)
				try {
					query.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
		return regionList;
	}
	
	public default void savePlayer(UUID uuid, MinePlayer player) {
		String apperanceText = getApperancesAsString(player.getApperances());
		String chancePacks = getChancePacksAsString(player.getChancePacks());
		String loadedChunks = player.getAllChunks().stream().collect(Collectors.joining(","));
		if(Settings.DATABASE_COMPRESS.getBoolean()) {
			loadedChunks = CompressTools.zip(loadedChunks);
		}
		PreparedStatement state = null;
		try {
			state = getConnection().prepareStatement("UPDATE MinePlayers SET APPERANCE=?, RENEVALDATE = ?, CHANCEPACKS=?, LOADEDCHUNKS=?, LASTEXITDATE=? WHERE UUID=?;");
			state.setString(1, apperanceText);
			state.setLong(2, player.getRenevalDate());
			state.setString(3, chancePacks);
			state.setString(4, loadedChunks);
			state.setLong(5, System.currentTimeMillis());
			state.setString(6, uuid.toString());
			state.executeUpdate();
		} catch(SQLException e1) {
			e1.printStackTrace();
		} finally {
			if(state != null)
				try {
					state.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public default void saveMinePlayers(Map<UUID, MinePlayer> minePlayers) {
		PreparedStatement state = null;
		try {
			state = getConnection().prepareStatement("UPDATE MinePlayers SET APPERANCE=?, RENEVALDATE = ?, CHANCEPACKS=?, LOADEDCHUNKS=?, LASTEXITDATE=? WHERE UUID=?;");
			int counter = 0;
			for(Entry<UUID, MinePlayer> entry : minePlayers.entrySet()) {
				String apperanceText = getApperancesAsString(entry.getValue().getApperances());
				String chancePacks = getChancePacksAsString(entry.getValue().getChancePacks());
				String loadedChunks = "";
				List<String> chunks = entry.getValue().getAllChunks();
				for(int i = 0; i < chunks.size(); i++) {
					loadedChunks += chunks.get(i);
					if(i + 1 != chunks.size())
						loadedChunks += ",";
				}
				if(Settings.DATABASE_COMPRESS.getBoolean()) {
					loadedChunks = CompressTools.zip(loadedChunks);
				}
				state.setString(1, apperanceText);
				state.setLong(2, entry.getValue().getRenevalDate());
				state.setString(3, chancePacks);
				state.setString(4, loadedChunks);
				state.setLong(5, System.currentTimeMillis());
				state.setString(6, entry.getKey().toString());
				state.addBatch();
				if(++counter % 100 == 0) {
					state.executeBatch();
					counter = 0;
				}
			}
			if(counter > 0)
				state.executeBatch();
		} catch(SQLException e1) {
			e1.printStackTrace();
		} finally {
			if(state != null)
				try {
					state.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public default void insertMineBlock(MineBlock mineBlock) {
		PreparedStatement state = null;
		try {
			state = getConnection().prepareStatement("INSERT INTO MineBlocks (POS, CREATEDATE, TYPE) VALUES(?, ?, ?);");
			state.setString(1, mineBlock.getLocationAsString());
			state.setLong(2, mineBlock.getCreateDate());
			state.setInt(3, mineBlock.getTypeCode());
			state.executeUpdate();
		} catch(SQLException e1) {
			e1.printStackTrace();
		} finally {
			if(state != null)
				try {
					state.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public default void deleteMineBlock(MineBlock mineBlock) {
		PreparedStatement state = null;
		try {
			state = getConnection().prepareStatement("DELETE FROM MineBlocks WHERE POS=?;");
			state.setString(1, mineBlock.getLocationAsString());
			state.execute();
		} catch(SQLException e1) {
			e1.printStackTrace();
		} finally {
			if(state != null)
				try {
					state.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	default String getApperancesAsString(Map<Location, Pair<AppearanceType, Pair<Material, Byte>>> map) {
		String returnText = map.entrySet().stream().map(entry -> (Utils.getStringFromLocation(entry.getKey()) + "*" + entry.getValue().getValue().getKey().name() + "*"
				+ entry.getValue().getValue().getValue() + "*" + entry.getValue().getKey().getId())).collect(Collectors.joining(","));
		if(Settings.DATABASE_COMPRESS.getBoolean()) {
			returnText = CompressTools.zip(returnText);
		}
		return returnText;
	}
	
	default String getChancePacksAsString(Map<String, Boolean> chancePacks) {
		String returnText = chancePacks.entrySet().stream().map(entry -> (entry.getKey() + "*" + entry.getValue().toString())).collect(Collectors.joining(","));
		if(Settings.DATABASE_COMPRESS.getBoolean()) {
			returnText = CompressTools.zip(returnText);
		}
		return returnText;
	}
	
	default Map<Location, Pair<AppearanceType, Pair<Material, Byte>>> getApperancesFromString(String serializedApperances) {
		serializedApperances = CompressTools.unzip(serializedApperances);
		String[] apperances = serializedApperances.split(",");
		HashMap<Location, Pair<AppearanceType, Pair<Material, Byte>>> blocks = new HashMap<>();
		for(String apperance : apperances) {
			if(!apperance.contains("*"))
				continue;
			String[] apperanceDetails = apperance.split("\\*");
			try {
				blocks.put(Utils.getLocationFromString(apperanceDetails[0]),
						Pair.of(AppearanceType.getType(apperanceDetails[3]), Pair.of(Material.valueOf(apperanceDetails[1]), Byte.parseByte(apperanceDetails[2]))));
			} catch(Exception e) {
				continue;
			}
		}
		return blocks;
	}
	
	default Map<String, Boolean> getChancePacksFromString(String serializedChancePacks) {
		serializedChancePacks = CompressTools.unzip(serializedChancePacks);
		Map<String, Boolean> chancePacks = new HashMap<>();
		String[] packs = serializedChancePacks.split(",");
		for(String chancePack : packs) {
			if(!chancePack.contains("*"))
				continue;
			String[] packDetails = chancePack.split("\\*");
			chancePacks.put(packDetails[0], Boolean.valueOf(packDetails[1]));
		}
		return chancePacks;
	}
	
}
