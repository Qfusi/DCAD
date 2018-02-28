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

import Message.MessageConvertion;
import Message.RemoveMessage;
import Message.Message;
import Message.DrawMessage;
import Message.JoinMessage;
import Message.LeaveMessage;

public class FrontEnd {
	static Thread serverThread;
	static Thread clientThread;
	static FrontEnd serverListener;
	static FrontEnd clientListener;
	private DatagramSocket m_clientSocket;
	private DatagramSocket m_serverSocket;
	
	public static void main(String[] args) {
		serverListener = new FrontEnd(readFile(1));
		clientListener = new FrontEnd(readFile(0));
		
		serverListener.setClientSocket(clientListener.getClientSocket());
		clientListener.setServerSocket(serverListener.getServerSocket());
		
		serverThread = new Thread(new Runnable() {
			public void run() {
				serverListener.listenForServerMessages();
			}
		});
		serverThread.start();
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

	private void listenForClientMessages(InetAddress address, int port) {
		InetAddress serverAddress = address;
		int serverPort  = port;
		System.out.println("Listening for Client messages...");
		
		while (true) {
			// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!
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
				System.out.println("ClientListener received join message of: " + packet.getLength() + " bytes");
				
				message.setAddress(packet.getAddress());
				message.setPort(packet.getPort());
				
				sendMessage(m_serverSocket, serverAddress, serverPort, message);
			} else if (message instanceof DrawMessage) {
				System.out.println("ClientListener received draw message of: " + packet.getLength() + " bytes");	
				sendMessage(m_serverSocket, serverAddress, serverPort, message);
			} else if (message instanceof RemoveMessage) {
				System.out.println("ClientListener received remove message of: " + packet.getLength() + " bytes");
				sendMessage(m_serverSocket, serverAddress, serverPort, message);
			}
		}
	}
	
	public void listenForServerMessages() {
		System.out.println("Listening for Server messages...");
		while (true) {
			// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!
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
				System.out.println("ServerListener received join message of: " + packet.getLength() + " bytes");
				if (((JoinMessage) message).isReply()) {
					sendMessage(m_clientSocket, message.getAddress(), message.getPort(), message);
				} else if (!((JoinMessage) message).isReply()) {
					((JoinMessage) message).setMayJoin(true);
					sendMessage(m_serverSocket, packet.getAddress(), packet.getPort(), message);
					
					//Now that the serverListener is up we can start the clientListener
					final InetAddress address = message.getAddress();
					final int port = message.getPort();
					
					clientThread = new Thread(new Runnable() {
						public void run() {
							clientListener.listenForClientMessages(address, port);
						}
					});
					clientThread.start();
				}
			} else {
				sendMessage(m_clientSocket, message.getAddress(), message.getPort(), message);
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

		//THIS BLOCK IS FOR TESTING. REMOVE FOR LIGHTWEIGHT FE-----------------
		if (message instanceof JoinMessage)
			System.out.println("Sent join message");
		else if (message instanceof DrawMessage)
			System.out.println("Sent draw message");
		else if (message instanceof RemoveMessage)
			System.out.println("Sent remove message");
		else if (message instanceof LeaveMessage)
			System.out.println("Sent leave message");
		//---------------------------------------------------------------------
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
	
	private DatagramSocket getServerSocket() {
		return m_serverSocket;
	}
	
	private DatagramSocket getClientSocket() {
		return m_clientSocket;
	}
	
	private void setServerSocket(DatagramSocket socket) {
		m_serverSocket = socket;
	}
	
	private void setClientSocket(DatagramSocket socket) {
		m_clientSocket = socket;
	}
}
