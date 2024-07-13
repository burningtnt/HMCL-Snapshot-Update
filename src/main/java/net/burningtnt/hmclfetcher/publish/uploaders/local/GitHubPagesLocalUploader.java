package net.burningtnt.hmclfetcher.publish.uploaders.local;

import net.burningtnt.hmclfetcher.publish.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.publish.uploaders.AbstractLocalUploader;

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
