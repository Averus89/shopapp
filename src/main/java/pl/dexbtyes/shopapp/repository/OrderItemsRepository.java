package pl.dexbtyes.shopapp.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pl.dexbtyes.shopapp.entity.OrderItemsEntity;

@Repository
public interface OrderItemsRepository extends R2dbcRepository<OrderItemsEntity, Long> {
}
