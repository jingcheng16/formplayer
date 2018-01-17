package auth;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import util.StringUtils;

import java.nio.charset.Charset;

/**
 * Created by willpride on 1/13/16.
 */
public class BasicAuth implements HqAuth {
    private final String username;
    private final String password;

    public BasicAuth(String asUsername, String password){
        this.username = asUsername;
        this.password = password;
    }

    @Override
    public HttpHeaders getAuthHeaders() {
        return new HttpHeaders(){
            {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.encodeBase64(
                        auth.getBytes(Charset.forName("US-ASCII")) );
                String authHeader = "Basic " + new String( encodedAuth );
                set( "Authorization", authHeader );
            }
        };
    }

    @Override
    public String toString(){
        return "BasicAuth [username=" + username +
                ", password=" + password + "]";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
