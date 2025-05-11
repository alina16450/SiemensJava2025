package com.siemens.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testFindAllIds() {
        Item item1 = new Item(null, "Item1", "desc", "NEW", "item1@example.com");
        Item item2 = new Item(null, "Item2", "desc", "NEW", "item2@example.com");

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Long> ids = itemRepository.findAllIds();

        assertThat(ids).containsExactlyInAnyOrder(item1.getId(), item2.getId());
    }
}
