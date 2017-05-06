package forum.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import forum.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
}