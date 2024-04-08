package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.SpotifyJWT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("spotifyJWTRepository")
public interface SpotifyJWTRepository extends JpaRepository<SpotifyJWT, Long> {

    SpotifyJWT findByUserId(Long userId);

}
