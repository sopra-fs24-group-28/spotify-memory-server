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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.aop.support.StaticMethodMatcher;
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
        testUser.setSpotifyUserId("testSpotifyUserId");
        testUser.setUsername("testUsername");
        testUser.setSpotifyJWT(new SpotifyJWT());

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
                // create the Host
                testUser.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                testPlayList.put("playlist_length", "2");
                when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                // assert user status
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
                // create the Host
                testUser.setState(UserStatus.ONLINE);
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                testPlayList.put("playlist_length", "1");
                when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                // assert user status
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
                // create the Host
                testUser.setState(UserStatus.INGAME);
                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

                HashMap<String, String> testPlayList = new HashMap<>();
                testPlayList.put("playlist_name", "testPlayList");
                testPlayList.put("image_url", "test_url");
                when(SpotifyService.getPlaylistMetadata(Mockito.any(), Mockito.any())).thenReturn(testPlayList);

                // assert user status
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
                    // create the Host
                    testGame.setHostId(testUser.getUserId());
                    User testApponent = new User();
                    testApponent.setUserId(2L);
                    testGame.getPlayers().add(testApponent);

                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(inMemoryGameRepository.getLatestGameStatsId()).thenReturn(1);
                    when(statsService.getLatestGameId()).thenReturn(2);

                    // assert user status
                    gameService.startGame(testGame.getGameId());

                    assertEquals(testGame.getMatchCount(), 0);
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
                // create the Host
                testGame.setHostId(testUser.getUserId());

                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                // assert user status
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.startGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void startGame_invalidHost_throwException() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            try (MockedConstruction<CardCollection> mockedCardCollections = mockConstruction(CardCollection.class)) {
                // create the Host
                testGame.setHostId(new User().getUserId());
                testGame.getPlayers().add(new User());

                when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

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

            when(inMemoryGameRepository.findAll()).thenReturn(games);

            // assert user status
            List<Game> resultGames = gameService.getGames();

            assertEquals(resultGames, games);
        }
    }

    @Test
    public void getGameById_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {

            when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
            Game resultGame = gameService.getGameById(testGame.getGameId());
            assertEquals(resultGame, testGame);
        }
    }

    @Test
    public void addPlayerToGame_validInput_success() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set Game state Open
                testGame.setGameState(GameState.OPEN);

                // create new Participant
                User testParticipant = new User();

                // set Users State
                testParticipant.setState(UserStatus.ONLINE);

                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                when(UserContextHolder.getCurrentUser()).thenReturn(testParticipant);

                // execute method
                gameService.addPlayerToGame(testGame.getGameId());

                verify(userService).setGameIdForGivenUser(testParticipant, testGame.getGameId());
                verify(userService).setPlayerState(testParticipant, UserStatus.INGAME);
                verify(inMemoryGameRepository).save(testGame);
                verify(eventPublisher, times(2)).publishEvent(Mockito.any());
            }
        }
    }

    @Test
    public void addPlayerToGame_userWithCurrentGameId_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set GameState not open
                testGame.setGameState(GameState.OPEN);
                // set User with gameId non-null
                User testParticipant = new User();
                testParticipant.setCurrentGameId(1);

                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                when(UserContextHolder.getCurrentUser()).thenReturn(testParticipant);

                // throw Exception
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_userINGAME_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set GameState open
                testGame.setGameState(GameState.OPEN);
                // set User with User state INGAME
                User testParticipant = new User();
                testParticipant.setState(UserStatus.INGAME);

                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                when(UserContextHolder.getCurrentUser()).thenReturn(testParticipant);

                // throw Exception
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_exceedPlayerLimit_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set GameState open
                testGame.setGameState(GameState.OPEN);

                // create playersList with 2 players
                testGame.getPlayers().add(new User());

                // set User with User state INGAME
                User testParticipant = new User();
                testParticipant.setState(UserStatus.ONLINE);

                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                when(UserContextHolder.getCurrentUser()).thenReturn(testParticipant);

                // throw Exception
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_gameOnPlay_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)){
                // set Game state Onplay
                testGame.setGameState(GameState.ONPLAY);

                // set User
                User testParticipant = new User();
                testParticipant.setState(UserStatus.ONLINE);

                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                when(UserContextHolder.getCurrentUser()).thenReturn(testParticipant);

                // throw Exception
                ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> gameService.addPlayerToGame(testGame.getGameId()));
                assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Test
    public void addPlayerToGame_gameFinished_throwException() {
        try (MockedStatic<InMemoryGameRepository> mockedGameRepository = mockStatic(InMemoryGameRepository.class)) {
            try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
                // set GameState finished (unexpected)
                testGame.setGameState(GameState.FINISHED);
                // set User
                User testParticipant = new User();
                testParticipant.setState(UserStatus.ONLINE);

                when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                when(UserContextHolder.getCurrentUser()).thenReturn(testParticipant);

                // throw Exception
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
                    // set testUser
                    testUser.setState(UserStatus.INGAME);
                    User testParticipant = new User();
                    testParticipant.setState(UserStatus.INGAME);

                    // set testGame setting
                    testGame.getPlayers().add(testParticipant);
                    testGame.setHostId(testUser.getUserId());

                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
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
                    // set testUser
                    testUser.setState(UserStatus.INGAME);
                    User testParticipant = new User();
                    testParticipant.setUserId(2L);
                    testParticipant.setState(UserStatus.INGAME);

                    // set testGame setting
                    testGame.getPlayers().add(testParticipant);
                    testGame.setHostId(testUser.getUserId());
                    testGame.setGameState(GameState.ONPLAY);
                    testGame.getScoreBoard().put(1L, 2L);
                    testGame.getScoreBoard().put(2L, 2L);

                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(statsService.saveStats(Mockito.any())).thenReturn(new Stats());

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
                    // set testUser
                    testUser.setState(UserStatus.INGAME);
                    User testHost = new User();
                    testHost.setUserId(2L);
                    testHost.setState(UserStatus.INGAME);

                    // set testGame setting
                    testGame.getPlayers().add(testHost);
                    testGame.setHostId(testHost.getUserId());

                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);

                    gameService.removePlayerFromGame(testGame.getGameId());

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
                    // set testUser
                    testUser.setState(UserStatus.INGAME);
                    User testHost = new User();
                    testHost.setUserId(2L);
                    testHost.setState(UserStatus.INGAME);

                    // set testGame setting
                    testGame.getPlayers().add(testHost);
                    testGame.setHostId(testHost.getUserId());
                    testGame.setGameState(GameState.ONPLAY);
                    testGame.getScoreBoard().put(1L, 2L);
                    testGame.getScoreBoard().put(2L, 2L);

                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(statsService.saveStats(Mockito.any())).thenReturn(new Stats());

                    gameService.removePlayerFromGame(testGame.getGameId());

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
                    // set test condition (checkActivePlayer())
                    testGame.setActivePlayer(testUser.getUserId());

                    // set test condition (checkActiveCard())
                    Card testCard = mock(Card.class);
                    CardCollection testCardCollection = mock(CardCollection.class);
                    testGame.setCardCollection(testCardCollection);

                    //set test conditions (runActiveTurn())
                    testGame.getHistory().add(new Turn(testUser.getUserId()));

                    //set test conditions (checkFinished())
                    testGame.setMatchCount(1);

                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                    when(testCard.getCardState()).thenReturn(CardState.FACEDOWN);
                    when(testCardCollection.getCards()).thenReturn(new ArrayList<>(Arrays.asList(testCard)));
                    doNothing().when(eventPublisher).publishEvent(any());

                    gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                    verify(inMemoryGameRepository, times(2)).save(Mockito.any());
                    verify(eventPublisher, times(2)).publishEvent(Mockito.any());

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
                            // set testGame -> STANDARDSONG
                            testGame.getGameParameters().setGameCategory(STANDARDSONG);

                            // set test condition (checkActivePlayer())
                            testGame.setActivePlayer(testUser.getUserId());

                            // set test condition (checkActiveCard())
                            Card testCard = mock(Card.class);
                            CardCollection testCardCollection = mock(CardCollection.class);
                            testGame.setCardCollection(testCardCollection);

                            //set test conditions (runActiveTurn())
                            testGame.getHistory().add(new Turn(testUser.getUserId()));

                            //set test conditions (checkFinished())
                            testGame.setMatchCount(1);

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
                            when(SpotifyService.setSong(any(), any(), any())).thenReturn(true);

                            gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                            verify(inMemoryGameRepository, times(2)).save(Mockito.any());
                            verify(eventPublisher, times(2)).publishEvent(Mockito.any());

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
                    // set test condition (checkActivePlayer())
                    testGame.setActivePlayer(testUser.getUserId());

                    // set test condition (checkActiveCard())
                    Card testCard = mock(Card.class);
                    CardCollection testCardCollection = mock(CardCollection.class);
                    testGame.setCardCollection(testCardCollection);

                    //set test conditions (runActiveTurn())
                    Turn testTurn = new Turn(testUser.getUserId());
                    testTurn.getPicks().add(1);
                    testGame.getHistory().add(testTurn);

                    //set test conditions (checkFinished())
                    testGame.setMatchCount(0);

                    //set Scores for finished game
                    testGame.getScoreBoard().put(1L, 2L);


                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                    when(testCard.getCardState()).thenReturn(CardState.FACEDOWN);
                    when(testCardCollection.getCards()).thenReturn(new ArrayList<>(Arrays.asList(testCard)));
                    doNothing().when(eventPublisher).publishEvent(any());
                    when(statsService.saveStats(Mockito.any())).thenReturn(new Stats());
                    when(userService.findUserByUserId(Mockito.any())).thenReturn(testUser);
                    when(testGame.getCardCollection().checkMatch(Mockito.any())).thenReturn(true);


                    gameService.runTurn(testGame.getGameId(), testCard.getCardId());

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
                    // set test condition (checkActivePlayer())
                    testGame.setActivePlayer(testUser.getUserId());

                    // set test condition (checkActiveCard())
                    Card testCard = mock(Card.class);
                    CardCollection testCardCollection = mock(CardCollection.class);
                    testGame.setCardCollection(testCardCollection);

                    //set test conditions (runActiveTurn())
                    Turn testTurn = new Turn(testUser.getUserId());
                    testTurn.getPicks().add(1);
                    testGame.getHistory().add(testTurn);

                    //set test conditions (checkFinished())
                    testGame.setMatchCount(1);

                    //set Scores for finished game
                    testGame.getScoreBoard().put(1L, 2L);
                    testGame.getScoreBoard().put(2L, 0L);

                    //set participant
                    User testParticipant = new User();
                    testParticipant.setUserId(2L);
                    testParticipant.setCurrentGameId(3);
                    testGame.getPlayers().add(testParticipant);

                    when(inMemoryGameRepository.findById(Mockito.any())).thenReturn(testGame);
                    when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
                    when(testGame.getCardCollection().getCardById(Mockito.any())).thenReturn(testCard);
                    when(testCard.getCardState()).thenReturn(CardState.FACEDOWN);
                    when(testCardCollection.getCards()).thenReturn(new ArrayList<>(Arrays.asList(testCard)));
                    doNothing().when(eventPublisher).publishEvent(any());
                    when(statsService.saveStats(Mockito.any())).thenReturn(new Stats());
                    when(userService.findUserByUserId(Mockito.any())).thenReturn(testUser);
                    when(testGame.getCardCollection().checkMatch(Mockito.any())).thenReturn(true);


                    gameService.runTurn(testGame.getGameId(), testCard.getCardId());

                    verify(inMemoryGameRepository, times(4)).save(Mockito.any());
                    verify(eventPublisher, times(4)).publishEvent(Mockito.any());

                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}