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
                TileJSONV2 tileJSON = objectMapper.readValue(jsonData, TileJSONV2.class);
                initMetadataMap(tileJSON);
                return true;
            } else if ("3.0.0".equals(tilejson)) {
                TileJSONV3 tileJSON = objectMapper.readValue(jsonData, TileJSONV3.class);
                initMetadataMap(tileJSON);
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


    private void initMetadataMap(TileJSONV2 tileJSON) {
        metaDataMap.put("tilejson", tileJSON.getTilejson());
        metaDataMap.put("scheme", tileJSON.getScheme());
        metaDataMap.put("type", tileJSON.getType());
        metaDataMap.put("format", tileJSON.getFormat());
        metaDataMap.put("tiles", tileJSON.getTiles());
        metaDataMap.put("bounds", tileJSON.getBounds());
        metaDataMap.put("name", tileJSON.getName());
        metaDataMap.put("version", tileJSON.getVersion());
        metaDataMap.put("description", tileJSON.getDescription());
        metaDataMap.put("minzoom", tileJSON.getMinzoom());
        metaDataMap.put("maxzoom", tileJSON.getMaxzoom());
        metaDataMap.put("attribution", tileJSON.getAttribution());
        metaDataMap.put("vector_layers", tileJSON.getVector_layers());
    }

    private void initMetadataMap(TileJSONV3 tileJSON) {
        metaDataMap.put("tilejson", tileJSON.getTilejson());
        metaDataMap.put("tiles", tileJSON.getTiles());
        metaDataMap.put("vector_layers", tileJSON.getVector_layers());
        metaDataMap.put("attribution", tileJSON.getAttribution());
        metaDataMap.put("bounds", tileJSON.getBounds());
        metaDataMap.put("center", tileJSON.getCenter());
        metaDataMap.put("data", tileJSON.getData());
        metaDataMap.put("description", tileJSON.getDescription());
        metaDataMap.put("fillzoom", tileJSON.getFillzoom());
        metaDataMap.put("grids", tileJSON.getGrids());
        metaDataMap.put("legend", tileJSON.getLegend());
        metaDataMap.put("maxzoom", tileJSON.getMaxzoom());
        metaDataMap.put("minzoom", tileJSON.getMinzoom());
        metaDataMap.put("name", tileJSON.getName());
        metaDataMap.put("scheme", tileJSON.getScheme());
        metaDataMap.put("template", tileJSON.getTemplate());
        metaDataMap.put("version", tileJSON.getVersion());
    }
}
