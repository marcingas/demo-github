package pl.marcin.demogithub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.model.Branch;
import pl.marcin.demogithub.model.Commit;
import pl.marcin.demogithub.model.RepoBranchCommit;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitHubServiceTest {
    private MockWebServer mockWebServer;
    private GitHubService gitHubService;
    private Integer page =2;
    private String token = "abcd";

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        gitHubService = new GitHubService(webClient);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void getAnswer() throws Exception {
        //given
        String owner = "marcingas";
        List<RepoBranchCommit> repoList = new ArrayList<>();
        repoList.add(new RepoBranchCommit("project1", "marcingas", "master", "sha"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(new ObjectMapper().writeValueAsString(repoList)));

        //when
        Flux<RepoBranchCommit> branchesFlux = gitHubService.getRepositories(owner,token,page);
        //then
        StepVerifier.create(branchesFlux)
                .expectNextCount(1)
                .verifyComplete();
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
        Flux<RepoBranchCommit> branchesFlux = gitHubService.getRepositories(owner,token,page);
        //then
        StepVerifier.create(branchesFlux)
                .expectErrorMatches(throwable -> throwable instanceof CustomNotFoundUserException)
                .verify();
    }
}