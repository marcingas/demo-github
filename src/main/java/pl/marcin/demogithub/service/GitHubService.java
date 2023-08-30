package pl.marcin.demogithub.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.marcin.demogithub.exceptions.CustomNotAcceptableHeaderException;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.model.Branch;
import pl.marcin.demogithub.model.BranchInfo;
import pl.marcin.demogithub.model.MyRepository;
import pl.marcin.demogithub.model.RepoWithBranches;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubService {

    private WebClient webClient;
    private final String GITHUB_API_URL = "https://api.github.com";


    public GitHubService(WebClient webClient) {
        this.webClient = webClient;
    }

    @NotNull
    private static Flux<MyRepository> handleError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            if (ex.getStatusCode().value() == 404) {
                String message = ex.getMessage();
                return Flux.error(new CustomNotFoundUserException(HttpStatus.NOT_FOUND.value(), message));
            } else if (ex.getStatusCode().value() == 406) {
                String message = ex.getMessage();
                return Flux.error(new CustomNotAcceptableHeaderException(HttpStatus.NOT_ACCEPTABLE.value(), message));
            } else {
                return Flux.error(throwable);
            }
        }
        return Flux.error(throwable);
    }

    public Flux<RepoWithBranches> getRepositories(String owner, String token, String baseUrl) {
        String uriRepoList = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("users", owner, "repos")
                .toUriString();

        Flux<MyRepository> myRepositoryFlux = getMyRepositoryFlux(token, uriRepoList);
        return myRepositoryFlux.flatMap(myRepository ->
                getRepoBranchCommitFlux(owner, token, myRepository));
    }

    @NotNull
    private Mono<RepoWithBranches> getRepoBranchCommitFlux(String owner, String token,
                                                           MyRepository myRepository) {

        String myBranchesUri = UriComponentsBuilder.fromHttpUrl(GITHUB_API_URL)
                .pathSegment("repos", owner, myRepository.name(), "branches")
                .toUriString();
        return webClient.get()
                .uri(myBranchesUri)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToFlux(Branch.class)
                .collectList()
                .flatMap(branches -> {
                    List<BranchInfo> branchIntoList = branches.stream()
                            .map(branch -> new BranchInfo(branch.name(), branch.commit().sha()))
                            .collect(Collectors.toList());
                    return Mono.just(new RepoWithBranches(
                            myRepository.name(),
                            myRepository.owner().login(),
                            branchIntoList
                    ));
                });
    }

    @NotNull
    private Flux<MyRepository> getMyRepositoryFlux(String token, String uriRepoList) {
        return webClient.get()
                .uri(uriRepoList)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToFlux(MyRepository.class)
                .filter(repository -> !repository.fork())
                .onErrorResume(throwable -> {
                    return handleError(throwable);
                });
    }
}
