package ch.uzh.ifi.hase.soprafs24.websocket.dto;

import ch.uzh.ifi.hase.soprafs24.model.helper.Change;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSCardContents;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSCardsStates;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSGameChanges;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSScoreBoardChanges;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Optional;

public record WSGameChangesDto(@JsonProperty Change<WSGameChanges> gameChangesDto,
                               @JsonProperty Change<WSCardContents> cardContents,
                               @JsonProperty Change<WSCardsStates> cardsStates,
                               @JsonProperty Change<WSScoreBoardChanges> scoreBoard) {

    @Builder
    public static WSGameChangesDto create(Change<WSGameChanges> gameChangesDto,
                                          Change<WSCardContents> cardContents,
                                          Change<WSCardsStates> cardsStates,
                                          Change<WSScoreBoardChanges> scoreBoard) {
        return new WSGameChangesDto(gameChangesDto, cardContents, cardsStates, scoreBoard);
    }

    public static class WSGameChangesDtoBuilder {
        private Change<WSGameChanges> gameChangesDto = Change.of(false, Optional.empty());
        private Change<WSCardContents> cardContents = Change.of(false, Optional.empty());
        private Change<WSCardsStates> cardsStates = Change.of(false, Optional.empty());
        private Change<WSScoreBoardChanges> scoreBoard = Change.of(false, Optional.empty());

        public WSGameChangesDtoBuilder gameChangesDto(WSGameChanges gameChangesDto) {
            this.gameChangesDto = Change.of(true, Optional.of(gameChangesDto));
            return this;
        }

        public WSGameChangesDtoBuilder cardContent(WSCardContents cardContents) {
            this.cardContents = Change.of(true, Optional.of(cardContents));
            return this;
        }

        public WSGameChangesDtoBuilder cardsStates(WSCardsStates cardsStates) {
            this.cardsStates = Change.of(true, Optional.of(cardsStates));
            return this;
        }

        public WSGameChangesDtoBuilder scoreBoard(WSScoreBoardChanges scoreBoard) {
            this.scoreBoard = Change.of(true, Optional.of(scoreBoard));
            return this;
        }
    }
}
