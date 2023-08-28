package pl.marcin.demogithub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.marcin.demogithub.model.Branch;
import pl.marcin.demogithub.model.RepoBranchCommit;
import pl.marcin.demogithub.service.GitHubService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/git")
public class GithubController {
    @Autowired
    private GitHubService gitHubService;

    @GetMapping("/{owner}")
    public Flux<RepoBranchCommit> getAnswers(@PathVariable String owner) {
        return gitHubService.getRepositories(owner);
    }

    @GetMapping("/branches/{owner}")
    public Flux<Branch> getBranches(@PathVariable String owner) {
        return gitHubService.getBranches(owner);
    }

    @GetMapping()
    public String hello() {
        return "Hello";
    }
}
