package net.burningtnt.hmclfetcher.api.structure.workflow;

import com.google.gson.annotations.SerializedName;

public class GitHubWorkflowRun {
    @SerializedName("id")
    private long id;

    public long getID() {
        return id;
    }
}
