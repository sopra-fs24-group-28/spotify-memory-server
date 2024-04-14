package ch.uzh.ifi.hase.soprafs24.rest.dto.ws;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.helper.LobbyGameChanges;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LobbyOverviewChangesDTO {
    private HashMap<Integer, LobbyGameChanges> gameMap = new HashMap<>();

    public LobbyOverviewChangesDTO(Game game) {
        gameMap.put(game.getGameId(), new LobbyGameChanges(game));
    }
}
