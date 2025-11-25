package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Domain.ContactValidator;
import ec.edu.upse.backend.Entity.ContactEntity;
import ec.edu.upse.backend.Repository.ContactRepository;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    // CREATE
    public ContactEntity save(ContactEntity contact) {
        if (!ContactValidator.sonIdsValidos(contact.getUserId(), contact.getContactId())) {
            throw new IllegalArgumentException("Ids de contacto inválidos");
        }

        String normalizedState = ContactValidator.normalizarEstado(contact.getState());
        if (normalizedState == null) {
            throw new IllegalArgumentException("Estado de contacto inválido");
        }

        contact.setState(normalizedState);

        return contactRepository.save(contact);
    }

    // READ
    public List<ContactEntity> getAllContacts() {
        return contactRepository.findAll();
    }

    public Optional<ContactEntity> getContactById(String id) {
        return contactRepository.findById(id);
    }

    public List<ContactEntity> getContactsByUserId(String userId) {
        return contactRepository.findByUserId(userId);
    }

    public List<ContactEntity> getContactsByContactId(String contactId) {
        return contactRepository.findByContactId(contactId);
    }

    // UPDATE SOLO ESTADO (por ejemplo aceptar/bloquear)
    public ContactEntity updateContactState(String id, String newState) {
        Optional<ContactEntity> aux = contactRepository.findById(id);
        if (aux.isPresent()) {
            String normalized = ContactValidator.normalizarEstado(newState);
            if (normalized == null) {
                throw new IllegalArgumentException("Estado de contacto inválido");
            }
            ContactEntity contact = aux.get();
            contact.setState(normalized);
            return contactRepository.save(contact);
        }
        return null;
    }

    // DELETE
    public boolean deleteContact(String id) {
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
            return true;
        }
        return false;
    }
}