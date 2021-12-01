package server.model;

import java.util.ArrayList;

public class BroadcastModel extends DAO {
	public ArrayList<String> getListBroadcast(){
		return executeQuery("sp_getListBroadcast");
	}
	
	public boolean add(String sender, String content) {
		return executeNoneQuery("sp_addBroadcast '"+sender+"',N'"+content+"'");
	}
}
