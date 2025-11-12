package ec.edu.upse.backend.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.upse.backend.Entity.ContactEntity;
import ec.edu.upse.backend.Service.ContactService;

@RestController
@RequestMapping("/app/v1/contacts")
public class ContactController {
    @Autowired
    private ContactService contactService;
    @PostMapping
    public ResponseEntity<ContactEntity> create(@RequestBody ContactEntity contact) {
        return ResponseEntity.ok(contactService.save(contact));
    }
    @GetMapping
    public ResponseEntity<List<ContactEntity>> getAll() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ContactEntity> getById(@PathVariable String id) {
        return contactService.getContactById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = contactService.deleteContact(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
