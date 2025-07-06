package com.example.ImageHandling.controller;

import com.example.ImageHandling.domains.dto.ContactRequest;
import com.example.ImageHandling.domains.Contact;
import com.example.ImageHandling.domains.dto.ErrorResponse;
import com.example.ImageHandling.services.ContactService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

import static com.example.ImageHandling.utils.ResponseUtil.createErrorResponse;
import static com.example.ImageHandling.utils.SortUtil.getSortingOrders;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService; // Injected ContactService dependency

    /**
     * GET endpoint to retrieve all contacts from the database.
     *
     * @return List of Contact objects
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllContacts(
        @RequestParam( defaultValue = "0" ) Integer pageNo,
        @RequestParam( defaultValue = "10" ) Integer pageSize,
        @RequestParam( defaultValue = "updatedAt,desc" ) String[] sort
    ) {
            List<Contact> contacts = contactService.getAllContactsWithPaginationSorting( pageNo, pageSize, Sort.by( getSortingOrders( sort ) ) );
            return ResponseEntity.ok( contacts );
    }

    /**
     * GET endpoint to retrieve a contact by ID from the database.
     *
     * @param id ID of the contact to retrieve
     * @return ResponseEntity with Contact object if found, or 404 Not Found if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getContactById(@PathVariable String id) {
            return contactService.getContactById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST endpoint to create a new contact based on the provided ContactRequest data.
     *
     * @param contactRequest Request body containing data to create a new contact
     * @return ResponseEntity with created Contact object and HTTP status 201 Created
     */
    @PostMapping("/create")
    public ResponseEntity<?> createContact(@RequestBody ContactRequest contactRequest) {
            // Map fields from ContactRequest to Contact entity
            Contact contact = Contact.builder().email( contactRequest.getEmail() ).name( contactRequest.getName() ).requestedBy( contactRequest.getRequestedBy() ).build();

            // Save contact using ContactService
            Contact createdContact = contactService.createContact(contact);
            return new ResponseEntity<>(createdContact, HttpStatus.CREATED);

    }

    /**
     * DELETE endpoint to delete a contact by ID.
     *
     * @param id ID of the contact to delete
     * @return ResponseEntity with HTTP status 204 No Content if successful
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable String id) {
            contactService.deleteContact(id);
            return ResponseEntity.noContent().build();
    }

    /**
     * PUT endpoint to update an existing contact based on the provided ContactRequest data.
     *
     * @param id             ID of the contact to update
     * @param contactRequest Request body containing updated data for the contact
     * @return ResponseEntity with updated Contact object if found, or 404 Not Found if contact doesn't exist
     */
    @PutMapping( "/update/{id}" )
    public ResponseEntity<?> updateContact( @PathVariable String id, @RequestBody ContactRequest contactRequest ) {
            Contact contact = Contact.builder().id( id ).email( contactRequest.getEmail() ).name( contactRequest.getName() ).requestedBy( contactRequest.getRequestedBy() ).build();
            Optional<Contact> updatedContact = contactService.updateContact( id, contact );
            return updatedContact.map( ResponseEntity::ok ).orElseGet( () -> ResponseEntity.notFound().build() );

    }
}
