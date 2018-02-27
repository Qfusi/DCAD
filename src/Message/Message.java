package Message;
import java.io.Serializable;
import java.net.InetAddress;

public abstract class Message implements Serializable{
	private InetAddress address;
	private int port;
	public Message() {
	}
	
	public abstract Object getObj();

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
