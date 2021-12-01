package server.model;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class UserModel extends DAO {
	/**
	 * USER MODEL
	 * 
	 * public: 	getName:String 	||	getGender:String 
	 * 			add:boolean		||	verifyAccount: boolean
	 * 
	 * private: getOneRow:String	||	isExist:boolean
	 * @throws Exception 
	 * 
	 * */
	
	public String getName(String username) throws Exception {
		if(isExist(username))
			return getOneRow("sp_getAccountInfo '"+username+"'","account_name");
		throw new Exception("The user not exist");
	}
	
	public String getGender(String username) throws Exception {
		if(isExist(username))
			return (Integer.parseInt(getOneRow("sp_getAccountInfo '"+username+"'","account_gender"))==1)
					?"Nam":"Nữ";
		throw new Exception("The user not exist");
	}
	
	public boolean add(String username, String password, String name, int gender) {
		if(!isExist(username)) {
			String hashpass = Crypto.getSHA256(password);
			return executeNoneQuery("sp_addAccount '"+username+"','"+hashpass+"',N'"+name+"',"+gender);
		}
		return false;
	}
	
	public boolean updateLastAccess(String username) {
		return executeNoneQuery("sp_updateLastaccess '"+username+"'");
	}
	
	public boolean verifyAccount(String username, String password) {
		String passwordHash = Crypto.getSHA256(password);
		if(Integer.parseInt(getOneRow("sp_verityAccount '"+username+"','"+passwordHash+"'", "result"))==1)
			return true;
		return false;
	}
	
	public ArrayList<String> getList(){
		return executeQuery("sp_getList");
	}
	
	private String getOneRow(String query,String columnName){
		try {
			return ((JSONObject) JSONValue.parse(executeQuery(query).get(0))).get(columnName).toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	private boolean isExist(String username) {
		try {
			ArrayList<String> data = executeQuery("sp_checkValidUsername "+"'"+username+"'");
			int result = Integer.parseInt(((JSONObject) JSONValue.parse(data.get(0))).get("result").toString());
			if(result==1) return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
}
