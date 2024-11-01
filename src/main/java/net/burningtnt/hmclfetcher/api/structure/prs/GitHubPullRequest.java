package net.burningtnt.hmclfetcher.api.structure.prs;

import com.google.gson.annotations.SerializedName;

public final class GitHubPullRequest {
    private final long id;

    private final int number;

    private final String title;

    @SerializedName("html_url")
    private final String htmlURL;

    private final boolean draft;

    private final GitHubPullRequestReference base;

    private final GitHubPullRequestReference head;

    private GitHubPullRequest(long id, int number, String title, boolean draft, GitHubPullRequestReference base, GitHubPullRequestReference head, String htmlURL) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.draft = draft;
        this.base = base;
        this.head = head;
        this.htmlURL = htmlURL;
    }

    public long getID() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getHtmlURL() {
        return htmlURL;
    }

    public boolean isDraft() {
        return draft;
    }

    public GitHubPullRequestReference getBase() {
        return base;
    }

    public GitHubPullRequestReference getHead() {
        return head;
    }
}
