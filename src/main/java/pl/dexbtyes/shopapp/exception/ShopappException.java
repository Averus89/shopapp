package pl.dexbtyes.shopapp.exception;

public abstract class ShopappException extends Throwable{
    public abstract String getMessage();
    public abstract int getCode();
}
