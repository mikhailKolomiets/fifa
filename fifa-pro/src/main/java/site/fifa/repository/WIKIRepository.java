package site.fifa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.fifa.entity.WIKI;

public interface WIKIRepository extends JpaRepository<WIKI, Long> {
}
