package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class GameParameters {

    private int playerLimit;

    private int numOfSets;

    private int numOfCardsPerSet;

    private GameCategory gameCategory;

    private Playlist playlist;

    private int streakStart = 3;

    private int streakMultiplier = 2;

    private int timePerTurn = 10;

    private int timePerTurnPowerUp = 2;

}
