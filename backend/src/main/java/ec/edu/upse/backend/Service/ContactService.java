package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.ContactEntity;
import ec.edu.upse.backend.Repository.ContactRepository;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    public ContactEntity save(ContactEntity contact) {
        return contactRepository.save(contact);
    }

    public List<ContactEntity> getAllContacts() {
        return contactRepository.findAll();
    }

    public Optional<ContactEntity> getContactById(String id) {
        return contactRepository.findById(id);
    }

    public boolean deleteContact(String id) {
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
