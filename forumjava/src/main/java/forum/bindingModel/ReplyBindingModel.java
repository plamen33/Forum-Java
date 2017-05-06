package forum.bindingModel;


import forum.entity.User;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

public class ReplyBindingModel {

    @NotNull
    @Size(min=1, max=1400)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
