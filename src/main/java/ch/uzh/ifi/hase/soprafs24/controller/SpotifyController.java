package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.AuthPostCodeDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistCollectionDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("spotify")
@AllArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @GetMapping("user/playlist/names")
    public ResponseEntity<PlaylistCollectionDTO> getSpotifyUserId() {
        User user = UserContextHolder.getCurrentUser();
        // Call the service method and return its response
        PlaylistCollectionDTO dto = SpotifyService.getUserPlaylistNames(user.getSpotifyJWT().getAccessToken());

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/user/deviceid")
    @ResponseStatus(HttpStatus.OK)
    public void setSpotifyDeviceId(@RequestBody String deviceId) {
        User user = UserContextHolder.getCurrentUser();
        spotifyService.setDeviceId(user.getSpotifyUserId(), deviceId);
    }
}




