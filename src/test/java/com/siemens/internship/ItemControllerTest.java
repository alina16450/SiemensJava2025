package com.siemens.internship;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;

import java.util.*;

class ItemControllerTest {

    @InjectMocks
    private ItemController itemController;

    @Mock
    private ItemService itemService;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllItems() {
        List<Item> mockItems = List.of(new Item(1L, "item1", "desc", "NEW", "a@b.com"));
        when(itemService.findAll()).thenReturn(mockItems);

        ResponseEntity<List<Item>> response = itemController.getAllItems();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyElementsOf(mockItems);
    }

    @Test
    void testCreateItem_Valid() {
        Item item = new Item(null, "item1", "desc", "NEW", "a@b.com");
        when(bindingResult.hasErrors()).thenReturn(false);
        when(itemService.save(item)).thenReturn(item);

        ResponseEntity<Item> response = itemController.createItem(item, bindingResult);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(item);
    }

    @Test
    void testCreateItem_Invalid() {
        Item item = new Item();
        when(bindingResult.hasErrors()).thenReturn(true);

        ResponseEntity<Item> response = itemController.createItem(item, bindingResult);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testGetItemById_Found() {
        Item item = new Item(1L, "item1", "desc", "NEW", "a@b.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Item> response = itemController.getItemById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(item);
    }

    @Test
    void testGetItemById_NotFound() {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Item> response = itemController.getItemById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateItem_Found() {
        Item item = new Item(1L, "updated", "desc", "PROCESSED", "x@y.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));
        when(itemService.save(item)).thenReturn(item);

        ResponseEntity<Item> response = itemController.updateItem(1L, item);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(item);
    }

    @Test
    void testUpdateItem_NotFound() {
        Item item = new Item();
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Item> response = itemController.updateItem(1L, item);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteItem_Found() {
        Item item = new Item(1L, "item", "desc", "NEW", "email@test.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemService).deleteById(1L);

        ResponseEntity<Void> response = itemController.deleteItem(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testDeleteItem_NotFound() {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = itemController.deleteItem(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
