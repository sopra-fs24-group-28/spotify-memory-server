package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private int cardId;
    private String content;
    private CardState cardState;
}
