package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameConstant;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Turn;
import ch.uzh.ifi.hase.soprafs24.model.game.CardCollection;
import ch.uzh.ifi.hase.soprafs24.model.game.Card;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    private final UserService userService;
    public InMemoryGameRepository inMemoryGameRepository;

     public Game createGame(GameParameters gameParameters) {
         User host = UserContextHolder.getCurrentUser();
         if (host.getState().equals(UserStatus.INGAME)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a game");
         }
         Game newGame = new Game(gameParameters, host);
         addPlayerToGame(newGame, host);
         setPlaylistNameAndURL(newGame);
         return inMemoryGameRepository.save(newGame);
     }

    public Game startGame(Integer gameId) {
        Game currentGame = inMemoryGameRepository.findById(gameId);
        User host = UserContextHolder.getCurrentUser();
        Long hostId = host.getUserId();
        if (currentGame.getPlayers().size() >= GameConstant.getMinPlayers() && Objects.equals(currentGame.getHostId(), hostId)){
            currentGame.setGameState(GameState.ONPLAY);

            randomizePlayersIndex(currentGame);
            createCardCollection(currentGame);
            createScoreBoard(currentGame);
            initiateNewTurn(currentGame);
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

    private void createScoreBoard(Game currentGame){
        List<User> players = currentGame.getPlayers();
        HashMap<Long, Long> scoreBoard = currentGame.getScoreBoard();

        for (User player: players) {
            Long playerId = player.getUserId();
            scoreBoard.put(playerId, 0L);
        }
        currentGame.setScoreBoard(scoreBoard);
        inMemoryGameRepository.save(currentGame);
    }

    private void initiateNewTurn(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Long activePlayer = currentGame.getActivePlayer();
        int activePlayerIndex;

        if (activePlayer == null) {
            activePlayerIndex = 0;
        } else {
            activePlayerIndex = players.indexOf(userService.findUserByUserId(activePlayer));
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

    private void createCardCollection(Game currentGame){
        User host = UserContextHolder.getCurrentUser();
        CardCollection CardCollection = new CardCollection(currentGame.getGameParameters(), host.getSpotifyJWT().getAccessToken());
        currentGame.setCardCollection(CardCollection);
        inMemoryGameRepository.save(currentGame);
    }


     public List<Game> getGames() {
         return inMemoryGameRepository.findAll();
     }

     public Game getGameById(Integer gameId) {return inMemoryGameRepository.findById(gameId);}

     public List<User> addPlayerToGame(Integer gameId) {
         User newUser = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId);
         if (game.getGameState() == GameState.OPEN && game.getPlayers().size() < game.getGameParameters().getPlayerLimit()){
             return addPlayerToGame(game, newUser);
         } else {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to join the game.");
         }
     }

     public List<User> removePlayerFromGame(Integer gameId) {
         User userToRemove = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId);
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

    private Game setPlaylistNameAndURL(Game game) {
         HashMap<String,String> playlistMetadata = SpotifyService.getPlaylistMetadata(
                 UserContextHolder.getCurrentUser().getSpotifyJWT().getAccessToken(),
                 game.getGameParameters().getPlaylist().getPlaylistId()
         );

        game.getGameParameters().getPlaylist().setPlaylistName(playlistMetadata.get("playlist_name"));
        game.getGameParameters().getPlaylist().setPlaylistImageUrl(playlistMetadata.get("image_url"));

        return inMemoryGameRepository.save(game);
    }

    public void runTurn(Integer gameId, int cardId) {
         Game currentGame = inMemoryGameRepository.findById(gameId);
         User currentPlayer = UserContextHolder.getCurrentUser();
         // check IF, Player is activePlayer
        if (Objects.equals(currentGame.getActivePlayer(), currentPlayer.getUserId())) {
            List<Turn> history = currentGame.getHistory();
            Turn currentTurn = history.get(history.size()-1);

            checkValidCardState(gameId, cardId); // IF not, don't do anything.
            currentTurn.getPicks().add(cardId);
            currentGame.getCardCollection().getCardById(cardId).setCardState(CardState.FACEUP);
            // check IF, current picks are matching
            if (currentGame.getCardCollection().checkMatch(currentTurn.getPicks())) {
                // check IF, got all cards
                if (currentTurn.getPicks().size() == currentGame.getGameParameters().getNumOfCardsPerSet()){
                    excludeCards(gameId, currentTurn);
                    winPoints(gameId, currentPlayer.getUserId());
                    // check game to terminate or not. (set gameState.finished / active.set null, 
                    initiateNewTurn(currentGame);
                    inMemoryGameRepository.save(currentGame);
                }
            } else {
                // need event > let user view card contents & wait time.
                initiateNewTurn(currentGame);
                inMemoryGameRepository.save(currentGame);
            }
        }
     }

    private void excludeCards(Integer gameId, Turn turn){
        Game currentGame = inMemoryGameRepository.findById(gameId);
        for (int i = 0 ; i < turn.getPicks().size(); i++) {
            Card card = currentGame.getCardCollection().getCardById(turn.getPicks().get(i));
            card.setCardState(CardState.EXCLUDED);
        }
        inMemoryGameRepository.save(currentGame);
    }

    private void winPoints(Integer gameId, long userId) {
        Game currentGame = inMemoryGameRepository.findById(gameId);
        Long score = currentGame.getScoreBoard().get(userId);
        currentGame.getScoreBoard().put(userId, score + currentGame.getGameParameters().getNumOfCardsPerSet());
        inMemoryGameRepository.save(currentGame);
    }

    private void checkValidCardState(Integer gameId, int cardId){
        Game currentGame = inMemoryGameRepository.findById(gameId);
        Card currentCard = currentGame.getCardCollection().getCardById(cardId);
        if (currentCard.getCardState() != CardState.FACEDOWN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This card is unavailable.");
        }
    }
}