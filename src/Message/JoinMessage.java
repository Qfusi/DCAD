package Message;

import java.util.ArrayList;
import DCAD.GObject;

public class JoinMessage extends Message{
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	private boolean mayJoin = false;
	private boolean isReply = false;
	public JoinMessage() {
	}

	public boolean getMayJoin() {
		return mayJoin;
	}
	
	public void setMayJoin(boolean bool) {
		mayJoin = bool;
	}
	
	public ArrayList<GObject> getList() {
		return m_GObjects;
	}
	
	public void setList(ArrayList<GObject> list) {
		m_GObjects = list;
	}
	
	public boolean isReply() {
		return isReply;
	}

	public void setReply(boolean isReply) {
		this.isReply = isReply;
	}
	
	@Override
	public Object getObj() {
		return mayJoin;
	}
}