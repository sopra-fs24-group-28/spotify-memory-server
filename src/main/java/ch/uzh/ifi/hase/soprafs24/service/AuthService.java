package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.util.HashMap;

/**
 * Authentication Service
 * This class is the "worker" and responsible for all functionality related to
 * the authentication
 */
@Service
@Transactional
@AllArgsConstructor
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    private UserRepository userRepository;
    private UserService userService;

    public User authenticateFromCode(String code) {
        AuthorizationCodeCredentials authorizationCodeCredentials = getAuthorizationCodeCredentials(code);
        HashMap<String,String> spotifyUserData = getSpotifyUserData(authorizationCodeCredentials.getAccessToken());

        // make sure user has a premium account
        if(!spotifyUserData.get("product").equals("premium")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing premium account.");
        }

        SpotifyJWT spotifyJWT = new SpotifyJWT();
        spotifyJWT.setAccessToken(authorizationCodeCredentials.getAccessToken());
        spotifyJWT.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        spotifyJWT.setScope(authorizationCodeCredentials.getScope());
        spotifyJWT.setTokenType(authorizationCodeCredentials.getTokenType());
        spotifyJWT.setExpiresln(authorizationCodeCredentials.getExpiresIn());

        // create and/or login the user
        User user = createOrLogin(spotifyUserData.get("id"), spotifyUserData.get("display_name"), spotifyJWT);

        return user;
    }

    public String getAccessToken(String sessionHeader) {
        String sessionToken = sessionHeader.substring(7);
        User user = userRepository.findBySessionToken(sessionToken);

        try {
            return user.getSpotifyJWT().getAccessToken();
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The unexpected error: " + e.getMessage());
        }
    }

    private AuthorizationCodeCredentials getAuthorizationCodeCredentials(String code){
        return SpotifyService.authorizationCode_Sync(code);
    }

    private HashMap<String,String> getSpotifyUserData(String accessToken){
        return SpotifyService.getUserData(accessToken);
    }

    private User createOrLogin(String spotifyUserId, String username, SpotifyJWT spotifyJWT) {
        User userToCreateOrLogin = new User();
        userToCreateOrLogin.setSpotifyUserId(spotifyUserId);
        userToCreateOrLogin.setUsername(username);

        if (userService.userExists(userToCreateOrLogin)) {
            return userService.loginUser(userToCreateOrLogin.getSpotifyUserId(), spotifyJWT);
        } else {
            User createdUser = userService.createUser(userToCreateOrLogin);
            return userService.loginUser(createdUser.getSpotifyUserId(), spotifyJWT);
        }
    }

    public void logout(String sessionHeader) {
        try{
            String sessionToken = sessionHeader.substring(7);
            User logoutUser = userRepository.findBySessionToken(sessionToken);

            userService.logoutUser(logoutUser);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The unexpected error: " + e.getMessage());
        }
    }

    public Boolean validateUserBySessionTokenAndSetContext(String sessionHeader) {
        String sessionToken = sessionHeader.substring(7);

        return true;
    }

}
