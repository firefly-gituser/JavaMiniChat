package server.model;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONObject;

public abstract class DAO {
	private static Connection connection;
	private static Statement statement;
	private String connectionUrl;
	private Properties properties;
	
	{
		if(connection==null || statement==null)
			initialize();
	}
	
	private void initialize() {
		this.connectionUrl = getConnectionString();
		try {
			connection = DriverManager.getConnection(connectionUrl);
			statement = connection.createStatement();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	protected String getConnectionString() {
		int port;
		String ip, database,user,password;
		this.properties = new Properties();
		try {
			properties.load(new FileReader("common\\configs.properties"));
		} catch (IOException e) {e.printStackTrace();}
		port = Integer.parseInt(properties.getProperty("port").toString());
		ip = properties.getProperty("ip").toString();
		database =  properties.getProperty("database").toString();
		user = properties.getProperty("username").toString();
		password = properties.getProperty("password").toString();
		return "jdbc:sqlserver://"+ip+":"+port+";databaseName="+database+";user="+user+";password="+password;
	}
	
	protected ArrayList<String> executeQuery(String queryString) {
		ArrayList<String> arrayList = new ArrayList<>();
		try {
			ResultSet resultSet = statement.executeQuery(queryString);
			int column = resultSet.getMetaData().getColumnCount();
			ResultSetMetaData metaData = resultSet.getMetaData();
			while (resultSet.next()) {
				Map<String, String> map = new HashMap<>();
				for(int i = 1; i<=column; i++)
					map.put(metaData.getColumnName(i).toString(),resultSet.getString(i));
				arrayList.add(new JSONObject(map).toJSONString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return arrayList;
	}
	
	protected boolean executeNoneQuery(String queryString) {
		try {
			int resultSet;
			resultSet = statement.executeUpdate(queryString);
			if(resultSet>0) return true;
			return false;
		} catch (SQLException e) {
			return false;
		}
	}
	
	public boolean isAlive() {
		return (DAO.connection!=null) && (DAO.statement!=null);
	}
}
