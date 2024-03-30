package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PostGameStartDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PostGameStartDTO startGame(@RequestBody GameParameters gameParameters) {
        return new PostGameStartDTO(1, gameParameters);
    }
}
