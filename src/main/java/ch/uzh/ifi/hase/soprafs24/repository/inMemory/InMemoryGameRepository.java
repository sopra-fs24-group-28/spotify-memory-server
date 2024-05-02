package ch.uzh.ifi.hase.soprafs24.repository.inMemory;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Repository
public class InMemoryGameRepository implements GameRepository {
    private Map<Integer, Game> games;
    private final AtomicInteger currentId = new AtomicInteger(0);

    @PostConstruct
    private void init() {
        games = new ConcurrentHashMap<>();
    }

    @Override
    public Game save(Game game) {
        if (game.getGameId() == null){
            game.setGameId(currentId.getAndIncrement());
        }
        games.put(game.getGameId(), game);
        return game;
    }

    @Override
    public Game findById(Integer id) {
        return games.get(id);
    }

    @Override
    public void deleteById(Integer id) {
        games.remove(id);
    }

    public Integer getLatestGameStatsId() {
        return games.values().stream()
                .map(Game::getGameStatsId)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(1);
    }

    @Override
    public List<Game> findAll() {
        return new ArrayList<>(games.values());
    }
}
