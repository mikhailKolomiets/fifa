package site.fifa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import site.fifa.entity.IpCounter;

import java.time.LocalDateTime;

public interface IpCounterRepository extends CrudRepository<IpCounter, Long> {

    IpCounter getLastByIp(String ip);

    @Query(nativeQuery = true, value = "select * from ip_counter ic where ic.ip = :ip and ic.last_enter > :day limit 1")
    IpCounter findTodayByIp(@Param("ip") String ip, @Param("day") LocalDateTime day);

    @Query(nativeQuery = true, value = "select count(*) from ip_counter ip where ip.last_enter > :time")
    Long countAllAfter(@Param("time") LocalDateTime time);
}
