package net.burningtnt.hmclfetcher.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class FileUtils {
    private FileUtils() {
    }

    public static void delete(Path root) throws IOException {
        if (Files.isDirectory(root)) {
            try (Stream<Path> files = Files.list(root)) {
                for (Path path : (Iterable<Path>) files::iterator) {
                    delete(path);
                }
            }
        }
        Files.delete(root);
    }

    public static void ensureDirectoryClear(Path directory) throws IOException {
        if (Files.exists(directory)) {
            delete(directory);
        }

        Files.createDirectories(directory);
    }

    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index == -1 ? fileName : fileName.substring(index + 1);
    }

    public static Path ensureFileExist(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }

        return path;
    }
}
