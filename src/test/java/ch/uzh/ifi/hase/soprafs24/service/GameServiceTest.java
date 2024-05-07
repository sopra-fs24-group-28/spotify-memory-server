package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.*;
import ch.uzh.ifi.hase.soprafs24.repository.SpotifyJWTRepository;
import ch.uzh.ifi.hase.soprafs24.repository.StatsRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.websocket.events.GameChangesEvent;
import ch.uzh.ifi.hase.soprafs24.websocket.events.LobbyOverviewChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.logging.Logger;

import static ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory.STANDARDALBUMCOVER;
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
    private StatsService statsService;

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

    private User testOpponent;

    private Game testGame;

    private GameParameters gameParameters;

    private Playlist playlist;

    private SpotifyJWT testSpotifyJWT;

    private Stats testStats;

    private static final Logger logger = Logger.getLogger("GameServiceTest");

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given testUser
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setSpotifyUserId("testSpotifyUserId1");
        testUser.setUsername("testUsername1");
        testUser.setSpotifyJWT(new SpotifyJWT());

        testOpponent = new User();
        testOpponent.setUserId(2L);
        testOpponent.setSpotifyUserId("testSpotifyUserId2");
        testOpponent.setUsername("testUsername2");
        testOpponent.setSpotifyJWT(new SpotifyJWT());

        gameParameters = new GameParameters(2,2,2, STANDARDALBUMCOVER, new Playlist("playlist"),10,1,10,15);
        playlist = new Playlist("testPlayerId");
        gameParameters.setPlaylist(playlist);

        testGame = new Game(gameParameters, testUser);
        testGame.getPlayers().add(testUser);
        testGame.setGameStatsId(3);
        testGame.setScoreBoard(new HashMap<Long, Long>());

        testSpotifyJWT = new SpotifyJWT();
        testStats = new Stats();

        // when -> any object is being saved in the userRepository -> return the dummy
        // testUser
        when(inMemoryGameRepository.save(Mockito.any())).thenReturn(testGame);
        when(userRepository.save(Mockito.any())).thenReturn(testUser);
        when(userRepository.saveAndFlush(Mockito.any())).thenReturn(testUser);
        when(spotifyJWTRepository.save(Mockito.any())).thenReturn(testSpotifyJWT);
        when(spotifyJWTRepository.saveAndFlush(Mockito.any())).thenReturn(testSpotifyJWT);
        when(statsRepository.saveAndFlush(Mockito.any())).thenReturn(testStats);
    }

    @Test
    public void createGame_validInputs_success() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
                // setup Host
                testUser.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                // setup gamePlayList
                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                testPlayList.put("playlist_length", "2");
                when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                Game createdGame = gameService.createGame(gameParameters);

                assertEquals(createdGame.getHostId(), testUser.getUserId());
                assertEquals(createdGame.getGameParameters(), gameParameters);
            }
        }
    }

    @Test
    public void createGame_validInputs_listTooShort_success() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
                // setup Host
                testUser.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                // setup gamePlayList (with length 1, while numSets is 2)
                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                testPlayList.put("playlist_length", "1");
                when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                Game createdGame = gameService.createGame(gameParameters);

                assertEquals(createdGame.getHostId(), testUser.getUserId());
                assertEquals(createdGame.getGameParameters(), gameParameters);
                assertEquals(createdGame.getGameParameters().getNumOfSets(), 1);
            }
        }
    }

    @Test
    public void createGame_HostAlreadyInGame_throwsException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
                // setup Host(INGAME)
                testUser.setState(UserStatus.INGAME);
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                // setup gamePlayList
                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.createGame(gameParameters));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void startGame_validInput_success() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedConstruction<CardCollection> mockedCardCollections = mockConstruction(CardCollection.class)) {
                try (MockedConstruction<Game> mockedNewGame = mockConstruction(Game.class, (mock, context) ->
                {
                    when(mock.getActivePlayer()).thenReturn(null);
                })) {
                    // setup Game
                    testGame.setHostId(testUser.getUserId());
                    testGame.getPlayers().add(testOpponent);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(inMemoryGameRepository.getLatestGameStatsId()).thenReturn(1);
                    when(statsService.getLatestGameId()).thenReturn(2);

                    gameService.startGame(testGame.getGameId());

                    assertEquals(testGame.getMatchCount(), 0);
                    assertEquals(testGame.getGameState(), GameState.ONPLAY);
                    verify(inMemoryGameRepository).save(testGame);
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());
                }
            }
        }
    }

    @Test
    public void startGame_invalidPlayersSize_throwException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedConstruction<CardCollection> mockedCardCollections = mockConstruction(CardCollection.class)) {
                // setup Game (with only 1 player)
                testGame.setHostId(testUser.getUserId());
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.startGame(testGame.getGameId()));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void startGame_invalidHost_throwException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedConstruction<CardCollection> mockedCardCollections = mockConstruction(CardCollection.class)) {
                // setup Game (testOpponent as host)
                testGame.setHostId(testOpponent.getUserId());
                testGame.getPlayers().add(testOpponent);
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.startGame(testGame.getGameId()));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void getGames_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            // set inMemoryGameRepository.findAll return
            List<Game> games = new ArrayList<>();
            games.add(testGame);
            when(inMemoryGameRepository.findAll()).thenReturn(games);

            List<Game> resultGames = gameService.getGames();

            assertEquals(resultGames, games);
        }
    }

    @Test
    public void getGameById_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            // set inMemoryGameRepository.findById return
            when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

            Game resultGame = gameService.getGameById(testGame.getGameId());

            assertEquals(resultGame, testGame);
        }
    }

    @Test
    public void addPlayerToGame_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set Game (state.Open)
                testGame.setGameState(GameState.OPEN);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // set user State
                testOpponent.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testOpponent);

                gameService.addPlayerToGame(testGame.getGameId());

                assertEquals(testOpponent, testGame.getPlayers().get(testGame.getPlayers().size()-1));
                verify(userService).setGameIdForGivenUser(testOpponent, testGame.getGameId());
                verify(userService).setPlayerState(testOpponent, UserStatus.INGAME);
                verify(inMemoryGameRepository).save(testGame);
                verify(eventPublisher, times(2)).publishEvent(Mockito.any());
            }
        }
    }

    @Test
    public void addPlayerToGame_userWithCurrentGameId_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set Game (state.Open)
                testGame.setGameState(GameState.OPEN);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // set user State (already has game)
                testOpponent.setCurrentGameId(1);
                when(UserContextHolder.getCurrentUser()).thenReturn(testOpponent);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_userINGAME_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set Game (state.Open)
                testGame.setGameState(GameState.OPEN);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // set user State (Status.INGAME)
                testOpponent.setState(UserStatus.INGAME);
                when(UserContextHolder.getCurrentUser()).thenReturn(testOpponent);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_exceedPlayerLimit_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set Game (state.Open but full)
                testGame.setGameState(GameState.OPEN);
                testGame.getPlayers().add(new User());
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // set user State
                testOpponent.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testOpponent);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_gameOnPlay_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set Game (state.ONPLAY)
                testGame.setGameState(GameState.ONPLAY);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // set User
                testOpponent.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testOpponent);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_gameFinished_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                // set Game (state.FINISHED)
                testGame.setGameState(GameState.FINISHED);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // set User
                testOpponent.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testOpponent);

                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));

                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void removePlayerFromGame_validInput_userIsHost_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    // set users
                    testUser.setState(UserStatus.INGAME);
                    testOpponent.setState(UserStatus.INGAME);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                    // set games (with user as host)
                    testGame.setHostId(testUser.getUserId());
                    testGame.getPlayers().add(testOpponent);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                    gameService.removePlayerFromGame(testGame.getGameId());

                    verify(userService, times(testGame.getPlayers().size()+1)).setPlayerState(Mockito.any(), Mockito.any());
                    verify(inMemoryGameRepository).deleteById(Mockito.any());
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());
                }
            }
        }
    }

    @Test
    public void removePlayerFromGame_validInput_userIsHostAndAborted_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    // set users
                    testUser.setState(UserStatus.INGAME);
                    testOpponent.setState(UserStatus.INGAME);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                    // set games (with user as host, and ONPLAY)
                    testGame.getPlayers().add(testOpponent);
                    testGame.setHostId(testUser.getUserId());
                    testGame.setGameState(GameState.ONPLAY);
                    testGame.getScoreBoard().put(1L, 2L);
                    testGame.getScoreBoard().put(2L, 2L);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                    gameService.removePlayerFromGame(testGame.getGameId());

                    verify(userService, times(testGame.getPlayers().size()+1)).setPlayerState(Mockito.any(), Mockito.any());
                    verify(inMemoryGameRepository).deleteById(Mockito.any());
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());
                    verify(statsService, times(testGame.getPlayers().size())).saveStats(Mockito.any());
                }
            }
        }
    }

    @Test
    public void removePlayerFromGame_validInput_userIsNotHost_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    // set users
                    testUser.setState(UserStatus.INGAME);
                    testOpponent.setState(UserStatus.INGAME);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                    // set games (testOpponent as host)
                    testGame.getPlayers().add(testOpponent);
                    testGame.setHostId(testOpponent.getUserId());
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                    gameService.removePlayerFromGame(testGame.getGameId());

                    assertEquals(1, testGame.getPlayers().size());
                    assertEquals(testOpponent, testGame.getPlayers().get(testGame.getPlayers().size()-1));
                    verify(userService).setPlayerState(testUser, UserStatus.ONLINE);
                    verify(inMemoryGameRepository).save(Mockito.any());
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());
                }
            }
        }
    }

    @Test
    public void removePlayerFromGame_validInput_userIsNotHostAndAborted_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try (MockedStatic<UserRepository> mockedUserRepository = mockStatic(UserRepository.class)) {
                    // set users
                    testUser.setState(UserStatus.INGAME);
                    testOpponent.setState(UserStatus.INGAME);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                    // set games (testOpponent as host, and ONPLAY)
                    testGame.getPlayers().add(testOpponent);
                    testGame.setHostId(testOpponent.getUserId());
                    testGame.setGameState(GameState.ONPLAY);
                    testGame.getScoreBoard().put(1L, 2L);
                    testGame.getScoreBoard().put(2L, 2L);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(statsService.saveStats(Mockito.any())).thenReturn(new Stats());

                    gameService.removePlayerFromGame(testGame.getGameId());

                    assertEquals(1, testGame.getPlayers().size());
                    assertEquals(testOpponent, testGame.getPlayers().get(testGame.getPlayers().size()-1));
                    verify(userService).setPlayerState(testUser, UserStatus.ONLINE);
                    verify(inMemoryGameRepository).save(Mockito.any());
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());
                    verify(statsService, times(1)).saveStats(Mockito.any());
                }
            }
        }
    }

    @Test
    public void runTurn_validInput_STANDARDALBUMCOVER_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try(MockedStatic<ApplicationEventPublisher> mockedEventPublisher = mockStatic(ApplicationEventPublisher.class)) {
                    // set game variables
                    Card testCard = mock(Card.class);
                    CardCollection testCardCollection = mock(CardCollection.class);
                    testGame.setCardCollection(testCardCollection);
                    testGame.setMatchCount(1);

                    // set game (testUser's turn)
                    testGame.getHistory().add(new Turn(testUser.getUserId()));
                    testGame.getPlayers().add(testOpponent);
                    testGame.setActivePlayer(testUser.getUserId());

                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                    when(testCard.getCardState()).thenReturn(CardState.FACEDOWN);
                    when(testCardCollection.getCards()).thenReturn(new ArrayList<>(Arrays.asList(testCard)));
                    doNothing().when(eventPublisher).publishEvent(any());

                    gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                    assertEquals(testOpponent.getUserId(), testGame.getActivePlayer());
                    verify(inMemoryGameRepository, times(2)).save(Mockito.any());
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());
                    verify(userService, times(0)).findUserByUserId(Mockito.any());

                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void runTurn_validInput_STANDARDSONG_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try(MockedStatic<ApplicationEventPublisher> mockedEventPublisher = mockStatic(ApplicationEventPublisher.class)) {
                    try(MockedStatic<UserService> mockedUserService = mockStatic(UserService.class)) {
                        try(MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
                            // set game variables (with STANDARDSONG)
                            testGame.getGameParameters().setGameCategory(STANDARDSONG);
                            Card testCard = mock(Card.class);
                            CardCollection testCardCollection = mock(CardCollection.class);
                            testGame.setCardCollection(testCardCollection);
                            testGame.setMatchCount(1);

                            // set game (testUser's turn)
                            testGame.getHistory().add(new Turn(testUser.getUserId()));
                            testGame.setActivePlayer(testUser.getUserId());
                            testGame.getPlayers().add(testOpponent);

                            when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                            when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                            when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                            when(testCard.getCardState()).thenReturn(CardState.FACEDOWN);
                            when(testCardCollection.getCards()).thenReturn(new ArrayList<>(Arrays.asList(testCard)));
                            doNothing().when(eventPublisher).publishEvent(any());
                            when(userService.findUserByUserId(Mockito.any())).thenAnswer(new Answer<User>() {
                                private int count = 0;
                                @Override
                                 public User answer(InvocationOnMock invocationOnMock) throws Throwable {
                                     return testGame.getPlayers().get(count++);
                                 }
                             });
                            doNothing().when(SpotifyService.class);
                            SpotifyService.setSong(any(), any(), any());

                            gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                            assertEquals(testOpponent.getUserId(), testGame.getActivePlayer());
                            verify(inMemoryGameRepository, times(2)).save(Mockito.any());
                            verify(eventPublisher, times(2)).publishEvent(Mockito.any());
                            verify(userService, times(2)).findUserByUserId(Mockito.any());

                        }
                        catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void runTurn_validInput_cardsMatched_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try(MockedStatic<ApplicationEventPublisher> mockedEventPublisher = mockStatic(ApplicationEventPublisher.class)) {
                    // set game variables
                    Card testCard = mock(Card.class);
                    CardCollection testCardCollection = mock(CardCollection.class);
                    testGame.setCardCollection(testCardCollection);
                    testGame.setMatchCount(0);
                    testGame.getScoreBoard().put(1L, 2L);

                    // set game (testUser's turn)
                    Turn testTurn = new Turn(testUser.getUserId());
                    testGame.setActivePlayer(testUser.getUserId());
                    testGame.getPlayers().add(testOpponent);

                    // set handleMatch
                    testTurn.getPicks().add(1);
                    testGame.getHistory().add(testTurn);
                    when(testGame.getCardCollection().checkMatch(Mockito.any())).thenReturn(true);

                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                    when(testCard.getCardState()).thenReturn(CardState.FACEDOWN);
                    when(testCardCollection.getCards()).thenReturn(new ArrayList<>(Arrays.asList(testCard)));
                    doNothing().when(eventPublisher).publishEvent(any());
                    when(statsService.saveStats(Mockito.any())).thenReturn(new Stats());
                    when(userService.findUserByUserId(Mockito.any())).thenReturn(testUser);

                    gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                    assertEquals(4L, testGame.getScoreBoard().get(1L));
                    assertEquals(testUser.getUserId(), testGame.getActivePlayer());
                    verify(inMemoryGameRepository, times(3)).save(Mockito.any());
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());

                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void runTurn_validInput_gameFinished_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                try(MockedStatic<ApplicationEventPublisher> mockedEventPublisher = mockStatic(ApplicationEventPublisher.class)) {
                    // set game variables
                    Card testCard = mock(Card.class);
                    CardCollection testCardCollection = mock(CardCollection.class);
                    testGame.setCardCollection(testCardCollection);
                    testGame.setMatchCount(1);
                    testGame.getScoreBoard().put(1L, 2L);
                    testGame.getScoreBoard().put(2L, 0L);

                    // set game (testUser's turn)
                    Turn testTurn = new Turn(testUser.getUserId());
                    testGame.setActivePlayer(testUser.getUserId());
                    testGame.getPlayers().add(testOpponent);
                    testOpponent.setCurrentGameId(3);

                    // set handleMatch
                    testTurn.getPicks().add(1);
                    testGame.getHistory().add(testTurn);
                    when(testGame.getCardCollection().checkMatch(Mockito.any())).thenReturn(true);

                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                    when(testCard.getCardState()).thenReturn(CardState.FACEDOWN);
                    when(testCardCollection.getCards()).thenReturn(new ArrayList<>(Arrays.asList(testCard)));
                    doNothing().when(eventPublisher).publishEvent(any());
                    when(statsService.saveStats(Mockito.any())).thenReturn(new Stats());
                    when(userService.findUserByUserId(Mockito.any())).thenReturn(testUser);

                    gameService.runTurn(testGame.getGameId(), testCard.getCardId());

//                    assertEquals(4L, testGame.getScoreBoard().get(1L));
                    assertEquals(GameState.OPEN, testGame.getGameState());
                    assertNull(testGame.getCardCollection());
                    assertNull(testGame.getMatchCount());
                    assertNull(testGame.getScoreBoard());
                    assertNull(testGame.getActivePlayer());
                    verify(inMemoryGameRepository, times(5)).save(Mockito.any());
                    verify(eventPublisher, times(3)).publishEvent(any(GameChangesEvent.class));
                    verify(eventPublisher, times(1)).publishEvent(any(LobbyOverviewChangedEvent.class));
                    verify(statsService, times(2)).saveStats(Mockito.any());
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}