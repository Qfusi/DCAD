package Message;

import java.util.UUID;

//Is send by Replica Server to client to check if client answer and by that definition is actice/not crashed. Client send this messge to answer
public class ClientCheckUpMessage extends Message{

    private boolean m_active;

    public ClientCheckUpMessage(UUID messageID,boolean active) {
        m_messageID = messageID;
        m_active=active;
    }


    public boolean getActive() {
        return m_active;
    }

    public void setActive(boolean active) {
        m_active= active;
    }

    @Override
    public Object getObj() {
        return null;
    }


}