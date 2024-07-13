package net.burningtnt.hmclfetcher.api;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.burningtnt.hmclfetcher.api.structure.commits.GitHubCommitsCompare;
import net.burningtnt.hmclfetcher.api.structure.pagination.GitHubPagination;
import net.burningtnt.hmclfetcher.api.structure.prs.GitHubPullRequest;
import net.burningtnt.hmclfetcher.api.structure.workflow.GitHubWorkflowRun;
import net.burningtnt.hmclfetcher.api.structure.workflow.GitHubWorkflowRunLookup;
import net.burningtnt.hmclfetcher.api.structure.workflow.artifacts.GitHubArtifact;
import net.burningtnt.hmclfetcher.api.structure.workflow.artifacts.GitHubArtifactsLookup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class GitHubAPI {
    final String token;

    public GitHubAPI(String token) {
        this.token = "Bearer " + token;
    }

    public long getLatestWorkflowID(String owner, String repository, String workflowID, String branch) throws IOException {
        GitHubWorkflowRun[] runs = GitHubRequestUtils.ofStructuredResult(
                this, GitHubRequestUtils.Type.GET, String.format("https://api.github.com/repos/%s/%s/actions/workflows/%s/runs", owner, repository, workflowID),
                Map.of("branch", branch, "event", "push", "page", "1", "per_page", "1"),
                GitHubWorkflowRunLookup.class
        ).getRuns();
        if (runs.length == 0) {
            return -1;
        }

        return runs[0].getID();
    }

    public GitHubArtifact[] getArtifacts(String owner, String repository, long runID) throws IOException {
        return GitHubRequestUtils.ofStructuredResult(
                this, GitHubRequestUtils.Type.GET, String.format("https://api.github.com/repos/%s/%s/actions/runs/%s/artifacts", owner, repository, runID),
                GitHubArtifactsLookup.class
        ).getArtifacts();
    }

    public InputStream getArtifactData(GitHubArtifact artifact) throws IOException {
        return GitHubRequestUtils.ofStream(this, GitHubRequestUtils.Type.GET, artifact.getDonloadURL());
    }

    private static final TypeToken<List<GitHubPullRequest>> GET_PULL_REQUESTS_TT = new TypeToken<>() {
    };

    public GitHubPagination<GitHubPullRequest> getPullRequests(String owner, String repository, String baseBranch) {
        return GitHubPagination.of(config -> GitHubRequestUtils.ofStructuredResult(
                this, GitHubRequestUtils.Type.GET, String.format("https://api.github.com/repos/%s/%s/pulls", owner, repository),
                config.wrap(Map.of("state", "open", "base", baseBranch)),
                GET_PULL_REQUESTS_TT
        ));
    }

    public void updatePullRequestBody(String owner, String repository, int id, String body) throws IOException {
        JsonObject b = new JsonObject();
        b.addProperty("body", body);
        GitHubRequestUtils.ofSend(this, GitHubRequestUtils.Type.PATCH, String.format("https://api.github.com/repos/%s/%s/pulls/%d", owner, repository, id), b);
    }

    public GitHubCommitsCompare compareCommits(String baseOwner, String baseRepository, String baseCommit, String headOwner, String headRepository, String headCommit) throws IOException {
        return GitHubRequestUtils.ofStructuredResult(
                this, GitHubRequestUtils.Type.GET, String.format("https://api.github.com/repos/%s/%s/compare/%s...%s:%s:%s", baseOwner, baseRepository, baseCommit, headOwner, headRepository, headCommit),
                GitHubCommitsCompare.class
        );
    }
}
