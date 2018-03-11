package Message;

public class DisconnectMessage extends Message{

	//DisconnectMessage - Sent to the server when the client clicks the 'x' in the GUI window. 
	//This allows the server to safely remove the client from the list of connected clients
	public DisconnectMessage(int port) {
		m_port = port;
	}

	@Override
	public Object getObj() {
		return null;
	}

}
