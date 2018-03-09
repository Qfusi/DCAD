package Message;

import java.net.InetAddress;
import java.util.UUID;

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