package ch.uzh.ifi.hase.soprafs24.service;

import com.google.gson.JsonParser;

import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistCollectionDTO;
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
import java.util.Map;

/**
 * Spotify Service
 * This class is the "worker" and responsible for all functionality related to
 * the spotify api (e.g., it gets access tokens and spotify content)
 */
@Service
@Transactional
public class SpotifyService {

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The authorization code is invalid: " + e.getMessage());
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

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong (getUserData)!\n" + e.getMessage());
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
            playlistMetadata.put("image_url", playlist.getImages()[0].getUrl());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong (getPlaylistMetadata)!\n" + e.getMessage());
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong (getUserPlaylistNames)!\n" + e.getMessage());
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong (getPlaylistData)!\n" + e.getMessage());
        }
        return songs;
    }

    public static void setSong(String accessToken, String trackId) {

        final SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

        final StartResumeUsersPlaybackRequest startResumeUsersPlaybackRequest = spotifyApi
                .startResumeUsersPlayback()
                //.device_id("5fbb3ba6aa454b5534c4ba43a8c7e8e45a63ad0e")
                .uris(JsonParser.parseString("[\"spotify:track:" + trackId + "\"]").getAsJsonArray())
                .position_ms(0)
                .build();

        final PauseUsersPlaybackRequest pauseUsersPlaybackRequest = spotifyApi.pauseUsersPlayback()
                //.device_id("5fbb3ba6aa454b5534c4ba43a8c7e8e45a63ad0e")
                .build();

        try {
            startResumeUsersPlaybackRequest.execute(); // also starts execution
            pauseUsersPlaybackRequest.execute(); // pauses execution
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong (setSong)!\n" + e.getMessage());
        }
    }

    private static ArrayList<ArrayList<String>> parsePlaylistTrackPaging(Paging<PlaylistTrack> playlistTrackPaging, String accessToken, Integer numOfSongs) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong (getTrackAlbumCover)!\n" + e.getMessage());
        }
        return trackAlbumCover;
    }
}
