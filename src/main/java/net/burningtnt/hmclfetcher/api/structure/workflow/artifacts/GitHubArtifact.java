package net.burningtnt.hmclfetcher.api.structure.workflow.artifacts;

import com.google.gson.annotations.SerializedName;

public class GitHubArtifact {
    @SerializedName("name")
    private String name;

    @SerializedName("archive_download_url")
    private String donloadURL;

    public String getName() {
        return this.name;
    }

    public String getDonloadURL() {
        return this.donloadURL;
    }
}
