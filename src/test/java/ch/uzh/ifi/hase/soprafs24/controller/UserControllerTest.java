package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserStatsDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.StatsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.persistence.PersistenceException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private StatsService statsService;

    @MockBean
    private AuthService authService;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setUserId(1L);
        testUser.setSessionToken("token");

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getUserProfile_returnProfile() throws Exception {
        // set expected results
        PlayerDTO testUserDTO = new PlayerDTO(testUser);
        UserStatsDTO testStatsDTO = new UserStatsDTO(
                testUser.getUserId(), 6, 3, 2, 1, 10L
        );
        UserGetDTO expectedResponse = new UserGetDTO(testUserDTO, testStatsDTO);

        Mockito.when(authService.getUserBySessionToken(Mockito.any())).thenReturn(testUser);
        Mockito.when(userService.getPlayerDTOForCurrentUser()).thenReturn(testUserDTO);
        Mockito.when(statsService.getCurrentUserStats()).thenReturn(testStatsDTO);

        MockHttpServletRequestBuilder getRequest = get("/users/profiles")
                .contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer token");

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(expectedResponse)));
    }

    @Test
    public void getUserProfile_returnException() throws Exception {
        // set expected results
        PlayerDTO testUserDTO = new PlayerDTO(testUser);
        UserStatsDTO worngTestStatsDTO = new UserStatsDTO(
                testUser.getUserId(), 7, 3, 2, 1, 10L
        );

        Mockito.when(authService.getUserBySessionToken(Mockito.any())).thenReturn(testUser);
        Mockito.when(userService.getPlayerDTOForCurrentUser()).thenReturn(testUserDTO);
        Mockito.when(statsService.getCurrentUserStats()).thenThrow(PersistenceException.class);

        MockHttpServletRequestBuilder getRequest = get("/users/profiles")
                .contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer token");

        mockMvc.perform(getRequest)
                .andExpect(status().isBadRequest());
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}