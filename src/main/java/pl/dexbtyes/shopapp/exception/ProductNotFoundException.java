package pl.dexbtyes.shopapp.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ShopappException {
    private final static int CODE = HttpStatus.NOT_FOUND.value();
    private final static String MESSAGE = "Product not found";

    @Override
    public String getMessage() {
        return MESSAGE;
    }

    @Override
    public int getCode() {
        return CODE;
    }
}
