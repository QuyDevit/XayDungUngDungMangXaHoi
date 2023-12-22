package ngocquy.nnq.social_app.Model;

public class Message {
    String description;

    public String getMessBy() {
        return messBy;
    }

    public void setMessBy(String messBy) {
        this.messBy = messBy;
    }

    String messBy;
    long messAt;

    public Message(String description, String messBy, long messAt) {
        this.description = description;
        this.messBy = messBy;
        this.messAt = messAt;
    }


    public Message() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public long getMessAt() {
        return messAt;
    }

    public void setMessAt(long messAt) {
        this.messAt = messAt;
    }
}
