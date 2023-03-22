package pl.dexbtyes.shopapp.dto;

import java.util.List;

public record Order(long id, List<LineItem> items) {

    public Order addAll(List<LineItem> items) {
        this.items.addAll(items);
        return this;
    }

    public Order addItem(LineItem item) {
        this.items.add(item);
        return this;
    }

    public int getOrderTotal() {
        return items.stream().map(LineItem::getTotal)
                .reduce(Integer::sum)
                .orElse(0);
    }
}
