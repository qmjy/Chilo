package io.github.qmjy.mapserver.util;

import io.github.qmjy.mapserver.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
    private static final FileUtils INSTANCE = new FileUtils();
    private static String BASEPATH;

    private FileUtils() {
    }


    public static FileUtils getInstance(AppConfig config) {
        FileUtils.BASEPATH = config.getDataPath();
        return INSTANCE;
    }

    public Optional<File> getSafeFileOfSprites(String spritesName, String fileName, String fileExtension) {
        Path basePath = Paths.get(BASEPATH, "assets").normalize();
        Path requestedPath = basePath.resolve("sprites").resolve(spritesName).resolve(fileName + fileExtension).normalize();
        return getSafeFile(requestedPath);
    }

    public Optional<File> getSafeFileOfStyle(String style) {
        Path basePath = Paths.get(BASEPATH).normalize();
        Path requestedPath = basePath.resolve("styles").resolve(style).normalize();
        return getSafeFile(requestedPath);
    }

    public Optional<File> getSafeFileOfMetadataFile(String tileset) {
        Path basePath = Paths.get(BASEPATH).normalize();
        Path requestedPath = basePath.resolve("tilesets").resolve(tileset).resolve("metadata.json").normalize();
        return getSafeFile(requestedPath);
    }


    public Optional<File> getSafeFileOfPbfFile(String tileset, String z, String x, String y) {
        Path basePath = Paths.get(BASEPATH).normalize();
        Path requestedPath = basePath.resolve("tilesets").resolve(tileset).resolve(z).resolve(x).resolve(y + AppConfig.FILE_EXTENSION_NAME_PBF).normalize();
        return getSafeFile(requestedPath);
    }

    private Optional<File> getSafeFile(Path path) {
        Path basePath = Paths.get(BASEPATH).normalize();
        if (!path.startsWith(basePath)) {
            LOGGER.error("Path traversal attempt detected: {}", path);
            return Optional.empty();
        }
        return Optional.of(path.toFile());
    }
}
