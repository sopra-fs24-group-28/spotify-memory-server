package ch.uzh.ifi.hase.soprafs24.entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpotifyJWT {

    private String accessToken;

    private String refreshToken;

    private Integer expiresln;

    private String tokenType;

    private String scope;
}
