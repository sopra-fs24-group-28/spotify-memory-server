package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;

public record PostGameStartDTO(int gameId, GameParameters gameParameters) {}
