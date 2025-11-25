package ec.edu.upse.backend.Controller;

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

import ec.edu.upse.backend.Entity.ChannelEntity;
import ec.edu.upse.backend.Service.ChannelService;

@RestController
@RequestMapping("/app/v1/channels")
public class ChannelController {
    /**
     * sevicio para manejar las operaciones de canales
     * tales como crear, obtener, actualizar y eliminar canales
     * está inyectado automáticamente por Spring
     * esta clase utiliza ChannelService para realizar operaciones relacionadas con canales
     * ademas se usa en los endpoints definidos en este controlador
     */
    @Autowired
    private ChannelService channelService;
    @PostMapping
    public ResponseEntity<ChannelEntity> createChannel(@RequestBody ChannelEntity channel) {
        return ResponseEntity.ok(channelService.save(channel));
    }
    @GetMapping
    public ResponseEntity<List<ChannelEntity>> getAllChannels() {
        return ResponseEntity.ok(channelService.getAllChannels());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ChannelEntity> getChannelById(@PathVariable String id) {
        return channelService.getChannelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<ChannelEntity> updateChannel(@PathVariable String id, @RequestBody ChannelEntity newData) {
        ChannelEntity updated = channelService.updateChannel(id, newData);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable String id) {
        boolean deleted = channelService.deleteChannel(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/public")
    public ResponseEntity<List<ChannelEntity>> searchPublicChannels(String search) {
        return ResponseEntity.ok(channelService.searchPublic(search));
    }

    @PostMapping("/{id}/join/{userId}")
    public ResponseEntity<ChannelEntity> joinChannel(
            @PathVariable String id,
            @PathVariable String userId
    ) {
        ChannelEntity updated = channelService.joinChannel(id, userId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<ChannelEntity>> searchPublic(
        @RequestParam String name) {
        // El frontend espera que este endpoint devuelva solo canales públicos y no necesita el userId 
        // si el filtrado por miembro ya se hace en getAllChannels. Dejamos este como estaba.
        return ResponseEntity.ok(channelService.searchPublic(name));
    }
    @GetMapping("/member/{userId}")
    public ResponseEntity<List<ChannelEntity>> getChannelsByMember(@PathVariable String userId) {
        // Llama al nuevo servicio, pasando el userId para el filtrado
        return ResponseEntity.ok(channelService.getChannelsByMemberId(userId)); 
    }
}
