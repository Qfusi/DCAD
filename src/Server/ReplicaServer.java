package Server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;
import DCAD.GObject;
import Message.*;

public class ReplicaServer {
	// -------------------General
	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private ArrayList<ServerConnection> m_connectedServers = new ArrayList<ServerConnection>();
	private ArrayList<GObject> m_GObjects = new ArrayList<GObject>();
	private int m_ID;

	// -------------------FE
	private FrontEndConnection m_FEconnection;
	private InetAddress m_feAddress = null;
	private int m_fePort;
	private UUID m_fePingID = UUID.randomUUID();

	// -------------------UDP
	private DatagramSocket m_FEsocket;
	private InetAddress m_address = null;
	private int m_port;
	private long m_startTime;

	// -------------------TCP
	private ServerSocket m_Ssocket;
	private Socket m_Csocket;
	private long m_updateTimestamp = 0;

	// -------------------Election
	private int m_receivedElectionID = 15;
	private boolean m_canParticipate = true;

	public static void main(String[] args) {
		new ReplicaServer();
	}

	// This solution is build to support up to 3 servers, who will elect a leader which connects to the Front End. Servers will read from a file to set up Socket.
	// The UDP connection to the front end is controlled by a listen thread and send thread. The TCP which handles the connection between Replica Servers has a thread for each connection, so 2 for
	// each server. All connection are handled by list which hold the different connection types. Disconnects on server side are handled by catches which are sync. 

