package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import site.fifa.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByName(String name);

    User findFirstByUserLastIp(String userLastIp);

}
