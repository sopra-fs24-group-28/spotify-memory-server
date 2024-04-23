package ch.uzh.ifi.hase.soprafs24.model.game;

public final class GameConstant {
    private static final Integer minPlayers = 2;

    private static final Integer viewSleep = 2000;

    public static Integer getMinPlayers() {
        return minPlayers;
    }

    public static Integer getViewSleep() {
        return viewSleep;
    }
}
