package pl.dexbtyes.shopapp.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;

class OrderTest {
    @Test
    void shouldCreateObject() {
        Order order = new Order(1, Arrays.asList(getApple(), getApple(), getApple()));

        assertThat(order).isNotNull();
        assertThat(order.id()).isEqualTo(1);
        assertThat(order.items()).isNotNull();
        assertThat(order.items().size()).isEqualTo(3);
        assertThat(order.items()).contains(getApple());
        assertThat(order.getOrderTotal()).isEqualTo(150);
    }

    @Test
    void emptyOrderShouldHaveTotalOfZero() {
        Order order = new Order(1, new ArrayList<>());

        assertThat(order.getOrderTotal()).isEqualTo(0);
    }

    @Test
    void shouldAddItemsToTheOrder() {
        Order empty = new Order(1, new ArrayList<>());
        assertThat(empty.items()).isEmpty();

        Order order = empty.addItem(getApple());
        assertThat(order).isNotNull();
        assertThat(order.items()).isNotEmpty();
        assertThat(order.items()).hasSize(1);
        assertThat(order.items()).contains(getApple());

        Order bigOrder = order.addAll(IntStream.range(0, 10).boxed().map(i -> getApple()).collect(Collectors.toList()));
        assertThat(bigOrder).isNotNull();
        assertThat(bigOrder.items()).isNotEmpty();
        assertThat(bigOrder.items()).hasSize(11);
        assertThat(bigOrder.items()).containsAtLeastElementsIn(IntStream.range(0,  11).boxed().map(i -> getApple()).collect(Collectors.toList()));
    }

    private static LineItem getApple() {
        return LineItem.builder()
                .discount(0)
                .product(
                        Product.builder()
                                .basePrice(50)
                                .name("apple")
                                .build()
                )
                .build();
    }
}