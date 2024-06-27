package net.burningtnt.hmclfetcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.burningtnt.hmclfetcher.storage.IUploadAction;
import net.burningtnt.hmclfetcher.storage.IUploader;
import net.burningtnt.hmclfetcher.storage.UploadRejectedException;
import net.burningtnt.hmclfetcher.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.structure.SourceBranch;
import net.burningtnt.hmclfetcher.uploaders.EMIUploader;
import net.burningtnt.hmclfetcher.uploaders.local.GitHubPagesLocalUploader;
import net.burningtnt.hmclfetcher.uploaders.local.GitHubRepositoryLocalUploader;
import net.burningtnt.hmclfetcher.uploaders.local.ProxiedLocalUploader;
import net.burningtnt.hmclfetcher.utils.FileUtils;
import net.burningtnt.hmclfetcher.utils.GitHubAPI;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Main {
    private Main() {
    }

    private static final SourceBranch[] GITHUB_BRANCHES = {
            new SourceBranch("HMCL-dev", "HMCL", "javafx", "gradle.yml"),
            new SourceBranch("HMCL-dev", "HMCL", "main", "gradle.yml"),
            new SourceBranch("burningtnt", "HMCL", "prs", "gradle.yml")
    };

    private static final String CURRENT_BRANCH = "v5";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static List<IUploader> collectUploader(Path storage) {
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

    public static void main(String[] args) throws IOException, URISyntaxException {
        GitHubAPI GITHUB_API = new GitHubAPI(System.getenv("HMCL_GITHUB_TOKEN"));
        Path ARTIFACT_ROOT = Path.of("artifacts/" + CURRENT_BRANCH).toAbsolutePath();
        Path FILES_ROOT = ARTIFACT_ROOT.resolve("files");
        Path UPLOADER_ROOT = ARTIFACT_ROOT.resolve("uploaders");
        Map<String, ArchiveFile> ARCHIVE_FILES = ArchiveFile.of("exe", "jar");

        FileUtils.ensureDirectoryClear(ARTIFACT_ROOT);

        List<IUploader> uploaderList = collectUploader(FILES_ROOT);

        for (SourceBranch source : GITHUB_BRANCHES) {
            Map<ArchiveFile, Path> files = new HashMap<>();

            long runID = GITHUB_API.getLatestWorkflowID(source.owner(), source.repository(), source.workflow(), source.branch());
            GitHubAPI.GitHubArtifact artifact = GITHUB_API.getArtifacts(source.owner(), source.repository(), runID)[0];

            try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(GITHUB_API.getArtifactData(artifact)))) {
                ZipArchiveEntry entry;
                while ((entry = zis.getNextZipEntry()) != null) {
                    String entryPath = entry.getName();
                    if (entryPath.endsWith(".sha1")) {
                        ArchiveFile archiveFile = ARCHIVE_FILES.get(FileUtils.getFileExtension(entryPath.substring(0, entryPath.length() - 5)));
                        if (archiveFile != null) {
                            archiveFile.setFileHash(new String(zis.readNBytes(40)));
                        }
                    } else {
                        ArchiveFile archiveFile = ARCHIVE_FILES.get(FileUtils.getFileExtension(entryPath));
                        if (archiveFile != null) {
                            String fileName = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                            archiveFile.setFileName(fileName);

                            Path target = Files.createTempFile("hmcl-fetcher-", '.' + archiveFile.getExtension());
                            try (OutputStream os = Files.newOutputStream(target)) {
                                zis.transferTo(os);
                            }
                            files.put(archiveFile, target);
                        }
                    }
                }
            }

            for (ArchiveFile archiveFile : ARCHIVE_FILES.values()) {
                String fileName = archiveFile.getFileName();
                String fileHash = archiveFile.getFileHash();

                if (fileName == null || fileHash == null || !fileName.startsWith("HMCL-")) {
                    throw new IllegalStateException("Broken Artifact!");
                }

                Path target = files.get(archiveFile);
                if (target == null) {
                    throw new IllegalStateException("Broken Artifact!");
                }

                Path offset = Path.of(source.owner(), source.repository(), source.branch(), source.workflow() + '.' + archiveFile.getExtension() + ".json");
                for (IUploader uploader : uploaderList) {
                    IUploadAction action;
                    try {
                        action = uploader.build(target, archiveFile, source);
                    } catch (UploadRejectedException e) {
                        continue;
                    }
                    action.upload();
                    URI fileLink = action.getResult();

                    JsonObject json = new JsonObject();
                    json.add("jar", new JsonPrimitive(fileLink.toString()));
                    json.add("jarsha1", new JsonPrimitive(fileHash));
                    json.add("version", new JsonPrimitive(fileName.substring(5, fileName.length() - archiveFile.getExtension().length() - 1))); // Remove "HMCL-" prefix and ".exe" suffix.
                    json.add("universal", new JsonPrimitive("https://hmcl.huangyuhui.net"));

                    try (Writer writer = Files.newBufferedWriter(FileUtils.ensureFileExist(UPLOADER_ROOT.resolve(uploader.getUploaderID()).resolve(offset)), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                        GSON.toJson(json, writer);
                    }
                }
            }

            for (Path path : files.values()) {
                Files.delete(path);
            }
        }
    }
}
