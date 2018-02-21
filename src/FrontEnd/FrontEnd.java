package FrontEnd;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import Message.MessageConvertion;
import Message.Message;
import Message.JoinMessage;

public class FrontEnd {
	private static Scanner m_s;
	private DatagramSocket m_socket;
	
	public static void main(String[] args) {
		FrontEnd instance = new FrontEnd(readFile());
		instance.listenForMessages();
	}
	
	private FrontEnd(int portNumber) {
		try {
			m_socket = new DatagramSocket(portNumber);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private void listenForMessages() {
		System.out.println("Waiting for clients...");
		while (true) {
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			Message message = null;
			
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
			
			if (message instanceof JoinMessage) {
				sendMessage(packet.getAddress(), packet.getPort(), message);
				System.out.println("received join message");
			}
		}
	}
	
	public void sendMessage(InetAddress address, int port, Message message) {
		byte[] b = null;
		try {
			b = MessageConvertion.serialize(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(b, b.length, address, port);
		try {
			m_socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int readFile() {
		try {
			m_s = new Scanner(new FileReader("resources/frontEndFile"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int s = 0;
		
		while (m_s.hasNext()) {
			s = m_s.nextInt();
		}
		
		System.out.println("portnumber: " + s);
		
		return s;
	}
}
