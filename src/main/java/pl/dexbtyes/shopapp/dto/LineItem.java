package pl.dexbtyes.shopapp.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class LineItem {
    final Product product;
    int discount;

    public LineItem(LineItem item) {
        this.product = new Product(item.getProduct());
        this.discount = item.getDiscount();
    }

    public void setDiscount(int discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Accepting only percentage from range 0-100");
        }
        this.discount = discount;
    }

    public int getTotal() {
        return getProduct().basePrice() * (100 - getDiscount()) / 100;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineItem lineItem = (LineItem) o;
        return discount == lineItem.discount && product.equals(lineItem.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, discount);
    }
}
