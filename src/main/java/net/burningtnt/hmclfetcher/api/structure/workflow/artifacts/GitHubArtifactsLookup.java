package net.burningtnt.hmclfetcher.api.structure.workflow.artifacts;

import com.google.gson.annotations.SerializedName;

public class GitHubArtifactsLookup {
    @SerializedName("artifacts")
    private GitHubArtifact[] artifacts;

    public GitHubArtifact[] getArtifacts() {
        return artifacts;
    }
}
