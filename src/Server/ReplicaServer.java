package Server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import DCAD.GObject;
import Message.DrawMessage;
import Message.JoinMessage;
import Message.Message;
import Message.RemoveMessage;

public class ReplicaServer {
	//-------------------General
	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	private int m_ID;
	
	//-------------------FE
	private FrontEndConnection m_FEconnection;
	private InetAddress m_feAddress = null;
	private int m_fePort;
	
	//-------------------UDP
	private DatagramSocket m_socket;
	private static InetAddress m_address = null;
	private static int m_port;
	
	//-------------------TCP
	private ServerConnection m_serverConnection;
	
	public static void main(String[] args){
		if(args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		try {
			ReplicaServer instance = new ReplicaServer();
			instance.connectToFrontEnd();
			if (instance.m_FEconnection.handShake(m_address, m_port));
				instance.listenForFrontEndMessages();
		} catch(NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	//--------------------------------------------------------------------------------------

	//STARTING UP SEVERAL SERVER WORKS
	private ReplicaServer() {
		for (int i = 0; i < 3; i++) {
			try {
				try {
					m_address = readAddressFromFile(i, new FileReader("resources/ServerConfig"));
					m_port =  readPortFromFile(i, 1, new FileReader("resources/ServerConfig"));
					m_socket = new DatagramSocket(m_port);
					System.out.println("created UDP socket with port: " + m_port);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				m_ID = i;
				TCPsetup(m_ID);
				if (m_serverConnection.getID() == 0) {
					m_serverConnection.listenForNewConnections();
				} else if (m_serverConnection.getID() == 1) {
					
				}
				break;
			} catch (SocketException e) {
				System.err.println("Could not create UDP socket on row: " + i);
			}
		}
	}
	//--------------------------------------------------------------------------------------

	//SERVER NEEDS TO HAVE TWO THREADS, ONE FOR SERVER MESSAGES AND ONE FOR FRONTEND
	private void listenForFrontEndMessages() {
		System.out.println("Waiting for client messages... ");
		
		while(true) {
			Message message = m_FEconnection.receiveMessage();
			
			if (message instanceof JoinMessage) {
				if (addClient(message.getAddress(), message.getPort())) {
					((JoinMessage) message).setMayJoin(true);
					((JoinMessage) message).setReply(true);
					((JoinMessage) message).setList(m_GObjects);
				} else {
					((JoinMessage) message).setMayJoin(false);
					((JoinMessage) message).setReply(true);
				}
				 
				
				m_FEconnection.sendMessage(message);
			} else if (message instanceof DrawMessage) {
				m_GObjects.add((GObject) message.getObj());
				for (ClientConnection cc : m_connectedClients) {
					message.setAddress(cc.getAddress());
					message.setPort(cc.getPort());
					m_FEconnection.sendMessage(message);
				}
			} else if (message instanceof RemoveMessage) {
				m_GObjects.remove(m_GObjects.size() - 1);
				for (ClientConnection cc : m_connectedClients) {
					message.setAddress(cc.getAddress());
					message.setPort(cc.getPort());
					m_FEconnection.sendMessage(message);
				}
			}
		}
	}
	
	private void TCPsetup(int id) {
		InetAddress address = null;
		int port = 0;
		int port2 = 0;
		
		switch (id) {
		case 0:
			try {
				address = readAddressFromFile(id, new FileReader("resources/ServerConfig"));
				port = readPortFromFile(id, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			m_serverConnection = new ServerConnection(id, address, port);
			break;
		case 1:
			try {
				address = readAddressFromFile(id, new FileReader("resources/ServerConfig"));
				port = readPortFromFile(id, 2, new FileReader("resources/ServerConfig"));
				port2 = readPortFromFile(0, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			m_serverConnection = new ServerConnection(id, address, port, address, port2);
			break;
		case 2:
			try {
				address = readAddressFromFile(0, new FileReader("resources/ServerConfig"));
				port = readPortFromFile(0, 2, new FileReader("resources/ServerConfig"));
				port2 = readPortFromFile(1, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			m_serverConnection = new ServerConnection(id, address, port, address, port2);
			break;
		}
	}
	
	//WORKS
	private void connectToFrontEnd() {
		try {
			m_feAddress = readAddressFromFile(1, new FileReader("resources/FrontEndConfig"));
			m_fePort = readPortFromFile(1, 1, new FileReader("resources/FrontEndConfig"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
			
		m_FEconnection = new FrontEndConnection(m_feAddress, m_fePort, m_socket);
	}
	
	//WORKS
	private boolean addClient(InetAddress address, int port) {
		for (ClientConnection c : m_connectedClients) {
			if (c.getAddress() == address && c.getPort() == port) {
				System.out.println("didn't add client");
				return false;
			}
		}
		m_connectedClients.add(new ClientConnection(address, port));
		System.out.println("added client");
		return true;
	}
	
	//WORKS
	private static int readPortFromFile(int row, int collumn, FileReader file) {
		Scanner m_s = null;
		ArrayList<String> list = new ArrayList<String>();
		int i;
		m_s = new Scanner(file);
		
		while (m_s.hasNextLine()) {
			list.add(m_s.nextLine());
		}
		
		i = Integer.parseInt(list.get(row).split(" ")[collumn]);
		
		m_s.close();
		return i;
	}
	
	//WORKS
	private static InetAddress readAddressFromFile(int row, FileReader file) {
		Scanner m_s = null;
		ArrayList<String> list = new ArrayList<String>();
		String address;
		m_s = new Scanner(file);
		
		while (m_s.hasNextLine()) {
			list.add(m_s.nextLine());
		}
		
		address = list.get(row).split(" ")[0];
		
		m_s.close();
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
}

