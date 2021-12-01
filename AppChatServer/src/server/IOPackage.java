package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import datapackage.ChatPackage;
import datapackage.DataPackage;
import datapackage.DataType;
import datapackage.LoginInfo;
import datapackage.SendType;
import server.model.BroadcastModel;
import server.model.SingleModel;
import server.model.UserModel;

public class IOPackage extends Thread {
	/**
	 * author: tamth
	 * 
	 * class: IOPackage
	 * 
	 * desc: get a connect, serve/ receive and take message, then execute
	 * 
	 * */
	
	private Socket socket;
	
	//Listen And Send Data Gateway
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	
	// Database Object Model
	private SingleModel single;
	private BroadcastModel broadcast;
	private UserModel user;
	
	// Connecting List
	private ArrayList<IOPackage> list;

	//Username Of The Connecting @allownull
	private String username;

	//Constructor
	public IOPackage(Socket socket, ArrayList<IOPackage> list) {
		this.socket = socket;
		this.list = list;
		
		this.broadcast = new BroadcastModel();
		this.single = new SingleModel();
		this.user = new UserModel();
		
		initStream();
	}
	
	@Override
	public void run() {
		startServe();
	}
	
	/*
	 *			PUBLIC METHODS 
	 */
	public String getUserName() {
		return this.username;
	}
	
	public boolean sendData(ChatPackage data) {
		try {
			outputStream.writeObject(data);
			outputStream.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*
	 *			PRIVATE METHODS 
	 */

	private void initStream() {
		try {
			this.inputStream = new ObjectInputStream(socket.getInputStream());
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());

		} catch (IOException e) {e.getMessage();}
	}

	private void startServe() {
		try {
			while (true)
				getInfoFromPackage(inputStream.readObject());
		} catch (Exception e) {
			if(username!=null) {
				user.updateLastAccess(username);
				username = null;
				sendLoginBroadcast();
			}
			list.remove(this);
		}
	}
	
	private void getInfoFromPackage(Object data) {
		ChatPackage chatPackage = (ChatPackage) data;
		switch (chatPackage.getSendType()) {
			case LOGIN:
				loginHandle(chatPackage.getDataPackage());
				break;
			case LOGOUT:
				logoutHandle();
				break;
			case SINGLE:
				singleHandle(chatPackage);
				break;
			case MINICHATINIT:
				minichatInit(chatPackage);
				break;
			case BROADCAST:
				broadcastHandle(chatPackage);
				break;
			case SIGNUP:
				signupHandle(chatPackage.getDataPackage());
				break;
			case ROOMINFO:
				roomInfoHandle(chatPackage.getSender());
				break;
			default:
				break;
		}
	}
	
	private void singleHandle(ChatPackage chatPackage) {
		if(((DataPackage)chatPackage.getDataPackage()).getType()==DataType.MESSAGE) {
			String message = (String) ((DataPackage)chatPackage.getDataPackage()).getData();
			single.add(chatPackage.getSender(), chatPackage.getDestination(), message);
		}
		
		for (IOPackage ioPackage : list) {
			if(ioPackage.getUserName()!=null)
				if(ioPackage.getUserName().equalsIgnoreCase(chatPackage.getDestination()))
					ioPackage.sendData(chatPackage);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void minichatInit(ChatPackage data) {
		ArrayList<String> list = single.getList(data.getSender(), data.getDestination());
		JSONObject json = new JSONObject();
		json.put("messageList", list);
		ChatPackage chatPackage = new ChatPackage(data.getSender(), SendType.MINICHATINIT, data.getDestination(),json);
		sendData(chatPackage);
	}
	
	private void broadcastHandle(ChatPackage data) {
		if(((DataPackage)(data.getDataPackage())).getType()==DataType.MESSAGE) {
			String message =(String) ((DataPackage)(data.getDataPackage())).getData();
			broadcast.add(data.getSender(), message);
		}
		sendBroadcast(data);
	}
	
	@SuppressWarnings("unchecked")
	private void roomInfoHandle(String username) {
		if(username==null || !username.equalsIgnoreCase(this.username))
			return;
		
		JSONObject roomInfo = getLoginUser();
		roomInfo.put("listBroadcast",broadcast.getListBroadcast());
		sendData(new ChatPackage("server",SendType.ROOMINFO,"thisuser", roomInfo));
	}
	
	@SuppressWarnings("unchecked")
	private void loginHandle(Object data) {
		LoginInfo login = (LoginInfo) data;
		if(checkingConnect(login.getUsername())) {
			boolean result = user.verifyAccount(login.getUsername(),login.getPassword());
			if(result) {
				try {
					this.username = login.getUsername();
					sendLoginBroadcast();
					String name = user.getName(login.getUsername());
					String gender = user.getGender(login.getUsername());
					login.setResult(result);
					JSONObject json = new JSONObject();
					json.put("name", name);
					json.put("gender", gender);
					json.put("username", login.getUsername());
					login.setMetaData(json);
				} catch (Exception e) {
					
					System.err.println(e.getMessage());
				}
			}
		}
		ChatPackage chatPackage= new ChatPackage("server",SendType.LOGIN,"client", login);
		sendData(chatPackage);
	}
	
	private void logoutHandle() {
		user.updateLastAccess(username);
		this.username = null;
		sendLoginBroadcast();
	}
	
	private void signupHandle(Object data) {
		JSONObject json = (JSONObject) data;
		if(user.add(json.get("username").toString(), json.get("password").toString(), json.get("name").toString(),Integer.parseInt(json.get("gender").toString()))) 
			sendData(new ChatPackage("server",SendType.SIGNUP,"client", true));
		else
			sendData(new ChatPackage("server",SendType.SIGNUP,"client", false));
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getLoginUser() {
		ArrayList<String> userlist = new ArrayList<>();
		for (IOPackage ioPackage : list)
			if(ioPackage.getUserName()!=null && !ioPackage.getName().equalsIgnoreCase(username))
				try {
					JSONObject info = new JSONObject();
					info.put("username",ioPackage.getUserName());
					info.put("name",this.user.getName(ioPackage.getUserName()));
					userlist.add(info.toJSONString());
				} catch (Exception e) {
					e.printStackTrace();
				}
		JSONObject json = new JSONObject();
		json.put("listUser", userlist);
		return json;
	}
	

	private void sendLoginBroadcast() {
		ChatPackage chatPackage =new ChatPackage("server",SendType.LOGINBROADCAST,"this user",getLoginUser());
		sendBroadcast(chatPackage);
	}
	
	private void sendBroadcast(ChatPackage data) {
		new Thread(()->{
			for (IOPackage ioPackage : list)
				if(ioPackage.getUserName()!=null)
					if(!ioPackage.getUserName().equalsIgnoreCase(this.username))
						ioPackage.sendData(data);
		}).start();
	}
	
	private boolean checkingConnect(String name) {
		for (IOPackage ioPackage : list) {
			if(ioPackage.getUserName()!=null)
				if(ioPackage.getUserName().equalsIgnoreCase(name))
					return false;
		}
		return true;
	}
}
