package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import DCAD.GObject;
import Message.DrawMessage;
import Message.JoinMessage;
import Message.Message;
import Message.RemoveMessage;

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
		
		if (id == 1) {
			try {
				m_Ssocket = new ServerSocket(m_port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_Csocket = new Socket();
		} else {
			m_Csocket = new Socket();
			m_Csocket2 = new Socket();
		}
	}
	
	public void listenForNewConnections() {
		System.out.println("SERVER MESSAGEEEEES?");
		
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
	
	private void listenForServerMessages(Socket socket) {
		System.out.println("Listening for server messages... ");
		
		while (true) {
			Message message = null;
			
			try {
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				message = (Message) inputStream.readObject();	
			} catch (IOException e) {
				//TODO
				//HANDLE DISCONNECTED SERVERS LEL
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("received message: " + message.getPort());
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
