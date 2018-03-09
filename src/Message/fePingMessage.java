package Message;

import java.net.InetAddress;
import java.util.UUID;

// This message exits because the primary server needs to constantly make sure that the FE is still up. 
// If the FE is crashed and rebooted it will not be aware of who is the primary, and thus where to send messages from clients
public class fePingMessage extends Message{
	public fePingMessage(InetAddress address, int port, UUID id) {
		m_address = address;
		m_port = port;
		m_messageID = id;
	}

	@Override
	public Object getObj() {
		return null;
	}
	
}
