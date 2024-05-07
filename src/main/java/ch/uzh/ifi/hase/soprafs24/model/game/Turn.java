package ch.uzh.ifi.hase.soprafs24.model.game;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Turn {
    private final long userId;
    private final List<Integer> picks = new ArrayList<>();

    public Turn(long userId) {
        this.userId = userId;
    }

}
