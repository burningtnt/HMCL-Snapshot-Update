package net.burningtnt.hmclfetcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.burningtnt.hmclfetcher.structure.ArchiveFile;
import net.burningtnt.hmclfetcher.structure.SourceBranch;
import net.burningtnt.hmclfetcher.utils.DigestUtils;
import net.burningtnt.hmclfetcher.utils.FileUtils;
import net.burningtnt.hmclfetcher.utils.GitHubAPI;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Main {
    private Main() {
    }

    private static final SourceBranch[] GITHUB_BRANCHES = {
            new SourceBranch("huanghongxun", "HMCL", "javafx", "gradle.yml"),
            new SourceBranch("burningtnt", "HMCL", "prs", "gradle.yml")
    };

    private static final String OFFICIAL_DOWNLOAD_LINK = "https://github.com/burningtnt/HMCL-SNAPSHOT-UPDATE/raw/v2/generated/";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static List<URI> collectDownloadLinks() throws URISyntaxException {
        List<URI> links = new ArrayList<>();
        links.add(new URI(OFFICIAL_DOWNLOAD_LINK));
        for (String proxy : System.getenv("HMCL_GITHUB_PROXYS").split(";")) {
            links.add(new URI(proxy + OFFICIAL_DOWNLOAD_LINK));
        }
        return links;
    }

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        String GITHUB_TOKEN = System.getenv("HMCL_GITHUB_TOKEN");
        GitHubAPI GITHUB_API = new GitHubAPI(GITHUB_TOKEN);
        Path ARTIFACT_ROOT = Path.of("generated").toAbsolutePath();
        List<URI> DOWNLOAD_LINKS = collectDownloadLinks();
        Map<String, ArchiveFile> ARCHIVE_FILES = ArchiveFile.of("exe", "jar");

        FileUtils.ensureDirectoryClear(ARTIFACT_ROOT);

        for (SourceBranch source : GITHUB_BRANCHES) {
            String branch = DigestUtils.digest(source.owner(), source.repository(), source.branch(), source.workflow());
            Path root = ARTIFACT_ROOT.resolve(branch);
            Files.createDirectory(root);

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
                            try (OutputStream os = Files.newOutputStream(root.resolve(fileName))) {
                                zis.transferTo(os);
                            }
                        }
                    }
                }
            }

            for (ArchiveFile archiveFile : ARCHIVE_FILES.values()) {
                String fileName = archiveFile.getFileName();
                String fileHash = archiveFile.getFileHash();

                if (fileName == null || fileHash == null) {
                    throw new IllegalStateException("Broken Artifact!");
                }

                JsonObject update = new JsonObject();
                update.add("jarsha1", new JsonPrimitive(fileHash));
                update.add("version", new JsonPrimitive(fileName.substring(5, fileName.length() - 4))); // Remove "HMCL-" prefix and ".exe" suffix.
                update.add("universal", new JsonPrimitive("https://www.mcbbs.net/forum.php?mod=viewthread&tid=142335"));

                for (URI downloadLink : DOWNLOAD_LINKS) {
                    String fileLink = downloadLink.resolve(branch + "/" + fileName).toString();
                    update.add("jar", new JsonPrimitive(fileLink));

                    try (BufferedWriter writer = Files.newBufferedWriter(root.resolve(DigestUtils.digest(fileLink) + ".json"))) {
                        GSON.toJson(update, writer);
                    }
                }
            }
        }
    }
}
