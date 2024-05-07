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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpotifyJWTRepository spotifyJWTRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testUser = new User();
        testUser.setSpotifyUserId("testSpotifyUserId");
        testUser.setUsername("testUsername");

        // when -> any object is being save in the userRepository -> return the dummy
        // testUser
        when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }

    @Test
    public void createUser_validInputs_success() {
        // create the user
        User createdUser = userService.createUser(testUser);

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        // assert user status
        assertEquals(testUser.getSpotifyUserId(), createdUser.getSpotifyUserId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNull(createdUser.getSpotifyJWT());
        assertEquals(UserStatus.OFFLINE, createdUser.getState());
    }

    @Test
    public void createUser_duplicateUserId_throwsException() {
        // given -> a first user has already been created
        userService.createUser(testUser);

        // when -> setup additional mocks for UserRepository
        when(userRepository.findBySpotifyUserId(Mockito.any())).thenReturn(testUser);

        // then -> attempt to create second user with same username -> check that an error
        // is thrown
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
        assertEquals(thrown.getStatus(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateUser_validInputs_success() {
        // create the user
        User createdUser = userService.createUser(testUser);
        when(userRepository.findBySpotifyUserId(Mockito.any())).thenReturn(testUser);

        User updatedUser = createdUser;
        updatedUser.setUsername("newUsername");
        updatedUser.setImageUrl("newImageUrl");
        when(userRepository.save(Mockito.any())).thenReturn(updatedUser);
        updatedUser = userService.updateUser(updatedUser);

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(2)).save(Mockito.any());

        // assert user status
        assertEquals(updatedUser.getSpotifyUserId(), createdUser.getSpotifyUserId());
        assertEquals(updatedUser.getUsername(), "newUsername");
        assertEquals(updatedUser.getImageUrl(), "newImageUrl");
    }

    @Test
    public void loginUser_validInputs_noLogout_success() {
        // mock repository functions
        SpotifyJWT spotifyJWT = new SpotifyJWT();
        spotifyJWT.setUser(testUser);
        spotifyJWT.setAccessToken("AccessToken");
        spotifyJWT.setRefreshToken("RefreshToken");
        spotifyJWT.setScope("Scope");
        spotifyJWT.setTokenType("Bearer");
        spotifyJWT.setExpiresln(3600);

        when(userRepository.findBySpotifyUserId(Mockito.any())).thenReturn(testUser);
        when(spotifyJWTRepository.save(Mockito.any())).thenReturn(spotifyJWT);

        // login the user with an empty SpotifyJWT
        User loggedInUser = userService.loginUser(testUser.getUsername(), spotifyJWT);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(spotifyJWTRepository, Mockito.times(1)).save(Mockito.any());

        // assert user status
        assertEquals(testUser.getSpotifyUserId(), loggedInUser.getSpotifyUserId());
        assertNotNull(loggedInUser.getSpotifyJWT());
        assertNotNull(loggedInUser.getSessionToken());
        assertEquals(UserStatus.ONLINE, loggedInUser.getState());
        assertEquals(testUser, loggedInUser.getSpotifyJWT().getUser());
        assertEquals("AccessToken", loggedInUser.getSpotifyJWT().getAccessToken());
        assertEquals("RefreshToken", loggedInUser.getSpotifyJWT().getRefreshToken());
        assertEquals("Scope", loggedInUser.getSpotifyJWT().getScope());
        assertEquals("Bearer", loggedInUser.getSpotifyJWT().getTokenType());
        assertEquals(3600, loggedInUser.getSpotifyJWT().getExpiresln());
        assertEquals(testUser.getUserId(), loggedInUser.getSpotifyJWT().getUserId());
    }

    @Test
    public void loginUser_validInputs_withLogout_success() {
        // mock repository functions
        SpotifyJWT spotifyJWT = new SpotifyJWT();
        when(userRepository.findBySpotifyUserId(Mockito.any())).thenReturn(testUser);
        when(spotifyJWTRepository.save(Mockito.any())).thenReturn(spotifyJWT);

        // login the user a first time and make sure it's status is online
        User loggedInUser = userService.loginUser("testSpotifyUserId", spotifyJWT);
        assertEquals(UserStatus.ONLINE, loggedInUser.getState());

        // login the same user a second time
        User twiceLoggedInUser = userService.loginUser("testSpotifyUserId", spotifyJWT);

        // make sure userRepository is saved three times (first login, logout, second login)
        Mockito.verify(userRepository, Mockito.times(3)).save(Mockito.any());

        // assert user status after second login
        assertEquals(UserStatus.ONLINE, twiceLoggedInUser.getState());
        assertEquals("testSpotifyUserId", twiceLoggedInUser.getSpotifyUserId());
        assertNotNull(twiceLoggedInUser.getSpotifyJWT());
        assertNotNull(twiceLoggedInUser.getSessionToken());
    }

    @Test
    public void logoutUser_validInputs_success() {
        // set some data manually & mock repository function
        SpotifyJWT spotifyJWT = new SpotifyJWT();
        testUser.setState(UserStatus.ONLINE);
        testUser.setSessionToken("sessionToken");
        testUser.setSpotifyJWT(spotifyJWT);
        when(userRepository.findByUserId(Mockito.any())).thenReturn(testUser);

        // logout the user
        User loggedOutUser = userService.logoutUser(testUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        // assert user status
        assertEquals(testUser.getSpotifyUserId(), loggedOutUser.getSpotifyUserId());
        assertNull(loggedOutUser.getSpotifyJWT());
        assertNull(loggedOutUser.getSessionToken());
        assertNull(loggedOutUser.getSpotifyDeviceId());
        assertEquals(UserStatus.OFFLINE, loggedOutUser.getState());
    }

    @Test
    public void findUserByUserId_validInputs_success() {
        // mock repository functions & add data
        testUser.setSessionToken("sessionToken");
        when(userRepository.findByUserId(Mockito.any())).thenReturn(testUser);

        User foundUser = userService.findUserByUserId(testUser.getUserId());

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any());

        // assert user status
        assertEquals(testUser.getSpotifyUserId(), foundUser.getSpotifyUserId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertNull(foundUser.getSpotifyJWT());
        assertNull(foundUser.getState());
    }

    @Test
    public void userExists_validInputs_success() {
        // mock repository functions
        when(userRepository.findBySpotifyUserId(Mockito.any())).thenReturn(testUser);

        // assert user status
        assertTrue(userService.userExists(testUser));
    }

    @Test
    public void getUserBySessionToken_validInputs_success() {
        // mock repository functions
        when(userRepository.findBySessionToken(Mockito.any())).thenReturn(testUser);

        User foundUser = userService.getUserBySessionToken(testUser.getSessionToken());

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any());

        // assert user status
        assertEquals(testUser.getSpotifyUserId(), foundUser.getSpotifyUserId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertNull(foundUser.getSpotifyJWT());
        assertNull(foundUser.getState());
    }

    @Test
    public void setPlayerState_validInputs_success() {
        // mock repository functions
        when(userRepository.saveAndFlush(Mockito.any())).thenReturn(testUser);

        // change user state
        User updatedUser = userService.setPlayerState(testUser, UserStatus.INGAME);

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(Mockito.any());

        // assert user status
        assertEquals(testUser.getSpotifyUserId(), updatedUser.getSpotifyUserId());
        assertEquals(testUser.getUsername(), updatedUser.getUsername());
        assertEquals(updatedUser.getState(), UserStatus.INGAME);
    }

    @Test
    public void setSpotifyDeviceId_validInputs_success() {
        // mock repository functions
        when(userRepository.findBySpotifyUserId(Mockito.any())).thenReturn(testUser);

        // change user state
        User updatedUser = userService.setSpotifyDeviceId(testUser.getSpotifyUserId(), "deviceId");

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

        // assert user status
        assertEquals(testUser.getSpotifyUserId(), updatedUser.getSpotifyUserId());
        assertEquals(updatedUser.getSpotifyDeviceId(), "deviceId");
    }

    @Test
    public void getPlayerDTOListFromListOfUsers_validInputs_success() {
        // prepare list of users
        List <User> users = new ArrayList<>();
        users.add(testUser);

        // create list of playerDTO
        List<PlayerDTO> playerDTOs = userService.getPlayerDTOListFromListOfUsers(users);

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any());

        // assert user status
        assertEquals(1, playerDTOs.size());
        assertEquals(testUser.getUsername(), playerDTOs.get(0).getUsername());
        assertEquals(testUser.getUserId(), playerDTOs.get(0).getUserId());
    }

    @Test
    public void getPlayerDTOForCurrentUser_validInputs_success() {
        // prepare currentUser
        when(UserContextHolder.getCurrentUser()).thenReturn(testUser);

        // create playerDTO
        PlayerDTO playerDTO = userService.getPlayerDTOForCurrentUser();

        // check userRepository is called once only
        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any());

        // assert user status
        assertEquals(testUser.getUsername(), playerDTO.getUsername());
        assertEquals(testUser.getUserId(), playerDTO.getUserId());
    }
}
