package ch.uzh.ifi.hase.soprafs24.websocket.dto;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LobbyGameChanges {
    private Pair<Boolean, Optional<GameParameters>> gameParameter =  Pair.of(false, Optional.empty());
    private Pair<Boolean, Optional<List<User>>> users =  Pair.of(false, Optional.empty());
    private Pair<Boolean, Optional<GameState>> gameState =  Pair.of(false, Optional.empty());
    private Pair<Boolean, Optional<Long>> hostId =  Pair.of(false, Optional.empty());
}
