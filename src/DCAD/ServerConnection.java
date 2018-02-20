package DCAD;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import Message.Message;
import Message.MessageConvertion;

public class ServerConnection {
	private DatagramSocket m_socket;
	private InetAddress m_serverAddress;
	private int m_port;
	
	public ServerConnection(String hostName, int port) {
		m_port = port;
		try {
			m_socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		try {
			m_serverAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public boolean handshake() {
		GObject temp = new GObject(Shape.OVAL, Color.RED, 363, 65, 25, 25);
		GObject temp2 = null;
		byte[] buf = null;
		
		System.out.println(temp.getShape() + " " + temp.getY());
		try {
			buf = MessageConvertion.serialize(temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			temp2 = (GObject) MessageConvertion.deserialize(buf);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(temp2.getShape() + " " + temp2.getY());
		
		return false;
	}
	
	public void sendMessage(Message message) {
		
	}
	
}
