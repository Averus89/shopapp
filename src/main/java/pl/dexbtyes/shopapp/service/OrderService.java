package pl.dexbtyes.shopapp.service;

import lombok.extern.log4j.Log4j2;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class OrderService {
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String PRODUCT_ADDED_TO_ORDER = "Product added to order";
    public static final String ORDER_NOT_FOUND = "Order not found";
    private final ProductRepository productRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final KieContainer kieContainer;

    public OrderService(ProductRepository productRepository, OrderItemsRepository orderItemsRepository, KieContainer kieContainer) {
        this.productRepository = productRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.kieContainer = kieContainer;
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
                .flatMap(this::processOrderItems)
                .flatMap(this::mergeOrderWithProducts)
                .map(tuple -> applyDroolRules(tuple.getT1(), tuple.getT2()));
    }

    private Mono<Tuple2<Order, List<Product>>> mergeOrderWithProducts(Order order) {
        return getProducts().map(products -> Tuples.of(order, products));
    }

    private Mono<List<Product>> getProducts() {
        return productRepository.findAll()
                .map(pe -> Product.builder()
                        .name(pe.name())
                        .basePrice(pe.price())
                        .build()
                )
                .collectList();
    }

    private Flux<Order> processOrderItems(GroupedFlux<Long, OrderItemsEntity> gf) {
        return gf.flatMap(orderItem -> orderItemsEntityToOrders(gf.key(), orderItem))
                .groupBy(Order::id)
                .flatMap(group -> group.reduce((o1, o2) -> o1.addAll(o2.items())));
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
                .reduce((order, order2) -> order.addAll(order2.items()))
                .zipWith(getProducts())
                .map(tuple -> applyDroolRules(tuple.getT1(), tuple.getT2()));
    }

    private Order applyDroolRules(Order order, List<Product> products) {
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("logger", log);
        products.forEach(kieSession::insert);
        kieSession.insert(order);
        kieSession.fireAllRules();
        kieSession.dispose();
        return order;
    }
}
