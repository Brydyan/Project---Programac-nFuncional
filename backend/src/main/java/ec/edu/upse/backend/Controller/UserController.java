package ec.edu.upse.backend.Controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Entity.UserEntity;
import ec.edu.upse.backend.Service.UserService;
import ec.edu.upse.backend.dto.UserProfileDto;
import ec.edu.upse.backend.dto.UserSummaryDto;

import ec.edu.upse.backend.dto.UserProfileDto;
import ec.edu.upse.backend.dto.UserSettingsDto;
import ec.edu.upse.backend.Entity.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/app/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

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

  // ========= PERFIL =========
    @GetMapping("/profile/me")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication auth) {
        String authUserId = auth.getName(); // aquí asumes que el subject del JWT es el ID del user
        return userService.getUserById(authUserId)
            .map(this::toProfileDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(
            @RequestBody UserProfileDto dto,
            Authentication auth
    ) {
        String authUserId = auth.getName();

        return userService.getUserById(authUserId)
            .map(user -> {
                user.setDisplayName(dto.getDisplayName());
                user.setBio(dto.getBio());
                user.setStatusMessage(dto.getStatusMessage());
                // user.setAvatarUrl(dto.getAvatarUrl());

                UserEntity saved = userService.save(user);
                return ResponseEntity.ok(toProfileDto(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }


    // ========= SETTINGS =========

    @GetMapping("/settings/me")
    public ResponseEntity<UserSettingsDto> getMySettings(Authentication auth) {
        String authUserId = auth.getName();
        return userService.getUserById(authUserId)
            .map(this::toSettingsDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/settings/me")
    public ResponseEntity<UserSettingsDto> updateMySettings(
            @RequestBody UserSettingsDto dto,
            Authentication auth
    ) {
    String authUserId = auth.getName();

    return userService.getUserById(authUserId)
        .map(user -> {
                user.setNotificationsActivate(dto.getNotificationsActivate());
                user.setNotificationsSound(dto.getNotificationsSound());
                user.setNotificationsDesktop(dto.getNotificationsDesktop());

                user.setDarkMode(dto.getDarkMode());
                user.setFontSize(dto.getFontSize());

                user.setInterfaceLanguage(dto.getInterfaceLanguage());
                user.setTimezone(dto.getTimezone());

                UserEntity saved = userService.save(user);
                return ResponseEntity.ok(toSettingsDto(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // ========= MAPPERS PRIVADOS =========

    private UserProfileDto toProfileDto(UserEntity user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setStatusMessage(user.getStatusMessage());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    private UserSettingsDto toSettingsDto(UserEntity user) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setId(user.getId());
        dto.setNotificationsActivate(user.getNotificationsActivate());
        dto.setNotificationsSound(user.getNotificationsSound());
        dto.setNotificationsDesktop(user.getNotificationsDesktop());
        dto.setDarkMode(user.getDarkMode());
        dto.setFontSize(user.getFontSize());
        dto.setInterfaceLanguage(user.getInterfaceLanguage());
        dto.setTimezone(user.getTimezone());
        return dto;
    }
}

