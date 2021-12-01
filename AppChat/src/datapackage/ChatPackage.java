package datapackage;

import java.io.Serializable;

public class ChatPackage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String sender;
	private SendType type;
	private Object dataPackage;
	private String to;
	
	public ChatPackage(String sender, SendType type,String to, Object data) {
		this.sender = sender;
		this.type =type;
		this.to =to;
		this.dataPackage = data;
	}
	
	public String getSender() {
		return this.sender;
	}
	
	public SendType getSendType() {
		return this.type;
	}
	
	public String getDestination() {
		return this.to;
	}
	
	public Object getDataPackage() {
		return this.dataPackage;
	}
	
	
}
