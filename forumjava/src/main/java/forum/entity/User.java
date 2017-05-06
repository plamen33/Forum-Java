package forum.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    private Integer id;

    private String email;

    private String fullName;

    private String password;

    private Set<Role> roles;

    private Set<Topic> topics;

    private String gender;

    private Timestamp regdate;

    private String picture;

    private Set<Reply> replies;

    public User(String email, String fullName, String password, String gender, Timestamp regdate, String picture) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.gender = gender;
        this.regdate = regdate;
        this.picture = picture;

        this.roles = new HashSet<>();
        this.topics = new HashSet<>();
    }

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "email", unique = true, nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "fullName", nullable = false)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "password", length = 60, nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles")
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    @OneToMany(mappedBy = "author")
    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    @Column(name = "gender", nullable = false)
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Column(name = "regdate", nullable = false)
    public Timestamp getRegdate() {
        return regdate;
    }

    public void setRegdate(Timestamp regdate) {
        this.regdate = regdate;
    }

    @Column(name = "picture")
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @OneToMany(mappedBy = "author")
    public Set<Reply> getReplies() {
        return replies;
    }

    public void setReplies(Set<Reply> replies) {
        this.replies = replies;
    }


    @Transient
    public boolean isAdmin() {
        return this.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    @Transient
    public boolean isAuthor(Topic topic) {
        return Objects.equals(this.getId(),
                topic.getAuthor().getId());
    }

    @Transient
    public boolean isAuthorOfReply(Reply reply) {
        return Objects.equals(this.getId(),
                reply.getAuthor().getId());
    }

    @Transient
    public boolean isReplyAuthor (Reply reply) {
        return Objects.equals(this.getId(), reply.getAuthor().getId());
    }
}