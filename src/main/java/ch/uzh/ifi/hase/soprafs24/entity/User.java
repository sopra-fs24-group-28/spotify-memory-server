package ch.uzh.ifi.hase.soprafs24.entity;

import lombok.Getter;
import lombok.Setter;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;

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
@Getter
@Setter
public class User implements Serializable {

  @Id
  @GeneratedValue
  private Long userId;

  @Column(nullable = false, unique = true)
  private String spotifyUserId;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(unique = true)
  private String sessionToken;

  private UserStatus state;

  @OneToOne(mappedBy = "user")
  @PrimaryKeyJoinColumn
  private SpotifyJWT spotifyJWT;

}
