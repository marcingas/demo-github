package pl.marcin.demogithub.exceptions;

public record ErrorResponse(int status, String message) {
}
