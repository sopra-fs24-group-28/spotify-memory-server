package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    public InMemoryGameRepository inMemoryGameRepository;

     public Game createGame(GameParameters gameParameters) {
         User host = UserContextHolder.getCurrentUser();
         Game newGame = new Game(gameParameters, host);
         return inMemoryGameRepository.save(newGame);
     }

     public List<Game> getGames() {
         return inMemoryGameRepository.findAll();
     }

     public List<User> addPlayerToGame(Integer gameId) {
         User newUser = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId).orElseThrow();
         game.getPlayers().add(newUser);
         return inMemoryGameRepository.save(game).getPlayers();
     }

     public List<User> removePlayerFromGame(Integer gameId) {
         User userToRemove = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId).orElseThrow();
         if (game.getHostId().equals(userToRemove.getUserId())) {
             inMemoryGameRepository.deleteById(gameId);
             return null;
         } else {
             game.getPlayers().remove(userToRemove);
             return inMemoryGameRepository.save(game).getPlayers();
         }
     }
}
