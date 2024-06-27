package net.burningtnt.hmclfetcher.uploaders;

import net.burningtnt.hmclfetcher.storage.IUploadAction;
import net.burningtnt.hmclfetcher.storage.IUploader;
import net.burningtnt.hmclfetcher.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.structure.SourceBranch;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class EMIUploader implements IUploader {
    public EMIUploader() {
    }

    @Override
    public IUploadAction build(Path file, ArchiveFile info, SourceBranch source) {
        return new IUploadAction() {
            @Override
            public void upload() {
            }

            @Override
            public URI getResult() throws URISyntaxException {
                return new URI("https://alist.8mi.tech/d/HMCL/139/" + info.getFileHash() + '/' + info.getFileName());
            }
        };
    }

    @Override
    public String getUploaderID() {
        return "8mi.139";
    }
}
