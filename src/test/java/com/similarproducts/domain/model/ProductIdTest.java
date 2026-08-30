package com.similarproducts.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductIdTest {

    @Test
    void createsProductIdFromNonBlankValue() {
        ProductId productId = new ProductId("42");

        assertEquals("42", productId.value());
    }

    @Test
    void rejectsBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> new ProductId("  "));
    }

    @Test
    void rejectsNullValue() {
        assertThrows(IllegalArgumentException.class, () -> new ProductId(null));
    }
}
