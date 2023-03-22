package pl.dexbtyes.shopapp.entity;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("order_items")
@Builder
public class OrderItemsEntity {
    final Long orderId;
    final Long productId;
    @With
    final Integer quantity;
    @Id
    Long id;
}
