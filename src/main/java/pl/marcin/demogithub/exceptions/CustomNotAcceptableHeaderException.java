package pl.marcin.demogithub.exceptions;

public class CustomNotAcceptableHeaderException extends RuntimeException{
    private final int value;
    private final String message;

    public int getValue() {
        return value;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public CustomNotAcceptableHeaderException(int value, String message) {
        this.value=value;
        this.message=message;
    }
}
