package pl.marcin.demogithub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.marcin.demogithub.exceptions.CustomNotAcceptableHeader;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.model.Branch;
import pl.marcin.demogithub.model.MyRepository;
import pl.marcin.demogithub.model.RepoBranchCommit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GitHubService {

    private WebClient webClient;
    private final String GITHUB_API_URL = "https://api.github.com";


    @Autowired
    public GitHubService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<RepoBranchCommit> getRepositories(String owner, String token, Integer page,Integer perPage, String baseUrl) {
        String uriRepoList = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("users", owner, "repos")
                .queryParam("page", page)
                .queryParam("per_page", perPage)
                .toUriString();

        Flux<MyRepository> myRepositoryFlux = webClient.get()
                .uri(uriRepoList)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToFlux(MyRepository.class)
                .filter(repository -> !repository.fork())
                .onErrorResume(throwable -> {
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;
                        if (ex.getStatusCode().value() == 404) {
                            String message = ex.getMessage();
                            return Flux.error(new CustomNotFoundUserException(HttpStatus.NOT_FOUND.value(), message));
                        } else if (ex.getStatusCode().value() == 406) {
                            String message = ex.getMessage();
                            return Flux.error(new CustomNotAcceptableHeader(HttpStatus.NOT_ACCEPTABLE.value(), message));
                        } else {
                            return Flux.error(throwable);
                        }
                    }
                    return Flux.error(throwable);
                });


        Flux<String> myBranchesUriFlux = myRepositoryFlux.flatMap(repository -> {
            return Mono.just(UriComponentsBuilder.fromHttpUrl(GITHUB_API_URL)
                    .pathSegment("repos", owner, repository.name(), "branches")
                    .toUriString());
        });

        Flux<RepoBranchCommit> repoBranchCommitFlux = myRepositoryFlux
                .flatMap(myRepository -> {
                    return myBranchesUriFlux.flatMap(uri -> webClient.get()
                                    .uri(uri)
                                    .header("Authorization", "Bearer " + token)
                                    .header("Accept", "application/json")
                                    .retrieve()
                                    .bodyToFlux(Branch.class))
                            .flatMap(branch -> {
                                return Flux.just(new RepoBranchCommit(myRepository.name(),
                                        myRepository.owner().login(), branch.name(), branch.commit().sha()));
                            });
                });
        return repoBranchCommitFlux;
    }
}
