package Message;

import java.util.ArrayList;
import java.util.UUID;

import DCAD.GObject;

public class ConnectMessage extends Message{
	private ArrayList<GObject> m_GObjects  = new ArrayList<GObject>();
	private boolean mayJoin = false;
	private boolean isReply = false;
	public ConnectMessage(UUID messageID) {
		m_messageID = messageID;
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