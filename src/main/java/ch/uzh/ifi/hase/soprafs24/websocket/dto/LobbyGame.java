package ch.uzh.ifi.hase.soprafs24.websocket.dto;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;

import java.util.List;


public record LobbyGame(GameParameters gameParameters, List<User> userList, GameState gameState, Long hostId) {
}
