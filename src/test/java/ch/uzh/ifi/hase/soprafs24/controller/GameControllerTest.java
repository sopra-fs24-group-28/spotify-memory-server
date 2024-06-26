package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Playlist;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.websocket.events.LobbyOverviewChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
public class GameControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private GameService gameService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private Game game1;
    private Game game2;
    private GameParameters gameParameters;
    private User user;

    private List<Game> games;

    @BeforeEach
    public void setup(WebApplicationContext context) {
        user = new User();
        user.setUsername("testUsername");
        user.setUserId(1L);
        user.setSessionToken("token");

        game1 = new Game(new GameParameters(2,2,2, GameCategory.STANDARDALBUMCOVER, new Playlist("id"), 1,1,10), user);
        game2 = new Game(new GameParameters(2,2,2, GameCategory.STANDARDALBUMCOVER, new Playlist("id"), 1,1,10), user);

        game1.setGameId(0);
        game2.setGameId(1);

        games = new ArrayList<>();
        games.add(game1);

        gameParameters = new GameParameters(2,2,2, GameCategory.STANDARDALBUMCOVER, new Playlist("id"), 1,1,10);

        game1.getPlayers().add(user);

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Mockito.when(gameService.getGames()).thenReturn(games);
        Mockito.when(gameService.createGame(Mockito.any())).thenReturn(game1);
        Mockito.when(gameService.getGameById(Mockito.any())).thenReturn(game1);
        doNothing().when(gameService).addPlayerToGame(Mockito.any());
        doNothing().when(gameService).removePlayerFromGame(Mockito.any());
        doNothing().when(gameService).startGame(Mockito.any());
        doNothing().when(gameService).handleInactivePlayer(Mockito.any());
        doNothing().when(eventPublisher).publishEvent(Mockito.any());
    }

    @Test
    void postGameParameters_returns_PostGameStartDTO() throws Exception {

        MockHttpServletRequestBuilder postRequest = post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerLimit\":2,\"numOfSets\":5,\"numOfCardsPerSet\":2,\"gameCategory\":\"STANDARDALBUMCOVER\",\"playlist\":\"37i9dQZF1EIdFa1mD9SkGv\",\"streakStart\":2,\"streakMultiplier\":3,\"timePerTurn\":15}");

        PostGameStartDTO expectedResponse = new PostGameStartDTO(game1.getGameId(), game1.getGameParameters());

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(content().json(asJsonString(expectedResponse)));
    }

    @Test
    void getGames_returns_lobbyOverviewDto() throws Exception {

        MockHttpServletRequestBuilder getRequest = get("/games")
                .contentType(MediaType.APPLICATION_JSON);

        LobbyOverviewDto expectedResponse = new LobbyOverviewDto();
        List<PlayerDTO> playerList = new ArrayList<>();
        expectedResponse.getGames().put(0, new LobbyGameDto(
                game1.getGameParameters(),
                playerList,
                game1.getGameState(),
                game1.getHostId()));

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(expectedResponse)));
    }

    @Test
    void putPlayer_returns_OK() throws Exception {

        MockHttpServletRequestBuilder putRequest = put("/games/1/player")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(putRequest)
                .andExpect(status().isOk());
    }

    @Test
    void deletePlayer_returns_NoContent() throws Exception {

        MockHttpServletRequestBuilder deleteRequest = delete("/games/1/player")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    void getGame_returns_lobbyGameDto() throws Exception {

        MockHttpServletRequestBuilder getRequest = get("/games/1")
                .contentType(MediaType.APPLICATION_JSON);
        List<PlayerDTO> playerList = new ArrayList<>();
        LobbyGameDto expectedResponse = new LobbyGameDto(game1.getGameParameters(), playerList, game1.getGameState(), game1.getHostId());

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(expectedResponse)));
    }

    @Test
    void startGame_returns_OK() throws Exception {

        MockHttpServletRequestBuilder postRequest = post("/games/1/start")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isOk());
    }

    @Test
    void putInactivePlayer_returns_OK() throws Exception {

        MockHttpServletRequestBuilder putRequest = put("/games/1/inactive")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(putRequest)
                .andExpect(status().isOk());
    }


    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
