package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("statsRepository")
public interface StatsRepository extends JpaRepository<Stats, Long> {

    @Query("SELECT COALESCE(MAX(g.gameId), 1) FROM Stats g")
    Integer findMaxGameID();

    List<Stats> findByGameId(Integer gameId);

    List<Stats> findByUserId(Long userId);

    Integer countByUserId(Long userId);

    Integer countByUserIdAndWinIsTrue(Long userId);

    Integer countByUserIdAndLossIsTrue(Long userId);

    Integer countByUserIdAndAbortedIsTrue(Long userId);;

    @Query("SELECT SUM(s.setsWon) FROM Stats s WHERE s.userId = :userId")
    Long sumSetsWonByUserId(@Param("userId") Long userId);
}
