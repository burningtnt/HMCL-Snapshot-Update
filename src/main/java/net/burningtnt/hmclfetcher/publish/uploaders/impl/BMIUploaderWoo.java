package net.burningtnt.hmclfetcher.publish.uploaders.impl;

import net.burningtnt.hmclfetcher.publish.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.publish.structure.SourceBranch;
import net.burningtnt.hmclfetcher.publish.uploaders.IUploadAction;
import net.burningtnt.hmclfetcher.publish.uploaders.IUploader;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class BMIUploaderWoo implements IUploader {
    public BMIUploaderWoo() {
    }

    @Override
    public IUploadAction build(Path file, ArchiveFile info, SourceBranch source) {
        return new IUploadAction() {
            @Override
            public void upload() {
            }

            @Override
            public URI getResult() throws URISyntaxException {
                return new URI("https://alist.8mi.tech/d/HMCL/Woo/" + info.getFileHash() + '/' + info.getFileName());
            }
        };
    }

    @Override
    public String getUploaderID() {
        return "8mi.Woo";
    }
}
