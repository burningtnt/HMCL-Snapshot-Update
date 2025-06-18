package net.burningtnt.hmclfetcher.api.structure.repo;

import net.burningtnt.hmclfetcher.api.structure.user.GitHubUser;

public final class GitHubRepository {
    private final String name;

    private final GitHubUser owner;

    private GitHubRepository(String name, GitHubUser owner) {
        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public GitHubUser getOwner() {
        return owner;
    }
}
