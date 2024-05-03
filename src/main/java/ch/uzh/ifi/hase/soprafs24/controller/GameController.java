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
        gameService.addPlayerToGame(gameId);
    }

    @DeleteMapping("/{gameId}/player")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromGame(@PathVariable Integer gameId) {
        gameService.removePlayerFromGame(gameId);
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
        gameService.startGame(gameId);
    }

    @PutMapping("/{gameId}/inactive")
    @ResponseStatus(HttpStatus.OK)
    public void inactivePlayerHandler(@PathVariable Integer gameId) {
        gameService.handleInactivePlayer(gameId);
    }
}
