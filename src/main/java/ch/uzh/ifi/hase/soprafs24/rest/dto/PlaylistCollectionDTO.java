package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistCollectionDTO {
    private List<PlaylistDTO> playlists;

    public void setPlaylists(List<PlaylistDTO> playlists) {
        this.playlists = playlists;
    }
}



