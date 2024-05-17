package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerDTO {
    private Long userId;
    private String username;
    private String imageUrl;

    public PlayerDTO(User user) {
        userId = user.getUserId();
        username = user.getUsername();
        imageUrl = user.getImageUrl();
    }
}
