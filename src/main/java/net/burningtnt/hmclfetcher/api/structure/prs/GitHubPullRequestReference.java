package net.burningtnt.hmclfetcher.api.structure.prs;

import com.google.gson.annotations.SerializedName;
import net.burningtnt.hmclfetcher.api.structure.repo.GitHubRepository;

public final class GitHubPullRequestReference {
    @SerializedName("ref")
    private final String reference;

    @SerializedName("repo")
    private final GitHubRepository repository;

    private GitHubPullRequestReference(String reference, GitHubRepository repository) {
        this.reference = reference;
        this.repository = repository;
    }

    public String getReference() {
        return reference;
    }

    public GitHubRepository getRepository() {
        return repository;
    }
}
