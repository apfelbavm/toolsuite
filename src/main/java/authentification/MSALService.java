package authentification;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.*;

public class MSALAuthService {

    private String accessToken;
    GraphServiceClient graphClient;

    public String getAccessToken() {
        return accessToken;
    }

    /*https://learn.microsoft.com/en-us/entra/msal/java/getting-started/client-applications*/
    public boolean login() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future future = executor.submit(tryLogin);
        executor.shutdown();

        try {
            future.get(5, TimeUnit.MINUTES);
            createGraphClient();
            return true;
        } catch (InterruptedException ie) {
            System.out.println("Interrupted Exception");
        } catch (ExecutionException ee) {
            System.out.println("Error: " + ee.getMessage());
        } catch (TimeoutException te) {
            System.out.println("Login timed out");
        }
        if (!executor.isTerminated()) {
            executor.shutdownNow();
        }
        return false;
    }

    void createGraphClient() {
        final DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
                .clientId(MSALConfig.clientId).tenantId(MSALConfig.tenantId).challengeConsumer(challenge -> {
                    // Display challenge to the user
                    System.out.println(challenge.getMessage());
                }).build();

        if (credential != null) {
            graphClient = new GraphServiceClient(credential, MSALConfig.scope.toArray(new String[0]));
        }
    }

    public void getUser() {
        if (graphClient != null) {
            User me = graphClient.me().get();
            System.out.println("displayName" + me.getDisplayName());
        } else {
            System.out.println("void getUser ERROR");
        }
    }

    final Runnable tryLogin = new Thread() {
        @Override
        public void run() {
            try {
                PublicClientApplication app = PublicClientApplication.builder(MSALConfig.clientId).build();
                InteractiveRequestParameters parameters = InteractiveRequestParameters.builder(new URI(MSALConfig.loginRedirectUrl)).scopes(MSALConfig.scope).build();
                CompletableFuture<IAuthenticationResult> future = app.acquireToken(parameters);
                IAuthenticationResult result = future.get();
                accessToken = result.accessToken();
                System.out.println("Access Token: " + accessToken);
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    };

    static String getURLWithoutParameters(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null, // Ignore the query part of the input url
                uri.getFragment()).toString();
    }

    /*
    @sharingUrl eg. https://microsoft2orgu.sharepoint.com/:x:/s/2OrgU/EaKmpegvX0hEoNB6BINIwHoB6bCryRgkS7pkT23l36PSOw?e=Dsf1Fr"
    * */
    public static String getDownloadUrl(String sharingUrl) {
        sharingUrl = parseSharingUrl(sharingUrl);
        if (sharingUrl != null && !sharingUrl.isBlank()) {
            return "https://graph.microsoft.com/v1.0/shares/" + sharingUrl + "/driveItem/content";
        }
        return null;
    }

    public static String getMetaUrl(String sharingUrl) {
        sharingUrl = parseSharingUrl(sharingUrl);
        if (sharingUrl != null && !sharingUrl.isBlank()) {
            return "https://graph.microsoft.com/v1.0/shares/" + sharingUrl;
        }
        return null;
    }

    static String parseSharingUrl(String sharingUrl) {
        try {
            sharingUrl = getURLWithoutParameters(sharingUrl);

            String base64Value = Base64.getUrlEncoder().encodeToString(sharingUrl.getBytes(StandardCharsets.UTF_8));
            String encodedUrl = "u!" + base64Value.replace('/', '_').replace('+', '-');

            while (encodedUrl.length() > 0 && encodedUrl.charAt(encodedUrl.length() - 1) == '=') {
                encodedUrl = encodedUrl.substring(0, encodedUrl.length() - 1);
            }
            return encodedUrl;
        } catch (
                Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }
}
