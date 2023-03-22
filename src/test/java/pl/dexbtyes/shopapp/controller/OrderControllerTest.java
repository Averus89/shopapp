package pl.dexbtyes.shopapp.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import pl.dexbtyes.shopapp.dto.Order;
import pl.dexbtyes.shopapp.dto.Status;
import pl.dexbtyes.shopapp.service.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
    private OrderController orderController;
    @Mock
    OrderService orderService;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldCreateOrderController() {
        assertThat(orderController).isNotNull();
    }

    @Test
    void shouldAddProductToOrder() {
        when(orderService.addItemsForOrder(1L, 1L, 1))
                .thenReturn(Mono.just(new Status(HttpStatus.CREATED.value(), OrderService.PRODUCT_ADDED_TO_ORDER)));

        orderController.addProductToOrder(1L, 1L, 1)
                .as(StepVerifier::create)
                .assertNext(status -> {
                    assertThat(status).isNotNull();
                    assertThat(status.code()).isEqualTo(HttpStatus.CREATED.value());
                    assertThat(status.status()).isEqualTo(OrderService.PRODUCT_ADDED_TO_ORDER);
                })
                .verifyComplete();
    }

    @Test
    void shouldNotAddNotExistingProductToOrder() {
        when(orderService.addItemsForOrder(1L, 1L, 1))
                .thenReturn(Mono.just(new Status(HttpStatus.NOT_FOUND.value(), OrderService.PRODUCT_NOT_FOUND)));

        orderController.addProductToOrder(1L, 1L, 1)
                .as(StepVerifier::create)
                .assertNext(status -> {
                    assertThat(status).isNotNull();
                    assertThat(status.code()).isEqualTo(HttpStatus.NOT_FOUND.value());
                    assertThat(status.status()).isEqualTo(OrderService.PRODUCT_NOT_FOUND);
                })
                .verifyComplete();
    }

    @Test
    void shouldGetAllOrders() {
        when(orderService.getOrders())
                .thenReturn(Flux.fromIterable(getOrderList(10)));

        orderController.getOrders()
                .as(StepVerifier::create)
                .expectNext(getOrderList(10).toArray(new Order[10]))
                .verifyComplete();
    }

    @Test
    void shouldGetOrderForId() {
        when(orderService.getOrderById(3))
                .thenReturn(Mono.just(new Order(3, new ArrayList<>())));

        orderController.getOrderSummary(3)
                .as(StepVerifier::create)
                .expectNext(new Order(3, new ArrayList<>()))
                .verifyComplete();
    }

    @Test
    void shouldGetExceptionIfOrderNotFound() {
        when(orderService.getOrderById(3))
                .thenReturn(Mono.error(new IllegalArgumentException(OrderService.ORDER_NOT_FOUND)));

        orderController.getOrderSummary(3)
                .as(StepVerifier::create)
                .expectError(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnProperOrderIds() {
        when(orderService.getOrders())
                .thenReturn(Flux.fromIterable(getOrderList(10)));

        orderController.getOrderIds()
                .as(StepVerifier::create)
                .expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
                .verifyComplete();
    }

    private static List<Order> getOrderList(int ordersCount) {
        return IntStream.range(0, ordersCount)
                .boxed()
                .map(i -> new Order(i, new ArrayList<>()))
                .collect(Collectors.toList());
    }
}