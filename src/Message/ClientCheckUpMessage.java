package Message;


import java.util.UUID;



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
        // TODO Auto-generated method stub
        return null;
    }


}