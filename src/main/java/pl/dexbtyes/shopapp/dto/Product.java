package pl.dexbtyes.shopapp.dto;

import lombok.Builder;

@Builder
public record Product(int basePrice, String name) {
}
