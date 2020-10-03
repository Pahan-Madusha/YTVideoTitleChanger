package yt.details.refresher.auth; /**
 * Sample Java code for youtube.videos.list
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/guides/code_samples#java
 */

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

public class AuthHandler
{
    private static final String CLIENT_SECRETS= "client_secret.json";
    private static final Collection<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/youtube.readonly",
            "https://www.googleapis.com/auth/youtube.force-ssl");
    private static final String APPLICATION_NAME = "Youtube data manager";
    private static final String tokenServerUrl = "https://oauth2.googleapis.com/token";
    private static final long tokenTtl = 540000L;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private Credential credential = null;
    private YouTube youtubeService = null;
    private long tokenStartTime;

    /**
     * Use this when popup authentication is not possible (running in a remote server)
     * Authenticate using main method and use the returned tokens as arguments to run TitleChanger
     * @param accessToken
     * @param refreshToken
     * @param expirationTime
     */
    public AuthHandler( String accessToken, String refreshToken, long expirationTime )
    {
        try
        {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            InputStream in = AuthHandler.class.getResourceAsStream(CLIENT_SECRETS);
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            credential = new Credential.Builder( BearerToken.authorizationHeaderAccessMethod() )
                    .setClock( System::currentTimeMillis )
                    .setTransport( httpTransport )
                    .setClientAuthentication( new ClientParametersAuthentication( clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret() ) )
                    .setJsonFactory( JSON_FACTORY )
                    .setTokenServerUrl( new GenericUrl( tokenServerUrl ) )
                    .build();

            credential.setAccessToken( accessToken );
            credential.setExpirationTimeMilliseconds( expirationTime );
            credential.setRefreshToken( refreshToken );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Use this if popup authentication is possible
     */
    public AuthHandler()
    {
        try
        {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            credential = authorize(httpTransport);
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public Credential authorize(final NetHttpTransport httpTransport) throws Exception {
        // Load client secrets.
        InputStream in = AuthHandler.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .setAccessType( "offline" )
                        .build();
        LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder().setHost( "localhost" ).setPort( 8080 ).build();

        return new AuthorizationCodeInstalledApp(flow, localServerReceiver ).authorize("user");
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    private YouTube getService() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        tokenStartTime = System.currentTimeMillis();
        youtubeService = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        return youtubeService;
    }

    /**
     * If ttl is not expired, return same youtubeService, refresh otherwise
     * @return
     * @throws Exception
     */
    public YouTube getYoutubeService() throws Exception
    {
        if( System.currentTimeMillis() - tokenStartTime > tokenTtl )
        {
            return getService();
        }
        return youtubeService;
    }


    public static void main( String[] args )
    {
        AuthHandler authHandler = new AuthHandler();
        System.out.println( authHandler.credential.getAccessToken() + " " + authHandler.credential.getRefreshToken() + " " + authHandler.credential.getExpirationTimeMilliseconds() );
    }
}