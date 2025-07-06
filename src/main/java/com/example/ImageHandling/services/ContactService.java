package com.example.ImageHandling.services;

import com.example.ImageHandling.domains.Contact;
import com.example.ImageHandling.domains.repository.ContactRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Service class for handling Contacts.
 */
@AllArgsConstructor
@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;

    /**
     * Retrieves all contacts from the repository.
     * @return List of all contacts
     */
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public List<Contact> getAllContactsWithPaginationSorting(Integer pageNo, Integer pageSize, Sort sort) {
        logger.info("Fetching all contacts with pagination and sorting - PageNo: {}, PageSize: {}, Sort: {}", pageNo, pageSize, sort);
        Pageable paging = PageRequest.of(pageNo, pageSize, sort);
        Page<Contact> pageResult = contactRepository.findAll( paging );
        if (pageResult.hasContent()) {
            return pageResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a contact by its ID from the repository.
     * @param id ID of the contact to retrieve
     * @return Optional containing the retrieved contact, or empty if not found
     */
    public Optional<Contact> getContactById(String id) {
        logger.info("Fetching contact with id {}", id);
        return contactRepository.findById(id);
    }

    /**
     * Creates a new contact in the repository.
     * @param contact Contact object to create
     * @return The created contact
     */
    public Contact createContact(Contact contact) {
        logger.info("Creating contact {}. id: {}", contact.getName(), contact.getId());
        return contactRepository.save(contact);
    }

    /**
     * Deletes a contact from the repository by its ID.
     * @param id ID of the contact to delete
     */
    public void deleteContact(String id) {
        logger.info("Deleting contact with id {}", id);
        contactRepository.deleteById(id);
    }

    /**
     * Updates an existing contact in the repository.
     * @param id ID of the contact to update
     * @param newContact Updated Contact object
     * @return The updated contact
     */
	public Optional<Contact> updateContact( String id, Contact newContact ) {
        logger.info("Updating contact with id {}", id);
		return Optional.ofNullable( getContactById( id ).isPresent() ? contactRepository.save( newContact ) : null );
	}
}
