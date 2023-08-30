package pl.marcin.demogithub.model;

import java.util.List;

public record RepoWithBranches(String name, String owner, List<BranchInfo> branches) {
}
