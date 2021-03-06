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

import Message.*;

// 2 threads, one to handled connection to servers side and one for client/Cad side. Before FE can send messages to servers it needs a message from prime server where to send. Reads from file and sets socket from that information
public class FrontEnd {
	static Thread serverThread;
	static Thread clientThread;
	static FrontEnd serverListener;
	static FrontEnd clientListener;
	private DatagramSocket m_clientSocket;
	private DatagramSocket m_serverSocket;

	//--------Active Server
	private int m_serverPort;
	private InetAddress m_serverAddress;

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

		clientThread = new Thread(new Runnable() {
			public void run() {
				clientListener.listenForClientMessages();
			}
		});
		clientThread.start();
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

		while (true) {
			// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!
			byte[] buf = new byte[2048];
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

			message.setAddress(packet.getAddress());
			message.setPort(packet.getPort());

			if (m_serverSocket != null && m_serverAddress != null && m_serverPort != 0) {
				if (message instanceof ConnectMessage) {
					sendMessage(m_serverSocket, getServerAddress(), getServerPort(), message);
				} else if (message instanceof DrawMessage) {
					sendMessage(m_serverSocket, getServerAddress(), getServerPort(), message);
				} else if (message instanceof RemoveMessage) {
					sendMessage(m_serverSocket, getServerAddress(), getServerPort(), message);
				} else if (message instanceof DisconnectMessage) {
					sendMessage(m_serverSocket, getServerAddress(), getServerPort(), message);
				} else if (message instanceof AckMessage) {
					sendMessage(m_serverSocket, getServerAddress(), getServerPort(), message);
				} else if (message instanceof ClientCheckUpMessage) {
					sendMessage(m_serverSocket, getServerAddress(), getServerPort(), message);
				}
			}
		}
	}

	public void listenForServerMessages() {

		while (true) {
			// IF EOFEXCEPTION OCCURS THIS BYTE ARRAY HAS BEEN EXCEEDED!!!!!!!!!!!!!!!!
			byte[] buf = new byte[2048];
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

			if (message instanceof NewActiveServerMessage) {
				//prints new prime server after election
				System.out.println("ServerListener received NewActiveServer message");
				System.out.println("-----------------------------------");
				System.out.println("New active Server with port: " + message.getPort());
				System.out.println("-----------------------------------");

				//Updating the server info so that client messages are sent to the right server	
				clientListener.setServerAddress(message.getAddress());
				clientListener.setServerPort(message.getPort());
			}
			else if (message instanceof ConnectMessage) {
				if (((ConnectMessage) message).isReply()) {
					sendMessage(m_clientSocket, message.getAddress(), message.getPort(), message);
				} else if (!((ConnectMessage) message).isReply()) {

				}
			} 
			else if (message instanceof DrawMessage) {
				sendMessage(m_clientSocket, message.getAddress(), message.getPort(), message);
			}
			else if (message instanceof RemoveMessage) {
				sendMessage(m_clientSocket, message.getAddress(), message.getPort(), message);
			}
			else if (message instanceof AckMessage) {
				sendMessage(m_clientSocket, message.getAddress(), message.getPort(), message);
			} 
			else if (message instanceof FEPingMessage) {


				//Updating the server info so that client messages are sent to the right server
				if ((clientListener.getServerAddress() != message.getAddress()) && (clientListener.getServerPort() != message.getPort())) {
					clientListener.setServerAddress(message.getAddress());
					clientListener.setServerPort(message.getPort());
				}
			}
			else if (message instanceof ClientCheckUpMessage) {
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
			if (socket != null)
				socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	private InetAddress getServerAddress() {
		return m_serverAddress;
	}

	private void setServerAddress(InetAddress address) {
		m_serverAddress = address;
	}

	private int getServerPort() {
		return m_serverPort;
	}

	private void setServerPort(int port) {
		m_serverPort = port;
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
