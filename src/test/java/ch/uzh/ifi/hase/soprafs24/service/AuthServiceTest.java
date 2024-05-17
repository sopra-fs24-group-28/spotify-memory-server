package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    @Mock
    private UserContextHolder userContextHolder;

    @Mock
    private UserService userService;

    @Mock
    private AuthorizationCodeCredentials authorizationCodeCredentials;

    @InjectMocks
    private AuthService authService;


    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testUser = new User();
        testUser.setSpotifyUserId("testSpotifyUserId");
        testUser.setUsername("testUsername");

        // mock userService functionalities
        Mockito.when(userService.loginUser(Mockito.any(), Mockito.any())).thenReturn(testUser);
        Mockito.when(userService.createUser(Mockito.any())).thenReturn(testUser);
        Mockito.when(userService.logoutUser(Mockito.any())).thenReturn(testUser);
        Mockito.when(userService.getUserBySessionToken(Mockito.any())).thenReturn(testUser);
    }


    @Test
    void authenticateFromCode_validInputs_existingUser_success() {
        try (MockedStatic<SpotifyService> mocked = mockStatic(SpotifyService.class)) {
            HashMap<String, String> spotifyUserData = new HashMap<>();
            spotifyUserData.put("id", "testSpotifyUserId");
            spotifyUserData.put("display_name", "testUsername");
            spotifyUserData.put("product", "premium");

            when(SpotifyService.authorizationCode_Sync(Mockito.any())).thenReturn(authorizationCodeCredentials);
            when(SpotifyService.getUserData(Mockito.any())).thenReturn(spotifyUserData);
            Mockito.when(userService.userExists(Mockito.any())).thenReturn(true);

            User authenticatedUser = authService.authenticateFromCode("code");

            assertEquals(authenticatedUser.getUserId(), testUser.getUserId());
            assertEquals(authenticatedUser.getSpotifyUserId(), testUser.getSpotifyUserId());
        }
    }

    @Test
    void authenticateFromCode_validInputs_newUser_success() {
        try (MockedStatic<SpotifyService> mocked = mockStatic(SpotifyService.class)) {
            HashMap<String, String> spotifyUserData = new HashMap<>();
            spotifyUserData.put("id", "testSpotifyUserId");
            spotifyUserData.put("display_name", "testUsername");
            spotifyUserData.put("product", "premium");

            when(SpotifyService.authorizationCode_Sync(Mockito.any())).thenReturn(authorizationCodeCredentials);
            when(SpotifyService.getUserData(Mockito.any())).thenReturn(spotifyUserData);
            Mockito.when(userService.userExists(Mockito.any())).thenReturn(false);

            User authenticatedUser = authService.authenticateFromCode("code");

            assertEquals(authenticatedUser.getUserId(), testUser.getUserId());
            assertEquals(authenticatedUser.getSpotifyUserId(), testUser.getSpotifyUserId());
        }
    }

    @Test
    void authenticateFromCode_noPremiumAccount_throwsError() {
        try (MockedStatic<SpotifyService> mocked = mockStatic(SpotifyService.class)) {
            HashMap<String, String> spotifyUserData = new HashMap<>();
            spotifyUserData.put("id", "testId");
            spotifyUserData.put("display_name", "testName");
            spotifyUserData.put("product", "free");

            when(SpotifyService.authorizationCode_Sync(Mockito.any())).thenReturn(authorizationCodeCredentials);
            when(SpotifyService.getUserData(Mockito.any())).thenReturn(spotifyUserData);

            ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> authService.authenticateFromCode("code"));
            assertEquals(HttpStatus.FORBIDDEN, thrown.getStatus());
        }
    }

    @Test
    void getAccessToken_validInput_success() {
        try (MockedStatic<UserContextHolder> mocked = mockStatic(UserContextHolder.class)) {
            // add spotifyJWT to testUser
            SpotifyJWT spotifyJWT = new SpotifyJWT();
            spotifyJWT.setAccessToken("accessToken");
            testUser.setSpotifyJWT(spotifyJWT);

            Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

            assertEquals(authService.getAccessToken(), testUser.getSpotifyJWT().getAccessToken());
        }
    }

    @Test
    void getAccessToken_missingJWT_error() {
        try (MockedStatic<UserContextHolder> mocked = mockStatic(UserContextHolder.class)) {
            // add spotifyJWT to testUser as null
            testUser.setSpotifyJWT(null);

            Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

            ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> authService.getAccessToken());
            assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatus());
        }
    }

    @Test
    void getUserBySessionToken_validInput_success() {
        try (MockedStatic<UserContextHolder> mocked = mockStatic(UserContextHolder.class)) {
            User foundUser = authService.getUserBySessionToken("header");

            assertEquals(foundUser.getSpotifyUserId(), testUser.getSpotifyUserId());
            assertEquals(foundUser.getUsername(), testUser.getUsername());
        }
    }
}
