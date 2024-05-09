package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserStatsDTO;
import ch.uzh.ifi.hase.soprafs24.service.StatsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final StatsService statsService;

    @GetMapping("/profiles")
    @ResponseStatus(HttpStatus.OK)
    public UserGetDTO getUserProfile(){
        PlayerDTO currentUserDTD = userService.getPlayerDTOForCurrentUser();
        UserStatsDTO userStatsDTO = statsService.getCurrentUserStats();
        return new UserGetDTO(currentUserDTD, userStatsDTO);
    }



}
