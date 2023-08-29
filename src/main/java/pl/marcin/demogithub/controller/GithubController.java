package pl.marcin.demogithub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.marcin.demogithub.exceptions.CustomNotAcceptableHeader;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.exceptions.ErrorResponse;
import pl.marcin.demogithub.model.RepoBranchCommit;
import pl.marcin.demogithub.service.GitHubService;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/git")
public class GithubController {
    @Autowired
    private GitHubService gitHubService;
    private final String GITHUB_API_URL = "https://api.github.com";

    @GetMapping("/{owner}/{page}/{perPage}/{token}")
    public ResponseEntity<Flux<RepoBranchCommit>> getAnswers(@PathVariable String owner,
                                                             @PathVariable Integer page,
                                                             @PathVariable Integer perPage,
                                                             @PathVariable String token) {
        Flux<RepoBranchCommit> repositories = gitHubService.getRepositories(owner, token,
                page,
                perPage,
                GITHUB_API_URL);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(repositories);
    }

    @ExceptionHandler(CustomNotFoundUserException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundUserException(CustomNotFoundUserException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getValue(), ex.getMessage());
        return ResponseEntity.status(ex.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    @ExceptionHandler(CustomNotAcceptableHeader.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptableHeaderException(CustomNotAcceptableHeader ex) {

        ErrorResponse errorResponse = new ErrorResponse(ex.getValue(), ex.getMessage());

        return ResponseEntity.status(ex.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }
}


