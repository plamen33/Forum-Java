package forum.repository;

import forum.entity.Reply;
import forum.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    List<Reply> findByTopic(Topic topic);
}
