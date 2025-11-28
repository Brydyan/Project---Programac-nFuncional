package ec.edu.upse.backend.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class FirebaseStorageService {
    @Value("${firebase.credentials.path:}")
    private String credentialsPath;
    @Value("${firebase.bucket:}")
    private String bucketName;
    @Value("${firebase.credentials.json:}")
    private String credentialsJson;
    private Storage storage;

    private synchronized void init() throws IOException {
        if (this.storage != null)
            return;
        InputStream in;
        // 1. Try explicit JSON content from property/env
        String effectiveJson = credentialsJson;
        if (effectiveJson == null || effectiveJson.isEmpty()) {
            effectiveJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
        }

        if (effectiveJson != null && !effectiveJson.isEmpty()) {
            in = new java.io.ByteArrayInputStream(effectiveJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else {
            // 2. Fallback to file path
            // Prefer explicit property, fall back to env var FIREBASE_CREDENTIALS_PATH,
            // then classpath
            String effectivePath = credentialsPath;
            if (effectivePath == null || effectivePath.isEmpty()) {
                effectivePath = System.getenv("FIREBASE_CREDENTIALS_PATH");
            }
            if (effectivePath != null && !effectivePath.isEmpty()) {
                try {
                    in = new java.io.FileInputStream(effectivePath);
                } catch (IOException e) {
                    throw new IOException("Could not open Firebase credentials file at " + effectivePath, e);
                }
            } else {
                // Try to load from classpath
                in = FirebaseStorageService.class.getResourceAsStream("/firebase-service-account.json");
                if (in == null)
                    throw new IOException(
                            "No Firebase credentials provided (checked property json, env var json, property path, env var path and classpath)");
            }
        }
        GoogleCredentials creds = GoogleCredentials.fromStream(in);
        this.storage = StorageOptions.newBuilder().setCredentials(creds).build().getService();
    }

    public Map<String, String> uploadUserAvatar(String userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("file empty");
        if (this.storage == null)
            init();
        if (bucketName == null || bucketName.isEmpty())
            throw new IllegalStateException("firebase.bucket not configured");
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String path = String.format("users/%s/avatar_%d%s", userId, Instant.now().toEpochMilli(), ext);
        BlobId blobId = BlobId.of(bucketName, path);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        try (InputStream is = file.getInputStream()) {
            storage.create(blobInfo, is);
            // make public
            storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        }
        String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, path);
        Map<String, String> result = new HashMap<>();
        result.put("photoUrl", publicUrl);
        result.put("photoPath", path);
        return result;
    }
}
