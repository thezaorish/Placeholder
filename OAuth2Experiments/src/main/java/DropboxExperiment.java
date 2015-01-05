import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.json.simple.JSONValue;

import static spark.Spark.get;
import static spark.SparkBase.setPort;

/**
 * Created by zaorish on 27/12/14.
 * reference => https://www.dropbox.com/developers/blog/45/using-oauth-20-with-the-core-api
 */
public class DropboxExperiment {

    public static final String OAUTH2_AUTHORIZE = "https://www.dropbox.com/1/oauth2/authorize";
    public static final String OAUTH2_TOKEN = "https://api.dropbox.com/1/oauth2/token";
    public static final String ACCOUNT_INFO = "https://api.dropbox.com/1/account/info";

    public static void main(String[] args) throws IOException {
        Properties prop = getProperties();
        new DropboxExperiment(prop.getProperty("key"), prop.getProperty("secret"));
    }
    private static Properties getProperties() throws IOException {
        Properties prop = new Properties();
        InputStream input;

        String filename = "sensitive.properties";
        input = DropboxExperiment.class.getClassLoader().getResourceAsStream(filename);
        prop.load(input);
        return prop;
    }

    public DropboxExperiment(String key, String secret) {
        setPort(8196);
        initializeRoutes(key, secret);
    }
    private void initializeRoutes(String key, String secret) {
        get("/", (request, response) -> {
            try {
                // begin authorization
                response.redirect(new URIBuilder(OAUTH2_AUTHORIZE)
                        .addParameter("client_id", key)
                        .addParameter("response_type", "code")
                        .addParameter("redirect_uri", new URIBuilder(request.url()).setPath("/callback").build().toString())
                        .build().toString());

                return null;
            } catch (Exception ex) {
                return "something went wrong: " + ex.toString();
            }
        });

        get("/callback", (request, response) -> {
            String authorizationCode = request.queryParams("code");

            try {
                // convert the authorization code to an access token
                Map json = (Map) JSONValue.parse(
                        Request.Post(OAUTH2_TOKEN)
                                .bodyForm(Form.form()
                                        .add("code", authorizationCode)
                                        .add("grant_type", "authorization_code")
                                        .add("redirect_uri", new URIBuilder(request.url()).setPath("/callback").build().toString())
                                        .build())
                                .addHeader("Authorization", "Basic " + Base64.encodeBase64String((key + ":" + secret).getBytes()))
                                .execute().returnContent().asString());
                String accessToken = (String) json.get("access_token");

                // use the access token to get basic account info
                json = (Map) JSONValue.parse(
                        Request.Get(ACCOUNT_INFO)
                                .addHeader("Authorization", "Bearer " + accessToken)
                                .execute().returnContent().asString());

                return String.format("Successfully authenticated as %s.", json.get("display_name"));
            } catch (Exception ex) {
                return "something went wrong: " + ex.toString();
            }
        });
    }

}
