package net.burningtnt.hmclfetcher.api.structure.workflow;

import com.google.gson.annotations.SerializedName;

public class GitHubWorkflowRunLookup {
    @SerializedName("workflow_runs")
    private GitHubWorkflowRun[] runs;

    public GitHubWorkflowRun[] getRuns() {
        return runs;
    }
}
