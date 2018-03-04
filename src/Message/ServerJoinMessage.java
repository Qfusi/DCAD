package Message;

public class ServerJoinMessage extends Message{
	private int m_ID;
	private boolean m_isLeader = false;
	
	public ServerJoinMessage(int id, boolean isLeader) {
		m_ID = id;
		m_isLeader = isLeader;
	}

	public int getID() {
		return m_ID;
	}

	public void setID(int m_ID) {
		this.m_ID = m_ID;
	}

	public boolean isLeader() {
		return m_isLeader;
	}

	public void setLeader(boolean isLeader) {
		this.m_isLeader = isLeader;
	}
	
	@Override
	public Object getObj() {
		return null;
	}
}