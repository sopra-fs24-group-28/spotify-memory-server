package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
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

        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(
                this,
                gameId,
                userService.getPlayerDTOListFromListOfUsers(users)));
    }

    @DeleteMapping("/{gameId}/player")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromGame(@PathVariable Integer gameId) {
        List<User> users = gameService.removePlayerFromGame(gameId);

        if (users == null) {
            eventPublisher.publishEvent(new LobbyOverviewChangedEvent(
                    this,
                    gameId,
                    GameState.FINISHED));
        } else {
            eventPublisher.publishEvent(new LobbyOverviewChangedEvent(
                    this,
                    gameId,
                    userService.getPlayerDTOListFromListOfUsers(users)));
        }

    }

    @GetMapping("/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public LobbyGameDto getGameById(@PathVariable Integer gameId) {
        Game game = gameService.getGameById(gameId);
        return new LobbyGameDto(game.getGameParameters(), userService.getPlayerDTOListFromListOfUsers(game.getPlayers()), game.getGameState(), game.getHostId());
    }


    @PostMapping("/{gameId}") // TODO: change to websocket
    @ResponseStatus(HttpStatus.OK)
    public GameDTO startGame(@PathVariable Integer gameId) {
        User host = UserContextHolder.getCurrentUser();
        Game game = gameService.startGame(gameId, host); // TODO: remove id and get User from SecContext in Service
        return new GameDTO(userService.getPlayerDTOListFromListOfUsers(game.getPlayers()), game.getActivePlayer(), game.getHostId(), game.getScoreBoard(), game.getGameParameters());
    }
}