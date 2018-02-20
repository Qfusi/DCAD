package DCAD;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import Message.DrawMessage;
import Message.JoinMessage;
import Message.LeaveMessage;
import Message.Message;
import Message.MessageConvertion;
import Message.RemoveMessage;

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
		JoinMessage message = new JoinMessage(m_socket);
		sendMessage(message);
		
		Message message2;
		message2 = receiveMessage();
		
		//TODO FIX HANDSHAKE U FUKKER
		
		return false;
	}
	
	public Message receiveMessage() {
		Message message = null;
		byte[] b = new byte[256];
		DatagramPacket packet = new DatagramPacket(b, b.length);
		
		try {
			m_socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			message = (Message) MessageConvertion.deserialize(packet.getData());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (message instanceof JoinMessage)
			System.out.println("received join message");
		else if (message instanceof DrawMessage)
			System.out.println("received draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("received remove message");
		else if (message instanceof LeaveMessage)
			System.out.println("received leave message");
		
		return message;
	}
	
	public void sendMessage(Message message) {
		byte[] b = null;
		try {
			b = MessageConvertion.serialize(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(b, b.length, m_serverAddress, m_port);
		try {
			m_socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
