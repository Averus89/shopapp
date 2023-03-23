package pl.dexbtyes.shopapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LineItem {
    private Product product;
    private int discount;

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
