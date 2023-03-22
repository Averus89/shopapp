package pl.dexbtyes.shopapp.dto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {
    Product apple;
    Product orange;

    @BeforeEach
    void setUp() {
         apple = new Product(50, "apple");
         orange = new Product(70, "orange");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldCreateAnObject() {
        assertThat(new LineItem(apple, 0)).isNotNull();
    }

    @Test
    void lineItemsShouldBeEqual() {
        LineItem one = new LineItem(apple, 0);
        LineItem two = new LineItem(apple, 0);

        assertThat(one).isEqualTo(one);
        assertThat(one.equals(two)).isTrue(); //lombok sonar requirement to count as covered
        assertThat(one).isEqualTo(two);
    }

    @Test
    void lineItemsShouldNotBeEqual() {
        LineItem one = new LineItem(apple, 0);
        LineItem two = new LineItem(apple, 1);
        LineItem three = new LineItem(orange, 0);

        assertThat(one.equals(two)).isFalse(); //sonar coverage
        assertThat(one).isNotEqualTo(two);
        assertThat(two).isNotEqualTo(three);
        assertThat(one).isNotEqualTo(three);
    }

    @Test
    void toStringShouldProduceProperText() {
        LineItem one = new LineItem(apple, 0);

        assertThat(one.toString()).isEqualTo("LineItem(product=Product[basePrice=50, name=apple], discount=0)");
    }

    @Test
    void builderShouldProduceObject() {
        LineItem built = LineItem.builder()
                .product(apple)
                .discount(0)
                .build();
        LineItem expected = new LineItem(apple, 0);

        assertThat(built).isNotNull();
        assertThat(built).isEqualTo(expected);
    }

    @Test
    void shouldThrowExceptionOnTooBigDiscountValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            LineItem item = new LineItem(apple, 0);
            item.setDiscount(101);
        });
    }

    @Test
    void shouldThrowExceptionOnNegativeDiscountValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            LineItem item = new LineItem(apple, 0);
            item.setDiscount(-1);
        });
    }

    @Test
    void shouldCalculateProperTotalPrice() {
        LineItem item1 = new LineItem(apple, 0);
        item1.setDiscount(30);

        assertThat(item1.getTotal()).isEqualTo(35);
    }
}