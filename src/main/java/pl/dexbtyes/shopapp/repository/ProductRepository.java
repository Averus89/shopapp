package pl.dexbtyes.shopapp.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pl.dexbtyes.shopapp.entity.ProductEntity;

@Repository
public interface ProductRepository extends R2dbcRepository<ProductEntity, Long> {
}
