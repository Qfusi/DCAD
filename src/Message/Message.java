package Message;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

public abstract class Message implements Serializable{
	protected InetAddress m_address;
	protected int m_port;
	protected UUID m_messageID;
	public Message() {
	}
	
	public abstract Object getObj();

	public InetAddress getAddress() {
		return m_address;
	}

	public void setAddress(InetAddress address) {
		this.m_address = address;
	}
	
	public int getPort() {
		return m_port;
	}

	public void setPort(int port) {
		this.m_port = port;
	}
	
	public UUID getMessageID() {
		return m_messageID;
	}
}
