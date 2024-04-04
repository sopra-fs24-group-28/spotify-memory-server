package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthPostCodeDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthTokensDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    private final AuthService authService;

    public AuthController(SpotifyService spotifyService, AuthService authService) {
        this.spotifyService = spotifyService;
        this.authService = authService;
    }

    @PostMapping("/auth/token")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AuthTokensDTO getAccessTokenFromCode(@RequestBody AuthPostCodeDTO AuthPostCodeDTO) {
        User user = authService.authenticateFromCode(AuthPostCodeDTO.getCode());

        AuthTokensDTO response = new AuthTokensDTO();
        response.setSessionToken(user.getSessionToken());

        return response;
    }

    @DeleteMapping("/auth/token")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void revokeSessionToken(@RequestHeader("Authorization") String sessionHeader) {
        authService.logout(sessionHeader);
    }
}
