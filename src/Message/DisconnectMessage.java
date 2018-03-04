package Message;

public class DisconnectMessage extends Message{
	
	public DisconnectMessage(int port) {
		m_port = port;
	}
	
	@Override
	public Object getObj() {
		return null;
	}

}
