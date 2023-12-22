package ngocquy.nnq.social_app.Model;

public class Like {
    String likedby;
    boolean isLike;

    public Like(String likedby, boolean isLike) {
        this.likedby = likedby;
        this.isLike = isLike;
    }

    public Like() {
    }

    public String getLikedby() {
        return likedby;
    }

    public void setLikedby(String likedby) {
        this.likedby = likedby;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }
}
