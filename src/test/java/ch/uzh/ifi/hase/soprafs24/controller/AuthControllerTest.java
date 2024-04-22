package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthPostCodeDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthTokensDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GetAccessTokenDTO;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private SpotifyService spotifyService;

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setUsername("testUsername");
        user.setUserId(1L);
        user.setSessionToken("token");

        MockitoAnnotations.openMocks(this);

        Mockito.when(authService.getUserBySessionToken(Mockito.any())).thenReturn(user);
    }

    @Test
    public void givenValidCodeAndUser_ReturnAuthTokenDTO() throws Exception {
        User user = new User();
        user.setUsername("testUsername");
        user.setUserId(1L);
        user.setSessionToken("token");

        AuthPostCodeDTO authPostCodeDTO = new AuthPostCodeDTO();

        given(authService.authenticateFromCode(authPostCodeDTO.getCode())).willReturn(user);

        MockHttpServletRequestBuilder postRequest = post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(authPostCodeDTO));

        AuthTokensDTO expectedResponse = new AuthTokensDTO(user.getSessionToken(), user.getUserId());

        mockMvc.perform(postRequest)
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(expectedResponse)));
    }

    @Test
    public void whenGetAccessToken_thenReturnAccessTokenDTO() throws Exception {
        GetAccessTokenDTO expectedResponse = new GetAccessTokenDTO();
        expectedResponse.setAccessToken("access-token-sample");

        given(authService.getAccessToken()).willReturn("access-token-sample");

        mockMvc.perform(get("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(expectedResponse)));
    }

    @Test
    public void whenRevokeSessionToken_thenReturnStatusOk() throws Exception {
        doNothing().when(authService).logout();

        mockMvc.perform(delete("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON).header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }





    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
