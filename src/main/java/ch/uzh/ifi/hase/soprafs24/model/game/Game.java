package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.List;

@Setter
@Getter
public class Game {
    @Id
    @GeneratedValue
    private int gameId;

    private int activePlayer;

    private int activePlayerStreak;

    private GameState gameState;

    private int hostId;

    private GameParameters gameParameters;

    private List<Turn> history;

    private HashMap<String, Integer> scoreBoard;

    private List<User> players;

    private HashMap<String, Integer> quickTurn;

    private boolean quickTurnActive = false;

    private CardCollection cardCollection;

    public Game(GameParameters gameParameters, int hostId) {
        this.gameState = GameState.OPEN;
        this.gameParameters = gameParameters;
        this.hostId = hostId;
    }

}
