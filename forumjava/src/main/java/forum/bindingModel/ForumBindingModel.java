package forum.bindingModel;

import javax.validation.constraints.NotNull;

public class ForumBindingModel {
    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private Integer categoryId;

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
    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}
