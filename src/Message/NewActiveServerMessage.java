package Message;

import java.net.InetAddress;

//Send message after election from prime server to Front End, is needed to configure the port which FE uses to send packages to Server. 
public class NewActiveServerMessage extends Message{
	public NewActiveServerMessage(InetAddress address, int port) {
		m_address = address;
		m_port = port;
	}

	@Override
	public Object getObj() {
		return null;
	}
}