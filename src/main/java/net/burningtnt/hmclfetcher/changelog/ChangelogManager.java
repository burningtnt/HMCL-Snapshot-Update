package net.burningtnt.hmclfetcher.changelog;

import net.burningtnt.hmclfetcher.api.GitHubAPI;
import net.burningtnt.hmclfetcher.api.structure.commits.GitHubCommitsCompare;
import net.burningtnt.hmclfetcher.api.structure.pagination.GitHubPagination;
import net.burningtnt.hmclfetcher.api.structure.prs.GitHubPullRequest;
import net.burningtnt.hmclfetcher.api.structure.prs.GitHubPullRequestReference;
import net.burningtnt.hmclfetcher.api.structure.repo.GitHubRepository;
import net.burningtnt.hmclfetcher.publish.structure.SourceBranch;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ChangelogManager {
    private ChangelogManager() {
    }

    private static final SourceBranch OFFICIAL_SOURCE = new SourceBranch("HMCL-dev", "HMCL", "main", null);

    private static final SourceBranch PRC_SOURCE = new SourceBranch("burningtnt", "HMCL", "prs", null);

    private static final String M_DRAFT = "\uD83D\uDFE5", M_NOT_MERGED = "\uD83D\uDFE8", M_PARTLY_MERGED = "\uD83D\uDFE6", M_MERGED = "\uD83D\uDFE9";

    public static void execute(GitHubAPI apiHandle) throws Exception {
        StringBuilder p2 = new StringBuilder();
        int p2I = 0;

        StringBuilder p3 = new StringBuilder();
        GitHubPagination<GitHubPullRequest> pulls = apiHandle.getPullRequests(OFFICIAL_SOURCE.owner(), OFFICIAL_SOURCE.repository(), OFFICIAL_SOURCE.branch());
        while (pulls.hasNext()) {
            GitHubPullRequest pull = pulls.next();

            String state;
            GitHubPullRequestReference head = pull.getHead();
            GitHubRepository headRepo = head.getRepository();

            GitHubCommitsCompare c2 = apiHandle.compareCommits(
                    PRC_SOURCE.owner(), PRC_SOURCE.repository(), PRC_SOURCE.branch(),
                    headRepo.getOwner().getLogin(), headRepo.getName(), head.getReference()
            );

            if (c2.getAheadByCount() == 0) {
                state = M_MERGED;
            } else {
                GitHubCommitsCompare c1 = apiHandle.compareCommits(
                        OFFICIAL_SOURCE.owner(), OFFICIAL_SOURCE.repository(), OFFICIAL_SOURCE.branch(),
                        headRepo.getOwner().getLogin(), headRepo.getName(), head.getReference()
                );

                if (c1.getAheadByCount() == c2.getAheadByCount()) {
                    if (pull.isDraft()) {
                        state = M_DRAFT;
                    } else {
                        state = M_NOT_MERGED;
                    }
                } else {
                    state = M_PARTLY_MERGED;
                }
            }

            p2.append(state);
            p2I++;
            if (p2I % 16 == 0) {
                p2.append('\n');
            }

            p3.append("[#").append(pull.getNumber()).append("](").append(pull.getHtmlURL()).append(") ").append(state).append(": `").append(pull.getTitle()).append("`\n");
        }

        StringBuilderWriter result = new StringBuilderWriter();
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(
                ChangelogManager.class.getResourceAsStream("/changelog.template.md")
        ), StandardCharsets.UTF_8)) {
            reader.transferTo(result);
        }

        apiHandle.updatePullRequestBody("burningtnt", "HMCL", 9, String.format(result.toString(), M_DRAFT, M_NOT_MERGED, M_PARTLY_MERGED, M_MERGED, p2, p3));
    }
}
