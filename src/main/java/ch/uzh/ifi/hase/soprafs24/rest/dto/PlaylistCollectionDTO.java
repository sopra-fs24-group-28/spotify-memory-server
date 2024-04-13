package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlaylistCollectionDTO {
    private List<PlaylistDTO> playlists;

    public PlaylistCollectionDTO() {
        this.playlists = new ArrayList<>();
    }


    public void setPlaylists(List<PlaylistDTO> playlists) {
        this.playlists = playlists;
    }
}



