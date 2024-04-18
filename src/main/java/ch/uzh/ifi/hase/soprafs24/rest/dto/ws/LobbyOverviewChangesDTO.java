package ch.uzh.ifi.hase.soprafs24.rest.dto.ws;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.helper.LobbyGameChanges;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LobbyOverviewChangesDTO {
    private HashMap<Integer, LobbyGameChanges> gameMap = new HashMap<>();

    public LobbyOverviewChangesDTO(Game game) {
        gameMap.put(game.getGameId(), new LobbyGameChanges(game));
    }

    public LobbyOverviewChangesDTO(Integer gameId, List<User> users) {
        gameMap.put(gameId, new LobbyGameChanges(users));
    }

    public LobbyOverviewChangesDTO(Integer gameId, GameState newGameState) {
        gameMap.put(gameId, new LobbyGameChanges(newGameState));
    }
}
