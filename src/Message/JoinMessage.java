package Message;

public class JoinMessage extends Message{
	boolean mayJoin = false;
	public JoinMessage() {
	}

	public boolean getMayJoin() {
		return mayJoin;
	}
	
	public void setMayJoin(boolean bool) {
		mayJoin = bool;
	}
	
	@Override
	public Object getObj() {
		return mayJoin;
	}
}