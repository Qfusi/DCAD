package Server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import Message.JoinMessage;
import Message.MessageConvertion;

public class ReplicaServer {

	private ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
	private DatagramSocket m_socket;
	private String clientMessage="";
	private String [] clientMessageArray;
	private String serverMessage="";
	private InetAddress clientAdress=null;
	private int clientPort=0;
	private String ClientList;
	private String privateMessage="";

	private long startTime;

	public static void main(String[] args){
		if(args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		try {
			ReplicaServer instance = new ReplicaServer(readFile(Integer.parseInt(args[0])));
			
			instance.listenForClientMessages();
		} catch(NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	//--------------------------------------------------------------------------------------

	private ReplicaServer(int portNumber) {
		// TODO: create a socket, attach it to port based on portNumber, and assign it to m_socket		DONE
		try {
			m_socket = new DatagramSocket(portNumber);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//--------------------------------------------------------------------------------------

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		startTime=System.currentTimeMillis();

		
		//---------------------------TRYING TO DO THIS HERE------------------------------
		//				~Jonathan
		
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
		
		try {
			clientAdress = InetAddress.getByName(list.get(0).split(" ")[0]);
			clientPort = Integer.parseInt(list.get(0).split(" ")[1]);
		} catch (UnknownHostException e3) {
			e3.printStackTrace();
		}
		m_s.close();
		
		byte[] b = null;
		
		try {
			b = MessageConvertion.serialize(new JoinMessage());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(b, b.length, clientAdress, clientPort);
		try {
			m_socket.send(packet);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//----------------------------------------------------------------------------
		do {

			byte[] buf= new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, buf.length);
			try {// all parts important to enable the server to answer
				m_socket.receive(dp);
				clientMessage = new String(dp.getData(),0,dp.getLength());
				clientAdress= dp.getAddress();
				clientPort= dp.getPort();


			} catch (IOException e) {
				System.err.println("Server: didnt recive message or translate to string");
				System.exit(-1);
			}
			clientMessageArray = clientMessage.split(" ");
			if(clientMessageArray.length>=2) {// checking if user has send a message with anything in it. /should maybe be done on client side before sending.!!!!!!!!!!!!




				//-------------------------------------------------COMMANDS? ----------------------------------------------------------------
				//reading commands and acting on it



				//HANDSHAKE! --------------------------------------------------------------------------
				if(clientMessageArray[1].equals("/connect")) {

					//---------------------------connection process--------------------------------------
					if(addClient(clientMessageArray[0],clientAdress,clientPort)) {

						serverMessage="connecting";
						sendPrivateMessage(serverMessage, clientMessageArray[0]);
						broadcast(clientMessageArray[0]+" joined the chatroom ");
					}
				}
			}
			

			else {//connection established, ready for use
				if(clientMessageArray[1].equals("/connect")) {//for clients trying to use already taken user name				?????

					serverMessage="cant connect, name already in use, by you ";
					byte[] x= (serverMessage).getBytes();
					DatagramPacket dpSend= new DatagramPacket(x, x.length, clientAdress, clientPort);
					try {
						m_socket.send(dpSend);																// kan inte skicka med sendPrivate... för att den inte finns med i listan
						//testing
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.err.println("Something worong with sending first message.");
					}
				}

				//-------------------------------------------PRIVATE MESSAGE---------------------------------------------------			
				else if (clientMessageArray[1].equals("/tell")) {
					if(!checkIfConnected(clientMessageArray[2])){//if receivername is not registered in server
						privateMessage="username not found, try /list commando";
						sendPrivateMessage(privateMessage, clientMessageArray[0]);


					}
					else {

						privateMessage="";
						//putting togheter message from array elements.
						for(int i=3; i<clientMessageArray.length;i++) {
							privateMessage=privateMessage+" "+ clientMessageArray[i];
						}
						serverMessage= clientMessageArray[0]+ ": "+ privateMessage;//name of sender + message ->for receiver
						sendPrivateMessage(serverMessage, clientMessageArray[2]); //to receiver
						serverMessage= clientMessageArray[2]+ ": "+ privateMessage;//name of receiver + message -> for sender
						sendPrivateMessage(serverMessage, clientMessageArray[0]); //to sender
					}
				}
				//------------------------------------------PRINT LIST-----------------------------------------------------------			
				else if (clientMessageArray[1].equals("/list")) {

					updateUserList();
					sendPrivateMessage(ClientList, clientMessageArray[0]);

				}
				//-----------------------------------------leaving server---------------------------------			
				else if (clientMessageArray[1].equals("/leave")) {

					removeUser(clientMessageArray[0]);
				}
				else if (clientMessageArray[1].equals("/answer")) {


					ClientConnection c;
					for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {

						c = itr.next();
						if(c.hasName(clientMessageArray[0])) {
							c.setActiveStatus(true);
						}
					}
				}
				else {
					String broadMessage="";
					for(int i=1; i<clientMessageArray.length;i++) {
						broadMessage=broadMessage+" "+ clientMessageArray[i];
					}
					//	serverMessage= clientMessageArray[0]+ ": "+ broadMessage;//name of sender + message ->for receiver
					broadcast(clientMessageArray[0]+": "+ broadMessage);
				}
				//--------------------------------------------------------------------------------------

			}


			kickInactiveClients();//checks after 20 seconds if anyone crashed
		} while (true);
	}

	public boolean addClient(String name, InetAddress address, int port) {


		ClientConnection c;
		for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {

			c = itr.next();
			if(c.hasName(name)) {

				return false; // Already exists a client with this name


			}
		}
		m_connectedClients.add(new ClientConnection(name, address, port));



		return true;
	}

	public void sendPrivateMessage(String message, String name) {
		ClientConnection c;
		for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();

			if(c.hasName(name)) {
				c.sendMessage(message, m_socket,10);
			}
		}
	}

	public void broadcast(String message) {


		for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message, m_socket,10);
		}
	}
	private void updateUserList() {
		ClientConnection c;
		ClientList="Users on server: ";
		for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {

			c = itr.next();
			ClientList= ClientList+ c.getName()+ ", " ;

		}
	}
	private void removeUser(String name) {
		ClientConnection c;
		for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();

			if(c.hasName(name)) {
				broadcast(clientMessageArray[0]+" left the chatroom ");
				m_connectedClients.remove(c);


				return;

			}
		}

	}
	//needed for checking if user is on list or not without putting him/her on list if not
	private Boolean checkIfConnected(String name) {
		ClientConnection c;
		for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {

			c = itr.next();
			if(c.hasName(name)) {
				return true; // client is connected

			}
		}
		return false;// client not connected
	}
	private void kickInactiveClients() {

		if(startTime+20000<System.currentTimeMillis()) {

			if(m_connectedClients.size()==0) {//check if there is a client on server at all, even if not needed in this version
				startTime=System.currentTimeMillis();//do nothing while no clients registered, reset timer
			}
			else{


				ClientConnection c;
				for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {

					c = itr.next();


					if(!c.getActiveStatus()) { //if false, not active, meaning crashed
						broadcast(c.getName()+" was kicked because of crash");
						m_connectedClients.remove(c);
						break;
					}
				}


				String checkingClientsMessage="veryComplicatedWordSoNoUserWritesThisInChatAndActivatesTheProtocol";
				broadcast(checkingClientsMessage);
				startTime=System.currentTimeMillis();
				//set all current clients to false
				ClientConnection c2;
				for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {

					c2 = itr.next();
					c2.setActiveStatus(false);
				}
			}
		}
	}
	
	private static int readFile(int id) {
		Scanner m_s = null;
		ArrayList<String> list = new ArrayList<String>();
		int i;
		try {
			m_s = new Scanner(new FileReader("resources/ServerConfig"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (m_s.hasNextLine()) {
			list.add(m_s.nextLine());
		}
		
		i = Integer.parseInt(list.get(id).split(" ")[1]);
		
		System.out.println(i);
		return i;
	}
}

