package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameConstant;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Turn;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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

    public Game startGame(Integer gameId, User host) {
        Game currentGame = inMemoryGameRepository.findById(gameId);
        Long hostId = host.getUserId();
        if (currentGame.getPlayers().size() >= GameConstant.getMinPlayers() && Objects.equals(currentGame.getHostId(), hostId)){
            currentGame.setGameState(GameState.ONPLAY);

            // randomizePlayersIndex(currentGame) * if needed
            // createCardCollection(currentGame.getGameParameters());
            createScoreBoard(currentGame);
            runTurn(currentGame);
            return inMemoryGameRepository.save(currentGame);
        } else {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to start the game.");
        }
    }

    public void randomizePlayersIndex(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Collections.shuffle(players); // Set players List to random order.
        currentGame.setPlayers(players);
        inMemoryGameRepository.save(currentGame);
    }

    public void createScoreBoard(Game currentGame){
        List<User> players = currentGame.getPlayers();
        HashMap<Long, Long> scoreBoard = currentGame.getScoreBoard();

        for (User player: players) {
            Long playerId = player.getUserId();
            scoreBoard.put(playerId, 0L);
        }
        currentGame.setScoreBoard(scoreBoard);
    }

    public void runTurn(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Long activePlayer = currentGame.getActivePlayer();
        int activePlayerIndex;

        if (activePlayer == null) {
            activePlayerIndex = 0;
        } else {
            activePlayerIndex = players.indexOf(userRepository.findByUserId(activePlayer));
            activePlayerIndex++;
            if (activePlayerIndex == players.size()){
                activePlayerIndex = 0;
            }
        }

        currentGame.setActivePlayer(players.get(activePlayerIndex).getUserId());
        Turn turn = new Turn(activePlayer);
        currentGame.getHistory().add(turn);
        inMemoryGameRepository.save(currentGame);
    }



}
