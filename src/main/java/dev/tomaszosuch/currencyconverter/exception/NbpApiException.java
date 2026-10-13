package dev.tomaszosuch.currencyconverter.exception;

public class NbpApiException extends RuntimeException {

    public NbpApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
