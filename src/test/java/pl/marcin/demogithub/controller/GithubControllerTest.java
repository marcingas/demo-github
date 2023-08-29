package pl.marcin.demogithub.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.marcin.demogithub.exceptions.CustomNotAcceptableHeader;
import pl.marcin.demogithub.exceptions.CustomNotFoundUserException;
import pl.marcin.demogithub.model.RepoBranchCommit;
import pl.marcin.demogithub.service.GitHubService;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubControllerTest.class)
class GithubControllerTest {
    @MockBean
    private GitHubService gitHubService;
    @Autowired
    private MockMvc mockMvc;
    String owner = "rambo";
    String token = "12hfdsaalkfasj32424";
    int page = 1;
    int perPage = 2;
    String baseUrl = "https://api.github.com";

    @Test
    void getAnswers() throws Exception {
        //given
        RepoBranchCommit repoBranchCommit = new RepoBranchCommit("name", owner,
                "branch", "lastsha");
        RepoBranchCommit repoBranchCommit2 = new RepoBranchCommit("name2", owner,
                "branch2", "lastsha2");

        Flux<RepoBranchCommit> dataJust = Flux.just(repoBranchCommit,repoBranchCommit2);

        //when
        when(gitHubService.getRepositories(owner, token, page,perPage, baseUrl))
                .thenReturn(dataJust);
        //then
        mockMvc.perform(get("/git/{owner}/{page}/{perPage}/{token}",owner,page,perPage,token))
                .andExpect(status().isOk());

    }

    @Test
    public void testNotFoundUserException() throws Exception {
        //when
        when(gitHubService.getRepositories(anyString(),anyString(),anyInt(),anyInt(),anyString()))
                .thenThrow(CustomNotFoundUserException.class);

        //then
        mockMvc.perform(get("/git/{owner}/{page}/{perPage}/{token}",owner,page,perPage,token))
                .andExpect(status().isNotFound());

    }

    @Test
    public void testNotAcceptableHeader() throws Exception {
        //when
        when(gitHubService.getRepositories(anyString(),anyString(),anyInt(),anyInt(),anyString()))
                .thenThrow(CustomNotAcceptableHeader.class);

        //then
        mockMvc.perform(get("/git/{owner}/{page}/{perPage}/{token}",owner,page,perPage,token))
                .andExpect(status().is(406));
    }
}