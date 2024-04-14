package ch.uzh.ifi.hase.soprafs24.rest.dto.helper;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class LobbyGame {
    private GameParameters gameParameters;
    private List<PlayerDTO> playerList;
    private GameState gameState;
    private Long hostId;

    @Override
    public String toString() {
        return "LobbyGame{" +
                "gameParameters=" + gameParameters +
                ", userList=" + playerList +
                ", gameState=" + gameState +
                ", hostId=" + hostId +
                '}';
    }
}