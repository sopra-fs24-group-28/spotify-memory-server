package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ProductType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SpotifyServiceTest {

    /*@Mock
    private SpotifyApi.Builder builder;

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private GetCurrentUsersProfileRequest profileRequest;

    @Mock
    private User userProfile;

    @Mock
    private ProductType productType;

    @InjectMocks
    private SpotifyService spotifyService;

    @BeforeEach
    public void setup() throws IOException, ParseException, SpotifyWebApiException {
        MockitoAnnotations.openMocks(this);

        // mock userService functionalities
        builder = Mockito.mock(SpotifyApi.Builder.class);

        // Mock SpotifyApi and other dependencies
        Mockito.when(builder.setAccessToken("accessToken")).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(spotifyApi);

        Mockito.when(spotifyApi.getCurrentUsersProfile().build()).thenReturn(profileRequest);
        Mockito.when(profileRequest.execute()).thenReturn(userProfile);
        Mockito.when(userProfile.getId()).thenReturn("TestId");
        Mockito.when(userProfile.getDisplayName()).thenReturn("TestName");
        Mockito.when(userProfile.getProduct()).thenReturn(productType);
        Mockito.when(productType.getType()).thenReturn("premium");
    }

    @Test
    void testGetPlaylistMetadata() {
        Map<String, String> userData = SpotifyService.getUserData("accessToken");

        // Assertions
        assertEquals("TestId", userData.get("id"));
        assertEquals("TestName", userData.get("display_name"));
        assertEquals("premium", userData.get("product"));
    }*/
}