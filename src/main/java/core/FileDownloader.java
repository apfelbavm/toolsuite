package core;

import authentification.MSALAuthService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileDownloader {
//    String downloadUrl = "https://cbshd.sharepoint.com/:x:/s/CP_Bertelsmann_BertelsmannKarriereseiten/EZG9rfLq_9tMlPEJHMDjPjwBfBERLxZ2-oRZYVHTxztN-w?e=bN0Lb5";
    String downloadUrl = "https://microsoft2orgu.sharepoint.com/:x:/s/2OrgU/EaKmpegvX0hEoNB6BINIwHoB6bCryRgkS7pkT23l36PSOw?e=vCLmrh";
    MSALAuthService authService;

    public FileDownloader(MSALAuthService authService_) {
        authService = authService_;
    }

    public void downloadFile() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + authService.getAccessToken());

            InputStream inputStream = connection.getInputStream();
            {
                Files.copy(inputStream, Paths.get("C:/Users/kilia/Downloads/my_excel_file.xlsx"), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("FileDownloader: SUCCESS");
        } catch (Exception e) {
            System.out.println("FileDownloader: " + e.getLocalizedMessage());
        }
    }
}
