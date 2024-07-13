package net.burningtnt.hmclfetcher.changelog;

import net.burningtnt.hmclfetcher.api.GitHubAPI;
import net.burningtnt.hmclfetcher.api.pagination.Pagination;
import net.burningtnt.hmclfetcher.api.structure.commits.GitHubCommitsCompare;
import net.burningtnt.hmclfetcher.api.structure.prs.GitHubPullRequest;
import net.burningtnt.hmclfetcher.api.structure.prs.GitHubPullRequestReference;
import net.burningtnt.hmclfetcher.api.structure.repo.GitHubRepository;
import net.burningtnt.hmclfetcher.publish.structure.SourceBranch;

public final class ChangelogManager {
    private ChangelogManager() {
    }

    private static final SourceBranch OFFICIAL_SOURCE = new SourceBranch("HMCL-dev", "HMCL", "main", null);

    private static final SourceBranch PRC_SOURCE = new SourceBranch("burningtnt", "HMCL", "prs", null);


    private static final String M_DRAFT = "\uD83D\uDFE5", M_NOT_MERGED = "\uD83D\uDFE8", M_PARTLY_MERGED = "\uD83D\uDFE6", M_MERGED = "\uD83D\uDFE9";

    public static void execute(GitHubAPI apiHandle) throws Exception {
        StringBuilder sb = new StringBuilder("|").append(M_DRAFT).append('|').append(M_NOT_MERGED).append('|')
                .append(M_PARTLY_MERGED).append('|').append(M_MERGED).append("|\n|----|----|----| ----|\n|Draft|Not Merged|Partly Merged|Merged|\n\n");

        Pagination<GitHubPullRequest> pulls = apiHandle.getPullRequests(OFFICIAL_SOURCE.owner(), OFFICIAL_SOURCE.repository(), OFFICIAL_SOURCE.branch());
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

            sb.append("[#").append(pull.getNumber()).append("](").append(pull.getHtmlURL()).append(") ").append(state).append(": `").append(pull.getTitle()).append("`\n");
        }

        apiHandle.updatePullRequestBody("burningtnt", "HMCL", 9, sb.toString());
    }
}
