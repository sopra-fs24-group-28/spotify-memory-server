package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGameDto;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyOverviewDto;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PostGameStartDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.WSGameChangesDto;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSCardsStates;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSGameChanges;
import ch.uzh.ifi.hase.soprafs24.websocket.events.GameChangesEvent;
import ch.uzh.ifi.hase.soprafs24.websocket.events.LobbyOverviewChangedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
@AllArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    private ApplicationEventPublisher eventPublisher;

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public PostGameStartDTO createGame(@RequestBody GameParameters gameParameters) {
        Game game = gameService.createGame(gameParameters);

        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, game));

        return new PostGameStartDTO(game.getGameId(), game.getGameParameters());
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public LobbyOverviewDto getGames() {
        List<Game> games = gameService.getGames();

        LobbyOverviewDto lobbyOverviewDto = new LobbyOverviewDto();

        for (Game game : games) {
            LobbyGameDto lobbyGame = new LobbyGameDto(
                    game.getGameParameters(),
                    userService.getPlayerDTOListFromListOfUsers(game.getPlayers()),
                    game.getGameState(),
                    game.getHostId());
            lobbyOverviewDto.getGames().put(game.getGameId(), lobbyGame);
        }

        return lobbyOverviewDto;
    }

    @PutMapping("/{gameId}/player")
    @ResponseStatus(HttpStatus.OK)
    public void addPlayerToGame(@PathVariable Integer gameId) {
        List<User> users = gameService.addPlayerToGame(gameId);
        sendPlayersChangedWsDto(gameId, users);
    }

    @DeleteMapping("/{gameId}/player")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromGame(@PathVariable Integer gameId) {
        List<User> users = gameService.removePlayerFromGame(gameId);

        if (users == null) {
            sendGameStateChangedWsDto(gameId, GameState.FINISHED);
        } else {
            sendPlayersChangedWsDto(gameId, users);
        }

    }

    @GetMapping("/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public LobbyGameDto getGameById(@PathVariable Integer gameId) {
        Game game = gameService.getGameById(gameId);
        return new LobbyGameDto(game.getGameParameters(), userService.getPlayerDTOListFromListOfUsers(game.getPlayers()), game.getGameState(), game.getHostId());
    }


    @PostMapping("/{gameId}/start")
    @ResponseStatus(HttpStatus.OK)
    public void startGame(@PathVariable Integer gameId) {
        User host = UserContextHolder.getCurrentUser();
        Game game = gameService.startGame(gameId, host);
        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, game.getGameState()));

        WSGameChanges wsGameChanges = WSGameChanges.builder()
                .gameState(game.getGameState())
                .activePlayer(game.getActivePlayer())
                .build();

        WSCardsStates wsCardsStates = new WSCardsStates(game.getCardCollection().getAllCardStates());

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(wsGameChanges)
                .cardsStates(wsCardsStates)
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));
    }

    /*
    * HELPER METHODS FOR WEBSOCKET UPDATES
    * */

    private void sendPlayersChangedWsDto(Integer gameId, List<User> users) {
        List<PlayerDTO> players = userService.getPlayerDTOListFromListOfUsers(users);

        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, players));


        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder().playerList(players).build()).build();

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));
    }

    private void sendGameStateChangedWsDto(Integer gameId, GameState gameState) {
        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, gameState));

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder().gameState(gameState).build()).build();

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));

    }
}
