package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Card {
    private int cardId;
    private String songId;
    private String imageUrl;
    private CardState cardState;

    public Card(String songId, String imageUrl) {
        Random random = new Random();

        // upper bound in nextInt corresponds to max positive integer
        this.cardId = random.nextInt(2147483647);
        this.songId = songId;
        this.imageUrl = imageUrl;
        this.cardState = CardState.FACEDOWN;
    }
}
