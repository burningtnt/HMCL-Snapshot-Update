package net.burningtnt.hmclfetcher.publish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.burningtnt.hmclfetcher.api.GitHubAPI;
import net.burningtnt.hmclfetcher.api.structure.workflow.artifacts.GitHubArtifact;
import net.burningtnt.hmclfetcher.publish.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.publish.structure.SourceBranch;
import net.burningtnt.hmclfetcher.publish.uploaders.IUploadAction;
import net.burningtnt.hmclfetcher.publish.uploaders.IUploader;
import net.burningtnt.hmclfetcher.publish.uploaders.UploadRejectedException;
import net.burningtnt.hmclfetcher.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class UpdaterManager {
    private UpdaterManager() {
    }

    private static final SourceBranch[] GITHUB_BRANCHES = {
//            new SourceBranch("HMCL-dev", "HMCL", "javafx", "gradle.yml"),
            new SourceBranch("HMCL-dev", "HMCL", "main", "gradle.yml"),
            new SourceBranch("burningtnt", "HMCL", "prs", "gradle.yml")
    };
    private static final String CURRENT_BRANCH = "v5";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void execute(GitHubAPI apiHandle) throws Exception {
        Path ARTIFACT_ROOT = Path.of("artifacts/" + CURRENT_BRANCH).toAbsolutePath();
        Path FILES_ROOT = ARTIFACT_ROOT.resolve("files");
        Path UPLOADER_ROOT = ARTIFACT_ROOT.resolve("uploaders");
        Map<String, ArchiveFile> ARCHIVE_FILES = ArchiveFile.of("exe", "jar", "sh");

        FileUtils.ensureDirectoryClear(ARTIFACT_ROOT);

        List<IUploader> uploaderList = IUploader.collectUploader(FILES_ROOT);

        for (SourceBranch source : GITHUB_BRANCHES) {
            Map<ArchiveFile, Path> files = new HashMap<>();

            long runID = apiHandle.getLatestWorkflowID(source.owner(), source.repository(), source.workflow(), source.branch());
            GitHubArtifact[] artifacts = apiHandle.getArtifacts(source.owner(), source.repository(), runID);
            if (artifacts.length == 0) {
                break;
            }

            for (GitHubArtifact artifact : artifacts) {
                try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(apiHandle.getArtifactData(artifact)))) {
                    ZipEntry entry = zis.getNextEntry();
                    if (entry == null) {
                        throw new IllegalArgumentException(String.format("Cannot handle action artifact: Cannot locate artifact in '%s'.", artifact.getName()));
                    }
                    if (entry.getName().endsWith(".sha256")) {
                        continue;
                    }

                    ArchiveFile file = ARCHIVE_FILES.get(FileUtils.subStringAfterLast(entry.getName(), '.'));
                    if (file != null) {
                        MessageDigest digest = MessageDigest.getInstance("SHA1");
                        Path target = Files.createTempFile("hmcl-fetcher-", '.' + file.getExtension());
                        try (OutputStream os = new OutputStream() {
                            private final OutputStream delegate = Files.newOutputStream(target);

                            @Override
                            public void write(int b) throws IOException {
                                delegate.write(b);
                                digest.update((byte) b);
                            }

                            @Override
                            public void write(byte[] b, int off, int len) throws IOException {
                                delegate.write(b, off, len);
                                digest.update(b, off, len);
                            }

                            @Override
                            public void close() throws IOException {
                                delegate.close();
                            }
                        }) {
                            zis.transferTo(os);
                        }

                        file.setFileName(FileUtils.subStringAfterLast(entry.getName(), '/'));
                        file.setFileHash(HexFormat.of().formatHex(digest.digest()));
                        files.put(file, target);
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
