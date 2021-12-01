package datapackage;

import java.io.Serializable;

import org.json.simple.JSONObject;

public class LoginInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	private boolean result =false;
	private JSONObject metaData;
	public LoginInfo(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public void setResult(boolean status) {
		this.result = status;
	}
	
	public boolean getResult() {
		return this.result;
	}
	
	public void setMetaData(JSONObject jsonObject) {
		this.metaData = jsonObject;
	}
	
	public JSONObject getMetaData() {
		return this.metaData;
	}
}
