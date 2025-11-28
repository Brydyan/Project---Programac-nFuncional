package ec.edu.upse.backend.Controller;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Service.FirebaseStorageService;
import ec.edu.upse.backend.Service.UserService;
import ec.edu.upse.backend.dto.UserSummaryDto;

@RestController
@RequestMapping("/app/v1/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    // CREATE
    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        return ResponseEntity.ok(userService.save(user));
    }

    // READ
    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserEntity> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable String id, @RequestBody UserEntity newUser) {
        UserEntity updated = userService.updateUser(id, newUser);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Actualizar foto de perfil: el cliente sube la imagen a Firebase Storage y envía la URL pública
    @PostMapping("/{id}/photo")
    public ResponseEntity<UserEntity> updatePhoto(@PathVariable String id, @RequestBody java.util.Map<String, String> body) {
        String photoUrl = body.get("photoUrl");
        String photoPath = body.get("photoPath");
        UserEntity updated = userService.updateUserPhoto(id, photoUrl, photoPath);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Endpoint para subir la foto directamente al backend y que éste la guarde en Firebase Storage
    @PostMapping(value = "/{id}/photo-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserEntity> uploadPhoto(@PathVariable String id, @RequestPart("file") MultipartFile file) {
        try {
            // read previous photoPath to attempt deletion later
            String previousPath = null;
            try {
                previousPath = userService.getUserById(id).map(u -> u.getPhotoPath()).orElse(null);
            } catch (Exception ex) {
                logger.warn("Could not fetch previous user photoPath for userId={}", id);
            }

            java.util.Map<String, String> res = firebaseStorageService.uploadUserAvatar(id, file);
            String photoUrl = res.get("photoUrl");
            String photoPath = res.get("photoPath");
            UserEntity updated = userService.updateUserPhoto(id, photoUrl, photoPath);

            // attempt to delete previous file from storage (best-effort)
            try {
                if (previousPath != null && previousPath.trim().length() > 0 && !previousPath.equals(photoPath)) {
                    boolean del = firebaseStorageService.deleteByPath(previousPath);
                    if (!del) logger.warn("Could not delete previous photo at path={} for userId={}", previousPath, id);
                }
            } catch (Exception ex) {
                logger.warn("Error deleting previous photo for userId={}: {}", id, ex.getMessage());
            }

            return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error uploading user photo for userId={}", id, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


    @GetMapping("/search")
    public ResponseEntity<List<UserSummaryDto>> searchUser(
        @RequestParam("q") String query, 
        @RequestParam(value = "excludeId", required = false) String excludeId){
            List<UserSummaryDto> result = userService.searchUsers(query, excludeId);
            return ResponseEntity.ok(result);
    }
    
    @GetMapping("/by-ids")
    public ResponseEntity<List<UserSummaryDto>> getUsersByIds(@RequestParam("ids") String idsCsv) {
        List<String> ids = Arrays.asList(idsCsv.split(","));
        List<UserSummaryDto> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }
}