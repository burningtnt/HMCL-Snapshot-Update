package net.burningtnt.hmclfetcher.api.structure.pagination;

import java.io.IOException;
import java.util.List;

public interface GitHubPaginationAccessor<R> {
    List<R> execute(GitHubPaginationConfig pagination) throws IOException;
}
