package authentification;

import java.util.Collections;
import java.util.Set;

public class MSALConfig {
    public static final String clientId = "4f1b439f-4528-4f17-a448-432b30fccbbc";
    public static final String tenantId = "72e7f916-be33-4c9a-9af8-870cb122d285";
    public static final String authority = "https://login.microsoftonline.com/" + tenantId + "/";
    public static final Set<String> scope = Set.of("Files.ReadWrite.All", "Sites.ReadWrite.All", "User.Read");
    public static final String loginRedirectUrl = "http://localhost";
    public static final String userUrl = "https://graph.microsoft.com/v1.0/me";
}
// , "FileStorageContainer.Selected"

