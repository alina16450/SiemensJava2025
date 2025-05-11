package com.siemens.internship;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    private final Item mockItem = new Item(1L, "Test", "Test Desc", "NEW", "a@b.com");

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<Item> items = List.of(mockItem);
        when(itemRepository.findAll()).thenReturn(items);

        List<Item> result = itemService.findAll();

        assertThat(result).containsExactlyElementsOf(items);
    }

    @Test
    void testFindById_Found() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));

        Optional<Item> result = itemService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockItem);
    }

    @Test
    void testFindById_NotFound() {
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Item> result = itemService.findById(2L);

        assertThat(result).isNotPresent();
    }

    @Test
    void testSave() {
        when(itemRepository.save(mockItem)).thenReturn(mockItem);

        Item saved = itemService.save(mockItem);

        assertThat(saved).isEqualTo(mockItem);
    }

    @Test
    void testDeleteById() {
        doNothing().when(itemRepository).deleteById(1L);

        itemService.deleteById(1L);

        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void testProcessItem_Success() {
        Item expected = new Item(1L, "Test", "Desc", "PROCESSED", "a@b.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));
        when(itemRepository.save(any(Item.class))).thenReturn(expected);

        Item result = itemService.processItem(1L);

        assertThat(result.getStatus()).isEqualTo("PROCESSED");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void testProcessItem_NotFound() {
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.processItem(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void testProcessItemsAsync_Success() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        Item item1 = new Item(1L, "A", "desc", "NEW", "a@b.com");
        Item item2 = new Item(2L, "B", "desc", "NEW", "b@b.com");

        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item i = invocation.getArgument(0);
            i.setStatus("PROCESSED");
            return i;
        });

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get(5, TimeUnit.SECONDS);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(item -> "PROCESSED".equals(item.getStatus()));
    }

    @Test
    void testProcessItemsAsync_WithMissingItem() {
        List<Long> ids = List.of(1L);
        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();

        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }
}
