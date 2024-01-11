package net.burningtnt.hmclfetcher.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class FileUtils {
    private FileUtils() {
    }

    public static void remove(Path root) throws IOException {
        if (Files.isDirectory(root)) {
            try (Stream<Path> files = Files.list(root)) {
                for (Path path : (Iterable<Path>) files::iterator) {
                    remove(path);
                }
            }
        }
        Files.delete(root);
    }

    public static void ensureDirectoryClear(Path directory) throws IOException {
        if (Files.exists(directory)) {
            remove(directory);
        }

        Files.createDirectories(directory);
    }

    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index == -1 ? fileName : fileName.substring(index + 1);
    }
}
