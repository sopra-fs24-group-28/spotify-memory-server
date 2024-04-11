package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    public InMemoryGameRepository inMemoryGameRepository;

    public UserRepository userRepository;

     public Game createGame(GameParameters gameParameters, Long hostId) {
         User host = userRepository.findByUserId(hostId); //TODO: needs to be changed to method of retrieving from SecContext
         Game newGame = new Game(gameParameters, host);
         return inMemoryGameRepository.save(newGame);
     }

     public List<Game> getGames() {
         return inMemoryGameRepository.findAll();
     }
}
