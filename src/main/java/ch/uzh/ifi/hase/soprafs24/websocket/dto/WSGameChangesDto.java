package ch.uzh.ifi.hase.soprafs24.websocket.dto;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.helper.Change;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.*;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class WSGameChangesDto {
    @Builder.Default
    private final Change<Optional<WSGameChanges>> gameChangesDto = Change.of(false, Optional.empty());

    @Builder.Default
    private final Change<Optional<WSCardContent>> cardContent = Change.of(false, Optional.empty());

    @Builder.Default
    private final Change<Optional<WSCardsStates>> cardsStates = Change.of(false, Optional.empty());

    @Builder.Default
    private final Change<Optional<WSScoreBoardChanges>> scoreBoard = Change.of(false, Optional.empty());
}