package pl.dexbtyes.shopapp.dto;

import lombok.Builder;

@Builder
public record Product(int basePrice, String name) {
    public Product(Product product) {
        this(product.basePrice(), product.name());
    }
}
