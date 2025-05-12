package authentification;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;

import java.net.URI;
import java.util.concurrent.*;

public class MSALAuthService {

    private String accessToken;

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
}
