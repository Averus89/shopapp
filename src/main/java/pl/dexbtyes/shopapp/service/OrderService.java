package pl.dexbtyes.shopapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.dexbtyes.shopapp.dto.LineItem;
import pl.dexbtyes.shopapp.dto.Order;
import pl.dexbtyes.shopapp.dto.Product;
import pl.dexbtyes.shopapp.dto.Status;
import pl.dexbtyes.shopapp.entity.OrderItemsEntity;
import pl.dexbtyes.shopapp.entity.ProductEntity;
import pl.dexbtyes.shopapp.repository.OrderItemsRepository;
import pl.dexbtyes.shopapp.repository.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String PRODUCT_ADDED_TO_ORDER = "Product added to order";
    public static final String ORDER_NOT_FOUND = "Order not found";
    private final ProductRepository productRepository;
    private final OrderItemsRepository orderItemsRepository;

    public OrderService(ProductRepository productRepository, OrderItemsRepository orderItemsRepository) {
        this.productRepository = productRepository;
        this.orderItemsRepository = orderItemsRepository;
    }

    public Mono<Status> addItemsForOrder(long orderId, long productId, int quantity) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(PRODUCT_NOT_FOUND)))
                .flatMapMany(item -> saveItems(orderId, item, quantity))
                .then(Mono.just(new Status(HttpStatus.CREATED.value(), PRODUCT_ADDED_TO_ORDER)))
                .onErrorResume(throwable -> Mono.just(new Status(HttpStatus.NOT_FOUND.value(), throwable.getMessage())));
    }

    private Flux<OrderItemsEntity> saveItems(long orderId, ProductEntity item, int quantity) {
        return orderItemsRepository.findAll(getExampleOrderItem(orderId, item))
                .defaultIfEmpty(getNewOrderItemEntity(orderId, item))
                .map(e -> e.withQuantity(e.getQuantity() + quantity))
                .flatMap(orderItemsRepository::save);
    }

    private Example<OrderItemsEntity> getExampleOrderItem(long orderId, ProductEntity item) {
        return Example.of(
                OrderItemsEntity.builder()
                        .orderId(orderId)
                        .productId(item.id())
                        .build()
        );
    }

    private OrderItemsEntity getNewOrderItemEntity(long orderId, ProductEntity item) {
        return OrderItemsEntity.builder()
                .productId(item.id())
                .orderId(orderId)
                .quantity(0)
                .build();
    }

    public Flux<Order> getOrders() {
        return orderItemsRepository.findAll()
                .groupBy(OrderItemsEntity::getOrderId)
                .flatMap(this::processOrderItems);
    }

    private Mono<Order> processOrderItems(GroupedFlux<Long, OrderItemsEntity> gf) {
        return gf
                .flatMap(orderItem -> orderItemsEntityToOrders(gf.key(), orderItem))
                .reduce((order, order2) -> order.addAll(order2.items()));
    }

    private Mono<Order> orderItemsEntityToOrders(long orderId, OrderItemsEntity orderItem) {
        return productRepository.findById(orderItem.getProductId())
                .flatMap(productEntity -> productEntityToLineItems(orderItem, productEntity))
                .map(list -> new Order(orderId, list));
    }

    private Mono<List<LineItem>> productEntityToLineItems(OrderItemsEntity orderItem, ProductEntity productEntity) {
        return Flux.range(0, orderItem.getQuantity())
                .map(i -> mapProductEntityToLineItem(productEntity))
                .collect(Collectors.toList());
    }

    private LineItem mapProductEntityToLineItem(ProductEntity productEntity) {
        return LineItem.builder()
                .product(Product.builder()
                        .name(productEntity.name())
                        .basePrice(productEntity.price())
                        .build())
                .discount(0)
                .build();
    }

    public Mono<Order> getOrderById(long orderId) {
        return orderItemsRepository.findAll(Example.of(OrderItemsEntity.builder().orderId(orderId).build()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(ORDER_NOT_FOUND)))
                .flatMap(orderItem -> orderItemsEntityToOrders(orderId, orderItem))
                .reduce((order, order2) -> order.addAll(order2.items()));
    }
}
