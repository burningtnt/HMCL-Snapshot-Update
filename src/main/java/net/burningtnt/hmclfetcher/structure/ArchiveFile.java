package net.burningtnt.hmclfetcher.structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ArchiveFile {
    private final String extension;

    private String fileName, fileHash;

    public ArchiveFile(String extension) {
        this.extension = extension;
    }

    public static Map<String, ArchiveFile> of(String... extension) {
        return Collections.unmodifiableMap(Arrays.stream(extension).collect(Collectors.toMap(Function.identity(), ArchiveFile:: new)));
    }

    public String getExtension() {
        return extension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArchiveFile that = (ArchiveFile) o;

        return extension.equals(that.extension);
    }

    @Override
    public int hashCode() {
        return extension.hashCode();
    }
}
