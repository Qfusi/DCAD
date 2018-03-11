package Message;


//ElectionWinnerMessage - Sent out after an election to alert all servers of the new primary server

public class ElectionWinnerMessage extends Message{
	private int m_ID;

	public ElectionWinnerMessage(int id) {
		m_ID = id;
	}

	public int getID() {
		return m_ID;
	}

	@Override
	public Object getObj() {
		return null;
	}
}