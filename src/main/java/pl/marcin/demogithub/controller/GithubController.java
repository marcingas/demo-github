package pl.marcin.demogithub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.marcin.demogithub.exceptions.CustomNotAcceptableHeaderException;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.exceptions.ErrorResponse;
import pl.marcin.demogithub.model.RepoBranchCommit;
import pl.marcin.demogithub.service.GitHubService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/git")
public class GithubController {
    @Autowired
    private GitHubService gitHubService;
    private final String GITHUB_API_URL = "https://api.github.com";

    /**
     *get answers
     * method sets path variable param and fetches answers
     * in a reactive manner
     * @return returns ResponseEntity
     */
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

    /**
     *
     * @param ex- exception from service layer which warns
     *         that there is no such user
     * @return ResponseEntity with code value and message
     */

    @ExceptionHandler(CustomNotFoundUserException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundUserException(CustomNotFoundUserException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getValue(), ex.getMessage());
        return ResponseEntity.status(ex.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    /**
     *
     * not Acceptable Header handler
     * @return returns code and message if header is not JSON
     */

    @ExceptionHandler(CustomNotAcceptableHeaderException.class)
    public ResponseEntity<ErrorResponse> handleNotAcceptableHeaderException(CustomNotAcceptableHeaderException ex) {

        ErrorResponse errorResponse = new ErrorResponse(ex.getValue(), ex.getMessage());

        return ResponseEntity.status(ex.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }
}


