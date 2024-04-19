package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;

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

    private Game setPlaylistNameAndURL(Game game) {
         HashMap<String,String> playlistMetadata = SpotifyService.getPlaylistMetadata(
                 UserContextHolder.getCurrentUser().getSpotifyJWT().getAccessToken(),
                 game.getGameParameters().getPlaylist().getPlaylistId()
         );

        game.getGameParameters().getPlaylist().setPlaylistName(playlistMetadata.get("playlist_name"));
        game.getGameParameters().getPlaylist().setPlaylistImageUrl(playlistMetadata.get("image_url"));

        return inMemoryGameRepository.save(game);
    }

    public void runTurn(Integer gameId) {

    }
}
