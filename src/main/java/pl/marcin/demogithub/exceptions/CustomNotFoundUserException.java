package pl.marcin.demogithub.exceptions;

public class CustomNotFoundUserException extends RuntimeException{
    private final int value;
    private final String message;

    public CustomNotFoundUserException(int value, String message) {
        this.value = value;
        this.message = message;
    }
}
