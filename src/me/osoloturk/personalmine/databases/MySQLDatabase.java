package me.osoloturk.personalmine.databases;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import me.osoloturk.personalmine.misc.Settings;

public class MySQLDatabase implements IDatabase {
	private Connection connection;
	
	public MySQLDatabase() {
		getConnection();
		createTable();
	}
	
	@Override
	public Connection getConnection() {
		try {
			if(connection != null && !connection.isClosed())
				return connection;
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setUser(Settings.MYSQL_USER.getString());
			dataSource.setPassword(Settings.MYSQL_PASSWORD.getString());
			dataSource.setServerName(Settings.MYSQL_HOST.getString());
			dataSource.setDatabaseName(Settings.MYSQL_DATABASE.getString());
			dataSource.setConnectTimeout(200);
			dataSource.setPort(Settings.MYSQL_PORT.getInt());
			dataSource.setUseUnicode(true);
			dataSource.setCharacterEncoding("UTF-8");
			dataSource.setAutoReconnect(true);
			dataSource.setFailOverReadOnly(false);
			dataSource.setMaxReconnects(10);
			dataSource.setVerifyServerCertificate(false);
			connection = dataSource.getConnection();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return connection;
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
		//MineBlocks OPENWORLD | 1 yes  0 no
		update("CREATE TABLE IF NOT EXISTS MinePlayers (UUID VARCHAR(36) PRIMARY KEY, APPERANCE MEDIUMTEXT, RENEVALDATE INTEGER, CHANCEPACKS TEXT, LOADEDCHUNKS MEDIUMTEXT, LASTEXITDATE LONG);");
		update("CREATE TABLE IF NOT EXISTS MineBlocks (POS TEXT PRIMARY KEY, CREATEDATE LONG, TYPE INTEGER);");
		update("CREATE TABLE IF NOT EXISTS OpenWorldFilter (SCANNEDCHUNKS MEDIUMTEXT);");
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
}
