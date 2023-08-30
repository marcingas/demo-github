package pl.marcin.demogithub.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.marcin.demogithub.exceptions.CustomNotAcceptableHeaderException;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.exceptions.ErrorResponse;
import pl.marcin.demogithub.model.RepoWithBranches;
import pl.marcin.demogithub.service.GitHubService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/git")
public class GithubController {
    private final GitHubService gitHubService;

    private final String GITHUB_API_URL = "https://api.github.com";

    public GithubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/{owner}/{token}")
    public ResponseEntity<Flux<RepoWithBranches>> getAnswers(@PathVariable String owner,
                                                             @PathVariable String token) {
        Flux<RepoWithBranches> repositories = gitHubService.getRepositories(owner, token,
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

    @ExceptionHandler(CustomNotAcceptableHeaderException.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptableHeaderException(CustomNotAcceptableHeaderException ex) {

        ErrorResponse errorResponse = new ErrorResponse(ex.getValue(), ex.getMessage());

        return ResponseEntity.status(ex.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }
}


