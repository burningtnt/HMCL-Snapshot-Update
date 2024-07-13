package net.burningtnt.hmclfetcher.api.structure.user;

public final class GitHubUser {
    private final String login;

    private GitHubUser(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }
}
