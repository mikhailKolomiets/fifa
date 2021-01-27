package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import site.fifa.entity.Stadium;

public interface StadiumRepository extends CrudRepository<Stadium, Long> {
}
