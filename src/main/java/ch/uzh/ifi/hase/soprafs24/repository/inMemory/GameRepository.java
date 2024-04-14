package ch.uzh.ifi.hase.soprafs24.repository.inMemory;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;

public interface GameRepository {
    Game save(Game game);

    Game findById(Integer id);

    void deleteById(Integer id);
}
