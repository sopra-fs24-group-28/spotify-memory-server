package ch.uzh.ifi.hase.soprafs24.entity;

public class SpotifyJWT {

    private String accessToken;

    private String refreshToken;

    private Integer expiresln;

    private String tokenType;

    private String scope;


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getExpiresln() {
        return expiresln;
    }

    public void setExpiresln(Integer expiresln) {
        this.expiresln = expiresln;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
