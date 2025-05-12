package core;

import authentification.MSALAuthService;
import authentification.MSALConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ProfileManager {
    MSALAuthService authService;

    public ProfileManager(MSALAuthService authService_) {
        authService = authService_;
    }

    public void getProfile() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(MSALConfig.userUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + authService.getAccessToken());

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                Scanner scanner = new Scanner(connection.getInputStream()).useDelimiter("\\A");

                String response = scanner.hasNext() ? scanner.next() : "";
                System.out.println("User Info: " + response);

            } else {
                System.out.println("ProfileManager: code= " + responseCode);
            }

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
