package net.burningtnt.hmclfetcher.api.structure.commits;

import com.google.gson.annotations.SerializedName;

public final class GitHubCommitsCompare {
    @SerializedName("ahead_by")
    private final int aheadByCount;

    private GitHubCommitsCompare(int aheadByCount) {
        this.aheadByCount = aheadByCount;
    }

    public int getAheadByCount() {
        return aheadByCount;
    }
}
