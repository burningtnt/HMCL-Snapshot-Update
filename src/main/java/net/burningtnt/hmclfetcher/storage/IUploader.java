package net.burningtnt.hmclfetcher.storage;

import net.burningtnt.hmclfetcher.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.structure.SourceBranch;

import java.io.IOException;
import java.nio.file.Path;

public interface IUploader {
    IUploadAction build(Path file, ArchiveFile info, SourceBranch source) throws IOException, UploadRejectedException;

    String getUploaderID();
}
