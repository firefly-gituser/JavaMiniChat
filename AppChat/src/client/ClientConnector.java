package client;

import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import org.json.simple.JSONObject;

import app.controller.RoomController;
import app.controller.LoginController;
import datapackage.ChatPackage;
import datapackage.LoginInfo;

public class ClientConnector {

	private Socket socket;

	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	private int port;
	private String ip;

	private Properties properties;

	private boolean status = false;
	private boolean isDoing = false;

	private RoomController roomController;
	private LoginController loginController;

	private JSONObject cookie;

	private Queue<Object> queue;

	private void initialize() {
		try {

			this.properties = new Properties();
			properties.load(new FileReader("common\\configs.properties"));

			this.port = Integer.parseInt(properties.getProperty("port").toString());
			this.ip = properties.getProperty("ip").toString();

			this.socket = new Socket(ip, port);
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			this.inputStream = new ObjectInputStream(socket.getInputStream());

			this.queue = new LinkedList<>();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stop() {
		this.status = false;
		try {
			this.inputStream.close();
			this.outputStream.close();
			this.socket.close();
		} catch (Exception e) {}
	}

	private void wakeUp() {
		new Thread(() -> callWakeUp()).start();
	}

	private void callWakeUp() {
		if (!isDoing) {
			isDoing = !isDoing;
			packageHandle();
		}
	}

	private void loginHandle(Object data) {
		LoginInfo info = (LoginInfo) data;
		if (info.getResult())
			this.cookie = info.getMetaData();
		loginController.switchToRoom(info.getResult());
	}
	
	@SuppressWarnings("unchecked")
	private void loginBroadcastHandle(Object data) {
		JSONObject jsonObject = (JSONObject) data;
		ArrayList<String> list = (ArrayList<String>) jsonObject.get("listUser");
		roomController.renderUserOnline(list);
	}

	private void singleHandle(ChatPackage data) {
		roomController.renderSingleMessage(data);
	}

	private void broadcastHandle(ChatPackage data) {
		roomController.renderBroadcastMessage(data,false);
	}
	
	private void signupHandle(Object data) {
		boolean result = (boolean) data;
		loginController.signupComplete(result);
	}
	
	@SuppressWarnings("unchecked")
	private void minichatHandle(ChatPackage data) {
		JSONObject json = (JSONObject)data.getDataPackage();
		ArrayList<String> dataString =  (ArrayList<String>) json.get("messageList");
		roomController.initMiniChatRoom(dataString, data.getDestination());
	}


	@SuppressWarnings("unchecked")
	private void roomInfoHandle(Object data) {
		JSONObject json = (JSONObject) data;
		ArrayList<String> listOnlineUser = (ArrayList<String>) json.get("listUser");
		ArrayList<String> listBroadcast = (ArrayList<String>) json.get("listBroadcast");
		roomController.renderUserOnline(listOnlineUser);
		roomController.renderBroadcastOldMessage(listBroadcast);
	}

	private void packageHandle() {
		ChatPackage chatPackage;
		while ((chatPackage = (ChatPackage) queue.poll()) != null) {
			switch (chatPackage.getSendType()) {
			case LOGIN:
				loginHandle(chatPackage.getDataPackage());
				break;
			case LOGINBROADCAST:
				loginBroadcastHandle(chatPackage.getDataPackage());
				break;
			case SINGLE:
				singleHandle(chatPackage);
				break;
			case BROADCAST:
				broadcastHandle(chatPackage);
				break;
			case SIGNUP:
				signupHandle(chatPackage.getDataPackage());
				break;
			case MINICHATINIT:
				minichatHandle(chatPackage);
				break;
			case ROOMINFO:
				roomInfoHandle(chatPackage.getDataPackage());
				break;
			default:
				break;
			}

		}
		isDoing = false;
	}

	private void startServe() {
		while (status) {
			try {
				queue.add(inputStream.readObject());
				wakeUp();
			} catch (ClassNotFoundException | IOException e) {
				break;
			}
		}
	}

	/**
	 * @author tamth
	 * 
	 *         The Object for connecting and communicating with server
	 */
	private ClientConnector() {
		initialize();
		start();
	}

	private static ClientConnector clientConnector;

	public static ClientConnector getInstance() {
		if (clientConnector == null)
			clientConnector = new ClientConnector();
		return clientConnector;
	}

	public JSONObject getCookie() {
		return this.cookie;
	}

	public void setContext(Object context) {
		if (context instanceof LoginController) {
			this.loginController = (LoginController) context;
		} else {
			this.roomController = (RoomController) context;
		}
	}

	public void clearContext() {
		this.roomController = null;
		this.loginController = null;
	}

	public void start() {
		this.status = true;
		new Thread(() -> startServe()).start();
	}

	public boolean sendPackage(ChatPackage data) {
		try {
			outputStream.writeObject(data);
			outputStream.flush();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
