package ch.uzh.ifi.hase.soprafs24.repository.inMemory;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    Game save(Game game);

    Optional<Game> findById(Integer id);

    void deleteById(Integer id);

    List<Game> findAll();
}
