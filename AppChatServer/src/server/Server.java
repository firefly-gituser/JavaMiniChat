package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server extends Thread {
	private ArrayList<IOPackage> lists = new ArrayList<>();
	private ServerSocket serverSocket;
	
	public static void main(String[] args) {
		new Server().start();
	}
	
	@Override
	public void run() {
		System.out.println("Server is running in port 8000 localhost");
		initialize();
	}
	
	public void initialize() {
		try {
			serverSocket = new ServerSocket(8000);
			while (true) {
				IOPackage ioPackage =  new IOPackage(serverSocket.accept(),lists);
				lists.add(ioPackage);
				ioPackage.start();
				System.out.println("A client connected");
			}
		} catch (IOException e) {
			e.getMessage();
		}
	}
}
