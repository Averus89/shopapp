package pl.dexbtyes.shopapp.exception;

import org.springframework.http.HttpStatus;

public class QuantityTooLowException extends ShopappException {
    private static final int CODE = HttpStatus.NOT_ACCEPTABLE.value();
    private static final String MESSAGE = "Quantity is lower or equal to zero";

    @Override
    public String getMessage() {
        return MESSAGE;
    }

    @Override
    public int getCode() {
        return CODE;
    }
}
