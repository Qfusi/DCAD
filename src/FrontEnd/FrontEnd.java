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
	private DatagramSocket m_clientSocket;
	private DatagramSocket m_serverSocket;
	private InetAddress m_serverAddress;
	private int m_serverPort;
	
	//temporary (for testing)
	static FrontEnd instance;
	static FrontEnd instance2;
	static Thread thread;
	
	
	//move to server later----
	private ArrayList<Server.FrontEndConnection> m_connectedClients = new ArrayList<Server.FrontEndConnection>();
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	//------------------------
	
	public static void main(String[] args) {
		instance = new FrontEnd(readFile(1));
		instance2 = new FrontEnd(readFile(0));
		instance.listenForServerMessages();
	}

	private FrontEnd(int portNumber) {
		try {
			if (portNumber == 25005)
				m_clientSocket = new DatagramSocket(portNumber);
			if (portNumber == 25006)
				m_serverSocket = new DatagramSocket(portNumber);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void listenForClientMessages() {
		System.out.println("Listening for Client messages...");
		System.out.println("_______________________________________");
		while (true) {
			// THE SIZE OF THIS BYTE ARRAY IS MUCHO IMPORTANTE
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			Message message = null;

			try {
				m_clientSocket.receive(packet);
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
				System.out.println("Client side received a Join Message");
				
				message.setAddress(packet.getAddress());
				message.setPort(packet.getPort());
				
				sendMessage(m_serverSocket, m_serverAddress, m_serverPort, message);
			} else if (message instanceof DrawMessage) {
				System.out.println("received draw message of: " + packet.getLength() + " bytes");	
				
				sendMessage(m_serverSocket, m_serverAddress, m_serverPort, message);
				
				//Move to server ------
				/*m_GObjects.add((GObject) message.getObj());
				for (Server.FrontEndConnection cc : m_connectedClients) {
					sendMessage(m_clientSocket, cc.getAddress(), cc.getPort(), message);
				}*/
				//----------------------
			} else if (message instanceof RemoveMessage) {
				System.out.println("received remove message of: " + packet.getLength() + " bytes");
				//Move to server ------
				m_GObjects.remove(m_GObjects.size() - 1);
				for (Server.FrontEndConnection cc : m_connectedClients) {
					sendMessage(m_clientSocket, cc.getAddress(), cc.getPort(), message);
				}
				//---------------------
			}
		}
	}
	
	public void listenForServerMessages() {
		System.out.println("Listening for Server messages...");
		while (true) {
			byte[] buf = new byte[1024];
			final DatagramPacket packet = new DatagramPacket(buf, buf.length);
			Message message = null;

			try {
				m_serverSocket.receive(packet);
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
				System.out.println("Server side received a Join Message");
				if (((JoinMessage) message).isReply()) {
					System.out.println(m_clientSocket);
					sendMessage(m_clientSocket, message.getAddress(), message.getPort(), message);
				} else if (!((JoinMessage) message).isReply()) {
					thread = new Thread(new Runnable() {
						public void run() {
							instance2.listenForClientMessages();
						}
					});
					thread.start();
					
					((JoinMessage) message).setMayJoin(true);
					sendMessage(m_serverSocket, packet.getAddress(), packet.getPort(), message);
				}
			}
		}
	}

	public void sendMessage(DatagramSocket socket, InetAddress address, int port, Message message) {
		byte[] b = null;
		try {
			b = MessageConvertion.serialize(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(b, b.length, address, port);
		try {
			socket.send(packet);
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

	private static int readFile(int row) {
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
		
		int i = Integer.parseInt(list.get(row).split(" ")[1]);
		
		return i;
	}
}
