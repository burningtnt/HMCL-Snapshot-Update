package net.burningtnt.hmclfetcher.uploaders;

import net.burningtnt.hmclfetcher.storage.IUploadAction;
import net.burningtnt.hmclfetcher.storage.IUploader;
import net.burningtnt.hmclfetcher.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.structure.SourceBranch;
import net.burningtnt.hmclfetcher.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class AbstractLocalUploader implements IUploader {
    private final Path root;

    protected AbstractLocalUploader(Path root) {
        this.root = root;
    }

    @Override
    public final IUploadAction build(Path file, ArchiveFile info, SourceBranch source) {
        Path target = root.resolve(info.getFileHash()).resolve(info.getFileName());
        return new IUploadAction() {
            @Override
            public void upload() throws IOException {
                if (!Files.exists(target)) {
                    Files.copy(file, FileUtils.ensureFileExist(target), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            @Override
            public URI getResult() throws URISyntaxException {
                return AbstractLocalUploader.this.getResult(info);
            }
        };
    }

    protected abstract URI getResult(ArchiveFile info) throws URISyntaxException;
}
