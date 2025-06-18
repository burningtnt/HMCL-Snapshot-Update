package net.burningtnt.hmclfetcher.publish.uploaders;

import net.burningtnt.hmclfetcher.publish.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.publish.structure.SourceBranch;
import net.burningtnt.hmclfetcher.publish.uploaders.impl.EMIUploader;
import net.burningtnt.hmclfetcher.publish.uploaders.impl.local.GitHubPagesLocalUploader;
import net.burningtnt.hmclfetcher.publish.uploaders.impl.local.GitHubRepositoryLocalUploader;
import net.burningtnt.hmclfetcher.publish.uploaders.impl.local.ProxiedLocalUploader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public interface IUploader {
    IUploadAction build(Path file, ArchiveFile info, SourceBranch source) throws IOException, UploadRejectedException;

    String getUploaderID();

    static List<IUploader> collectUploader(Path storage) {
        List<IUploader> uploader = new ArrayList<>();
        uploader.add(new GitHubPagesLocalUploader(storage));
        uploader.add(new GitHubRepositoryLocalUploader(storage));
        for (String proxy : System.getenv("HMCL_GITHUB_PROXYS").split(";")) {
            int i = proxy.indexOf('=');
            uploader.add(new ProxiedLocalUploader(storage, proxy.substring(i + 1), proxy.substring(0, i)));
        }
        uploader.add(new EMIUploader());
        return uploader;
    }
}
