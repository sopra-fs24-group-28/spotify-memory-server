package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.model.helper.Change;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Builder
public class WSGameChanges {
    @Builder.Default
    private Change<Long> activePlayer = Change.of(false, Optional.empty());
    @Builder.Default
    private Change<Long> activePlayerStreak = Change.of(false, Optional.empty());
    @Builder.Default
    private Change<Boolean> quickTurnActive = Change.of(false, Optional.empty());
    @Builder.Default
    private Change<HashMap<Long, Long>> quickTurn = Change.of(false, Optional.empty());
    @Builder.Default
    private Change<List<PlayerDTO>> playerList = Change.of(false, Optional.empty());
    @Builder.Default
    private Change<GameState> gameState = Change.of(false, Optional.empty());
    @Builder.Default
    private Change<Long> hostId = Change.of(false, Optional.empty());
}
