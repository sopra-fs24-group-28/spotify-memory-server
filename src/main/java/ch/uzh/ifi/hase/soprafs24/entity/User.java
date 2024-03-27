package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import org.hibernate.stat.Statistics;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
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
@Document(collection = "USER")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  private Long userId;

  @Indexed(unique = true) // nullable false
  private String username;

  @Indexed(unique = true) // nullable false
  private String sessionToken;

  // nullable NOT able in mongoDB => validation needed
  private UserStatus state;

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


}
