package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyOverviewDto;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PostGameStartDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGameDto;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.websocket.events.LobbyOverviewChangedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/games")
@AllArgsConstructor
public class GameController {

    private final GameService gameService;

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
            List<PlayerDTO> players = new ArrayList<>();
            for (User user: game.getPlayers()) {
                players.add(new PlayerDTO(user));
            }
            LobbyGameDto lobbyGame = new LobbyGameDto(game.getGameParameters(), players, game.getGameState(), game.getHostId());
            lobbyOverviewDto.getGames().put(game.getGameId(), lobbyGame);
        }

        return lobbyOverviewDto;
    }

    @PutMapping("/{gameId}/player")
    @ResponseStatus(HttpStatus.OK)
    public void addPlayerToGame(@PathVariable Integer gameId) {
        List<User> users = gameService.addPlayerToGame(gameId);

        List<PlayerDTO> playerDTOList = users.stream()
                .map(PlayerDTO::new)
                .toList();

        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId ,playerDTOList));
    }

    @DeleteMapping("/{gameId}/player")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromGame(@PathVariable Integer gameId) {
        List<User> users = gameService.removePlayerFromGame(gameId);

        if (users == null) {
            eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, GameState.FINISHED));
        } else {
            List<PlayerDTO> playerDTOList = users.stream()
                    .map(PlayerDTO::new)
                    .toList();
            eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId , playerDTOList));
        }

    }


}
