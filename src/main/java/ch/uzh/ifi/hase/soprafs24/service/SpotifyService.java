package ch.uzh.ifi.hase.soprafs24.service;

import com.google.gson.JsonParser;

import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistCollectionDTO;
import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.player.PauseUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;
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

    private UserService userService;

    public static AuthorizationCodeCredentials authorizationCode_Sync(String code) {
        final String clientId = "5aac3ff5093942be92372c19a12fdecd";
        //private static final String clientSecret = "clientSecret";
        final String clientSecret = System.getenv("clientSecret");
        //private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:3000/auth_callback");
        final URI redirectUri = SpotifyHttpManager.makeUri(System.getenv("redirectURL"));

        final SpotifyApi spotifyApiAuth = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).setRedirectUri(redirectUri).build();

        final AuthorizationCodeRequest authorizationCodeRequest = spotifyApiAuth.authorizationCode(code).build();
        try {
            return authorizationCodeRequest.execute();
        }
        catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "The authorization code is invalid: " + e.getMessage());
        }
    }
    
    public static HashMap<String, String> getUserData(String accessToken) {

        SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final GetCurrentUsersProfileRequest profileRequest = spotifyApi.getCurrentUsersProfile().build();

        HashMap<String, String> spotifyUserData = new HashMap<>();

        try {
            // Execute the request synchronous
            final User userProfile = profileRequest.execute();

            spotifyUserData.put("id", userProfile.getId());
            spotifyUserData.put("display_name", userProfile.getDisplayName());
            spotifyUserData.put("product", userProfile.getProduct().getType());
            spotifyUserData.put("image_url", getHighestResolutionImage(userProfile.getImages()));

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 1)!\n" + e.getMessage());
        }

        return spotifyUserData;
    }

    public static HashMap<String, String> getPlaylistMetadata(String accessToken, String playlistId) {

        SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(playlistId).build();

        HashMap<String, String> playlistMetadata = new HashMap<>();

        try {
            // Execute the request synchronous
            final Playlist playlist = playlistRequest.execute();

            playlistMetadata.put("playlist_name", playlist.getName());
            playlistMetadata.put("playlist_length", String.valueOf(playlist.getTracks().getItems().length));
            // add url to profile image (if available, otherwise default placeholder)
            try {
                playlistMetadata.put("image_url", playlist.getImages()[0].getUrl());
            }
            catch (Exception e) {
                playlistMetadata.put("image_url", "https://onedrive.live.com/embed?resid=3FDF6D9F7AFE5B85%21109887&authkey=%21ANjdWjIEnjkmc5A&width=360&height=360");
            }

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 2)!\n" + e.getMessage());
        }

        return playlistMetadata;
    }

    public static PlaylistCollectionDTO getUserPlaylistNames(String accessToken) {

        SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

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
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 3)!\n" + e.getMessage());
        }
    }

    public static ArrayList<ArrayList<String>> getPlaylistData(String accessToken, String playlistId, Integer numOfSongs) {

        SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final GetPlaylistsItemsRequest playlistRequest = spotifyApi.getPlaylistsItems(playlistId).build();

        ArrayList<ArrayList<String>> songs = null;
        try {
            // Execute the request synchronous
            final Paging<PlaylistTrack> playlistTrackPaging = playlistRequest.execute();

            songs = parsePlaylistTrackPaging(playlistTrackPaging, accessToken, numOfSongs);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 4)!\n" + e.getMessage());
        }
        return songs;
    }

    public static void setSong(String accessToken, String deviceId, String trackId) {

        final SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final StartResumeUsersPlaybackRequest startResumeUsersPlaybackRequest = spotifyApi
                .startResumeUsersPlayback()
                .device_id(deviceId)
                .uris(JsonParser.parseString("[\"spotify:track:" + trackId + "\"]").getAsJsonArray())
                .position_ms(0)
                .build();

        final PauseUsersPlaybackRequest pauseUsersPlaybackRequest = spotifyApi.pauseUsersPlayback()
                .device_id(deviceId)
                .build();

        try {
            startResumeUsersPlaybackRequest.execute(); // also starts execution
            pauseUsersPlaybackRequest.execute(); // pauses execution
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 5)!\n" + e.getMessage());
        }
    }

    public void setDeviceId(String spotifyUserId, String deviceId) {
        userService.setSpotifyDeviceId(spotifyUserId, deviceId);
    }

    private static ArrayList<ArrayList<String>> parsePlaylistTrackPaging(Paging<PlaylistTrack> playlistTrackPaging, String accessToken, Integer numOfSongs) {
        try {
            // This function parses only the first page of the paginated PlaylistTrack! (seems to bee 100 songs)
            ArrayList<ArrayList<String>> songs = new ArrayList<>();

            int numSongs = Math.min(numOfSongs, playlistTrackPaging.getItems().length);

            for (int i = 0; i < numSongs; i++) {
                ArrayList<String> song = new ArrayList<>();
                String trackId = playlistTrackPaging.getItems()[i].getTrack().getId();
                song.add(trackId);
                song.add(getTrackAlbumCover(accessToken, trackId));
                songs.add(song);
            }
            return songs;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 6)!\n" + e.getMessage());
        }
    }

    private static String getTrackAlbumCover(String accessToken, String trackId) {

        SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final GetTrackRequest trackRequest = spotifyApi.getTrack(trackId).build();

        String trackAlbumCover = null;
        try {
            // Execute the request synchronous
            final Track track = trackRequest.execute();
            trackAlbumCover = track.getAlbum().getImages()[0].getUrl();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 7)!\n" + e.getMessage());
        }
        return trackAlbumCover;
    }

    private static String getHighestResolutionImage(Image[] images) {
        try {
            String default_image = "https://onedrive.live.com/embed?resid=3FDF6D9F7AFE5B85%21109886&authkey=%21AGmGcFZLnNAQFf4&width=640&height=640";
            if (images.length > 0) {
                int max_width = -1;
                String image_url = default_image;
                for (Image image : images) {
                    if (image.getWidth() > max_width) {
                        max_width = image.getWidth();
                        image_url = image.getUrl();
                    }
                }
                return image_url;
            }
            else {
                return default_image;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong (Code 8)!\n" + e.getMessage());
        }
    }
}
