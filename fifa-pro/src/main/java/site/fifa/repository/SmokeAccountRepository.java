package site.fifa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.fifa.entity.SmokeAccount;

public interface SmokeAccountRepository extends JpaRepository<SmokeAccount, Long> {

    SmokeAccount findFirstByUserId(Long userId);

}
