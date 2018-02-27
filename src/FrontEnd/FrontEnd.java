package FrontEnd;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

import DCAD.GObject;
import Message.MessageConvertion;
import Message.RemoveMessage;
import Message.Message;
import Message.DrawMessage;
import Message.JoinMessage;
import Message.LeaveMessage;

public class FrontEnd {
	private DatagramSocket m_socket;
	
	//move to server later----
	private ArrayList<Server.ClientConnection> m_connectedClients = new ArrayList<Server.ClientConnection>();
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	//------------------------
	
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
		System.out.println("Listening for messages...");
		while (true) {
			// THE SIZE OF THIS BYTE ARRAY IS MUCHO IMPORTANTE
			byte[] buf = new byte[1500];
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
				System.out.println("received join message of: " + packet.getLength() + " bytes");
				
				//Move to server later-----
				((JoinMessage) message).setMayJoin(true);
				m_connectedClients.add(new Server.ClientConnection("temp", packet.getAddress(), packet.getPort()));
				((JoinMessage) message).setList(m_GObjects);
				//------------------------
				
				sendMessage(packet.getAddress(), packet.getPort(), message);
			} else if (message instanceof DrawMessage) {
				System.out.println("received draw message of: " + packet.getLength() + " bytes");
				
				//Move to server ------
				m_GObjects.add((GObject) message.getObj());
				for (Server.ClientConnection cc : m_connectedClients) {
					sendMessage(cc.getAddress(), cc.getPort(), message);
				}
				//----------------------
			} else if (message instanceof RemoveMessage) {
				System.out.println("received remove message of: " + packet.getLength() + " bytes");
				//Move to server ------
				m_GObjects.remove(m_GObjects.size() - 1);
				for (Server.ClientConnection cc : m_connectedClients) {
					sendMessage(cc.getAddress(), cc.getPort(), message);
				}
				//---------------------
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

		if (message instanceof JoinMessage)
			System.out.println("sent join message");
		else if (message instanceof DrawMessage)
			System.out.println("sent draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("sent remove message");
		else if (message instanceof LeaveMessage)
			System.out.println("sent leave message");
	}

	private static int readFile() {
		Scanner m_s = null;
		ArrayList<String> list = new ArrayList<String>();
		try {
			m_s = new Scanner(new FileReader("resources/FrontEndConfig"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (m_s.hasNextLine()) {
			list.add(m_s.nextLine());
		}
		
		int i = Integer.parseInt(list.get(0).split(" ")[1]);
		System.out.println(list.get(0));
		
		return i;
	}
}
