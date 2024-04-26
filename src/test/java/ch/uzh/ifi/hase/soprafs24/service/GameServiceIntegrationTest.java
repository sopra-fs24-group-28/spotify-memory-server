package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Playlist;
import ch.uzh.ifi.hase.soprafs24.repository.SpotifyJWTRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory.STANDARDALBUMCOVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@WebAppConfiguration
@SpringBootTest
public class GameServiceIntegrationTest {
    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Qualifier("spotifyJWTRepository")
    @Autowired
    private SpotifyJWTRepository spotifyJWTRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    private User user1;
    private User user2;
    private HashMap<String, String> playlistMetadata = new HashMap<>();

    ArrayList<ArrayList<String>> playlistData = null;
    private GameParameters gameParameters = new GameParameters(5,2,2, STANDARDALBUMCOVER, new Playlist("playlist"),10,1,10,15);

    @BeforeEach
    public void setup() {
        // setup two users with corresponding SpotifyJWTs
        // create first user
        SpotifyJWT spotifyJWT1 = new SpotifyJWT();
        spotifyJWT1.setAccessToken("accessToken");
        spotifyJWT1.setRefreshToken("refreshToken");
        spotifyJWT1.setScope("scope");
        spotifyJWT1.setTokenType("Bearer");
        spotifyJWT1.setExpiresln(3600);

        user1 = new User();
        user1.setSpotifyUserId("id1");
        user1.setUsername("testUsername1");
        userService.createUser(user1);
        userService.loginUser(user1.getSpotifyUserId(), spotifyJWT1);

        // create second user
        SpotifyJWT spotifyJWT2 = new SpotifyJWT();
        spotifyJWT2.setAccessToken("accessToken");
        spotifyJWT2.setRefreshToken("refreshToken");
        spotifyJWT2.setScope("scope");
        spotifyJWT2.setTokenType("Bearer");
        spotifyJWT2.setExpiresln(3600);

        user2 = new User();
        user2.setSpotifyUserId("id2");
        user2.setUsername("testUsername2");
        userService.createUser(user2);
        userService.loginUser(user2.getSpotifyUserId(), spotifyJWT2);

        // add mocked data for playlistMetadata
        playlistMetadata.put("playlist_name", "playlist_name");
        playlistMetadata.put("image_url", "image_url");

        // add mocked data for playlistData
        playlistData = new ArrayList<>() {{
            add(new ArrayList<String>() {{add("song1");add("url");}});
            add(new ArrayList<String>() {{add("song2");add("url");}});
        }};
    }

    @AfterEach
    public void teardown() {
        userRepository.delete(user1);
        userRepository.delete(user2);
    }

    @Test
    public void simulateGame() {
        try (MockedStatic<SpotifyService> mocked = mockStatic(SpotifyService.class)) {
            // mock static SpotifyService functions
            when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(playlistMetadata);
            when(SpotifyService.getPlaylistData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(playlistData);

            // user1 creates game
            UserContextHolder.setCurrentUser(userRepository.findBySpotifyUserId("id1"));
            Game game = gameService.createGame(gameParameters);

            // user2 joins game
            UserContextHolder.setCurrentUser(userRepository.findBySpotifyUserId("id2"));
            gameService.addPlayerToGame(game.getGameId());

            // host starts game
            UserContextHolder.setCurrentUser(userRepository.findBySpotifyUserId("id1"));
            gameService.startGame(game.getGameId());

            // find out who is first and who is second player
            String firstPlayer = userService.findUserByUserId(game.getActivePlayer()).getSpotifyUserId();
            String secondPlayer;
            if(Objects.equals(firstPlayer, "id1")){secondPlayer = "id2";} else {secondPlayer = "id1";}

            // first player makes two selections that don't match
            UserContextHolder.setCurrentUser(userRepository.findBySpotifyUserId(firstPlayer));
            Integer firstCard1 = game.getCardCollection().getCards().get(0).getCardId();
            Integer secondCard1 = game.getCardCollection().getCards().get(1).getCardId();
            if (game.getCardCollection().checkMatch(Arrays.asList(new Integer[]{firstCard1, secondCard1}))){
                secondCard1 = game.getCardCollection().getCards().get(2).getCardId();
            }
            gameService.runTurn(game.getGameId(), firstCard1);
            gameService.runTurn(game.getGameId(), secondCard1);

            // second player makes all matching picks (4 picks to finish the two pairs)
            UserContextHolder.setCurrentUser(userRepository.findBySpotifyUserId(secondPlayer));
            Integer firstCard2 = game.getCardCollection().getCards().get(0).getCardId();
            Integer secondCard2 = game.getCardCollection().getMatchingCards().get(firstCard2).get(0);
            Integer thirdCard2 = game.getCardCollection().getCards().get(1).getCardId();
            if (thirdCard2.equals(secondCard2)){
                thirdCard2 = game.getCardCollection().getCards().get(2).getCardId();
            }
            Integer fourthCard2 = game.getCardCollection().getMatchingCards().get(thirdCard2).get(0);

            gameService.runTurn(game.getGameId(), firstCard2);
            gameService.runTurn(game.getGameId(), secondCard2);
            gameService.runTurn(game.getGameId(), thirdCard2);
            gameService.runTurn(game.getGameId(), fourthCard2);

            assertEquals(game.getGameState(), GameState.FINISHED);
            assertEquals(game.getScoreBoard().get(userRepository.findBySpotifyUserId(secondPlayer).getUserId()), 4);
            assertEquals(game.getScoreBoard().get(userRepository.findBySpotifyUserId(firstPlayer).getUserId()), 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
