package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import ch.uzh.ifi.hase.soprafs24.repository.StatsRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class StatsService {

    private StatsRepository statsRepository;

    public Integer getNewGameStatsId(){
        Integer newGameId = statsRepository.findMaxGameID();
        newGameId++;
        return newGameId;
    }

    public Boolean checkGameIdExist(Integer gameId){
        return statsRepository.existsByGameId(gameId);
    }

    public Integer getLatestGameId(){
        return statsRepository.findMaxGameID();
    }

    public Stats saveStats(Stats stats){
        return statsRepository.saveAndFlush(stats);
    }

}
