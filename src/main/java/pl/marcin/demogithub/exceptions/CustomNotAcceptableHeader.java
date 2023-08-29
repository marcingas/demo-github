package pl.marcin.demogithub.exceptions;

public class CustomNotAcceptableHeader extends RuntimeException{
    private final int value;
    private final String message;

    public int getValue() {
        return value;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public CustomNotAcceptableHeader(int value, String message) {
        this.value=value;
        this.message=message;
    }
}
