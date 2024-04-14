package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PostGameStartDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public PostGameStartDTO createGame(@RequestBody GameParameters gameParameters) {
        Game game = gameService.createGame(gameParameters, 1L); // TODO: remove id and get User from SecContext in Service
        return new PostGameStartDTO(game.getGameId(), game.getGameParameters());
    }

    @PostMapping("/{gameId}") // TODO: change to websocket
    @ResponseStatus(HttpStatus.OK)
    public GameDTO startGame(@PathVariable Integer gameId) {
        User host = UserContextHolder.getCurrentUser();
        Game game = gameService.startGame(gameId, host); // TODO: remove id and get User from SecContext in Service
        return new GameDTO(game.getPlayers(), game.getActivePlayer(), game.getHostId(), game.getScoreBoard(), game.getGameParameters());
    }
}
