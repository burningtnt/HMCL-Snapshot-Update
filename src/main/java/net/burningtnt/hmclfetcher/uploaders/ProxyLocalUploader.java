package net.burningtnt.hmclfetcher.uploaders;

import net.burningtnt.hmclfetcher.structure.ArchiveFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class ProxyLocalUploader extends AbstractLocalUploader {
    private final String id;

    private final String proxy;

    public ProxyLocalUploader(Path root,String proxy, String id) {
        super(root);
        this.proxy = proxy;
        this.id = "local-storage.proxy." + id;
    }

    @Override
    protected URI getResult(ArchiveFile info) throws URISyntaxException {
        return new URI(this.proxy + "https://github.com/burningtnt/HMCL-SNAPSHOT-UPDATE/raw/v5/artifacts/v5/files/" + info.getFileHash() + '/' + info.getFileName());
    }

    @Override
    public String getUploaderID() {
        return this.id;
    }
}
