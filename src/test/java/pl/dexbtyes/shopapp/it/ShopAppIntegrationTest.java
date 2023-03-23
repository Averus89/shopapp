package pl.dexbtyes.shopapp.it;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.dexbtyes.shopapp.controller.OrderController;
import pl.dexbtyes.shopapp.dto.LineItem;
import pl.dexbtyes.shopapp.dto.Order;
import pl.dexbtyes.shopapp.dto.Product;
import pl.dexbtyes.shopapp.dto.Status;
import pl.dexbtyes.shopapp.entity.OrderItemsEntity;
import pl.dexbtyes.shopapp.exception.ProductNotFoundException;
import pl.dexbtyes.shopapp.exception.QuantityTooLowException;
import pl.dexbtyes.shopapp.exception.ShopappException;
import pl.dexbtyes.shopapp.repository.OrderItemsRepository;
import pl.dexbtyes.shopapp.service.OrderService;
import reactor.test.StepVerifier;

import javax.sound.sampled.Line;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@Import({BuildProperties.class})
public class ShopAppIntegrationTest {
    WebTestClient webTestClient;
    @Autowired
    ApplicationContext context;
    @Autowired
    OrderItemsRepository orderItemsRepository;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .build();
        orderItemsRepository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldNotAddNegativeQuantityProductToOrder() {
        Status status = exceptionToStatus(new QuantityTooLowException());

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=1&quantity=-1")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void shouldNotAddZeroQuantityProductToOrder() {
        Status status = exceptionToStatus(new QuantityTooLowException());

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=1&quantity=0")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void shouldNotAddNonExistingProductToOrder() {
        Status status = exceptionToStatus(new ProductNotFoundException());

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=100&quantity=1")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void shouldAddProductToOrder() {
        Status status = new Status(201, OrderService.PRODUCT_ADDED_TO_ORDER);

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=1&quantity=1")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void shouldApplyAppleDiscountToOrder() {
        Status status = new Status(201, OrderService.PRODUCT_ADDED_TO_ORDER);

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=1&quantity=2")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();

        webTestClient.get()
                .uri("/orders/v1/getOrderSummary/1")
                .exchange()
                .returnResult(Order.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .assertNext(order -> {
                    assertThat(order).isNotNull();
                    assertThat(order.id()).isEqualTo(1L);
                    assertThat(order.items()).isNotEmpty();
                    assertThat(order.items().size()).isEqualTo(2);
                    assertThat(order.items().get(1).getDiscount()).isEqualTo(30);
                    assertThat(order.items().get(1).getTotal()).isEqualTo((int) (50 * 0.7));
                    assertThat(order.getOrderTotal()).isEqualTo(85);
                })
                .verifyComplete();
    }

    @Test
    void shouldApplyOrangeDiscountToOrder() {
        Status status = new Status(201, OrderService.PRODUCT_ADDED_TO_ORDER);

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=2&quantity=2")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();

        webTestClient.get()
                .uri("/orders/v1/getOrderSummary/1")
                .exchange()
                .returnResult(Order.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .assertNext(order -> {
                    assertThat(order).isNotNull();
                    assertThat(order.id()).isEqualTo(1L);
                    assertThat(order.items()).isNotEmpty();
                    assertThat(order.items().size()).isEqualTo(3);
                    assertThat(order.items().get(2).getDiscount()).isEqualTo(100);
                    assertThat(order.items().get(2).getTotal()).isEqualTo(0);
                    assertThat(order.getOrderTotal()).isEqualTo(140);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnCorrectOrdersWithAppliedDiscounts() {
        Status status = new Status(201, OrderService.PRODUCT_ADDED_TO_ORDER);
        Product apple = new Product(50, "apple");
        Product orange = new Product(70, "orange");
        Order one = new Order(
                1,
                Arrays.asList(
                        LineItem.builder().product(apple).discount(0).build(),
                        LineItem.builder().product(apple).discount(30).build()
                )
        );
        Order two = new Order(
                2,
                Arrays.asList(
                        LineItem.builder().product(orange).discount(0).build(),
                        LineItem.builder().product(orange).discount(0).build(),
                        LineItem.builder().product(orange).discount(100).build()
                )
        );

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=1&quantity=2")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();

        webTestClient.put()
                .uri("/orders/v1/add/2?productId=2&quantity=2")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();

        webTestClient.get()
                .uri("/orders/v1/getOrders")
                .exchange()
                .returnResult(Order.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .assertNext(order -> {
                    assertThat(order).isEqualTo(one);
                    assertThat(order.getOrderTotal()).isEqualTo(85);
                })
                .assertNext(order -> {
                    assertThat(order).isEqualTo(two);
                    assertThat(order.getOrderTotal()).isEqualTo(140);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnCorrectOrderIds() {
        Status status = new Status(201, OrderService.PRODUCT_ADDED_TO_ORDER);

        webTestClient.put()
                .uri("/orders/v1/add/1?productId=1&quantity=1")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();

        webTestClient.put()
                .uri("/orders/v1/add/2?productId=2&quantity=1")
                .exchange()
                .returnResult(Status.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(status)
                .verifyComplete();

        webTestClient.get()
                .uri("/orders/v1/getOrderIds")
                .exchange()
                .returnResult(Integer.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNext(1, 2)
                .verifyComplete();
    }

    private static Status exceptionToStatus(Throwable exception) {
        if (exception instanceof ShopappException ex) {
            return new Status(ex.getCode(), ex.getMessage());
        }
        return new Status(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage());
    }
}
