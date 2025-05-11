package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * Retrieves all items from the database.
     * @return HTTP 200 OK with a list of all {@link Item} objects.
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /**
     * Creates a new item.
     * @param item the {@link Item} to create (must pass validation)
     * @param result holds validation results; if errors exist, returns 400
     * @return HTTP 201 CREATED with the saved item, or HTTP 400 BAD REQUEST if validation fails
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); //fixed the status codes for bad request vs successful request
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    /**
     * Returns a single item by its ID.
     * @param id the ID of the item to return
     * @return HTTP 200 OK with the item if found, or HTTP 404 NOT FOUND if not
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); //fixed the status code for not found instance
    }

    /**
     * Updates an existing item with new data after checking it exists.
     * @param id the ID of the item to update
     * @param item the updated {@link Item} object
     * @return HTTP 200 OK if update was successful, or HTTP 404 NOT FOUND if item doesn't exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK); //fixed status code to return 200 instead of Created, as this is an update.
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); //fixed status code to correctly show item not found.
        }
    }

    /**
     * Deletes an item by its ID after checking it exists.
     * @param id the ID of the item to delete
     * @return HTTP 204 NO CONTENT if deleted, or HTTP 404 NOT FOUND if item doesn't exist
     */
    //updated method to handle case when Id given is not found. Also fixed status code to reflect either the standard delete code, or not found if no id match.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Initiates asynchronous processing of all items (status set to "PROCESSED").
     * @return a {@link CompletableFuture} containing HTTP 200 OK with processed items,
     *         or HTTP 500 INTERNAL SERVER ERROR if processing fails
     */
    //updated method to update the return type, as well as ensuring it is returning asynchronously to avoid incorrect or premature responses.
    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync().thenApply(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
