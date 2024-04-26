package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.*;
import ch.uzh.ifi.hase.soprafs24.repository.SpotifyJWTRepository;
import ch.uzh.ifi.hase.soprafs24.repository.StatsRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.websocket.events.GameChangesEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.logging.Logger;

import static ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory.STANDARDSONG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class GameServiceTest {

    @Mock
    private InMemoryGameRepository inMemoryGameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpotifyJWTRepository spotifyJWTRepository;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User testUser;

    private Game testGame;

    private GameParameters gameParameters;

    private Playlist playlist;

    private SpotifyJWT testSpotifyJWT;

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

        testSpotifyJWT = new SpotifyJWT();

        // when -> any object is being save in the userRepository -> return the dummy
        // testUser
        Mockito.when(inMemoryGameRepository.save(Mockito.any())).thenReturn(testGame);
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
        Mockito.when(userRepository.saveAndFlush(Mockito.any())).thenReturn(testUser);
        Mockito.when(spotifyJWTRepository.save(Mockito.any())).thenReturn(testSpotifyJWT);
        Mockito.when(spotifyJWTRepository.saveAndFlush(Mockito.any())).thenReturn(testSpotifyJWT);
    }

    @Test
    public void createGame_validInputs_success() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
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
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
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
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
                try (MockedConstruction<Game> mockedNewGame = Mockito.mockConstruction(Game.class, (mock, context) ->
                {
                    when(mock.getGameState()).thenReturn(GameState.ONPLAY);
                })) {
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
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)) {
                try (MockedConstruction<Game> mockedNewGame = Mockito.mockConstruction(Game.class, (mock, context) ->
                {
                    when(mock.getActivePlayer()).thenReturn(null);
                })) {
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
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)) {
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
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)) {
                // create the Host
                testGame.setHostId(new User().getUserId());
                List<User> testPlayers = new ArrayList<>();
                testPlayers.add(testUser);
                testPlayers.add(new User());
                testGame.setPlayers(testPlayers);

                Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // throw Exception
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.startGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void getGames_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
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
    public void getGameById_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {

            Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
            Game resultGame = gameService.getGameById(testGame.getGameId());
            assertEquals(resultGame, testGame);
        }
    }

    @Test
    public void addPlayerToGame_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            // create playersList
            List<User> testPlayers = new ArrayList<>();
            testPlayers.add(new User());
            testGame.setPlayers(testPlayers);
            // set Game state Open
            testGame.setGameState(GameState.OPEN);

            GameParameters testGameParameter = testGame.getGameParameters();
            testGameParameter.setPlayerLimit(2);
            testGame.setGameParameters(testGameParameter);

            Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

            // assert user status
            List<User> resultPlayers = gameService.addPlayerToGame(testGame.getGameId());

            assertEquals(resultPlayers, testPlayers);
        }
    }

    @Test
    public void addPlayerToGame_gameNotOpen_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {

            // create playersList
            List<User> testPlayers = new ArrayList<>();
            testPlayers.add(new User());
            testGame.setPlayers(testPlayers);
            // set GameState not open
            testGame.setGameState(GameState.ONPLAY);

            GameParameters testGameParameter = testGame.getGameParameters();
            testGameParameter.setPlayerLimit(2);
            testGame.setGameParameters(testGameParameter);

            Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

            // throw Exception
            ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));
            assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    public void addPlayerToGame_exceedPlayerLimit_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {

            // create playersList
            List<User> testPlayers = new ArrayList<>();
            testPlayers.add(new User());
            testPlayers.add(new User());
            testGame.setPlayers(testPlayers);
            // set GameState not open
            testGame.setGameState(GameState.OPEN);

            GameParameters testGameParameter = testGame.getGameParameters();
            testGameParameter.setPlayerLimit(2);
            testGame.setGameParameters(testGameParameter);

            Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

            // throw Exception
            ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));
            assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    public void removePlayerFromGame_validInput_success_userIsHost() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    // set testUser
                    testUser.setState(UserStatus.INGAME);
                    User testParticipant = new User();
                    testParticipant.setState(UserStatus.INGAME);

                    // set testGame setting
                    List<User> testPlayers = new ArrayList<>();
                    testPlayers.add(testUser);
                    testPlayers.add(testParticipant);
                    testGame.setPlayers(testPlayers);
                    testGame.setHostId(testUser.getUserId());

                    Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                    List<User> resultPlayers = gameService.removePlayerFromGame(testGame.getGameId());

                    // remove Host
                    assertEquals(resultPlayers, null);
                }
            }
        }
    }

    @Test
    public void removePlayerFromGame_validInput_success_userIsNotHost() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    // set testUser
                    testUser.setState(UserStatus.INGAME);
                    User testHost = new User();
                    testHost.setUserId(2L);
                    testHost.setState(UserStatus.INGAME);

                    // set testGame setting
                    List<User> testPlayers = new ArrayList<>();
                    testPlayers.add(testUser);
                    testPlayers.add(testHost);
                    testGame.setPlayers(testPlayers);
                    testGame.setHostId(testHost.getUserId());

                    Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                    List<User> resultPlayers = gameService.removePlayerFromGame(testGame.getGameId());

                    // remove Host
                    assertEquals(resultPlayers.get(0), testHost);
                }
            }
        }
    }

    @Test
    public void runTurn_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)) {
                        try(MockedStatic<ApplicationEventPublisher> mockedEventPublisher = mockStatic(ApplicationEventPublisher.class)) {
                            // set test condition (checkActivePlayer())
                            testGame.setActivePlayer(testUser.getUserId());
                            // set test condition (checkActiveCard())
                            Card testCard = new Card("testSongId", "testImageURL");
                            testCard.setCardId(1);
                            testCard.setCardState(CardState.FACEDOWN);
                            List<Card> testCards = new ArrayList<>();
                            testCards.add(testCard);
                            CardCollection testCardCollection = new CardCollection(gameParameters, "testToken");
                            testCardCollection.setCards(testCards);
                            testGame.setCardCollection(testCardCollection);
                            //set test conditions (runActiveTurn())
                            List<Turn> testHistory = new ArrayList<>();
                            testHistory.add(new Turn(testUser.getUserId()));
                            testGame.setHistory(testHistory);
                            //set test conditions (initiateNewTurn())
                            List<User> testPlayers = new ArrayList<>();
                            testPlayers.add(testUser);
                            testGame.setPlayers(testPlayers);
                            //set test conditions (checkFinished())
                            testGame.setMatchCount(1);
                            GameParameters testGameParameter = testGame.getGameParameters();
                            testGameParameter.setNumOfSets(2);
                            testGame.setGameParameters(testGameParameter);

                            Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                            Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                            Mockito.when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                            doNothing().when(eventPublisher).publishEvent(any());

                            gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                            Mockito.verify(inMemoryGameRepository, Mockito.times(4)).save(Mockito.any());
                            Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(Mockito.any());

                        }
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Test
    public void runTurn_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    try (MockedConstruction<CardCollection> mockedCardCollections = Mockito.mockConstruction(CardCollection.class)) {
                        try(MockedStatic<ApplicationEventPublisher> mockedEventPublisher = mockStatic(ApplicationEventPublisher.class)) {
                            // set test condition (checkActivePlayer())
                            testGame.setActivePlayer(testUser.getUserId());
                            // set test condition (checkActiveCard())
                            Card testCard = new Card("testSongId", "testImageURL");
                            testCard.setCardId(1);
                            testCard.setCardState(CardState.FACEDOWN);
                            List<Card> testCards = new ArrayList<>();
                            testCards.add(testCard);
                            CardCollection testCardCollection = new CardCollection(gameParameters, "testToken");
                            testCardCollection.setCards(testCards);
                            testGame.setCardCollection(testCardCollection);
                            //set test conditions (runActiveTurn())
                            List<Turn> testHistory = new ArrayList<>();
                            testHistory.add(new Turn(testUser.getUserId()));
                            testGame.setHistory(testHistory);
                            //set test conditions (initiateNewTurn())
                            List<User> testPlayers = new ArrayList<>();
                            testPlayers.add(testUser);
                            testGame.setPlayers(testPlayers);
                            //set test conditions (checkFinished())
                            testGame.setMatchCount(1);
                            GameParameters testGameParameter = testGame.getGameParameters();
                            testGameParameter.setNumOfSets(2);
                            testGame.setGameParameters(testGameParameter);

                            Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                            Mockito.when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                            Mockito.when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                            doNothing().when(eventPublisher).publishEvent(any());

                            gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                            Mockito.verify(inMemoryGameRepository, Mockito.times(4)).save(Mockito.any());
                            Mockito.verify(eventPublisher, Mockito.times(2)).publishEvent(Mockito.any());

                        }
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
