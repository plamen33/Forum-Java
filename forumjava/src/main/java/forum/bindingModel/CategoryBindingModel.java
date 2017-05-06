package forum.bindingModel;


import org.springframework.web.multipart.MultipartFile;

public class CategoryBindingModel {
    private String name;
    private MultipartFile picture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MultipartFile getPicture() {
        return picture;
    }

    public void setPicture(MultipartFile picture) {
        this.picture = picture;
    }
}
