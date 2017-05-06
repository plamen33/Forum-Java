package forum.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "forums")
public class Forum {

    private Integer id;
    private String title;
    private String description;
    private Category category;
    private Set<Topic> topics;

    // Constructors:
    public Forum() {
        this.topics =new HashSet<>();
    }

    public Forum(String title, String description, Category category) {

        this.title = title;
        this.description = description;
        this.category = category;
    }

    // getters and setters:
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId(){return id;}
    public void setId(Integer id) {this.id = id;}

    @Column(nullable = false)
    public String getTitle(){return title;}
    public void setTitle(String title){this.title = title;}

    @Column(nullable = false)
    public String getDescription(){return description;}
    public void setDescription(String description){this.description = description;}

    @ManyToOne()
    @JoinColumn(nullable = false, name = "categoryId")
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @OneToMany(mappedBy = "forum")
    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    @Transient
    public String getSummary(){
        return this.getDescription().substring(0, this.getDescription().length() / 2) + "...";
    }


}
