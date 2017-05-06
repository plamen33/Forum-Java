package forum.entity;


import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name="replies")
public class Reply {

    private Integer id;
    private String message;
    private Timestamp datePosted;
    private Timestamp dateUpdated;
    private Topic topic;
    private User author;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(name = "datePosted", nullable = false)
    public Timestamp getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(Timestamp datePosted) {
        this.datePosted = datePosted;
    }

    @Column(name = "dateUpdated", nullable = false)
    public Timestamp getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Timestamp dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    @ManyToOne
    @JoinColumn(nullable = false, name="topicId")
    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Reply() {
    }

    public Reply(String message, Timestamp datePosted, Timestamp dateUpdated, Topic topic, User author) {
        this.message = message;
        this.datePosted = datePosted;
        this.dateUpdated = dateUpdated;
        this.topic = topic;
        this.author = author;
    }






}
