package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class GameParameters {

    @Min(2)
    @Max(4)
    private int playerLimit;

    @Min(2)
    @Max(10)
    private int numOfSets;

    @Min(2)
    @Max(4)
    private int numOfCardsPerSet;

    @NotNull
    private GameCategory gameCategory;

    @NotNull
    private Playlist playlist;

    @Min(1)
    private int streakStart = 3;

    @Min(1)
    private int streakMultiplier = 2;

    @Min(10)
    @Max(60)
    private int timePerTurn = 10;

    private int timePerTurnPowerUp = 2;

}
