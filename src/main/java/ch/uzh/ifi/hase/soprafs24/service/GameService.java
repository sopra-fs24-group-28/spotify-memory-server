package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameConstant;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Turn;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
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

    private final UserService userService;
    public InMemoryGameRepository inMemoryGameRepository;

    public UserRepository userRepository;

     public Game createGame(GameParameters gameParameters) {
         User host = UserContextHolder.getCurrentUser(); //TODO: needs to be changed to method of retrieving from SecContext
         Game newGame = new Game(gameParameters, host);
         return inMemoryGameRepository.save(newGame);
     }

    public Game startGame(Integer gameId) {
        Game currentGame = inMemoryGameRepository.findById(gameId).orElseThrow();
        User host = UserContextHolder.getCurrentUser();
        if (currentGame.getPlayers().size() >= GameConstant.getMinPlayers() && Objects.equals(currentGame.getHostId(), host.getUserId())){
            currentGame.setGameState(GameState.ONPLAY);

            // randomizePlayersIndex(currentGame) * if needed
            // createCardCollection(currentGame.getGameParameters());
            createScoreBoard(currentGame);
            activateNewTurn(currentGame.getGameId());
            return inMemoryGameRepository.save(currentGame);
        } else {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to start the game.");
        }
    }

    public void terminateGame(Integer gameId, User host) {
        Game currentGame = inMemoryGameRepository.findById(gameId).orElseThrow();
        Long hostId = host.getUserId();
        if (Objects.equals(currentGame.getHostId(), hostId)){
            inMemoryGameRepository.deleteById(currentGame.getGameId());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to terminate the game.");
        }
    }

    public Game addPlayer(Integer gameId, User user){
        Game currentGame = inMemoryGameRepository.findById(gameId).orElseThrow();
        List<User> currentPlayers = currentGame.getPlayers();

        if (currentPlayers.contains(user)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user is already in the game.");
        }

        if (currentGame.getGameParameters().getPlayerLimit() > currentPlayers.size()){
            currentPlayers.add(user);
        } else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The game is full. Unable to join the game.");
        }
        return inMemoryGameRepository.save(currentGame);
    }

    public Game removePlayer(Integer gameId, User user){
        Game currentGame = inMemoryGameRepository.findById(gameId).orElseThrow();
        List<User> currentPlayers = currentGame.getPlayers();

        if (currentPlayers.contains(user)){
            currentPlayers.remove(user);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user is not in the game.");
        }
        return inMemoryGameRepository.save(currentGame);
    }

    public void addPicks(Integer gameId, Integer cardId) {
        User user = UserContextHolder.getCurrentUser();
        Game currentGame = inMemoryGameRepository.findById(gameId).orElseThrow();
        Turn currentTurn = currentGame.getHistory().get(currentGame.getHistory().size() - 1);

        if (user.getUserId() == currentTurn.getUserId()) {
            Boolean turnActive = currentGame.getCardCollection().checkMatch(currentTurn.addPick(cardId));
            if (!turnActive) {
                activateNewTurn(gameId); // If turn unmatched -> get to the next Turn
            }
            else if () {
                
            }
            {
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Access");
            }
        }

    public void createScoreBoard(Game currentGame){
        List<User> players = currentGame.getPlayers();
        HashMap<Long, Long> scoreBoard = currentGame.getScoreBoard();

        for (User player: players) {
            Long playerId = player.getUserId();
            scoreBoard.put(playerId, 0L);
        }
        currentGame.setScoreBoard(scoreBoard);
        inMemoryGameRepository.save(currentGame);
    }

    public void setScore(Turn turn){

    }

    public void activateNewTurn(Integer gameId){
        Game currentGame = inMemoryGameRepository.findById(gameId).orElseThrow();
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

    public List<Game> getGames() {
        return inMemoryGameRepository.findAll();
    }

    public List<User> addPlayerToGame(Integer gameId) {
        User newUser = UserContextHolder.getCurrentUser();
        Game game = inMemoryGameRepository.findById(gameId).orElseThrow();
        return addPlayerToGame(game, newUser);
    }

    public List<User> removePlayerFromGame(Integer gameId) {
        User userToRemove = UserContextHolder.getCurrentUser();
        Game game = inMemoryGameRepository.findById(gameId).orElseThrow();
        userService.setPlayerState(userToRemove, UserStatus.ONLINE);
        if (game.getHostId().equals(userToRemove.getUserId())) {
            inMemoryGameRepository.deleteById(gameId);
            return null;
        } else {
            game.getPlayers().removeIf(u -> u.getUserId().equals(userToRemove.getUserId()));
            return inMemoryGameRepository.save(game).getPlayers();
        }
    }

    private List<User> addPlayerToGame(Game game, User user) {
        userService.setPlayerState(user, UserStatus.INGAME);
        game.getPlayers().add(user);
        return inMemoryGameRepository.save(game).getPlayers();
    }

    public void runTurn(Integer gameId){

    }

    public void randomizePlayersIndex(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Collections.shuffle(players); // Set players List to random order.
        currentGame.setPlayers(players);
        inMemoryGameRepository.save(currentGame);
    }

}
