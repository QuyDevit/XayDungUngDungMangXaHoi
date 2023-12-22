package ngocquy.nnq.social_app.Model;

public class Friend {
    String friendBy;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    String userID;
    public boolean isAccept() {
        return isAccept;
    }

    public void setAccept(boolean accept) {
        isAccept = accept;
    }

    boolean isAccept;
    long friendAt;

    public Friend() {
    }

    public String getFriendBy() {
        return friendBy;
    }

    public void setFriendBy(String friendBy) {
        this.friendBy = friendBy;
    }

    public long getFriendAt() {
        return friendAt;
    }

    public void setFriendAt(long friendAt) {
        this.friendAt = friendAt;
    }
}
