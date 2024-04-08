package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.SpotifyJWTRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
@AllArgsConstructor
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private UserRepository userRepository;
  private SpotifyJWTRepository spotifyJWTRepository;

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setState(UserStatus.OFFLINE);
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public User loginUser(String spotifyUserId, SpotifyJWT spotifyJWT) {
      User user = userRepository.findBySpotifyUserId(spotifyUserId);

      // save the spotifyJWT first
      spotifyJWT.setUser(user);
      spotifyJWT = spotifyJWTRepository.save(spotifyJWT);
      spotifyJWTRepository.flush();

      // save the user with the spotifyJWT
      user.setSessionToken(UUID.randomUUID().toString());
      user.setState(UserStatus.ONLINE);
      user.setSpotifyJWT(spotifyJWT);
      user = userRepository.save(user);
      userRepository.flush();
      return user;
  }

  public User logoutUser(User user) {
      // delete the spotifyJWT
      SpotifyJWT spotifyJWTToDelete = spotifyJWTRepository.findByUserId(user.getUserId());
      spotifyJWTRepository.delete(spotifyJWTToDelete);

      // change user state
      user.setState(UserStatus.OFFLINE);
      user.setSessionToken(null);
      user.setSpotifyJWT(null);
      user = userRepository.save(user);
      userRepository.flush();
      return user;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userBySpotifyId = userRepository.findBySpotifyUserId(userToBeCreated.getSpotifyUserId());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userBySpotifyId != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "spotifyuserid", "is"));
    }
  }

  public boolean userExists(User user) {
      User userBySpotifyId = userRepository.findBySpotifyUserId(user.getSpotifyUserId());

      // returns true if user exists
      return userBySpotifyId != null;
  }
}
