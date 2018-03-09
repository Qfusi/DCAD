package Message;

import java.net.InetAddress;
import java.util.UUID;

public class NewActiveServerMessage extends Message{
	private boolean m_isOkay = false;
	public NewActiveServerMessage(InetAddress address, int port) {
		m_address = address;
		m_port = port;
	}
	@Override
	public Object getObj() {
		return null;
	}
	public boolean getisOkay() {
		return m_isOkay;
	}
	public void setOkay(boolean isOkay) {
		m_isOkay = isOkay;
	}
}