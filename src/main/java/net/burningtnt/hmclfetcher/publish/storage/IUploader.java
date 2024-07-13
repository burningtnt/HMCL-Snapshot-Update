package net.burningtnt.hmclfetcher.publish.storage;

import net.burningtnt.hmclfetcher.publish.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.publish.structure.SourceBranch;

import java.io.IOException;
import java.nio.file.Path;

public interface IUploader {
    IUploadAction build(Path file, ArchiveFile info, SourceBranch source) throws IOException, UploadRejectedException;

    String getUploaderID();
}
