package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpotifyController.class)
public class SpotifyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private SpotifyService spotifyService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void givenValidToken_ReturnPlaylistCollectionDTO() throws Exception {
        try (MockedStatic<UserContextHolder> mocked_UCH = mockStatic(UserContextHolder.class)) {
            try (MockedStatic<SpotifyService> mocked_spotify = mockStatic(SpotifyService.class)) {
                // prepare a user
                SpotifyJWT spotifyJWT = new SpotifyJWT();
                spotifyJWT.setAccessToken("accessToken");

                User testUser = new User();
                testUser.setUsername("testUsername");
                testUser.setUserId(1L);
                testUser.setSessionToken("token");
                testUser.setSpotifyJWT(spotifyJWT);

                Mockito.when(authService.getUserBySessionToken(Mockito.any())).thenReturn(testUser);

                // prepare return data
                PlaylistCollectionDTO playlistCollectionDTO = new PlaylistCollectionDTO();

                List<PlaylistDTO> playlists = new ArrayList<>();
                playlists.add(new PlaylistDTO("Name1", "Id1"));
                playlists.add(new PlaylistDTO("Name2", "Id2"));

                playlistCollectionDTO.setPlaylists(playlists);

                given(UserContextHolder.getCurrentUser()).willReturn(testUser);
                given(SpotifyService.getUserPlaylistNames(Mockito.any())).willReturn(playlistCollectionDTO);

                mockMvc.perform(get("/spotify/user/playlist/names").header("Authorization", "Bearer token"))
                        .andExpect(status().isOk())
                        .andExpect(content().json(asJsonString(playlistCollectionDTO)));
            }
        }
    }

    @Test
    public void givenValidDeviceId_ReturnOK() throws Exception {
        try (MockedStatic<UserContextHolder> mocked_UCH = mockStatic(UserContextHolder.class)) {
            User testUser = new User();
            testUser.setUsername("testUsername");
            testUser.setUserId(1L);
            testUser.setSessionToken("token");

            Mockito.when(authService.getUserBySessionToken(Mockito.any())).thenReturn(testUser);
            given(UserContextHolder.getCurrentUser()).willReturn(testUser);
            doNothing().when(spotifyService).setDeviceId(Mockito.any(), Mockito.any());

            mockMvc.perform(post("/spotify/user/deviceid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer token")
                            .content("deviceid")
                    )
                    .andExpect(status().isOk());
        }
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
