package forum.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
public class Category {
    private Integer id;
    private String name;
    private String picture;
    private Set<Forum> forums;

    public Category(){
        this.forums=new LinkedHashSet<>();
    }
    public Category(String name) {
        this();
        this.name=name;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "picture")
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @OneToMany(mappedBy = "category")
    public Set<Forum> getForums() {
        return forums;
    }

    public void setForums(Set<Forum> forums) {
        this.forums = forums;
    }
}
