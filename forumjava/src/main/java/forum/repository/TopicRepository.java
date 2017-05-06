package forum.repository;


import forum.entity.Topic;
import forum.entity.Forum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Integer> {
    List<Topic> findByForum(Forum forum);
}
