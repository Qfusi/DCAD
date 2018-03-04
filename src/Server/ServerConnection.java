package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import Message.JoinMessage;
import Message.Message;

public class ServerConnection {
	private int m_ID;
	
	//---------------------socket1
	private ServerSocket m_Ssocket;
	private  InetAddress m_address;
	private  int m_port;
	
	//---------------------socket2
	private Socket m_Csocket, m_Csocket2;
	private InetAddress m_address2;
	private  int m_port2;
	
	//SERVER 1
	public ServerConnection(int id, InetAddress address, int port) {
		m_ID = id;
		m_address = address;
		m_port = port;
		
		try {
			m_Ssocket = new ServerSocket(m_port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SERVER 2 & 3
	public ServerConnection(int id, InetAddress address, int port, InetAddress address2, int port2) {
		m_ID = id;
		m_address = address;
		m_port = port;
		m_address2 = address2;
		m_port2 = port2;
		SocketAddress socketAddress;
		
		//---------------------Server 2
		if (id == 1) {
			try {
				m_Ssocket = new ServerSocket(m_port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			m_Csocket = new Socket();
			socketAddress = new InetSocketAddress(address2, port2);
			try {
				m_Csocket.connect(socketAddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		} 
		//---------------------Server 3
		else {
			m_Csocket = new Socket();
			socketAddress = new InetSocketAddress(address, port);
			try {
				m_Csocket.connect(socketAddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			m_Csocket2 = new Socket();
			socketAddress = new InetSocketAddress(address2, port2);
			try {
				m_Csocket2.connect(socketAddress);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	//-----------------------------------------Initialises the different server behaviour
	public void startDoingThings() {
		if (m_ID == 0)
			listenForNewConnections();
		else if (m_ID == 1) {
			new Thread(new Runnable() {
				public void run() {
					listenForNewConnections();
				}
			}).start();
			
			connectToServer(m_Csocket, m_port2);
		} else {
			new Thread(new Runnable() {
				public void run() {
					connectToServer(m_Csocket, m_port);
				}
			}).start();
			
			connectToServer(m_Csocket2, m_port2);
		}
	}
	
	//--------------------------------------------Listens for new server connections
	private void listenForNewConnections() {
		System.out.println("Listening for new connections... ");
		while(true) {
			try {
				final Socket socket;
				socket = m_Ssocket.accept();
				
				System.out.println("received new connection");
				
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
	
	//---------------------------------------------Listens for messages from already established server connections
	private void listenForServerMessages(Socket socket) {
		System.out.println("Listening for server messages... ");
		
		while (true) {
			Message message = null;
			
			try {
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				message = (Message) inputStream.readObject();
			} catch (IOException e) {
				System.err.println("A server has disconnected (Listening method)");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("received message from: " + message.getPort());
		}
	}
	
	//----------------------------------------------Constantly tries to connect to specified server
	private void connectToServer(Socket socket, int port) {
		while (true) {
			try {
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				Message message = new JoinMessage();
				message.setPort(socket.getLocalPort());
				
				outputStream.writeObject(message);
				
				System.out.println("sent message to port: " + port);
				
				Thread.sleep(5000);
			} catch (IOException e) {
				System.err.println("A server has disconnected (Send method");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public InetAddress getAddress() {
		return m_address;
	}

	public void setAddress(InetAddress m_address) {
		this.m_address = m_address;
	}

	public int getPort() {
		return m_port;
	}

	public void setPort(int m_port) {
		this.m_port = m_port;
	}
	
	public int getID() {
		return m_ID;
	}
}
