package pl.dexbtyes.shopapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import pl.dexbtyes.shopapp.dto.Status;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class, QuantityTooLowException.class})
    private static Mono<Status> processException(Throwable throwable) {
        if (throwable instanceof ShopappException shopappException) {
            return Mono.just(new Status(shopappException.getCode(), shopappException.getMessage()));
        }
        return Mono.just(new Status(HttpStatus.INTERNAL_SERVER_ERROR.value(), throwable.getMessage()));
    }
}
