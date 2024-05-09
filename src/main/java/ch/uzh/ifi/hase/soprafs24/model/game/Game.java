package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Setter
@Getter
public class Game {
    private Integer gameId;

    private Long activePlayer;

    private Integer activePlayerStreak;

    private GameState gameState;

    private Long hostId;

    private GameParameters gameParameters;

    private List<Turn> history = new ArrayList<>();

    private HashMap<Long, Long> scoreBoard = new HashMap<>();

    private List<User> players = new ArrayList<>();

    private HashMap<Long, Long> quickTurn = new HashMap<>();

    private Boolean quickTurnActive = false;

    private CardCollection cardCollection;

    private Integer matchCount;

    private Integer gameStatsId;

    public Game(GameParameters gameParameters, User host) {
        this.gameState = GameState.OPEN;
        this.gameParameters = gameParameters;
        this.hostId = host.getUserId();
    }
}
