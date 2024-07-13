package net.burningtnt.hmclfetcher.api.structure.pagination;

import java.util.HashMap;
import java.util.Map;

public final class GitHubPaginationConfig {
    public static final String PER_PAGE = "per_page", PAGE = "page";

    private final int perPage;

    private int currentPage;

    GitHubPaginationConfig() {
        this.perPage = 30;
        this.currentPage = 1;
    }

    void next() {
        this.currentPage++;
    }

    public Map<String, String> wrap(Map<String, String> arguments) {
        Map<String, String> result = new HashMap<>();

        result.put(PAGE, String.valueOf(currentPage));
        result.put(PER_PAGE, String.valueOf(perPage));
        result.putAll(arguments);

        return result;
    }
}
