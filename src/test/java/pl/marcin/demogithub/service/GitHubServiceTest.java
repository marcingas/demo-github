package pl.marcin.demogithub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import pl.marcin.demogithub.exceptions.CustomNotAcceptableHeaderException;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.model.RepoBranchCommit;
import pl.marcin.demogithub.model.RepoWithBranches;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

class GitHubServiceTest {

   private MockWebServer mockWebServer;
    private GitHubService gitHubService;
    private Integer page = 2;
    private Integer perPage = 2;
    private String token = "abcd";

    @BeforeEach
    public void setUp() {
        mockWebServer = new MockWebServer();
        String mockUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockUrl)
                .build();
        gitHubService = new GitHubService(webClient);
    }
    @AfterEach
    void shutDownMocWebServer() throws Exception{
        mockWebServer.shutdown();
    }


    @Test
    void getRepositoriesTest() throws Exception{
        //given
        String owner = "rambo";
        String repoName = "project";
        String branchName = "master";
        String lCSha = "12321691urso3242";
        String baseUrl = "https://api.github.com";
        RepoBranchCommit repoBranchCommit = new RepoBranchCommit(repoName,owner,branchName,lCSha);
        Flux<RepoBranchCommit>repoFlux = Flux.just(repoBranchCommit);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(new ObjectMapper().writeValueAsString(repoFlux)));
        gitHubService.getRepositories(owner,token,page,perPage,baseUrl);
    }

    @Test
    void getAnswer404() throws Exception {
        //given
        String owner = "marcingas";
        List<RepoBranchCommit> repoList = new ArrayList<>();
        repoList.add(new RepoBranchCommit("project1", "marcingas", "master", "sha"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(new ObjectMapper().writeValueAsString(repoList)));

        //when
        Flux<RepoWithBranches> branchesFlux = gitHubService.getRepositories(owner,
                token, page, perPage, mockWebServer.url("/").toString());
        //then
        StepVerifier.create(branchesFlux)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundUserException)
                .verify();
    }

    @Test
    void getAnswer406() throws Exception {
        //given
        String owner = "marcingas";
        List<RepoBranchCommit> repoList = new ArrayList<>();
        repoList.add(new RepoBranchCommit("project1", "marcingas", "master", "sha"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(406)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(new ObjectMapper().writeValueAsString(repoList)));

        //when
        Flux<RepoWithBranches> branchesFlux = gitHubService.getRepositories(owner,
                token, page, perPage, mockWebServer.url("/").toString());
        //then
        StepVerifier.create(branchesFlux)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotAcceptableHeaderException)
                .verify();
    }
}