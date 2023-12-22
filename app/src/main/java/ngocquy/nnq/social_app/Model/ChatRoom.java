package ngocquy.nnq.social_app.Model;

import java.util.ArrayList;
import java.util.Map;

public class ChatRoom {
    String roomID;
    private Map<String, Boolean> members;
    private ArrayList<Message> messages;

    public ChatRoom() {
        // Default constructor required for Firebase
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
