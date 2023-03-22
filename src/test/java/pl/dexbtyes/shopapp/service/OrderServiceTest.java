package pl.dexbtyes.shopapp.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieContainer;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import pl.dexbtyes.shopapp.configuration.OrderRulesEngine;
import pl.dexbtyes.shopapp.dto.LineItem;
import pl.dexbtyes.shopapp.dto.Order;
import pl.dexbtyes.shopapp.dto.Product;
import pl.dexbtyes.shopapp.dto.Status;
import pl.dexbtyes.shopapp.entity.OrderItemsEntity;
import pl.dexbtyes.shopapp.entity.ProductEntity;
import pl.dexbtyes.shopapp.repository.OrderItemsRepository;
import pl.dexbtyes.shopapp.repository.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {OrderRulesEngine.class})
class OrderServiceTest {
    @Mock
    ProductRepository productRepository;
    @Mock
    OrderItemsRepository orderItemsRepository;
    @Autowired
    KieContainer kieContainer;
    OrderService orderService;
    ProductEntity apple;
    ProductEntity orange;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(productRepository, orderItemsRepository, kieContainer);
        apple = new ProductEntity(1L, "apple", 50);
        orange = new ProductEntity(2L, "orange", 70);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldNotAddNewItemIfProductDoesNotExists() {
        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.empty());

        orderService.addItemsForOrder(1, apple.id(), 1)
                .as(StepVerifier::create)
                .expectError(IllegalArgumentException.class);
    }

    @Test
    void shouldAddNewItemForNewOrder() {
        OrderItemsEntity orderItemsEntity = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(1)
                .build();

        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.just(apple));
        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.empty());
        when(orderItemsRepository.save(any()))
                .thenReturn(Mono.just(orderItemsEntity));

        orderService.addItemsForOrder(1, apple.id(), 1)
                .as(StepVerifier::create)
                .expectNext(new Status(HttpStatus.CREATED.value(), OrderService.PRODUCT_ADDED_TO_ORDER))
                .verifyComplete();
    }

    @Test
    void shouldAddToItemQuantityForOrder() {
        OrderItemsEntity orderItemsEntity = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(1)
                .build();
        OrderItemsEntity updatedOrderItemsEntity = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(2)
                .build();

        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.just(apple));
        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(orderItemsEntity));
        when(orderItemsRepository.save(any()))
                .thenReturn(Mono.just(updatedOrderItemsEntity));

        orderService.addItemsForOrder(1, apple.id(), 1)
                .as(StepVerifier::create)
                .expectNext(new Status(HttpStatus.CREATED.value(), OrderService.PRODUCT_ADDED_TO_ORDER))
                .verifyComplete();
    }

    @Test
    void shouldReturnOrders() {
        OrderItemsEntity applesOne = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(1)
                .build();
        OrderItemsEntity orangesOne = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(orange.id())
                .quantity(1)
                .build();
        Order orderOne = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build()
        ));
        OrderItemsEntity applesTwo = OrderItemsEntity.builder()
                .orderId(2L)
                .productId(apple.id())
                .quantity(1)
                .build();
        OrderItemsEntity orangesTwo = OrderItemsEntity.builder()
                .orderId(2L)
                .productId(orange.id())
                .quantity(1)
                .build();
        Order orderTwo = new Order(2, Arrays.asList(
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build()
        ));

        when(orderItemsRepository.findAll())
                .thenReturn(Flux.just(applesOne, orangesOne, applesTwo, orangesTwo));
        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.just(apple));
        when(productRepository.findById(orange.id()))
                .thenReturn(Mono.just(orange));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrders()
                .as(StepVerifier::create)
                .expectNext(orderOne, orderTwo)
                .verifyComplete();
    }

    @Test
    void shouldReturnOrderById() {
        OrderItemsEntity apples = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(1)
                .build();
        OrderItemsEntity oranges = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(orange.id())
                .quantity(1)
                .build();
        Order order = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build()
        ));

        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(apples, oranges));
        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.just(apple));
        when(productRepository.findById(orange.id()))
                .thenReturn(Mono.just(orange));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrderById(1)
                .as(StepVerifier::create)
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldApplyAppleRule() {
        OrderItemsEntity apples = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(2)
                .build();
        Order order = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build(),
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(30).build()
        ));

        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(apples));
        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.just(apple));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrderById(1)
                .as(StepVerifier::create)
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldApplyAppleRuleToSecondAppleOnly() {
        OrderItemsEntity apples = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(3)
                .build();
        Order order = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build(),
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(30).build(),
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build()
        ));

        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(apples));
        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.just(apple));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrderById(1)
                .as(StepVerifier::create)
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldApplyAppleRuleToEverySecondApple() {
        OrderItemsEntity apples = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(apple.id())
                .quantity(4)
                .build();
        Order order = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build(),
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(30).build(),
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(0).build(),
                LineItem.builder().product(new Product(apple.price(), apple.name())).discount(30).build()
        ));

        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(apples));
        when(productRepository.findById(apple.id()))
                .thenReturn(Mono.just(apple));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrderById(1)
                .as(StepVerifier::create)
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldApplyOrangeRule() {
        OrderItemsEntity oranges = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(orange.id())
                .quantity(2)
                .build();
        Order order = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(100).build()
        ));

        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(oranges));
        when(productRepository.findById(orange.id()))
                .thenReturn(Mono.just(orange));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrderById(1)
                .as(StepVerifier::create)
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldApplyOrangeRuleAndAddOnlyOneFreeOrangeForThreeOrdered() {
        OrderItemsEntity oranges = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(orange.id())
                .quantity(3)
                .build();
        Order order = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(100).build()
        ));

        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(oranges));
        when(productRepository.findById(orange.id()))
                .thenReturn(Mono.just(orange));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrderById(1)
                .as(StepVerifier::create)
                .expectNext(order)
                .verifyComplete();
    }

    @Test
    void shouldApplyOrangeRuleAndAddFreeOrangeToEverySecondOrange() {
        OrderItemsEntity oranges = OrderItemsEntity.builder()
                .orderId(1L)
                .productId(orange.id())
                .quantity(4)
                .build();
        Order order = new Order(1, Arrays.asList(
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(0).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(100).build(),
                LineItem.builder().product(new Product(orange.price(), orange.name())).discount(100).build()
        ));

        when(orderItemsRepository.findAll(any(Example.class)))
                .thenReturn(Flux.just(oranges));
        when(productRepository.findById(orange.id()))
                .thenReturn(Mono.just(orange));
        when(productRepository.findAll())
                .thenReturn(Flux.just(apple, orange));

        orderService.getOrderById(1)
                .as(StepVerifier::create)
                .expectNext(order)
                .verifyComplete();
    }
}
