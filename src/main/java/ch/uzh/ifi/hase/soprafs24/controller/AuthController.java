package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthPostCodeDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthTokensDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.util.HashMap;

import static ch.uzh.ifi.hase.soprafs24.service.AuthService.getAuthorizationCodeCredentials;


/**
 * Auth Controller
 * This class is responsible for handling all REST request that are related to
 * user authentication (login/logout).
 * The controller will receive the request and delegate the execution to the
 * SpotifyService and UserService.
 */


@RestController
public class AuthController {

    private final SpotifyService spotifyService;

    public AuthController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @PostMapping("/auth/token")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AuthTokensDTO getAccessTokenFromCode(@RequestBody AuthPostCodeDTO AuthPostCodeDTO) {
        AuthorizationCodeCredentials authorizationCodeCredentials = getAuthorizationCodeCredentials(AuthPostCodeDTO.getCode());
        HashMap<String,String> spotifyUserData = AuthService.getSpotifyUserData(authorizationCodeCredentials.getAccessToken());

        // make sure user has a premium account
        if(!spotifyUserData.get("product").equals("premium")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing premium account.");
        }

        // add method to check whether user exists in DB

        // create user if not exists

        // get user session token

        AuthTokensDTO response = new AuthTokensDTO();
        response.setSessionToken("sessionToken");

        return response;
    }
}
