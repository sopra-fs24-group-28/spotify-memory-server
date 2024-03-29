package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.util.HashMap;

/**
 * Authentication Service
 * This class is the "worker" and responsible for all functionality related to
 * the authentication
 */
@Service
@Transactional
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserService userService;
    @Autowired
    public AuthService(@Qualifier("userRepository") UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public AuthorizationCodeCredentials getAuthorizationCodeCredentials(String code){
        return SpotifyService.authorizationCode_Sync(code);
    }

    public HashMap<String,String> getSpotifyUserData(String accessToken){
        return SpotifyService.getUserData(accessToken);
    }

    public User createOrLogin(String spotifyUserId, String username, SpotifyJWT spotifyJWT) {
        User userToCreateOrLogin = new User();
        userToCreateOrLogin.setSpotifyUserId(spotifyUserId);
        userToCreateOrLogin.setUsername(username);

        if (userService.userExists(userToCreateOrLogin)) {
            return userService.loginUser(userToCreateOrLogin.getSpotifyUserId(), spotifyJWT);
        } else {
            userToCreateOrLogin.setUserId(1L);
            User createdUser = userService.createUser(userToCreateOrLogin);
            return userService.loginUser(createdUser.getSpotifyUserId(), spotifyJWT);
        }
    }
}
