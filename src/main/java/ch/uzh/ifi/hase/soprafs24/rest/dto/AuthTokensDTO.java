package ch.uzh.ifi.hase.soprafs24.rest.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class AuthTokensDTO {

  private String sessionToken;

  private long userId;
}
