package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("StatisticsRepository")
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    Statistics findallbyUserId(Long userId);

}
