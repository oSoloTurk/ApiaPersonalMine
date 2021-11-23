package me.osoloturk.personalmine.databases;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import me.osoloturk.personalmine.APM;

public class SQLiteDatabase implements IDatabase {
	private final APM instance;
	private Connection connection;
	
	public SQLiteDatabase(APM instance) {
		this.instance = instance;
		createTable();
	}
	
	@Override
	public Connection getConnection() {
		try {
			if(connection != null && !connection.isClosed())
				return connection;
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + getDatabaseFile());
			return connection;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public ResultSet query(String query) {
		if(!isConnected())
			getConnection();
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();
			return resultSet;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void update(String query) {
		if(!isConnected())
			getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.execute(query);
			statement.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void createTable() {
		if(!isConnected())
			getConnection();
		//APPERANCE FORMAT | loc-apperance_material-apperance_data;loc-apperance_material-apperance_data
		//POS FORMAT | block loc for single mine block || posOne-posTwo for multi mine block
		//CHANCEPACKS FORMAT | identifier-isActive,identifier-isActive
		//OPENWORLD | 1 yes 0 no
		update("CREATE TABLE IF NOT EXISTS OpenWorldFilter (SCANNEDCHUNKS MEDIUMTEXT);");
		update("CREATE TABLE IF NOT EXISTS MinePlayers (UUID VARCHAR(36) PRIMARY KEY, APPERANCE MEDIUMTEXT, RENEVALDATE INTEGER, CHANCEPACKS TEXT, LOADEDCHUNKS MEDIUMTEXT, LASTEXITDATE LONG);");
		update("CREATE TABLE IF NOT EXISTS MineBlocks (POS TEXT PRIMARY KEY, CREATEDATE LONG, TYPE INTEGER);");
	}
	
	@Override
	public boolean isConnected() {
		try {
			return connection != null && !connection.isClosed();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void close() {
		if(isConnected()) {
			try {
				connection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private File getDatabaseFile() {
		File file = new File(instance.getDataFolder(), "database.db");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
}
