package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PostGameStartDTO(Long gameId, @JsonProperty GameParameters gameParameters) {}








