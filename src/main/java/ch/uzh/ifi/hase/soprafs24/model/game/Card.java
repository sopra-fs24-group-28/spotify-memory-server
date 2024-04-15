package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Card {
    private int cardId;
    private String content;
    private CardState cardState;

    Card(String content) {
        Random random = new Random();

        // upper bound in nextInt corresponds to max positive integer
        this.cardId = random.nextInt(2147483647);
        this.content = content;
        this.cardState = CardState.FACEDOWN;

    }
}
