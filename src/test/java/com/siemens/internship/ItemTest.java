package com.siemens.internship;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testValidEmail() {
        Item item = new Item();
        String validEmail = "user.name@example.com";
        item.setEmail(validEmail);
        assertEquals(validEmail, item.getEmail());
    }

    @Test
    public void testInvalidEmailThrowsException() {
        Item item = new Item();
        String invalidEmail1 = "invalid-email@.com";
        String invalidEmail2 = "invalid-email@a.";
        String invalidEmail3 = "_invalid-email@@a.com";

        assertThrows(IllegalArgumentException.class, () -> {
            item.setEmail(invalidEmail1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            item.setEmail(invalidEmail2);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            item.setEmail(invalidEmail3);
        });
    }
}
