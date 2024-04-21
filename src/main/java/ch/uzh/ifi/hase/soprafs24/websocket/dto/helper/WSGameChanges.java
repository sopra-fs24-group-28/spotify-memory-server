package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.model.helper.Change;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public record WSGameChanges(Change<Long> activePlayer, Change<Long> activePlayerStreak, Change<Boolean> quickTurnActive,
                            Change<HashMap<Long, Long>> quickTurn, Change<List<PlayerDTO>> playerList,
                            Change<GameState> gameState, Change<Long> hostId) {
    @Builder
    public WSGameChanges {
    }

    public static class WSGameChangesBuilder {
        private Change<Long> activePlayer = Change.of(false, Optional.empty());
        private Change<Long> activePlayerStreak = Change.of(false, Optional.empty());
        private Change<Boolean> quickTurnActive = Change.of(false, Optional.empty());
        private Change<HashMap<Long, Long>> quickTurn = Change.of(false, Optional.empty());
        private Change<List<PlayerDTO>> playerList = Change.of(false, Optional.empty());
        private Change<GameState> gameState = Change.of(false, Optional.empty());
        private Change<Long> hostId = Change.of(false, Optional.empty());

        public WSGameChangesBuilder activePlayer(Long activePlayer) {
            this.activePlayer = Change.of(true, Optional.ofNullable(activePlayer));
            return this;
        }

        public WSGameChangesBuilder activePlayerStreak(Long streak) {
            this.activePlayerStreak = Change.of(true, Optional.ofNullable(streak));
            return this;
        }

        public WSGameChangesBuilder quickTurnActive(Boolean quickTurnActive) {
            this.quickTurnActive = Change.of(true, Optional.ofNullable(quickTurnActive));
            return this;
        }

        public WSGameChangesBuilder quickTurn(HashMap<Long, Long> quickTurn) {
            this.quickTurn = Change.of(true, Optional.ofNullable(quickTurn));
            return this;
        }

        public WSGameChangesBuilder playerList(List<PlayerDTO> playerList) {
            this.playerList = Change.of(true, Optional.ofNullable(playerList));
            return this;
        }

        public WSGameChangesBuilder gameState(GameState gameState) {
            this.gameState = Change.of(true, Optional.ofNullable(gameState));
            return this;
        }

        public WSGameChangesBuilder hostId(Long hostId) {
            this.hostId = Change.of(true, Optional.ofNullable(hostId));
            return this;
        }
    }
}
