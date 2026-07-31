package io.github.qmjy.mapserver.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.qmjy.mapserver.spec.TileJSONV2;
import io.github.qmjy.mapserver.spec.TileJSONV3;
import io.github.qmjy.mapserver.util.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public class TileOfDir extends AbstractTile {
    public TileOfDir(File dir) {
        super(dir.getAbsolutePath());
        this.fileLength = -1; // 目录类型不计算文件大小
        if (loadMetadata()) {
            this.isGzip = isCompressed();
            this.valid = true;
        }
    }

    @Override
    public boolean loadMetadata() {
        File file = Path.of(filePath, "metadata.json").toFile();
        try {
            String jsonData = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            String tilejson = objectMapper.readValue(jsonData, Map.class).get("tilejson").toString();

            if ("2.0.0".equals(tilejson)) {
                this.tileJSON = objectMapper.readValue(jsonData, TileJSONV2.class);
                return true;
            } else if ("3.0.0".equals(tilejson)) {
                this.tileJSON = objectMapper.readValue(jsonData, TileJSONV3.class);
                return true;
            } else {
                logger.error("Unsupported tilejson version: {}", tilejson);
                return false;
            }
        } catch (IOException e) {
            logger.error("Failed to load metadata from directory: {}", filePath, e);
            return false;
        }
    }

    @Override
    public int getTileFileType() {
        return TILE_FILE_TYPE_OF_DIRECTORY;
    }

    @Override
    public byte[] getTile(int zoom, int x, int y) {
        return new byte[0];
    }

    @Override
    public void countSize() {
        // 目录类型的瓦片计数实现
        // TODO: 实现目录瓦片计数逻辑
    }

    @Override
    public boolean isCompressed() {
        File file = new File(filePath);
        if (file.isDirectory()) {
            try {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    File[] fs = files[0].listFiles();
                    if (fs != null && fs.length > 0) {
                        File leafFolder = fs[0];
                        File[] leafFolderOfFirst = leafFolder.listFiles();
                        if (leafFolderOfFirst != null && leafFolderOfFirst.length > 0) {
                            File leafFile = leafFolderOfFirst[0];
                            return IOUtils.isCompressed(FileUtils.readFileToByteArray(leafFile));
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to check if directory is compressed: {}", filePath, e);
            }
        }
        return false;
    }

    @Override
    public boolean releaseResource() {
        return false;
    }
}