	private ReplicaServer() {
		for (int i = 0; i < 3; i++) {
			try {
				// --------------------------UDP Setup
				try {
					m_address = readAddressFromFile(i, new FileReader("resources/ServerConfig"));
					m_port = readPortFromFile(i, 1, new FileReader("resources/ServerConfig"));
					m_FEsocket = new DatagramSocket(m_port);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				UDPsetup();
				// --------------------------TCP Setup
				m_ID = i;
				TCPsetup();
				break;
			} catch (SocketException e) {
				System.err.println("Could not create UDP socket on row: " + i);
			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//if (m_ID == 2)
		electionProtocol();

		listenForFrontEndMessages();
	}

	//UDP, listning to FE connection
	private void listenForFrontEndMessages() {

		while (true) {
			Message message = m_FEconnection.receiveMessage();

			if (message != null) {
				if (message instanceof ConnectMessage) {
					if (addClient(message.getAddress(), message.getPort())) {
						((ConnectMessage) message).setMayJoin(true);
						((ConnectMessage) message).setReply(true);
						((ConnectMessage) message).setList(m_GObjects);
						message.setMessageID(message.getMessageID());
						m_FEconnection.sendMessage(message);
						broadcastToServers(message);
					} else {
						((ConnectMessage) message).setMayJoin(false);
						((ConnectMessage) message).setReply(true);
						m_FEconnection.sendMessage(message);
					}
				} else if (message instanceof DrawMessage) {
					addObject((GObject) message.getObj());
					broadcastToClients(message);
					m_updateTimestamp = System.currentTimeMillis();
					broadcastToServers(new UpdateMessage(m_GObjects, m_updateTimestamp));
				} else if (message instanceof RemoveMessage) {
					if (!m_GObjects.isEmpty())
						removeObject((GObject) message.getObj());

					broadcastToClients(message);
					m_updateTimestamp = System.currentTimeMillis();
					broadcastToServers(new UpdateMessage(m_GObjects, m_updateTimestamp));
				} else if (message instanceof DisconnectMessage) {
					removeClient(message.getPort());
					broadcastToServers(message);
				} else if (message instanceof ClientCheckUpMessage) {
					setActiveState(message.getPort());
				}
			}
			kickInactiveClients();
		}
	}

	//TCP, listens for other Replica servers, this method is designed for server 1 and 2. 
	private void listenForNewServerConnections() {
		while (true) {
			try {
				final Socket socket;
				socket = m_Ssocket.accept();
				m_connectedServers
				.add(new ServerConnection(this, m_ID, socket.getInetAddress(), socket.getPort(), socket));
				new Thread(new Runnable() {
					public void run() {
						listenForServerMessages(socket, false);
					}
				}).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//TCP, listens for message from servers like election or backups from prime
	public void listenForServerMessages(Socket socket, boolean fromSC) {

		while (true) {
			ObjectInputStream inputStream = null;

			try {
				inputStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				// ------------------------------------------------------------------------------SERVER
				// DISCONNECTS
				System.err.println("Server " + socket.getPort() + " has disconnected (Exception found in ReplicaServer receive method)");
				// Remove the disconnected server from the list
				ServerConnection temporary = getServerConnection(socket.getPort());
				m_connectedServers.remove(temporary);

				synchronized (this.getClass()) {
					electionProtocol();
				}

				// If the thread is started in ServerConnection we will have to reconnect. If
				// the thread is not from SC we don't reconnect and let it die.
				if (fromSC)
					temporary.reconnect();

				// break in order to terminate this listener thread - disconnected server will
				// be accepted again in listenForServerMessages method
				// and new listener thread will be started again
				break;
			} // -----------------------------------------------------------------------------------------------------
			try {
				Message message = (Message) inputStream.readObject();

				ServerConnection sc = getServerConnection(message.getPort());

				if (message instanceof ServerPingMessage) {

					if (!((ServerPingMessage) message).isReply())
						sc.sendMessage(new ServerPingMessage(m_ID, true));
				} else if (message instanceof ElectionMessage) {

					// is message not a reply?
					if (!((ElectionMessage) message).isReply()) {
						m_receivedElectionID = ((ElectionMessage) message).getID();

						// is server out of date? -> may not participate in election -> other server
						// wins
						if (m_updateTimestamp < ((ElectionMessage) message).getUpdateTimestamp()) {
							broadcastToServers(new ElectionWinnerMessage(((ElectionMessage) message).getID()));
							m_canParticipate = false;
						}
						// is my ID lower than received ID? -> Let other server know
						else if (m_ID < m_receivedElectionID) {
							sc.sendMessage(new ElectionMessage(m_ID, true, m_updateTimestamp));
						}
					}
					// is message reply?
					else if (((ElectionMessage) message).isReply()) {
						// is server out of date? -> may not participate in election -> other server
						// wins
						if (m_updateTimestamp < ((ElectionMessage) message).getUpdateTimestamp()) {
							m_canParticipate = false;
						}
						// is save receivedID higher than new received one? -> update to new ID
						else if (m_receivedElectionID > ((ElectionMessage) message).getID()) {
							m_receivedElectionID = ((ElectionMessage) message).getID();
						}
					}
				}

				else if (message instanceof ElectionWinnerMessage) {

					if (((ElectionWinnerMessage) message).getID() == m_ID) {
						for (int i = 0; i < 5; i++)
							m_FEconnection.sendMessage(new NewActiveServerMessage(m_address, m_port));

						m_FEconnection.addToALO(new FEPingMessage(m_address, m_port, m_fePingID));
						m_receivedElectionID = 15;
					} else
						m_FEconnection.removeFEPing();
				} else if (message instanceof UpdateMessage) {
					m_GObjects = ((UpdateMessage) message).getList();
					m_updateTimestamp = ((UpdateMessage) message).getTimestamp();
				} else if (message instanceof ConnectMessage) {

					addClient(message.getAddress(), message.getPort());
				} else if (message instanceof DisconnectMessage) {
					removeClient(message.getPort());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("This error is expeted, handled by catch");
			}
		}
	}

	private synchronized void electionProtocol() {
		System.out.println("-------------------------------");
		System.out.println("     Initializing Election");
		System.out.println("-------------------------------");

		broadcastToServers(new ElectionMessage(m_ID, false, m_updateTimestamp));

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (m_canParticipate) {
			if (m_ID < m_receivedElectionID) {
				// We won the election -> alert FE
				for (int i = 0; i < 5; i++)
					m_FEconnection.sendMessage(new NewActiveServerMessage(m_address, m_port));
				m_FEconnection.addToALO(new FEPingMessage(m_address, m_port, m_fePingID));
				m_receivedElectionID = 15;
			}
			if (m_ID > m_receivedElectionID) {
				// We didn't win the election -> Alert the servers about who won and stop
				// pinging FE
				m_FEconnection.removeFEPing();
				broadcastToServers(new ElectionWinnerMessage(m_receivedElectionID));
				m_receivedElectionID = 15;
			}
			m_canParticipate = true;
		}

		System.out.println("-------------------------------");
		System.out.println("     Election Finished");
		System.out.println("-------------------------------");
	}

	// ---------------------------------------------------------------------------------Sets
	//  TCP  set-up checks which id the server has and assigns the right socket type(ServerSocket or Socket) in the right order and number, creates 2 threads each to listen . 
	private void TCPsetup() {
		SocketAddress serveraddress;
		InetAddress address = null;
		InetAddress address2 = null;
		int port = 0;
		int port2 = 0;

		switch (m_ID) {
		//server 1 has one serversocket to connect, 
		case 0:
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


			new Thread(new Runnable() {
				public void run() {
					listenForNewServerConnections();
				}
			}).start();

			break;
			//server 2 has a serversocket to connect to server 3 and a Socket to connect to server 1
		case 1:

			// ---------------------------------------------------------------------------------------------------------Server
			// 2
			try {
				address = readAddressFromFile(m_ID, new FileReader("resources/ServerConfig"));
				address2 = readAddressFromFile(0, new FileReader("resources/ServerConfig"));
				port = readPortFromFile(m_ID, 2, new FileReader("resources/ServerConfig"));
				port2 = readPortFromFile(0, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			// -----------------------------------------Server socket
			try {
				m_Ssocket = new ServerSocket(port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}


			new Thread(new Runnable() {
				public void run() {
					listenForNewServerConnections();
				}
			}).start();
			// ------------------------------------------Client socket

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
			//server 3 has 2 socket which connect to server 1 or 2. 
		case 2:
			// ---------------------------------------------------------------------------------------------------------Server
			// 3
			try {
				address = readAddressFromFile(0, new FileReader("resources/ServerConfig"));
				address2 = readAddressFromFile(1, new FileReader("resources/ServerConfig"));
				port = readPortFromFile(0, 2, new FileReader("resources/ServerConfig"));
				port2 = readPortFromFile(1, 2, new FileReader("resources/ServerConfig"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// --------------------------------------------Client socket 1

			m_Csocket = new Socket();
			serveraddress = new InetSocketAddress(address, port);
			try {
				m_Csocket.connect(serveraddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			m_connectedServers.add(new ServerConnection(this, m_ID, address, port, m_Csocket));

			// ---------------------------------------------Client socket 2

			m_Csocket = new Socket();
			serveraddress = new InetSocketAddress(address2, port2);
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
			new Thread(new Runnable() {
				public void run() {
					m_connectedServers.get(1).connectToServer();
				}
			}).start();
			break;
		}
	}

	// -------------------------------------------------------------------------------------Sets
	// setup connection with FrontEnd, reads adress and port from file and sends message with information for the Front End where to send message coming from client
	private void UDPsetup() {
		try {
			m_feAddress = readAddressFromFile(1, new FileReader("resources/FrontEndConfig"));
			m_fePort = readPortFromFile(1, 1, new FileReader("resources/FrontEndConfig"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		m_FEconnection = new FrontEndConnection(m_feAddress, m_fePort, m_FEsocket);
	}

	//checks if client in list allready or if needed to be added. Checks port number to do this. 
	private boolean addClient(InetAddress address, int port) {
		for (ClientConnection c : m_connectedClients) {
			if (c.getAddress().equals(address) && c.getPort() == port) {
				return false;
			}
		}
		m_connectedClients.add(new ClientConnection(address, port));
		return true;
	}

	private void removeClient(int port) {
		ClientConnection toBeRemoved = null;
		for (ClientConnection cc : m_connectedClients) {
			if (cc.getPort() == port)
				toBeRemoved = cc;
		}
		m_connectedClients.remove(toBeRemoved);
	}

	private static int readPortFromFile(int row, int collumn, FileReader file) {
		Scanner s = null;
		ArrayList<String> list = new ArrayList<String>();
		int i;
		s = new Scanner(file);
		while (s.hasNextLine()) {
			list.add(s.nextLine());
		}
		i = Integer.parseInt(list.get(row).split(" ")[collumn]);
		s.close();
		return i;
	}

	private static InetAddress readAddressFromFile(int row, FileReader file) {
		Scanner s = null;
		ArrayList<String> list = new ArrayList<String>();
		String address;
		s = new Scanner(file);

		while (s.hasNextLine()) {
			list.add(s.nextLine());
		}

		address = list.get(row).split(" ")[0];

		s.close();
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

	private void broadcastToServers(Message message) {
		for (ServerConnection SC : m_connectedServers)
			SC.sendMessage(message);
	}

	//UDP, needs to create a new object of this message to give it a new UUID, checks only which message type and acts on that. 
	private void broadcastToClients(Message message) {
		for (ClientConnection cc : m_connectedClients) {
			if (message instanceof DrawMessage) {
				Message temp = new DrawMessage((GObject) message.getObj(), UUID.randomUUID());
				temp.setAddress(cc.getAddress());
				temp.setPort(cc.getPort());
				m_FEconnection.sendMessage(temp);
			} else if (message instanceof RemoveMessage) {
				Message temp = new RemoveMessage((GObject) message.getObj(), UUID.randomUUID());
				temp.setAddress(cc.getAddress());
				temp.setPort(cc.getPort());
				m_FEconnection.sendMessage(temp);
			} else if (message instanceof ClientCheckUpMessage) {
				Message temp = new ClientCheckUpMessage(UUID.randomUUID(), false);
				temp.setAddress(cc.getAddress());
				temp.setPort(cc.getPort());
				m_FEconnection.sendMessage(temp);
			}
		}
	}

	public void addServerConnection(ServerConnection sc) {
		m_connectedServers.add(sc);
	}

	//UDP, checks every 15 seconds if clients answer on special message, if no answer they will be kicked 
	private void kickInactiveClients() {
		if (m_startTime + 15000 < System.currentTimeMillis()) { // checks every 15 sec

			if (m_connectedClients.size() == 0) { //is there a client connected?, but this method will not be called if no client send message -> not really needed for this version

				m_startTime = System.currentTimeMillis(); // do nothing while no clients connected, reset timer
			} else {

				while (true) {// this loop runs untill all list is free of crashed clients
					boolean crash = false;
					ClientConnection c;
					for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {//run this loop, if crashed client found kick, then start again until nothing found.
						c = itr.next();
						if (!c.getActiveStatus()) { // if false, not active, meaning crashed
							m_FEconnection.removeCrashedClientsMessagesBridge(c.getPort()); // empty AOL list
							m_connectedClients.remove(c);
							crash = true;
							break;
						} else
							crash = false;
					}

					if (crash == false) // if nothing found while loop ends
						break;
				}
				//send
				broadcastToClients(new ClientCheckUpMessage(UUID.randomUUID(), false));// send Checkmessage which clients need to answer to, if they dont want to be kicked.
				m_startTime = System.currentTimeMillis();

				// set all current clients crash states to false, untill proven wrong by the client when answering on message
				ClientConnection c2;
				for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
					c2 = itr.next();
					c2.setActiveStatus(false);
				}
			}
		}
	}

	private void setActiveState(int port) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (port == c.getPort()) {
				c.setActiveStatus(true);
			}
		}
	}

	//when object is new or recived this method selects the index where the object should be stored in the list. It has a 5 milsec limit or else the objects will be handled as concurrent.
	public void addObject(GObject obj) {



		if (!(m_GObjects.isEmpty())) {// if somethings in list

			//if last obj in list has a  smaller/younger timestamp than new obj (with more than +/-5 milsec diffrence ) -> new obj is older(bigger timestamp) and should be at the end of list
			if (obj.getTimestamp() > m_GObjects.get(m_GObjects.size() - 1).getTimestamp() + 5) {
				m_GObjects.add(obj);
				return;
			}
			//if first obj in list has a bigger/older timestamp than new obj (with more than +/-5 milsec diffrence )-> new obj is younger(smaller timestamp) and should be at the end of list
			else if (obj.getTimestamp() < m_GObjects.get(0).getTimestamp() - 5) {
				m_GObjects.add(0, obj);
				return;
			}

			for (int i = m_GObjects.size() - 2; i >= 0; i--) {// iteration from last element to first

				//checks if element on index is in +/-5 milsec radius of new objects timestamp -> its concurrent and should be placed there at this index 
				if ((obj.getTimestamp() + 5 <= m_GObjects.get(i).getTimestamp() && obj.getTimestamp() - 5 >= m_GObjects.get(i).getTimestamp())) {// if new obj is
					m_GObjects.add(i, obj);// gets added
					return;
				}
				//if not close enough (+/-5 milsec) but the time stamp of element is smaller than the new object it needs to be placed at index +1 to sort. 
				else if (obj.getTimestamp() - 5 >= m_GObjects.get(i).getTimestamp() ) {
					m_GObjects.add(i+1, obj);
					return;
				}
			}
		} 
		else {
			m_GObjects.add(obj);
		}
	}

	private void removeObject(GObject obj) {
		if (!m_GObjects.isEmpty()) {
			for (int i = 0; i < m_GObjects.size(); i++) {
				if (obj.getID().equals(m_GObjects.get(i).getID())) {
					m_GObjects.remove(i);
				}
			}
		}
	}
}