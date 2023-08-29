package pl.marcin.demogithub.exceptions;

public class CustomNotFoundUserException extends RuntimeException{
    private final int value;
    private final String message;

    public int getValue() {
        return value;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public CustomNotFoundUserException(int value, String message) {
        this.value = value;
        this.message = message;
    }
}
