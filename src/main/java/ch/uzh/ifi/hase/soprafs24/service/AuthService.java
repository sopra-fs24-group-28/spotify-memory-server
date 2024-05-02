package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import lombok.AllArgsConstructor;
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

    private UserService userService;
    private GameService gameService;

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
        return createOrLogin(spotifyUserData.get("id"), spotifyUserData.get("display_name"), spotifyUserData.get("image_url"), spotifyJWT);
    }

    public String getAccessToken() {
        User user = UserContextHolder.getCurrentUser();

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

    private User createOrLogin(String spotifyUserId, String username, String image_url, SpotifyJWT spotifyJWT) {
        User userToCreateOrLogin = new User();
        userToCreateOrLogin.setSpotifyUserId(spotifyUserId);
        userToCreateOrLogin.setUsername(username);
        userToCreateOrLogin.setImageUrl(image_url);

        if (userService.userExists(userToCreateOrLogin)) {
            if (userToCreateOrLogin.getCurrentGameId() != null) {
                gameService.removePlayerFromGame(userToCreateOrLogin.getCurrentGameId());
            }
            userService.updateUser(userToCreateOrLogin);
            return userService.loginUser(userToCreateOrLogin.getSpotifyUserId(), spotifyJWT);
        } else {
            User createdUser = userService.createUser(userToCreateOrLogin);
            return userService.loginUser(createdUser.getSpotifyUserId(), spotifyJWT);
        }
    }

    public void logout() {
        try {
            User user = UserContextHolder.getCurrentUser();
            if (user.getCurrentGameId() != null) {
                gameService.removePlayerFromGame(user.getCurrentGameId());
            }
            userService.logoutUser(user);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The unexpected error: " + e.getMessage());
        }
    }

    public User getUserBySessionToken(String authHeader) {
        return userService.getUserBySessionToken(authHeader);
    }

}
