package pl.dexbtyes.shopapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LineItem {
    final Product product;
    int discount;

    public void setDiscount(int discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Accepting only percentage from range 0-100");
        }
        this.discount = discount;
    }

    public int getTotal() {
        return getProduct().basePrice() * (100 - getDiscount()) / 100;
    }
}
