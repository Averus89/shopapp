package pl.dexbtyes.shopapp.dto;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class ProductTest {
    @Test
    void shouldCreateObject() {
        Product p = new Product(50, "apple");

        assertThat(p).isNotNull();
        assertThat(p.name()).isEqualTo("apple");
        assertThat(p.basePrice()).isEqualTo(50);
    }

    @Test
    void builderShouldCreateProperObject() {
        Product built = Product.builder()
                .name("apple")
                .basePrice(50)
                .build();
        Product expected = new Product(50, "apple");

        assertThat(built).isEqualTo(expected);
    }
}