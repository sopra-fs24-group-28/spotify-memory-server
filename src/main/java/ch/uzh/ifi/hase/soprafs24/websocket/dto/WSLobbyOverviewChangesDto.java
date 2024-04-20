package ch.uzh.ifi.hase.soprafs24.websocket.dto;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSLobbyGameChanges;
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
public class WSLobbyOverviewChangesDto {
    private HashMap<Integer, WSLobbyGameChanges> gameMap = new HashMap<>();

    public WSLobbyOverviewChangesDto(Game game) {
        gameMap.put(game.getGameId(), new WSLobbyGameChanges(game));
    }

    public WSLobbyOverviewChangesDto(Integer gameId, List<PlayerDTO> users) {
        gameMap.put(gameId, new WSLobbyGameChanges(users));
    }

    public WSLobbyOverviewChangesDto(Integer gameId, GameState newGameState) {
        gameMap.put(gameId, new WSLobbyGameChanges(newGameState));
    }
}
