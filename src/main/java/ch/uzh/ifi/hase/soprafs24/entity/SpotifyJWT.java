package ch.uzh.ifi.hase.soprafs24.entity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "SPOTIFYJWT")
@Getter
@Setter
public class SpotifyJWT {

    @Id
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private Integer expiresln;

    @Column(nullable = false)
    private String tokenType;

    @Column(nullable = false)
    private String scope;

    @OneToOne
    @MapsId
    @JoinColumn(name = "userId")
    private User user;
}
