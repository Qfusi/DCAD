package Server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import DCAD.GObject;
import Message.DisconnectMessage;
import Message.DrawMessage;
import Message.ElectionMessage;
import Message.JoinMessage;
import Message.Message;
import Message.NewActiveServerMessage;
import Message.RemoveMessage;
import Message.ServerJoinMessage;

public class ReplicaServer {
	//-------------------General
	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private ArrayList<ServerConnection> m_connectedServers = new ArrayList<ServerConnection>();
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	private static int m_ID;
	
	//-------------------FE
	private FrontEndConnection m_FEconnection;
	private InetAddress m_feAddress = null;
	private int m_fePort;
	
	//-------------------UDP
	private DatagramSocket m_FEsocket;
	private static InetAddress m_address = null;
	private static int m_port;
	
	//-------------------TCP
	private ServerSocket m_Ssocket;
	private Socket m_Csocket;
	
	//-------------------Election
	private int m_receivedElectionID = 15;
	
	public static void main(String[] args){
		if(args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		try {
			ReplicaServer instance = new ReplicaServer();
			
			if (instance.m_FEconnection.handShake(m_address, m_port));
				instance.listenForFrontEndMessages();
		} catch(NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	//--------------------------------------------------------------------------------------

	private ReplicaServer() {
		for (int i = 0; i < 3; i++) {
			try {
				//--------------------------UDP Setup
				try {
					m_address = readAddressFromFile(i, new FileReader("resources/ServerConfig"));
					m_port =  readPortFromFile(i, 1, new FileReader("resources/ServerConfig"));
					m_FEsocket = new DatagramSocket(m_port);
					System.out.println("created UDP socket with port: " + m_port);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				UDPsetup();
				//--------------------------TCP Setup
				m_ID = i;
				TCPsetup();
				break;
			} catch (SocketException e) {
				System.err.println("Could not create UDP socket on row: " + i);
			}
		}
	}
	//--------------------------------------------------------------------------------------

	private void listenForFrontEndMessages() {
		System.out.println("(UDP side) Listening for front end messages... ");
		
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
			} else if (message instanceof DisconnectMessage) {
				ClientConnection toBeRemoved = null;
				for (ClientConnection cc : m_connectedClients) {
					if (cc.getPort() == message.getPort())
						toBeRemoved = cc;
				}
				m_connectedClients.remove(toBeRemoved);
			}
		}
	}
	
	private void listenForNewServerConnections() {
		System.out.println("(TCP side) Listening for new connections...");
		while(true) {
			try {
				Message message = null;
				final Socket socket;
				socket = m_Ssocket.accept();
				
				try {
					ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
					message = (Message) inputStream.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				if (message instanceof ServerJoinMessage) {
					m_connectedServers.add(new ServerConnection(this, m_ID, message.getAddress(), message.getPort(), socket));
					System.out.println("(TCP side) received new connection from: " + message.getPort());
				}
				
				new Thread(new Runnable() {
					public void run() {
						listenForServerMessages(socket);
					}
				}).start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void listenForServerMessages(Socket socket) {
		System.out.println("(TCP side) Listening for server messages from socket: " + socket.getPort() + "...");
		System.out.println("--------------------------------------------------------------");
		
		while (true) {
			Message message = null;
			
			try {
				//-----------------------------------------------------------------------------RECEIVE
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				message = (Message) inputStream.readObject();
				
				if (message instanceof ServerJoinMessage)
					System.out.println("(TCP side) Server " + m_ID + " -=RECEIVED=- a ServerJoinMessage from server: " + ((ServerJoinMessage)message).getID() + " on port " + message.getPort());
				else if (message instanceof NewActiveServerMessage)
					System.out.println("(TCP side) Server " + m_ID + " -=RECEIVED=- a NewActiveServerMessage from port: " + message.getPort());
				else if (message instanceof ElectionMessage)
					System.out.println("(TCP side) Server " + m_ID + " -=RECEIVED=- a ElectionMessage from port: " + message.getPort() + " with ID: " + ((ElectionMessage)message).getID());
				
				
				
				
				//-----------------------------------------------------------------------------REPLY
				
				ServerConnection temp = getServerConnection(message.getPort());
				
				if (message instanceof ElectionMessage) {
					//check if message is not a reply and if server ID is smaller than received one - reply with own ID if that is the case
					if (!((ElectionMessage)message).isReply() && m_ID < ((ElectionMessage)message).getID())
						temp.sendMessage(new ElectionMessage(m_ID, true));
					else {
						System.out.println("hej");
						m_receivedElectionID = ((ElectionMessage) message).getID();
					}
				}
				
				
				//-----------------------------------
			} catch (IOException e) {
				System.err.println("Server " + socket.getPort() + " has disconnected (Exception found in ReplicaServer receive method)");
				
				ServerConnection connectionToRemove = null;
				for (ServerConnection SC : m_connectedServers) {
					if (SC.getSocket() == socket)
						connectionToRemove = SC;
				}
				m_connectedServers.remove(connectionToRemove);
				
				//---------HAVE ELECTION :D -WORKS
				electionProtocol();
				
				break;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void electionProtocol() {
		System.out.println("-------------------------------");
		System.out.println("Initializing Election... ");
		
		for (ServerConnection SC : m_connectedServers) {
			SC.sendMessage(new ElectionMessage(m_ID, false));
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(m_receivedElectionID);
		
		if (m_ID < m_receivedElectionID) {
			System.out.println(m_receivedElectionID);
			m_FEconnection.sendMessage(new NewActiveServerMessage(m_address, m_port));
			m_receivedElectionID = 15;
		}
	}
	
	//---------------------------------------------------------------------------------Sets up connection with replica servers
	private void TCPsetup() {
		SocketAddress serveraddress;
		InetAddress address = null;
		InetAddress address2 = null;
		int port = 0;
		int port2 = 0;
		
		switch (m_ID) {
		case 0:
			//---------------------------------------------------------------------------------------------------------Server 1
			try {
				port = readPortFromFile(m_ID, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			try {
				m_Ssocket = new ServerSocket(port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			System.out.println("created TCP recieve socket with port: " + port);
			System.out.println("--------------------------------------------------");
			
			new Thread(new Runnable() {
				public void run() {
					listenForNewServerConnections();
				}
			}).start();
			break;
		case 1:
			//---------------------------------------------------------------------------------------------------------Server 2
			try {
				address = readAddressFromFile(m_ID, new FileReader("resources/ServerConfig"));
				address2 = readAddressFromFile(0, new FileReader("resources/ServerConfig"));
				port = readPortFromFile(m_ID, 2, new FileReader("resources/ServerConfig"));
				port2 = readPortFromFile(0, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			//-----------------------------------------Server socket
			try {
				m_Ssocket = new ServerSocket(port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			System.out.println("created TCP receive socket with port: " + port);
			System.out.println("created TCP send socket with port: " + port2);
			System.out.println("--------------------------------------------------");
			new Thread(new Runnable() {
				public void run() {
					listenForNewServerConnections();
				}
			}).start();
			//------------------------------------------Client socket
			
			m_Csocket = new Socket();
			serveraddress = new InetSocketAddress(address, port2);
			try {
				m_Csocket.connect(serveraddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			m_connectedServers.add(new ServerConnection(this, m_ID, address2, port2, m_Csocket));
			
			new Thread(new Runnable() {
				public void run() {
					m_connectedServers.get(0).connectToServer();
				}
			}).start();
			break;
		case 2:
			//---------------------------------------------------------------------------------------------------------Server 3
			try {
				address = readAddressFromFile(0, new FileReader("resources/ServerConfig"));
				address2 = readAddressFromFile(1, new FileReader("resources/ServerConfig"));
				port = readPortFromFile(0, 2, new FileReader("resources/ServerConfig"));
				port2 = readPortFromFile(1, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			//--------------------------------------------Client socket 1
			
			m_Csocket = new Socket();
			serveraddress = new InetSocketAddress(address, port);
			try {
				m_Csocket.connect(serveraddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			m_connectedServers.add(new ServerConnection(this, m_ID, address, port, m_Csocket));
			
			//---------------------------------------------Client socket 2
			
			m_Csocket = new Socket();
			serveraddress = new InetSocketAddress(address2, port2);
			try {
				m_Csocket.connect(serveraddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			m_connectedServers.add(new ServerConnection(this, m_ID, address2, port2, m_Csocket));
			
			System.out.println("created TCP send socket with port: " + port);
			System.out.println("created TCP send socket with port: " + port2);
			System.out.println("--------------------------------------------------");
			
			
			new Thread(new Runnable() {
				public void run() {
					m_connectedServers.get(0).connectToServer();
				}
			}).start();
			new Thread(new Runnable() {
				public void run() {
					m_connectedServers.get(1).connectToServer();
				}
			}).start();
			break;
		}
	}
	
	//-------------------------------------------------------------------------------------Sets up connection with FrontEnd
	private void UDPsetup() {
		try {
			m_feAddress = readAddressFromFile(1, new FileReader("resources/FrontEndConfig"));
			m_fePort = readPortFromFile(1, 1, new FileReader("resources/FrontEndConfig"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
			
		m_FEconnection = new FrontEndConnection(m_feAddress, m_fePort, m_FEsocket);
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
	
	private ServerConnection getServerConnection(int port) {
		for (ServerConnection SC : m_connectedServers)
			if (SC.getPort() == port)
				return SC;
		
		return null;
	}
}

