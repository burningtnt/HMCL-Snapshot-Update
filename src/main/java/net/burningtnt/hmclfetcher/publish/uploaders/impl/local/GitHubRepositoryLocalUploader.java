package net.burningtnt.hmclfetcher.publish.uploaders.impl.local;

import net.burningtnt.hmclfetcher.publish.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.publish.uploaders.impl.AbstractLocalUploader;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class GitHubRepositoryLocalUploader extends AbstractLocalUploader {
    public GitHubRepositoryLocalUploader(Path root) {
        super(root);
    }

    @Override
    protected URI getResult(ArchiveFile info) throws URISyntaxException {
        return new URI("https://github.com/burningtnt/HMCL-SNAPSHOT-UPDATE/raw/v5/artifacts/v5/files/" + info.getFileHash() + '/' + info.getFileName());
    }

    @Override
    public String getUploaderID() {
        return "local-storage.gh-repo";
    }
}
