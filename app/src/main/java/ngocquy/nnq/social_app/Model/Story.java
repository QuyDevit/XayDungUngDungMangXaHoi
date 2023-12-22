package ngocquy.nnq.social_app.Model;

import java.util.ArrayList;

public class Story {
    String storyBy;
    long sotryAt;
    ArrayList<UserStories> stories;

    public Story() {
    }

    public String getStoryBy() {
        return storyBy;
    }

    public void setStoryBy(String storyBy) {
        this.storyBy = storyBy;
    }

    public long getSotryAt() {
        return sotryAt;
    }

    public void setSotryAt(long sotryAt) {
        this.sotryAt = sotryAt;
    }

    public ArrayList<UserStories> getStories() {
        return stories;
    }

    public void setStories(ArrayList<UserStories> stories) {
        this.stories = stories;
    }
}
