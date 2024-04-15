package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistCollectionDTO;
import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Spotify Service
 * This class is the "worker" and responsible for all functionality related to
 * the spotify api (e.g., it gets access tokens and spotify content)
 */
@Service
@Transactional
@AllArgsConstructor
public class SpotifyService {

    private final Logger log = LoggerFactory.getLogger(SpotifyService.class);

    private static final String clientId = "5aac3ff5093942be92372c19a12fdecd";

    //private static final String clientSecret = "clientSecret";
    private static final String clientSecret = System.getenv("clientSecret");
    //private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:3000/auth_callback");
    private static final URI redirectUri = SpotifyHttpManager.makeUri(System.getenv("redirectURL"));

    private static final SpotifyApi spotifyApiAuth = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectUri(redirectUri)
            .build();

    private UserRepository userRepository;

    public static AuthorizationCodeCredentials authorizationCode_Sync(String code) {
        final AuthorizationCodeRequest authorizationCodeRequest = spotifyApiAuth.authorizationCode(code).build();
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            // spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            // spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            // System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());

            return authorizationCodeCredentials;
        }
        catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The authorization code is invalid: " + e.getMessage());
        }
    }


    public static HashMap<String, String> getUserData(String accessToken) {

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();

        final GetCurrentUsersProfileRequest profileRequest = spotifyApi.getCurrentUsersProfile().build();

        HashMap<String, String> spotifyUserData = new HashMap<String, String>();

        try {
            // Execute the request synchronous
            final User userProfile = profileRequest.execute();

            spotifyUserData.put("id", userProfile.getId());
            spotifyUserData.put("display_name", userProfile.getDisplayName());
            spotifyUserData.put("product", userProfile.getProduct().getType());

        } catch (Exception e) {
            System.out.println("Something went wrong!\n" + e.getMessage());
        }

        return spotifyUserData;
    }

    public static PlaylistCollectionDTO getUserPlaylistNames(String accessToken){
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();
        final String userid = getUserData(accessToken).get("id");
        final GetListOfUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfUsersPlaylists(userid).build();

        try {
            final Paging<PlaylistSimplified> playlistSimplifiedPaging = playlistsRequest.execute();
            List<PlaylistDTO> playlists = new ArrayList<>();
            for (PlaylistSimplified playlist : playlistSimplifiedPaging.getItems()) {
                String playlistName = playlist.getName();
                String playlistId = playlist.getId();
                playlists.add(new PlaylistDTO(playlistName, playlistId));
            }
            PlaylistCollectionDTO playlistCollectionDTO = new PlaylistCollectionDTO();
            playlistCollectionDTO.setPlaylists(playlists);

            return playlistCollectionDTO;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }






    }
}
