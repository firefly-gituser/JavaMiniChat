package server.model;

import java.util.ArrayList;

public class SingleModel extends DAO{
	public boolean add(String sender, String receiver, String content){
		return executeNoneQuery("sp_addSingleMessage '"+sender+"','"+receiver+"',N'"+content+"'");
	}
	
	public ArrayList<String> getList(String sender, String receiver){
		return executeQuery("sp_getSingleList '"+sender+"','"+receiver+"'");
	}
}
