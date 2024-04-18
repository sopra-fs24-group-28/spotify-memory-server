package ch.uzh.ifi.hase.soprafs24.rest.dto.helper;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.helper.Change;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LobbyGameChanges {
    private Change<GameParameters> gameParameters = Change.of(false, Optional.empty());
    private Change<List<PlayerDTO>> playerList = Change.of(false, Optional.empty());
    private Change<GameState> gameState = Change.of(false, Optional.empty());
    private Change<Long> hostId = Change.of(false, Optional.empty());

    // only to be used for game creation
    public LobbyGameChanges (Game game) {
        gameParameters = Change.of(true, Optional.ofNullable(game.getGameParameters()));
        playerList = Change.of(true, Optional.of(Collections.singletonList(new PlayerDTO(game.getPlayers().get(0)))));
        gameState = Change.of(true, Optional.ofNullable(game.getGameState()));
        hostId = Change.of(true, Optional.ofNullable(game.getHostId()));
    }

    public LobbyGameChanges (List<User> newUsers) {
        List<PlayerDTO> playerDTOList = newUsers.stream()
                .map(PlayerDTO::new)
                .toList();
        playerList = Change.of(true, Optional.of(playerDTOList));

    }

    public LobbyGameChanges (GameParameters newGameParameters) {
        gameParameters = Change.of(true, Optional.ofNullable(newGameParameters));
    }

    public LobbyGameChanges (Long id) {
        hostId = Change.of(true, Optional.ofNullable(id));
    }

    public LobbyGameChanges (GameState state) {
        gameState = Change.of(true, Optional.ofNullable(state));
    }


}
