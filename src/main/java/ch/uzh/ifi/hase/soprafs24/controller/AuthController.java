package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthPostCodeDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthTokensDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GetAccessTokenDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import lombok.AllArgsConstructor;
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
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final SpotifyService spotifyService;
    private final AuthService authService;

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AuthTokensDTO getAccessTokenFromCode(@RequestBody AuthPostCodeDTO AuthPostCodeDTO) {
        User user = authService.authenticateFromCode(AuthPostCodeDTO.getCode());

        return new AuthTokensDTO(user.getSessionToken(), user.getUserId());
    }

    @GetMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GetAccessTokenDTO getAccessToken() {
        GetAccessTokenDTO response = new GetAccessTokenDTO();
        response.setAccessToken(authService.getAccessToken());

        return response;
    }

    @DeleteMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void revokeSessionToken() {
        authService.logout();
    }
}
