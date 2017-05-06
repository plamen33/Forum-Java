package forum.repository;

import forum.entity.Category;
import forum.entity.Forum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumRepository extends JpaRepository<Forum, Integer> {
    Forum findById(Integer id);
    List<Forum> findByCategory(Category category);
}
