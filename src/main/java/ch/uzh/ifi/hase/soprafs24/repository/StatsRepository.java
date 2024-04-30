package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository("statsRepository")
public interface StatsRepository extends JpaRepository<Stats, Long> {

    @Query("SELECT COALESCE(MAX(g.gameId), 1) FROM Stats g")
    Integer findMaxGameID();

    boolean existsByGameId(Integer gameId);

}
