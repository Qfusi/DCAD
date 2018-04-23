package Message;

public class PrimaryServerMessage extends Message{
	private boolean m_isLeader = false;
	
	public PrimaryServerMessage(boolean isLeader) {
		m_isLeader = isLeader;
	}

	public boolean isLeader() {
		return m_isLeader;
	}
	
	@Override
	public Object getObj() {
		return null;
	}
}