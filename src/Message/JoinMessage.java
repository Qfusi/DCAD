package Message;
import java.net.DatagramSocket;

public class JoinMessage extends Message{
	DatagramSocket m_socket;
	public JoinMessage(DatagramSocket socket) {
		m_socket = socket;
	}
	
	@Override
	public Object getObj() {
		return m_socket;
	}
	
}
