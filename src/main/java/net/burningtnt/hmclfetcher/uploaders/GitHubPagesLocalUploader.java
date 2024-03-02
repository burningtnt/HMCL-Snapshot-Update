package net.burningtnt.hmclfetcher.uploaders;

import net.burningtnt.hmclfetcher.structure.ArchiveFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class GitHubPagesLocalUploader extends AbstractLocalUploader {
    public GitHubPagesLocalUploader(Path root) {
        super(root);
    }

    @Override
    protected URI getResult(ArchiveFile info) throws URISyntaxException {
        return new URI("https://burningtnt.github.io/HMCL-Snapshot-Update/artifacts/v5/files/" + info.getFileHash() + '/' + info.getFileName());
    }

    @Override
    public String getUploaderID() {
        return "local-storage.gh-pages";
    }
}
