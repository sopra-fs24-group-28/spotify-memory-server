package ch.uzh.ifi.hase.soprafs24.model.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class Playlist {

    private String playlistId;

    private String playlistName;

    private String playlistImageUrl;

    public Playlist(String playlistId) {
        this.playlistId = playlistId;
    }
}
