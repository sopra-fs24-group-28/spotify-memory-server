package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.SpotifyJWTRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    /*@Mock
    private UserRepository userRepository;*/

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
    public void authenticateFromCode_validInputs_existingUser_success() throws Exception {
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
    public void authenticateFromCode_validInputs_newUser_success() throws Exception {
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
    public void authenticateFromCode_noPremiumAccount_throwsError() throws Exception {
        try (MockedStatic<SpotifyService> mocked = mockStatic(SpotifyService.class)) {
            HashMap<String, String> spotifyUserData = new HashMap<>();
            spotifyUserData.put("id", "testId");
            spotifyUserData.put("display_name", "testName");
            spotifyUserData.put("product", "free");

            when(SpotifyService.authorizationCode_Sync(Mockito.any())).thenReturn(authorizationCodeCredentials);
            when(SpotifyService.getUserData(Mockito.any())).thenReturn(spotifyUserData);

            ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> authService.authenticateFromCode("code"));
            assertEquals(thrown.getStatus(), HttpStatus.FORBIDDEN);
        }
    }

    @Test
    public void getAccessToken_validInput_success() throws Exception {
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
    public void getAccessToken_missingJWT_error() throws Exception {
        try (MockedStatic<UserContextHolder> mocked = mockStatic(UserContextHolder.class)) {
            // add spotifyJWT to testUser as null
            testUser.setSpotifyJWT(null);

            Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

            ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> authService.getAccessToken());
            assertEquals(thrown.getStatus(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    public void logout_validInput_success() throws Exception {
        try (MockedStatic<UserContextHolder> mocked = mockStatic(UserContextHolder.class)) {
            Mockito.when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
            authService.logout();
        }
    }

    @Test
    public void getUserBySessionToken_validInput_success() throws Exception {
        try (MockedStatic<UserContextHolder> mocked = mockStatic(UserContextHolder.class)) {
            User foundUser = authService.getUserBySessionToken("header");

            assertEquals(foundUser.getSpotifyUserId(), testUser.getSpotifyUserId());
            assertEquals(foundUser.getUsername(), testUser.getUsername());
        }
    }
}
