package ch.uzh.ifi.hase.soprafs24.model.game;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Turn {
    private final int userId;
    private final List<Integer> picks = new ArrayList<>();

    public Turn(int userId) {
        this.userId = userId;
    }

    public void addPick(int pick) {
        picks.add(pick);
    }
}
