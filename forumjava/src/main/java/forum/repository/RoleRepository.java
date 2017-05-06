package forum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import forum.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}