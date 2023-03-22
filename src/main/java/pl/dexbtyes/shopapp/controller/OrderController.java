package pl.dexbtyes.shopapp.controller;

import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;
import pl.dexbtyes.shopapp.dto.Order;
import pl.dexbtyes.shopapp.dto.Status;
import pl.dexbtyes.shopapp.service.OrderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("orders/v1")
@Timed
public class OrderController {
    final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("add/{orderId}")
    public Mono<Status> addProductToOrder(
            @PathVariable("orderId") long orderId,
            @RequestParam("productId") long productId,
            @RequestParam(name = "quantity", defaultValue = "1", required = false) int quantity
    ) {
        return orderService.addItemsForOrder(orderId, productId, quantity);
    }

    @GetMapping("/getOrderIds")
    public Flux<Long> getOrderIds() {
        return getOrders().map(Order::id);
    }

    @GetMapping("/getOrders")
    public Flux<Order> getOrders() {
        return orderService.getOrders();
    }

    @GetMapping("/getOrderSummary/{orderId}")
    public Mono<Order> getOrderSummary(@PathVariable("orderId") long orderId) {
        return orderService.getOrderById(orderId);
    }
}
