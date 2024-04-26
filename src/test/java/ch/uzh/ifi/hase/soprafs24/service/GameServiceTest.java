package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.CardCollection;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Playlist;
import ch.uzh.ifi.hase.soprafs24.repository.StatsRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class GameServiceTest {

    @Mock
    private InMemoryGameRepository inMemoryGameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private SpotifyService spotifyService;

    @InjectMocks
    private GameService gameService;

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private UserContextHolder userContextHolder;

    private User testUser;

    private Game testGame;

    private GameParameters gameParameters;

    private Playlist playlist;

    private static final Logger logger = Logger.getLogger("GameServiceTest");

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setSpotifyUserId("testSpotifyUserId");
        testUser.setUsername("testUsername");
        testUser.setSpotifyJWT(new SpotifyJWT());

        gameParameters = new GameParameters();
        playlist = new Playlist("testPlayerId");
        gameParameters.setPlaylist(playlist);

        testGame = new Game(gameParameters, testUser);

        // when -> any object is being save in the userRepository -> return the dummy
        // testUser
        Mockito.when(inMemoryGameRepository.save(Mockito.any())).thenReturn(testGame);
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }

    @Test
    public void createGame_validInputs_success() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)){
                // create the Host
                testUser.setState(UserStatus.ONLINE);
                Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                Mockito.when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                // assert user status
                Game createdGame = gameService.createGame(gameParameters);

                assertEquals(createdGame.getHostId(), testUser.getUserId());
            }
        }
    }

    @Test
    public void createGame_HostAlreadyInGame_throwsException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)){
                // create the Host
                testUser.setState(UserStatus.INGAME);
                Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                Mockito.when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                // assert user status
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.createGame(gameParameters));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }
    @Test
    public void createGame_GameNotOpen_throwsException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)){
                try(MockedConstruction<Game> mockedNewGame = Mockito.mockConstruction(Game.class,(mock, context) ->
                {when(mock.getGameState()).thenReturn(GameState.ONPLAY);})) {
                    // create the Host
                    testUser.setState(UserStatus.ONLINE);
                    Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                    HashMap<String, String> testPlayList = new HashMap<>();
                    testPlayList.put("playlist_name", "testPlayList");
                    testPlayList.put("image_url", "test_url");
                    Mockito.when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                    // assert user status
                    ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.createGame(gameParameters));
                    assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
                }
            }
        }
    }

    @Test
    public void startGame_validInput_success() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
            try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)){
                try (MockedConstruction<Game> mockedNewGame = Mockito.mockConstruction(Game.class,(mock, context) ->
                {when(mock.getActivePlayer()).thenReturn(null);})){
                    // create the Host
                    testGame.setHostId(testUser.getUserId());
                    User testApponent = new User();
                    testApponent.setUserId(2L);
                    List<User> testPlayers = new ArrayList<>();
                    testPlayers.add(testUser);
                    testPlayers.add(testApponent);
                    testGame.setPlayers(testPlayers);

                    Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                    // assert user status
                    Game startedGame = gameService.startGame(testGame.getGameId());

                    assertEquals(startedGame.getHostId(), testUser.getUserId());
                }
            }
        }
    }

    @Test
    public void startGame_invalidPlayersSize_throwException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
            try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)){
                // create the Host
                testGame.setHostId(testUser.getUserId());
                List<User> testPlayers = new ArrayList<>();
                testPlayers.add(testUser);
                testGame.setPlayers(testPlayers);

                Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // assert user status
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.startGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void startGame_invalidHost_throwException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
            try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)){
                // create the Host
                testGame.setHostId(new User().getUserId());
                List<User> testPlayers = new ArrayList<>();
                testPlayers.add(testUser);
                testPlayers.add(new User());
                testGame.setPlayers(testPlayers);

                Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // assert user status
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.startGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void getGames_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedUserContext = mockStatic(InMemoryGameRepository.class)){
            // create the Host

            List<Game> games = new ArrayList<>();
            games.add(testGame);

            Mockito.when(inMemoryGameRepository.findAll()).thenReturn(games);

            // assert user status
            List<Game> resultGames = gameService.getGames();

            assertEquals(resultGames, games);
        }
    }

    @Test
    public void addPlayerToGame_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedUserContext = mockStatic(InMemoryGameRepository.class)){
            // create the Host

            List<Game> games = new ArrayList<>();
            games.add(testGame);

            Mockito.when(inMemoryGameRepository.findAll()).thenReturn(games);

            // assert user status
            List<Game> resultGames = gameService.getGames();

            assertEquals(resultGames, games);
        }
    }



}
