package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import org.hibernate.stat.Statistics;
import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long userId;

  @Column(nullable = false, unique = true)
  private String spotifyUserId;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String sessionToken;

  private UserStatus state;

  @Transient
  private SpotifyJWT spotifyJWT;

  public String getSpotifyUserId() {
    return spotifyUserId;
  }

  public void setSpotifyUserId(String spotifyUserId) {
    this.spotifyUserId = spotifyUserId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long id) {
    this.userId = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getSessionToken() {
      return sessionToken;
  }

    public UserStatus getState() {
        return state;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setState(UserStatus state) {
        this.state = state;
    }

    public void setSpotifyJWT(SpotifyJWT spotifyJWT) {
      this.spotifyJWT = spotifyJWT;
    }

    public SpotifyJWT getSpotifyJWT() {
        return spotifyJWT;
    }
}
