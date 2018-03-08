package Message;

public class ServerPingMessage extends Message{
	private int m_ID;
	private boolean m_isReply = false;
	
	public ServerPingMessage(int id, boolean isReply) {
		m_ID = id;
		m_isReply = isReply;
	}

	public int getID() {
		return m_ID;
	}

	public void setID(int m_ID) {
		this.m_ID = m_ID;
	}

	public boolean isReply() {
		return m_isReply;
	}

	public void setReply(boolean isLeader) {
		this.m_isReply = isLeader;
	}
	
	@Override
	public Object getObj() {
		return null;
	}
}