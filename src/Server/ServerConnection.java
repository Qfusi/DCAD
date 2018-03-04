package Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import Message.Message;
import Message.ServerJoinMessage;

public class ServerConnection {
	private ReplicaServer m_rs;
	private int m_id;
	private  InetAddress m_address;
	private  int m_port;
	private Socket m_socket;

	
	public ServerConnection(ReplicaServer rs, int id, InetAddress address, int port, Socket socket) {
		m_rs = rs;
		m_id = id;
		m_address = address;
		m_port = port;
		m_socket = socket;
	}
	
	//----------------------------------------------Constantly tries to connect to specified server
	public void connectToServer() {
		new Thread(new Runnable() {
			public void run() {
				m_rs.listenForServerMessages(m_socket);
			}
		}).start();
		
		while (true) {
			try {
				ObjectOutputStream outputStream = new ObjectOutputStream(m_socket.getOutputStream());
				Message message = new ServerJoinMessage(m_id, false);
				message.setPort(m_socket.getLocalPort());
				
				outputStream.writeObject(message);
				
				System.out.println("(TCP side) sent message to port: " + m_port);
				
				Thread.sleep(5000);
			} catch (IOException e) {
				System.err.println("Server " + m_socket.getPort() + " has disconnected (Exception found in ServerConnection Send method)");
				
				//TODO ------------------------------------------ELECTION BULLSHIT
				
				
				
				//---------------------------------------------
				
				reconnect();
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(Message message) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(m_socket.getOutputStream());
			outputStream.writeObject(message);
			
			System.out.println("sent a message");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void reconnect() {
		m_socket = new Socket();
		InetSocketAddress serveraddress = new InetSocketAddress(m_address, m_port);
		try {
			m_socket.connect(serveraddress);
		} catch (IOException e1) {
			System.err.println("Tried to reconnect");
		}
	}
	
	public Socket getSocket() {
		return m_socket;
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
		return m_id;
	}
}
