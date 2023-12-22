package ngocquy.nnq.social_app.Model;

public class Follow {

    String followedBy;
    long followedAt;

    public Follow() {
    }

    public void setFollowedBy(String followedBy) {
        this.followedBy = followedBy;
    }

    public long getFollowedAt() {
        return followedAt;
    }
    public String getFollowedBy() {
        return followedBy;
    }
    public void setFollowedAt(long followedAt) {
        this.followedAt = followedAt;
    }
}
