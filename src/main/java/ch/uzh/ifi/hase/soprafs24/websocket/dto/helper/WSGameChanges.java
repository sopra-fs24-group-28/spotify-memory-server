package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.model.helper.Change;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

public record WSGameChanges( @JsonProperty Change<Long> activePlayer,
                             @JsonProperty Change<Boolean> activePlayerStreakActive,
                             @JsonProperty Change<List<PlayerDTO>> playerList,
                             @JsonProperty Change<GameState> gameState,
                             @JsonProperty Change<Long> hostId) {
    @Builder
    public WSGameChanges {
    }

    public static class WSGameChangesBuilder {
        private Change<Long> activePlayer = Change.of(false, Optional.empty());
        private Change<Boolean> activePlayerStreakActive = Change.of(false, Optional.empty());
        private Change<List<PlayerDTO>> playerList = Change.of(false, Optional.empty());
        private Change<GameState> gameState = Change.of(false, Optional.empty());
        private Change<Long> hostId = Change.of(false, Optional.empty());

        public WSGameChangesBuilder activePlayer(Long activePlayer) {
            this.activePlayer = Change.of(true, Optional.ofNullable(activePlayer));
            return this;
        }

        public WSGameChangesBuilder activePlayerStreakActive(Boolean streakActive) {
            this.activePlayerStreakActive = Change.of(true, Optional.ofNullable(streakActive));
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
