package pl.dexbtyes.shopapp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("products")
public record ProductEntity(@Id Long id, String name, int price) {

}
