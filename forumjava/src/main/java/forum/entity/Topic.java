package forum.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "topics")
public class Topic {
    private Integer id;

    private String title;

    private String description;

    private User author;

    private Forum forum;

    private Integer visits;

    private Set<Reply> replies;

    private String lastReply;

    public Topic() {
        this.replies = new HashSet<>();
    }

    public Topic(String title, String description, User author, Forum forum, Integer visits) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.forum=forum;
        this.visits = visits;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "forumId")
    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    @Column(name = "visits", nullable = false)
    public Integer getVisits(){ return visits;}
    public void setVisits(Integer visits){this.visits = visits;}

    @OneToMany(mappedBy = "topic")
    public Set<Reply> getReplies() {
        return replies;
    }

    public void setReplies(Set<Reply> replies) {
        this.replies = replies;
    }

    @Column(nullable = false)
    public String getLastReply() {
        return lastReply;
    }

    public void setLastReply(String lastReply) {
        this.lastReply = lastReply;
    }

    @Transient
    public String getSummary(){
        return this.getDescription().substring(0, this.getDescription().length() / 2) + "...";
    }
}
