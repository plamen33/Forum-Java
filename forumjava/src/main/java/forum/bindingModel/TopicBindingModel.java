package forum.bindingModel;

import javax.validation.constraints.NotNull;

public class TopicBindingModel {
    @NotNull
    private String title;

    @NotNull
    private String description;
    @NotNull
    private Integer forumId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getForumId() {
        return forumId;
    }

    public void setForumId(Integer forumId) {
        this.forumId = forumId;
    }
}
