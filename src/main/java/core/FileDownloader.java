package core;

import authentification.MSALService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileDownloader {
    MSALService authService;

    //String sharingUrl = "https://cbshd.sharepoint.com/:x:/r/sites/CP_Bertelsmann_BertelsmannKarriereseiten/Shared%20Documents/General/Penguin%20Random%20House/PRH_Workbook_CareerSite.xlsx?d=wf2adbd91ffea4cdb94f1091cc0e33e3c&csf=1&web=1&e=aAfvOA";
//String sharingUrl = "https://cbshd.sharepoint.com/:x:/r/sites/CP_Bertelsmann_BertelsmannKarriereseiten/_layouts/15/guestaccess.aspx?e=bN0Lb5&share=EZG9rfLq_9tMlPEJHMDjPjwBfBERLxZ2-oRZYVHTxztN-w";
    String sharingUrl = "https://microsoft2orgu.sharepoint.com/:x:/s/2OrgU/EaKmpegvX0hEoNB6BINIwHoB6bCryRgkS7pkT23l36PSOw?e=Dsf1Fr";

    public FileDownloader(MSALService authService_) {
        authService = authService_;
    }

    public String downloadFileName() {
        try {
            String url = MSALService.getMetaUrl(sharingUrl);

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + authService.getAccessToken());

            if (connection.getResponseCode() == 200) {
                System.out.println("FileDownloader.downloadFileName: SUCCESS");
            } else {
                System.out.println("FileDownloader.downloadFileName Response: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            System.out.println("FileDownloader.downloadFileName: " + e.getLocalizedMessage());
        }
        return null;
    }

    public void downloadFile() {

        try {
            String fullUrl = MSALService.getDownloadUrl(sharingUrl);
            String fileName = MSALService.getMetaUrl(sharingUrl);

            HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + authService.getAccessToken());

            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                {
                    Files.copy(inputStream, Paths.get("C:/Users/kilia/Downloads/my_excel_file.xlsx"), StandardCopyOption.REPLACE_EXISTING);
                }
                System.out.println("FileDownloader: SUCCESS");
            } else {
                System.out.println("FileDownloader Response: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            System.out.println("FileDownloader: " + e.getLocalizedMessage());
        }
    }
}
