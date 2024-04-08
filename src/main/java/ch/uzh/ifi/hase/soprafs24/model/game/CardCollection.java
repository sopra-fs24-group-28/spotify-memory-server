package ch.uzh.ifi.hase.soprafs24.model.game;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class CardCollection {
    private List<Card> cards;
    private HashMap<Integer, List<Integer>> matchingCards;
}
