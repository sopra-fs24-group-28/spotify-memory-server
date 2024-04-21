package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

public record GameDTO(
        @JsonProperty List<PlayerDTO> playerDTOList,
        @JsonProperty Long activePlayer,
        @JsonProperty Long host,
        @JsonProperty HashMap<Long, Long> scoreBoard,
        @JsonProperty GameParameters gameParameters) {}