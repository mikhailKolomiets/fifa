package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import site.fifa.entity.Habit;


import java.util.List;

@Repository
public interface HabitRepository extends CrudRepository<Habit, Long> {

    List<Habit> getByUserId(Long userId);

}
