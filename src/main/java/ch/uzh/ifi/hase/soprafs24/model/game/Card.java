package ch.uzh.ifi.hase.soprafs24.model.game;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class Card {
    private int cardId;
    private String content;
    private CardState cardState;
}
