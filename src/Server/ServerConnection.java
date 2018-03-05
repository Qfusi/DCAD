package Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import Message.ElectionMessage;
import Message.ElectionWinnerMessage;
import Message.Message;
import Message.ServerPingMessage;

public class ServerConnection {
	private ReplicaServer m_rs;
	private int m_id;
	private  InetAddress m_address;
	private  int m_port;
	private Socket m_socket;
	private int m_disconnectedPort;

	
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
				Message message = new ServerPingMessage(m_id, false);
				message.setPort(m_socket.getLocalPort());
				
				outputStream.writeObject(message);
				
				System.out.println("(TCP side) Server " + m_id + " -=SENT=- ping to port: " + m_port);
				
				Thread.sleep(5000);
			} catch (IOException e) {
				if (m_disconnectedPort == 0)
					m_disconnectedPort = m_socket.getPort();
				
				System.err.println("Server " + m_disconnectedPort + " has disconnected (Exception found in ServerConnection Send method)");
				
				reconnect();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(Message message) {
		try {
			message.setPort(m_socket.getLocalPort());
			if (message instanceof ServerPingMessage)
				System.out.println("(TCP side) Server " + m_id + " -=SENT=- ServerJoinMessage to server: " + ((ServerPingMessage)message).getID() + " on port " + message.getPort());
			else if (message instanceof ServerPingMessage)
				((ServerPingMessage)message).setID(m_id);
			else if (message instanceof ElectionMessage)
				System.out.println("(TCP side) Server " + m_id + " -=SENT=- ElectionMessage with ID " + ((ElectionMessage)message).getID() + " to server on port: " + message.getPort());
			else if (message instanceof ElectionWinnerMessage)
				System.out.println("(TCP side) Server " + m_id + " -=SENT=- ElectionWinnerMessage with ID " + ((ElectionWinnerMessage)message).getID() + " to server on port: " + message.getPort());
			
			ObjectOutputStream outputStream = new ObjectOutputStream(m_socket.getOutputStream());
			outputStream.writeObject(message);
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void reconnect() {
		while (true) {
			m_socket = new Socket();
			InetSocketAddress serveraddress = new InetSocketAddress(m_address, m_port);
			try {
				m_socket.connect(serveraddress);
				m_disconnectedPort = 0;
				System.out.println("Reconnected to port: " + m_port);
				break;
			} catch (IOException e1) {
				System.err.println("Tried to reconnect to port: " + m_port);
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
