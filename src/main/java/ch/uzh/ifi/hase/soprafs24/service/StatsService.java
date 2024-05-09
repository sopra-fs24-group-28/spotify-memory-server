package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import ch.uzh.ifi.hase.soprafs24.repository.StatsRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserStatsDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;

@Service
@Transactional
@AllArgsConstructor
public class StatsService {

    private StatsRepository statsRepository;

    public Integer getLatestGameId(){
        return statsRepository.findMaxGameID();
    }

    public Stats saveStats(Stats stats){
        return statsRepository.saveAndFlush(stats);
    }

    public UserStatsDTO getCurrentUserStats() {
        Long userId = UserContextHolder.getCurrentUser().getUserId();
        Integer totalGames = statsRepository.countByUserId(userId);
        Integer gamesWon = statsRepository.countByUserIdAndWinIsTrue(userId);
        Integer gamesLoss = statsRepository.countByUserIdAndLossIsTrue(userId);
        Integer gamesAborted = statsRepository.countByUserIdAndAbortedIsTrue(userId);
        Long totalSetsWon = statsRepository.sumSetsWonByUserId(userId);
        if (totalGames == gamesWon + gamesLoss + gamesAborted) {
            return new UserStatsDTO(userId, totalGames, gamesWon, gamesLoss, gamesAborted, totalSetsWon);
        } else {
            throw new PersistenceException("Error in the number of games");
        }


    }

}
