package ch.uzh.ifi.hase.soprafs24.repository.inMemory;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;

import java.util.List;

public interface GameRepository {
    Game save(Game game);

    Game findById(Integer id);

    void deleteById(Integer id);

    List<Game> findAll();

}
