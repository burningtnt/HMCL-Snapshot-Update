package net.burningtnt.hmclfetcher;

import net.burningtnt.hmclfetcher.api.GitHubAPI;
import net.burningtnt.hmclfetcher.changelog.ChangelogManager;
import net.burningtnt.hmclfetcher.publish.UpdaterManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        GitHubAPI apiHandle = new GitHubAPI(System.getenv("HMCL_GITHUB_TOKEN"));

        UpdaterManager.execute(apiHandle);
        ChangelogManager.execute(apiHandle);
    }
}
