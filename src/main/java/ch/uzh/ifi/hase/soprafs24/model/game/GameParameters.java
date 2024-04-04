package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.GameCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class GameParameters {

    private int playerLimit;

    private int numOfSets;

    private int numOfCardsPerSet;

    private GameCategory gameCategory;

    private String playlist;

    private int streakStart = 3;

    private int streakMultiplier = 2;

    private int timePerTurn = 10;

    private int timePerTurnPowerUp = 2;

}
