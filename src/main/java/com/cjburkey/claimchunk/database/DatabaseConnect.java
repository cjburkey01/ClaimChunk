package com.cjburkey.claimchunk.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.cjburkey.claimchunk.Utils;

public class DatabaseConnect {

	private final String hostName;
	private final String database;
	private final String username;
	private final String password;
	private final int port;
	private Connection connection;
	
	public DatabaseConnect(String hostName, String database, String user, String pass, int port) {
		this.hostName = hostName;
		this.database = database;
		username = user;
		password = pass;
		this.port = port;
	}
	
	public boolean openConnection() throws SQLException {
		synchronized (this) {
			if (connection != null && !connection.isClosed()) {
				return false;
			}
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				Utils.log("JDBC not found.");
				return false;
			}
			StringBuilder connect = new StringBuilder();
			connect.append("jdbc:mysql://");
			connect.append(hostName);
			connect.append(':');
			connect.append(port);
			connect.append('/');
			connect.append(database);
			connection = DriverManager.getConnection(connect.toString(), username, password);
			return true;
		}
		
	}
	
}