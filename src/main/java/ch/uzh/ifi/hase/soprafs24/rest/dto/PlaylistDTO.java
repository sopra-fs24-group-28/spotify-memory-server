package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Data;

@Data
public class PlaylistDTO {
    private String name;
    private String id;

    public PlaylistDTO(String name, String id) {
        this.name = name;
        this.id = id;
    }

}
