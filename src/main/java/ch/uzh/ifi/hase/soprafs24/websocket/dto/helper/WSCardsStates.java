package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class WSCardsStates {
    private Map<Integer, CardState> cardStates;
}
